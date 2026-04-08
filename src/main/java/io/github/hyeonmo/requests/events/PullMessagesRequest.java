package io.github.hyeonmo.requests.events;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

public class PullMessagesRequest implements OnvifRequest {
    private final String timeout;
    private final int messageLimit;

    public PullMessagesRequest(String timeout, int messageLimit) {
        this.timeout = timeout;
        this.messageLimit = messageLimit;
    }

    @Override
    public String getXml() {
        return "<PullMessages xmlns=\"http://www.onvif.org/ver10/events/wsdl\">" +
                "<Timeout>" + timeout + "</Timeout>" +
                "<MessageLimit>" + messageLimit + "</MessageLimit>" +
                "</PullMessages>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.PULL_MESSAGES;
    }

    @Override
    public String getAction() {
        return "http://www.onvif.org/ver10/events/wsdl/PullPointSubscription/PullMessagesRequest";
    }
}
