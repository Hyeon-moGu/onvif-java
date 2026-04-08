package io.github.hyeonmo.models.events;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an active event subscription session that can be closed or unsubscribed.
 */
public class EventSession implements AutoCloseable {

    private final EventSubscription subscription;
    private final java.util.function.Supplier<CompletableFuture<Void>> unsubscribeAction;

    public EventSession(EventSubscription subscription, java.util.function.Supplier<CompletableFuture<Void>> unsubscribeAction) {
        this.subscription = subscription;
        this.unsubscribeAction = unsubscribeAction;
    }

    public EventSubscription getSubscription() {
        return subscription;
    }

    public CompletableFuture<Void> unsubscribe() {
        return unsubscribeAction.get();
    }

    @Override
    public void close() {
        unsubscribe().join();
    }
}
