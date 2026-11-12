package io.github.hyeonmo.models.events;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an active event subscription session that can be closed or unsubscribed.
 */
public class EventSession implements AutoCloseable {

    private final EventSubscription subscription;
    private final java.util.function.Supplier<CompletableFuture<Void>> unsubscribeAction;
    private final CompletableFuture<Void> completion;
    private final boolean completeOnUnsubscribe;
    private CompletableFuture<Void> unsubscribeFuture;

    public EventSession(EventSubscription subscription, java.util.function.Supplier<CompletableFuture<Void>> unsubscribeAction) {
        this(subscription, unsubscribeAction, new CompletableFuture<>(), true);
    }

    /**
     * Creates a session with an observable background polling/renewal lifetime.
     * The subscription value is the initial snapshot; renewal is managed internally.
     */
    public EventSession(EventSubscription subscription, java.util.function.Supplier<CompletableFuture<Void>> unsubscribeAction,
                        CompletableFuture<Void> completion) {
        this(subscription, unsubscribeAction, java.util.Objects.requireNonNull(completion, "completion"), false);
    }

    private EventSession(EventSubscription subscription, java.util.function.Supplier<CompletableFuture<Void>> unsubscribeAction,
                         CompletableFuture<Void> completion, boolean completeOnUnsubscribe) {
        this.subscription = subscription;
        this.unsubscribeAction = unsubscribeAction;
        this.completion = completion;
        this.completeOnUnsubscribe = completeOnUnsubscribe;
    }

    /** Returns the initial subscription snapshot; managed renewals run in the background. */
    public EventSubscription getSubscription() {
        return subscription;
    }

    /** Starts unsubscribe once and shares the same result with repeated callers. */
    public synchronized CompletableFuture<Void> unsubscribe() {
        if (unsubscribeFuture == null) {
            unsubscribeFuture = unsubscribeAction.get();
            if (completeOnUnsubscribe) {
                unsubscribeFuture.whenComplete((ignored, error) -> {
                    if (error == null) completion.complete(null);
                    else completion.completeExceptionally(error);
                });
            }
        }
        return unsubscribeFuture;
    }

    /**
     * Observes normal shutdown or terminal polling/renewal failure. Cancelling or
     * completing the returned observer does not stop the session; use unsubscribe().
     */
    public CompletableFuture<Void> getCompletion() {
        return completion.thenApply(ignored -> null);
    }

    @Override
    public void close() {
        unsubscribe().join();
    }
}
