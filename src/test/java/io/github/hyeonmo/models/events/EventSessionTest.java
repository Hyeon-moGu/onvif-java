package io.github.hyeonmo.models.events;

import io.github.hyeonmo.exceptions.OnvifNetworkException;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class EventSessionTest {
    @Test
    public void unsubscribeIsIdempotentAndLegacyCompletionFollowsIt() {
        AtomicInteger calls = new AtomicInteger();
        CompletableFuture<Void> closing = new CompletableFuture<>();
        EventSession session = new EventSession(null, () -> { calls.incrementAndGet(); return closing; });
        assertFalse(session.getCompletion().isDone());
        assertSame(closing, session.unsubscribe());
        assertSame(closing, session.unsubscribe());
        assertEquals(1, calls.get());
        closing.complete(null);
        session.getCompletion().join();
        session.close();
        assertEquals(1, calls.get());
    }

    @Test
    public void terminalFailureKeepsOriginalExceptionAndObserverCancellationIsIsolated() {
        CompletableFuture<Void> lifetime = new CompletableFuture<>();
        EventSession session = new EventSession(null, () -> CompletableFuture.completedFuture(null), lifetime);
        session.getCompletion().cancel(true);
        assertFalse(lifetime.isDone());
        OnvifNetworkException cause = new OnvifNetworkException("offline");
        lifetime.completeExceptionally(cause);
        try { session.getCompletion().join(); fail("Expected lifetime failure"); }
        catch (CompletionException error) { assertSame(cause, error.getCause()); }
        session.unsubscribe().join();
        assertTrue(session.getCompletion().isCompletedExceptionally());
    }

    @Test
    public void closeStillWaitsForUnsubscribe() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CompletableFuture<Void> closing = new CompletableFuture<>();
        EventSession session = new EventSession(null, () -> { entered.countDown(); return closing; });
        CompletableFuture<Void> closed = CompletableFuture.runAsync(session::close);
        try {
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            assertFalse(closed.isDone());
        } finally {
            closing.complete(null);
        }
        closed.get(3, TimeUnit.SECONDS);
    }

    @Test
    public void customUnsubscribeFailureRemainsExceptional() {
        CompletableFuture<Void> closing = new CompletableFuture<>();
        IllegalStateException cause = new IllegalStateException("custom failure");
        closing.completeExceptionally(cause);
        EventSession session = new EventSession(null, () -> closing);
        try { session.close(); fail("Expected close failure"); }
        catch (CompletionException error) { assertSame(cause, error.getCause()); }
        assertTrue(session.getCompletion().isCompletedExceptionally());
    }

    @Test(expected = NullPointerException.class)
    public void lifetimeConstructorRejectsNullCompletion() {
        new EventSession(null, () -> CompletableFuture.completedFuture(null), null);
    }
}
