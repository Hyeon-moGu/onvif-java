package io.github.hyeonmo.managers;

import io.github.hyeonmo.exceptions.OnvifAuthException;
import io.github.hyeonmo.exceptions.OnvifNetworkException;
import io.github.hyeonmo.exceptions.OnvifSoapException;
import io.github.hyeonmo.models.events.*;
import io.github.hyeonmo.operations.EventOperations;
import io.github.hyeonmo.responses.events.PullMessagesResponse;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class EventManagerTest {
    private static final String NOW = "2030-01-01T00:00:00Z";
    private static final String END = "2030-01-01T00:00:10Z";
    private final ManualScheduler clock = new ManualScheduler();
    private final Operations operations = new Operations();
    private final EventManager manager = new EventManager(operations, clock, clock::nanoTime);

    @After
    public void close() { manager.unsubscribe(); }

    @Test
    public void renewsBeforeCameraExpiryWhilePullIsPending() {
        manager.subscribe(event -> {}).join();
        clock.next();
        assertEquals(1, operations.pulls);
        clock.next();
        assertEquals(8000, clock.millis());
        assertEquals(1, operations.renews);
        assertEquals("PT1H", operations.requestedLease);
        assertEquals("<ref:Id xmlns:ref='urn:test'>token</ref:Id>", operations.renewed.getReferenceParametersXml());
        assertFalse(operations.pull.isDone());
        operations.renew.complete(new EventSubscriptionStatus("2030-01-01T00:00:08Z", "2030-01-01T00:01:08Z", ""));
        assertEquals("2030-01-01T00:01:08Z", manager.getSubscription().getTerminationTime());
        // A delayed pull response must not replace the newly renewed lease.
        operations.pull.complete(response());
        assertEquals("2030-01-01T00:01:08Z", manager.getSubscription().getTerminationTime());
        clock.next(); // next poll
        clock.next(); // next renewal at 80% of the new TTL
        assertEquals(56000, clock.millis());
        assertEquals(2, operations.renews);
    }

    @Test
    public void missingOrInvalidTimesUseConservativeRenewal() {
        operations.created = CompletableFuture.completedFuture(subscription("not-a-time", ""));
        manager.subscribe((Consumer<Event>) null).join();
        clock.next();
        clock.next();
        assertEquals(30000, clock.millis());
        assertEquals(1, operations.renews);
    }

    @Test
    public void renewalFailureIsReportedAndPendingPullCancelled() {
        manager.subscribe(event -> {}).join();
        clock.next();
        clock.next();
        OnvifSoapException error = new OnvifSoapException("lease rejected", "");
        operations.renew.completeExceptionally(error);
        assertSame(error, failure(manager.getCompletion()));
        assertTrue(operations.pull.isCancelled());
        assertTrue(clock.isShutdown());
        assertEquals(1, operations.unsubscribes);
    }

    @Test
    public void expiredRenewResponseStopsInsteadOfSpinning() {
        manager.subscribe(event -> {}).join();
        clock.next();
        clock.next();
        operations.renew.complete(new EventSubscriptionStatus(END, NOW, ""));
        assertTrue(failure(manager.getCompletion()) instanceof OnvifSoapException);
        assertTrue(clock.isShutdown());
    }

    @Test
    public void networkFailuresRetryThreeTimesWithBoundedBackoff() {
        OnvifNetworkException error = new OnvifNetworkException("offline");
        for (int i = 0; i < 4; i++) operations.pullResults.add(failed(new CompletionException(error)));
        manager.subscribe(event -> {}).join();
        for (long expected : new long[]{0, 1000, 3000, 7000}) {
            clock.next();
            assertEquals(expected, clock.millis());
        }
        assertEquals(4, operations.pulls);
        assertSame(error, failure(manager.getCompletion()));
        assertEquals(1, operations.unsubscribes);
    }

    @Test
    public void successfulPullResetsRetryBudget() {
        operations.pullResults.add(failed(new OnvifNetworkException("temporary")));
        operations.pullResults.add(CompletableFuture.completedFuture(response()));
        operations.pullResults.add(failed(new OnvifNetworkException("temporary")));
        manager.subscribe(event -> {}).join();
        clock.next();
        clock.next();
        clock.next();
        clock.next();
        assertEquals(2100, clock.millis());
        assertEquals(4, operations.pulls);
        assertFalse(manager.getCompletion().isDone());
    }

    @Test
    public void authenticationFailureIsNotRetried() {
        OnvifAuthException error = new OnvifAuthException(401, "denied");
        operations.pullResults.add(failed(error));
        manager.subscribe(event -> {}).join();
        clock.next();
        assertSame(error, failure(manager.getCompletion()));
        assertEquals(1, operations.pulls);
        assertTrue(clock.isShutdown());
    }

    @Test
    public void networkRenewalFailureCanRecoverWithoutStoppingPoll() {
        manager.subscribe(event -> {}).join();
        clock.next();
        clock.next();
        operations.renew.completeExceptionally(new OnvifNetworkException("temporary"));
        clock.next();
        assertEquals(9000, clock.millis());
        assertEquals(2, operations.renews);
        operations.renew.complete(new EventSubscriptionStatus(END, "2030-01-01T00:01:10Z", ""));
        assertFalse(manager.getCompletion().isDone());
        assertFalse(operations.pull.isCancelled());
    }

    @Test
    public void listenerExceptionDoesNotDiscardRemainingEventsOrStopPolling() {
        AtomicInteger delivered = new AtomicInteger();
        manager.subscribe(event -> {
            if (delivered.incrementAndGet() == 1) throw new IllegalStateException("user callback");
        }).join();
        clock.next();
        operations.pull.complete(response(event(), event()));
        assertEquals(2, delivered.get());
        clock.next();
        assertEquals(2, operations.pulls);
        assertFalse(manager.getCompletion().isDone());
    }

    @Test
    public void callbackCanCloseSessionWithoutDeadlockOrFurtherDelivery() {
        AtomicInteger delivered = new AtomicInteger();
        manager.subscribe(event -> {
            delivered.incrementAndGet();
            manager.unsubscribe().join();
        }).join();
        clock.next();
        operations.pull.complete(response(event(), event()));
        assertEquals(1, delivered.get());
        assertTrue(clock.isShutdown());
        manager.getCompletion().join();
    }

    @Test
    public void repeatedUnsubscribeSharesRequestAndCancelsInFlightWork() {
        operations.unsubscribed = new CompletableFuture<>();
        manager.subscribe(event -> {}).join();
        clock.next();
        clock.next();
        CompletableFuture<Void> first = manager.unsubscribe();
        assertSame(first, manager.unsubscribe());
        assertFalse(first.isDone());
        assertTrue(operations.pull.isCancelled());
        assertTrue(operations.renew.isCancelled());
        assertEquals(1, operations.unsubscribes);
        assertNull(manager.getSubscription());
        assertTrue(clock.isShutdown());
        operations.unsubscribed.completeExceptionally(new OnvifNetworkException("offline"));
        first.join(); // Existing best-effort unsubscribe behavior is retained.
        manager.getCompletion().join();
    }

    @Test
    public void latePullResponseAfterUnsubscribeCannotDeliverOrReviveSession() {
        CompletableFuture<PullMessagesResponse> late = new CompletableFuture<PullMessagesResponse>() {
            @Override public boolean cancel(boolean interrupt) { return false; }
        };
        operations.pullResults.add(late);
        AtomicInteger delivered = new AtomicInteger();
        manager.subscribe(event -> delivered.incrementAndGet()).join();
        clock.next();
        manager.unsubscribe().join();
        late.complete(response(event()));
        assertEquals(0, delivered.get());
        assertNull(manager.getSubscription());
        assertEquals(1, operations.pulls);
    }

    @Test
    public void cancellingCreationStillCleansUpLateRemoteSubscription() {
        operations.created = new CompletableFuture<>();
        CompletableFuture<Void> starting = manager.subscribe(event -> {});
        starting.cancel(true);
        CompletableFuture<Void> closing = manager.unsubscribe();
        assertFalse(closing.isDone());
        assertFalse(operations.created.isCancelled());
        operations.created.complete(subscription(NOW, END));
        closing.join();
        assertEquals(1, operations.unsubscribes);
        assertEquals(0, operations.pulls);
        assertTrue(clock.isShutdown());
        assertNull(manager.getSubscription());
    }

    @Test
    public void failedCreationClosesSchedulerAndReportsOriginalFailure() {
        OnvifAuthException error = new OnvifAuthException(403, "denied");
        operations.created = failed(error);
        assertSame(error, failure(manager.subscribe(event -> {})));
        assertSame(error, failure(manager.getCompletion()));
        assertTrue(clock.isShutdown());
        assertEquals(0, operations.unsubscribes);
    }

    @Test
    public void repeatedSubscribeIsRejectedWithoutLeaking() {
        operations.created = CompletableFuture.completedFuture(subscription(NOW, END));
        manager.subscribe(event -> {}).join();
        assertTrue(failure(manager.subscribe(event -> {})) instanceof IllegalStateException);
        assertEquals(1, operations.creates);
    }

    @Test
    public void missingSubscriptionEndpointFailsInitialization() {
        operations.created = CompletableFuture.completedFuture(new EventSubscription("", "", "", "", ""));
        assertTrue(failure(manager.subscribe(event -> {})) instanceof IllegalStateException);
        assertTrue(clock.isShutdown());
    }

    @Test
    public void cancellingCompletionObserverDoesNotStopPolling() {
        manager.subscribe(event -> {}).join();
        manager.getCompletion().cancel(true);
        clock.next();
        assertEquals(1, operations.pulls);
        assertFalse(manager.getCompletion().isDone());
    }

    @Test
    public void cancellingUnsubscribeObserverCannotLeakLateSubscription() {
        operations.created = new CompletableFuture<>();
        manager.subscribe(event -> {});
        manager.unsubscribe().cancel(true);
        operations.created.complete(subscription(NOW, END));
        assertEquals(1, operations.unsubscribes);
        manager.getCompletion().join();
    }

    @Test
    public void retryBudgetAlsoBoundsRenewalFailures() {
        manager.subscribe(event -> {}).join();
        clock.next();
        OnvifNetworkException error = new OnvifNetworkException("offline");
        for (int i = 0; i < 4; i++) {
            clock.next();
            operations.renew.completeExceptionally(error);
        }
        assertEquals(4, operations.renews);
        assertSame(error, failure(manager.getCompletion()));
        assertTrue(clock.isShutdown());
    }

    @Test
    public void absentPullTimesDoNotEraseKnownLeaseOrPostponeRenewal() {
        manager.subscribe(event -> {}).join();
        clock.next();
        operations.pull.complete(new PullMessagesResponse(java.util.Collections.emptyList(), "", null, ""));
        assertEquals(END, manager.getSubscription().getTerminationTime());
        clock.next(); // pending next pull
        clock.next(); // renewal still at original deadline
        assertEquals(8000, clock.millis());
    }

    @Test
    public void concurrentUnsubscribeStillSendsOnlyOneRemoteRequest() throws Exception {
        manager.subscribe(event -> {}).join();
        CountDownLatch release = new CountDownLatch(1);
        java.util.function.Supplier<CompletableFuture<Void>> close = () -> {
            try { release.await(); }
            catch (InterruptedException error) { throw new AssertionError(error); }
            return manager.unsubscribe();
        };
        CompletableFuture<CompletableFuture<Void>> first = CompletableFuture.supplyAsync(close);
        CompletableFuture<CompletableFuture<Void>> second = CompletableFuture.supplyAsync(close);
        release.countDown();
        assertSame(first.get(3, TimeUnit.SECONDS), second.get(3, TimeUnit.SECONDS));
        assertEquals(1, operations.unsubscribes);
    }

    @Test
    public void terminalObserverCanJoinCleanupWithoutBlockingItsStart() throws Exception {
        operations.unsubscribed = new CompletableFuture<>();
        manager.subscribe(event -> {}).join();
        clock.next();
        CompletableFuture<Void> observed = manager.getCompletion().handle((ignored, error) -> {
            assertTrue(clock.isShutdown());
            assertEquals(1, operations.unsubscribes);
            manager.unsubscribe().join();
            return null;
        });
        CompletableFuture<Void> failing = CompletableFuture.runAsync(() ->
                operations.pull.completeExceptionally(new OnvifAuthException(403, "denied")));
        try {
            assertTrue(operations.unsubscribeStarted.await(3, TimeUnit.SECONDS));
        } finally {
            operations.unsubscribed.complete(null);
        }
        failing.get(3, TimeUnit.SECONDS);
        observed.get(3, TimeUnit.SECONDS);
    }

    @Test
    public void blockingSubscribeContinuationDoesNotDelayPollingOrRenewal() throws Exception {
        operations.created = new CompletableFuture<>();
        CompletableFuture<Void> starting = manager.subscribe(event -> {});
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        starting.thenRun(() -> {
            entered.countDown();
            try { release.await(); }
            catch (InterruptedException error) { throw new AssertionError(error); }
        });
        CompletableFuture<Void> creation = CompletableFuture.runAsync(() -> operations.created.complete(subscription(NOW, END)));
        try {
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            clock.next();
            clock.next();
            assertEquals(1, operations.pulls);
            assertEquals(1, operations.renews);
        } finally {
            release.countDown();
        }
        creation.get(3, TimeUnit.SECONDS);
    }

    @Test
    public void waitingForEventDispatchDoesNotBlockRenewal() {
        Queue<Runnable> dispatch = new ArrayDeque<>();
        EventManager isolated = new EventManager(operations, clock, clock::nanoTime, dispatch::add);
        try {
            AtomicInteger delivered = new AtomicInteger();
            operations.pullResults.add(CompletableFuture.completedFuture(response(event())));
            isolated.subscribe(event -> delivered.incrementAndGet()).join();
            clock.next();
            assertEquals(0, delivered.get());
            assertEquals(1, dispatch.size());
            clock.next();
            assertEquals(1, operations.renews);
            dispatch.remove().run();
            assertEquals(1, delivered.get());
        } finally {
            isolated.unsubscribe();
        }
    }

    private static EventSubscription subscription(String current, String termination) {
        return new EventSubscription("http://camera.test/subscription", current, termination,
                "<ref:Id xmlns:ref='urn:test'>token</ref:Id>", "");
    }
    private static Event event() { return new Event("Motion", null, null, NOW, "Changed", ""); }
    private static PullMessagesResponse response(Event... events) {
        return new PullMessagesResponse(Arrays.asList(events), NOW, END, "");
    }
    private static <T> CompletableFuture<T> failed(Throwable error) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }
    private static Throwable failure(CompletableFuture<?> future) {
        assertTrue("Future must have completed", future.isDone());
        try { future.join(); throw new AssertionError("Expected failure"); }
        catch (CompletionException error) { return error.getCause(); }
    }

    private static class Operations implements EventOperations {
        CompletableFuture<EventSubscription> created = CompletableFuture.completedFuture(subscription(NOW, END));
        CompletableFuture<Void> unsubscribed = CompletableFuture.completedFuture(null);
        final Queue<CompletableFuture<PullMessagesResponse>> pullResults = new ArrayDeque<>();
        CompletableFuture<PullMessagesResponse> pull;
        CompletableFuture<EventSubscriptionStatus> renew;
        EventSubscription renewed;
        String requestedLease;
        int creates, pulls, renews, unsubscribes;
        final CountDownLatch unsubscribeStarted = new CountDownLatch(1);
        public CompletableFuture<EventSession> subscribe(Consumer<Event> listener) { throw new UnsupportedOperationException(); }
        public CompletableFuture<EventSession> subscribe(String topic, Consumer<Event> listener) { throw new UnsupportedOperationException(); }
        public CompletableFuture<EventSubscription> createPullPointSubscription() { return createPullPointSubscription(null); }
        public CompletableFuture<EventSubscription> createPullPointSubscription(String topic) { creates++; return created; }
        public CompletableFuture<PullMessagesResponse> pullMessages(EventSubscription subscription, String timeout, int limit) {
            assertEquals("PT30S", timeout);
            assertEquals(10, limit);
            pulls++;
            pull = pullResults.isEmpty() ? new CompletableFuture<>() : pullResults.remove();
            return pull;
        }
        public CompletableFuture<EventSubscriptionStatus> renew(EventSubscription subscription, String termination) {
            renews++;
            renewed = subscription;
            requestedLease = termination;
            renew = new CompletableFuture<>();
            return renew;
        }
        public CompletableFuture<Void> unsubscribe(EventSubscription subscription) {
            unsubscribes++;
            unsubscribeStarted.countDown();
            return unsubscribed;
        }
    }
}
