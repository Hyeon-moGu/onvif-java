package io.github.hyeonmo.requests.ptz;

import io.github.hyeonmo.core.AuthXMLBuilder;
import io.github.hyeonmo.models.ptz.PtzOperationType;

public class PtzPositionRequest implements PtzBaseRequest {
    private final String xAddr, profileToken, userName, password;
    private final long timeOffsetMs;

    public PtzPositionRequest(String xAddr, String profileToken, String userName, String password,
            long timeOffsetMs) {
        this.xAddr = xAddr;
        this.profileToken = profileToken;
        this.userName = userName;
        this.password = password;
        this.timeOffsetMs = timeOffsetMs;
    }

    @Override
    public String getXml() {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password, timeOffsetMs);
        builder.setWsaAction("http://www.onvif.org/ver20/ptz/wsdl/GetStatus");
        builder.setWsaTo(xAddr);
        return builder.getAuthHeader() + builder.getPtzStatusBody(profileToken)
                + builder.getAuthEnd();
    }

    @Override
    public String getXAddr() {
        return xAddr;
    }

    @Override
    public PtzOperationType getPtzOperationType() {
        return PtzOperationType.POSITION;
    }
}
