package io.github.hyeonmo.operations;

import io.github.hyeonmo.models.events.Event;
import io.github.hyeonmo.models.events.EventSession;
import io.github.hyeonmo.models.events.EventSubscription;
import io.github.hyeonmo.models.events.EventSubscriptionStatus;
import io.github.hyeonmo.responses.events.PullMessagesResponse;

import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

/**
 * Operations related to ONVIF Events and PullPoint subscriptions.
 */
public interface EventOperations {

    /**
     * Creates a PullPoint subscription and starts delivering events to the listener.
     */
    CompletableFuture<EventSession> subscribe(Consumer<Event> listener);

    /**
     * Creates a PullPoint subscription with an optional topic filter and starts delivering events.
     */
    CompletableFuture<EventSession> subscribe(String topicFilter, Consumer<Event> listener);

    /**
     * Creates a raw PullPoint subscription without starting a polling loop.
     */
    CompletableFuture<EventSubscription> createPullPointSubscription();

    /**
     * Creates a raw PullPoint subscription with an optional topic filter.
     */
    CompletableFuture<EventSubscription> createPullPointSubscription(String topicFilter);

    /**
     * Pulls messages from an existing subscription endpoint.
     */
    CompletableFuture<PullMessagesResponse> pullMessages(EventSubscription subscription, String timeout, int messageLimit);

    /**
     * Renews an existing subscription.
     */
    CompletableFuture<EventSubscriptionStatus> renew(EventSubscription subscription, String terminationTime);

    /**
     * Unsubscribes from an existing subscription.
     */
    CompletableFuture<Void> unsubscribe(EventSubscription subscription);
}
