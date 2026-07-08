package io.github.hyeonmo.requests.ptz;

import io.github.hyeonmo.core.AuthXMLBuilder;
import io.github.hyeonmo.models.ptz.PtzOperationType;

public class PtzConfigurationOptionsRequest implements PtzBaseRequest {
    private final String xAddr, configurationToken, userName, password;
    private final long timeOffsetMs;

    public PtzConfigurationOptionsRequest(String xAddr, String configurationToken, String userName,
            String password, long timeOffsetMs) {
        this.xAddr = xAddr;
        this.configurationToken = configurationToken;
        this.userName = userName;
        this.password = password;
        this.timeOffsetMs = timeOffsetMs;
    }

    @Override
    public String getXml() {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password, timeOffsetMs);
        builder.setWsaAction("http://www.onvif.org/ver20/ptz/wsdl/GetConfigurationOptions");
        builder.setWsaTo(xAddr);
        return builder.getAuthHeader() + builder.getPtzConfigurationOptionsBody(configurationToken)
                + builder.getAuthEnd();
    }

    @Override
    public String getXAddr() {
        return xAddr;
    }

    @Override
    public PtzOperationType getPtzOperationType() {
        return PtzOperationType.CONFIGURATION_OPTIONS;
    }
}
