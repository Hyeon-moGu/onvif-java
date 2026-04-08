package io.github.hyeonmo.operations.impl;

import io.github.hyeonmo.core.OnvifExecutor;
import io.github.hyeonmo.managers.EventManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.events.Event;
import io.github.hyeonmo.models.events.EventSession;
import io.github.hyeonmo.models.events.EventSubscription;
import io.github.hyeonmo.models.events.EventSubscriptionStatus;
import io.github.hyeonmo.operations.EventOperations;
import io.github.hyeonmo.requests.events.CreatePullPointSubscriptionRequest;
import io.github.hyeonmo.requests.events.PullMessagesRequest;
import io.github.hyeonmo.requests.events.RenewRequest;
import io.github.hyeonmo.requests.events.UnsubscribeRequest;
import io.github.hyeonmo.responses.events.PullMessagesResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EventOperationsImpl implements EventOperations {

    private final OnvifDevice device;
    private final OnvifExecutor executor;

    public EventOperationsImpl(OnvifDevice device) {
        this.device = device;
        this.executor = new OnvifExecutor();
    }

    @Override
    public CompletableFuture<EventSession> subscribe(Consumer<Event> listener) {
        return subscribe(null, listener);
    }

    @Override
    public CompletableFuture<EventSession> subscribe(String topicFilter, Consumer<Event> listener) {
        EventManager manager = new EventManager(device);
        return manager.subscribe(topicFilter, listener)
                .thenApply(ignored -> new EventSession(manager.getSubscription(), manager::unsubscribe));
    }

    @Override
    public CompletableFuture<EventSubscription> createPullPointSubscription() {
        return createPullPointSubscription(null);
    }

    @Override
    public CompletableFuture<EventSubscription> createPullPointSubscription(String topicFilter) {
        return executor.sendRequest(device, new CreatePullPointSubscriptionRequest(topicFilter));
    }

    @Override
    public CompletableFuture<PullMessagesResponse> pullMessages(EventSubscription subscription, String timeout, int messageLimit) {
        return executor.sendRequest(device, subscription.getAddress(), subscription.getReferenceParametersXml(), new PullMessagesRequest(timeout, messageLimit));
    }

    @Override
    public CompletableFuture<EventSubscriptionStatus> renew(EventSubscription subscription, String terminationTime) {
        return executor.sendRequest(device, subscription.getAddress(), subscription.getReferenceParametersXml(), new RenewRequest(terminationTime));
    }

    @Override
    public CompletableFuture<Void> unsubscribe(EventSubscription subscription) {
        return executor.sendRequest(device, subscription.getAddress(), subscription.getReferenceParametersXml(), new UnsubscribeRequest());
    }
}
