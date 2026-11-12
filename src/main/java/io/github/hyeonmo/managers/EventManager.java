package io.github.hyeonmo.managers;

import io.github.hyeonmo.exceptions.OnvifNetworkException;
import io.github.hyeonmo.exceptions.OnvifSoapException;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.events.Event;
import io.github.hyeonmo.models.events.EventSubscription;
import io.github.hyeonmo.models.events.EventSubscriptionStatus;
import io.github.hyeonmo.operations.EventOperations;
import io.github.hyeonmo.operations.impl.EventOperationsImpl;
import io.github.hyeonmo.responses.events.PullMessagesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/** Owns a single managed PullPoint subscription and its polling/renewal lifecycle. */
public class EventManager {
    private static final Logger log = LoggerFactory.getLogger(EventManager.class);
    private static final int MAX_RETRIES = 3;
    private final Object lock = new Object();
    private final EventOperations operations;
    private final ScheduledExecutorService scheduler;
    private final LongSupplier nanoTime;
    private final Executor callbackExecutor;
    private final CompletableFuture<Void> completion = new CompletableFuture<>();

    private volatile EventSubscription subscription;
    private volatile boolean active;
    private volatile Throwable terminalFailure;
    private boolean started;
    private boolean stopped;
    private boolean creating;
    private boolean remoteUnsubscribeStarted;
    private Consumer<Event> eventListener;
    private CompletableFuture<Void> startFuture;
    private CompletableFuture<Void> unsubscribeFuture;
    private CompletableFuture<PullMessagesResponse> pullRequest;
    private CompletableFuture<EventSubscriptionStatus> renewRequest;
    private ScheduledFuture<?> pollTask;
    private ScheduledFuture<?> renewTask;
    private long renewAtNanos = Long.MAX_VALUE;
    private long leaseVersion;
    private int pullFailures;
    private int renewFailures;

    public EventManager(OnvifDevice device) {
        this(new EventOperationsImpl(device), Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "onvif-events");
            thread.setDaemon(true);
            return thread;
        }), System::nanoTime, ForkJoinPool.commonPool());
    }

    EventManager(EventOperations operations, ScheduledExecutorService scheduler, LongSupplier nanoTime) {
        this(operations, scheduler, nanoTime, Runnable::run);
    }

    EventManager(EventOperations operations, ScheduledExecutorService scheduler, LongSupplier nanoTime, Executor callbackExecutor) {
        this.operations = operations;
        this.scheduler = scheduler;
        this.nanoTime = nanoTime;
        this.callbackExecutor = callbackExecutor;
    }

    public CompletableFuture<Void> subscribe(Consumer<Event> listener) {
        return subscribe(null, listener);
    }

    public CompletableFuture<Void> subscribe(String topicFilter, Consumer<Event> listener) {
        CompletableFuture<EventSubscription> request;
        synchronized (lock) {
            if (started || stopped) return failed(new IllegalStateException("Event manager is already used or closed."));
            started = true;
            creating = true;
            eventListener = listener;
            startFuture = new CompletableFuture<>();
            try {
                request = java.util.Objects.requireNonNull(operations.createPullPointSubscription(topicFilter), "create future");
            } catch (RuntimeException error) {
                request = failed(error);
            }
        }
        startFuture.whenComplete((ignored, error) -> {
            if (startFuture.isCancelled()) unsubscribe();
        });
        request.whenComplete(this::created);
        return startFuture;
    }

    private void created(EventSubscription created, Throwable error) {
        if (error == null && (created == null || !created.hasAddress())) {
            error = new IllegalStateException("Camera did not return a usable subscription endpoint.");
        }
        boolean closed;
        synchronized (lock) {
            creating = false;
            closed = stopped;
            if (error == null) {
                subscription = created;
                active = !closed;
            }
        }
        if (error != null) {
            if (!closed) fail(unwrap(error));
            startFuture.completeExceptionally(unwrap(error));
            finishUnsubscribeIfReady();
        } else if (closed) {
            startFuture.cancel(false);
            finishUnsubscribeIfReady();
        } else {
            // A caller's subscribe continuation may block; start timers before invoking it.
            synchronized (lock) {
                if (active) {
                    scheduleRenewal(created.getCurrentTime(), created.getTerminationTime());
                    schedulePoll(0);
                }
            }
            startFuture.complete(null);
        }
    }

    private void schedulePoll(long delayMs) {
        if (active) pollTask = scheduler.schedule(this::pollOnce, delayMs, TimeUnit.MILLISECONDS);
    }

    private void pollOnce() {
        CompletableFuture<PullMessagesResponse> request;
        long version;
        synchronized (lock) {
            if (!active || pullRequest != null) return;
            version = leaseVersion;
            try {
                request = java.util.Objects.requireNonNull(operations.pullMessages(subscription, "PT30S", 10), "pull future");
            } catch (RuntimeException error) {
                request = failed(error);
            }
            pullRequest = request;
        }
        // Never let a user listener occupy the renewal scheduler or an OkHttp callback thread.
        request.whenCompleteAsync((response, error) -> pulled(response, error, version), callbackExecutor);
    }

    private void pulled(PullMessagesResponse response, Throwable error, long version) {
        Consumer<Event> listener;
        synchronized (lock) {
            pullRequest = null;
            if (!active) return;
            if (error != null && retryable(error, ++pullFailures)) {
                schedulePoll(retryDelay(pullFailures));
                return;
            }
            listener = eventListener;
            if (error == null && response != null) {
                pullFailures = 0;
                // A pull issued before a successful Renew may contain stale lease times.
                if (version == leaseVersion) {
                    updateTimes(response.getCurrentTime(), response.getTerminationTime());
                    scheduleRenewal(response.getCurrentTime(), response.getTerminationTime());
                }
            }
        }
        if (error != null || response == null) {
            fail(error != null ? unwrap(error) : new OnvifSoapException("Missing PullMessages response", ""));
            return;
        }
        if (listener != null && response.getEvents() != null) {
            for (Event event : response.getEvents()) {
                if (!active) break;
                try {
                    listener.accept(event);
                } catch (RuntimeException ignored) {
                    // User callback failures must not destroy the camera subscription.
                    log.warn("Event listener failed; continuing event delivery");
                }
            }
        }
        synchronized (lock) {
            // Bound traffic from cameras that return empty responses without honoring PT30S.
            schedulePoll(response.getEvents() == null || response.getEvents().isEmpty() ? 100 : 0);
        }
    }

    private void scheduleRenewal(String currentTime, String terminationTime) {
        if (!active || renewRequest != null || renewFailures != 0) return;
        long lifetime = lifetimeMillis(currentTime, terminationTime);
        long delay = lifetime < 0 ? 30000 : Math.max(1, Math.min(1800000, lifetime - lifetime / 5));
        long due = nanoTime.getAsLong() + TimeUnit.MILLISECONDS.toNanos(delay);
        // Pull responses can shorten the deadline, but must not postpone an existing renewal.
        if (renewTask != null && !renewTask.isDone() && due >= renewAtNanos) return;
        if (renewTask != null) renewTask.cancel(false);
        renewAtNanos = due;
        renewTask = scheduler.schedule(this::renewOnce, delay, TimeUnit.MILLISECONDS);
    }

    private void renewOnce() {
        CompletableFuture<EventSubscriptionStatus> request;
        synchronized (lock) {
            if (!active || renewRequest != null) return;
            renewTask = null;
            renewAtNanos = Long.MAX_VALUE;
            try {
                request = java.util.Objects.requireNonNull(operations.renew(subscription, "PT1H"), "renew future");
            } catch (RuntimeException error) {
                request = failed(error);
            }
            renewRequest = request;
        }
        request.whenComplete(this::renewed);
    }

    private void renewed(EventSubscriptionStatus status, Throwable error) {
        synchronized (lock) {
            renewRequest = null;
            if (!active) return;
            if (error != null && retryable(error, ++renewFailures)) {
                renewTask = scheduler.schedule(this::renewOnce, retryDelay(renewFailures), TimeUnit.MILLISECONDS);
                return;
            }
            if (error == null && status != null) {
                if (lifetimeMillis(status.getCurrentTime(), status.getTerminationTime()) == 0) {
                    error = new OnvifSoapException("Renew returned an expired subscription", "");
                } else {
                    renewFailures = 0;
                    leaseVersion++;
                    updateTimes(status.getCurrentTime(), status.getTerminationTime());
                    scheduleRenewal(status.getCurrentTime(), status.getTerminationTime());
                    return;
                }
            }
        }
        fail(error != null ? unwrap(error) : new OnvifSoapException("Missing Renew response", ""));
    }

    private void updateTimes(String current, String termination) {
        subscription = subscription.withTimes(nonempty(current) ? current : subscription.getCurrentTime(),
                nonempty(termination) ? termination : subscription.getTerminationTime());
    }

    private static boolean nonempty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static long lifetimeMillis(String current, String termination) {
        try {
            if (!nonempty(current) || !nonempty(termination)) return -1;
            return Math.max(0, Duration.between(Instant.parse(current.trim()), Instant.parse(termination.trim())).toMillis());
        } catch (RuntimeException invalidTime) {
            return -1;
        }
    }

    private static boolean retryable(Throwable error, int failures) {
        return failures <= MAX_RETRIES && unwrap(error) instanceof OnvifNetworkException;
    }

    private static long retryDelay(int failures) {
        return 1000L << (failures - 1);
    }

    private static Throwable unwrap(Throwable error) {
        while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }

    private void fail(Throwable error) {
        synchronized (lock) {
            if (stopped) return;
            active = false;
            terminalFailure = error;
        }
        // Stop local work and initiate cleanup before invoking user error observers.
        // No user continuations run under the lifecycle lock; close() may join from a callback.
        unsubscribe();
        completion.completeExceptionally(error);
    }

    public EventSubscription getSubscription() {
        return subscription;
    }

    /** Completes on shutdown, or exceptionally when polling/renewal cannot recover. */
    public CompletableFuture<Void> getCompletion() {
        return completion.thenApply(ignored -> null);
    }

    public CompletableFuture<Void> unsubscribe() {
        CompletableFuture<?> pull;
        CompletableFuture<?> renew;
        synchronized (lock) {
            if (unsubscribeFuture != null) return unsubscribeFuture;
            unsubscribeFuture = new CompletableFuture<>();
            stopped = true;
            active = false;
            eventListener = null;
            if (pollTask != null) pollTask.cancel(false);
            if (renewTask != null) renewTask.cancel(false);
            pull = pullRequest;
            renew = renewRequest;
            scheduler.shutdownNow();
        }
        if (pull != null) pull.cancel(true);
        if (renew != null) renew.cancel(true);
        // Keep an in-flight Create alive so a late endpoint can still be unsubscribed.
        finishUnsubscribeIfReady();
        return unsubscribeFuture;
    }

    private void finishUnsubscribeIfReady() {
        EventSubscription closing;
        synchronized (lock) {
            if (!stopped || creating || remoteUnsubscribeStarted) return;
            closing = subscription;
            subscription = null;
            remoteUnsubscribeStarted = true;
        }
        CompletableFuture<Void> request;
        try {
            request = closing == null ? CompletableFuture.completedFuture(null)
                    : java.util.Objects.requireNonNull(operations.unsubscribe(closing), "unsubscribe future");
        } catch (RuntimeException error) {
            request = failed(error);
        }
        request.whenComplete((ignored, error) -> {
            // Preserve the existing best-effort unsubscribe contract.
            if (error != null) log.debug("SOAP unsubscribe failed");
            unsubscribeFuture.complete(null);
            if (terminalFailure == null) completion.complete(null);
            else completion.completeExceptionally(terminalFailure);
        });
    }

    private static <T> CompletableFuture<T> failed(Throwable error) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }
}
