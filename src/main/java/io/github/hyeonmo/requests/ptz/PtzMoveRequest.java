package io.github.hyeonmo.requests.ptz;

import io.github.hyeonmo.core.AuthXMLBuilder;
import io.github.hyeonmo.models.ptz.PtzOperationType;
import io.github.hyeonmo.models.ptz.PtzType;

public class PtzMoveRequest implements PtzBaseRequest {
    private final String xAddr;
    private final String profileToken;
    private final String userName;
    private final String password;
    private final PtzType ptzType;
    private final long timeOffsetMs;

    public PtzMoveRequest(String xAddr, String profileToken, String userName, String password, PtzType ptzType, long timeOffsetMs) {
        this.xAddr = xAddr;
        this.profileToken = profileToken;
        this.userName = userName;
        this.password = password;
        this.ptzType = ptzType;
        this.timeOffsetMs = timeOffsetMs;
    }

    @Override
    public String getXml() {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password, timeOffsetMs);
        return builder.getAuthHeader() + builder.getPtzMoveBody(profileToken, ptzType) + builder.getAuthEnd();
    }

    @Override
    public String getXAddr() {
        return xAddr;
    }

    @Override
    public PtzOperationType getPtzOperationType() {
        return PtzOperationType.MOVE;
    }
}
