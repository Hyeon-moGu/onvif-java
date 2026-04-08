package io.github.hyeonmo.requests.events;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

public class UnsubscribeRequest implements OnvifRequest {
    @Override
    public String getXml() {
        return "<wsnt:Unsubscribe xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.UNSUBSCRIBE;
    }

    @Override
    public String getAction() {
        return "http://docs.oasis-open.org/wsn/bw-2/SubscriptionManager/UnsubscribeRequest";
    }
}
