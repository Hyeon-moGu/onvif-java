package io.github.hyeonmo.requests.ptz;

import io.github.hyeonmo.core.AuthXMLBuilder;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzOperationType;

public class PtzPresetRequest implements PtzBaseRequest {
    private final String xAddr;
    private final String profileToken;
    private final String userName;
    private final String password;
    private final PresetCommand presetCommand;
    private final long timeOffsetMs;

    public PtzPresetRequest(String xAddr, String profileToken, String userName, String password, PresetCommand presetCommand, long timeOffsetMs) {
        this.xAddr = xAddr;
        this.profileToken = profileToken;
        this.userName = userName;
        this.password = password;
        this.presetCommand = presetCommand;
        this.timeOffsetMs = timeOffsetMs;
    }

    @Override
    public String getXml() {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password, timeOffsetMs);
        return builder.getAuthHeader() + builder.getPresetBody(profileToken, presetCommand) + builder.getAuthEnd();
    }

    @Override
    public String getXAddr() {
        return xAddr;
    }

    @Override
    public PtzOperationType getPtzOperationType() {
        return PtzOperationType.PRESET;
    }
}
