package io.github.hyeonmo.requests.events;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

public class RenewRequest implements OnvifRequest {
    private final String terminationTime;

    public RenewRequest(String terminationTime) {
        this.terminationTime = terminationTime;
    }

    @Override
    public String getXml() {
        return "<wsnt:Renew xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\">" +
                "<wsnt:TerminationTime>" + terminationTime + "</wsnt:TerminationTime>" +
                "</wsnt:Renew>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.RENEW;
    }

    @Override
    public String getAction() {
        return "http://docs.oasis-open.org/wsn/bw-2/SubscriptionManager/RenewRequest";
    }
}
