package io.github.hyeonmo.managers;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.events.Event;
import io.github.hyeonmo.models.events.EventSubscription;
import io.github.hyeonmo.operations.EventOperations;
import io.github.hyeonmo.operations.impl.EventOperationsImpl;
import io.github.hyeonmo.responses.events.PullMessagesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class EventManager {
    private static final Logger log = LoggerFactory.getLogger(EventManager.class);

    private final EventOperations operations;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    
    private volatile EventSubscription subscription;
    private volatile boolean isSubscribed = false;
    private Consumer<Event> eventListener;

    public EventManager(OnvifDevice device) {
        this.operations = new EventOperationsImpl(device);
    }

    public CompletableFuture<Void> subscribe(Consumer<Event> listener) {
        return subscribe(null, listener);
    }

    public CompletableFuture<Void> subscribe(String topicFilter, Consumer<Event> listener) {
        this.eventListener = listener;
        return operations.createPullPointSubscription(topicFilter)
                .thenAccept(subscription -> {
                    if (subscription == null || !subscription.hasAddress()) {
                        throw new IllegalStateException("Camera did not return a usable subscription endpoint.");
                    }
                    this.subscription = subscription;
                    this.isSubscribed = true;
                    startPolling();
                });
    }

    private void startPolling() {
        scheduler.execute(this::pollOnce);
    }

    private void pollOnce() {
        if (!isSubscribed || subscription == null || scheduler.isShutdown()) {
            return;
        }

        EventSubscription currentSubscription = subscription;
        operations.pullMessages(currentSubscription, "PT30S", 10)
                .thenAccept(response -> {
                    updateSubscriptionTimes(response);

                    if (eventListener != null && !response.getEvents().isEmpty()) {
                        response.getEvents().forEach(eventListener);
                    }

                    if (isSubscribed && !scheduler.isShutdown()) {
                        scheduler.execute(this::pollOnce);
                    }
                })
                .exceptionally(ex -> {
                    if (isSubscribed) {
                        isSubscribed = false;
                        log.warn("Event polling stopped: {}", ex.getMessage());
                    }
                    scheduler.shutdownNow();
                    return null;
                });
    }

    private void updateSubscriptionTimes(PullMessagesResponse response) {
        if (subscription == null) {
            return;
        }

        subscription = subscription.withTimes(response.getCurrentTime(), response.getTerminationTime());
    }

    public EventSubscription getSubscription() {
        return subscription;
    }

    public CompletableFuture<Void> unsubscribe() {
        isSubscribed = false;
        EventSubscription currentSubscription = subscription;
        subscription = null;

        if (currentSubscription != null) {
            return operations.unsubscribe(currentSubscription)
                    .handle((res, ex) -> {
                        if (ex != null) {
                            log.debug("SOAP unsubscribe failed: {}", ex.getMessage());
                        }
                        scheduler.shutdownNow();
                        return null;
                    });
        } else {
            scheduler.shutdownNow();
            return CompletableFuture.completedFuture(null);
        }
    }
}
