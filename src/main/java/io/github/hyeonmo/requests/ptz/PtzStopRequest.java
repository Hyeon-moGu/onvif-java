package io.github.hyeonmo.requests.ptz;

import io.github.hyeonmo.core.AuthXMLBuilder;
import io.github.hyeonmo.models.ptz.PtzOperationType;

public class PtzStopRequest implements PtzBaseRequest {
    private final String xAddr;
    private final String profileToken;
    private final String userName;
    private final String password;
    private final long timeOffsetMs;

    public PtzStopRequest(String xAddr, String profileToken, String userName, String password, long timeOffsetMs) {
        this.xAddr = xAddr;
        this.profileToken = profileToken;
        this.userName = userName;
        this.password = password;
        this.timeOffsetMs = timeOffsetMs;
    }

    @Override
    public String getXml() {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password, timeOffsetMs);
        return builder.getAuthHeader() + builder.getPtzStopBody(profileToken) + builder.getAuthEnd();
    }

    @Override
    public String getXAddr() {
        return xAddr;
    }

    @Override
    public PtzOperationType getPtzOperationType() {
        return PtzOperationType.STOP;
    }
}
