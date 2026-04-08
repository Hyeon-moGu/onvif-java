package io.github.hyeonmo.requests.events;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

public class CreatePullPointSubscriptionRequest implements OnvifRequest {

    private final String topicFilter;
    private final String initialTerminationTime;

    public CreatePullPointSubscriptionRequest() {
        this(null, "PT1H");
    }

    public CreatePullPointSubscriptionRequest(String topicFilter) {
        this(topicFilter, "PT1H");
    }

    public CreatePullPointSubscriptionRequest(String topicFilter, String initialTerminationTime) {
        this.topicFilter = topicFilter;
        this.initialTerminationTime = initialTerminationTime;
    }

    @Override
    public String getXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<CreatePullPointSubscription xmlns=\"http://www.onvif.org/ver10/events/wsdl\">");
        if (topicFilter != null && !topicFilter.trim().isEmpty()) {
            xml.append("<Filter>");
            xml.append("<tt:TopicExpression Dialect=\"http://www.onvif.org/ver10/tev/topics/Slice\" xmlns:tt=\"http://www.onvif.org/ver10/schema\" xmlns:tns1=\"http://www.onvif.org/ver10/topics\">");
            xml.append(topicFilter);
            xml.append("</tt:TopicExpression>");
            xml.append("</Filter>");
        }
        if (initialTerminationTime != null && !initialTerminationTime.trim().isEmpty()) {
            xml.append("<InitialTerminationTime>").append(initialTerminationTime).append("</InitialTerminationTime>");
        }
        xml.append("</CreatePullPointSubscription>");
        return xml.toString();
    }

    @Override
    public OnvifType getType() {
        return OnvifType.CREATE_PULLPOINT_SUBSCRIPTION;
    }

    @Override
    public String getAction() {
        return "http://www.onvif.org/ver10/events/wsdl/EventPortType/CreatePullPointSubscriptionRequest";
    }
}
