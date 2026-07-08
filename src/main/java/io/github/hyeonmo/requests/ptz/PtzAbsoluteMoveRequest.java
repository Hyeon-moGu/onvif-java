package io.github.hyeonmo.requests.ptz;

import io.github.hyeonmo.core.AuthXMLBuilder;
import io.github.hyeonmo.models.ptz.PtzConfigurationOptions;
import io.github.hyeonmo.models.ptz.PtzOperationType;
import io.github.hyeonmo.models.ptz.PtzPosition;
import io.github.hyeonmo.models.ptz.PtzSpace;
import io.github.hyeonmo.models.ptz.PtzSpeed;

public class PtzAbsoluteMoveRequest implements PtzBaseRequest {
    private final String xAddr, profileToken, userName, password;
    private final PtzPosition position;
    private final PtzSpeed speed;
    private final PtzConfigurationOptions options;
    private final long timeOffsetMs;

    public PtzAbsoluteMoveRequest(String xAddr, String profileToken, String userName,
            String password, PtzPosition position, PtzSpeed speed, PtzConfigurationOptions options,
            long timeOffsetMs) {
        this.xAddr = xAddr;
        this.profileToken = profileToken;
        this.userName = userName;
        this.password = password;
        this.position = position;
        this.speed = speed;
        this.options = options;
        this.timeOffsetMs = timeOffsetMs;
    }

    @Override
    public String getXml() {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password, timeOffsetMs);
        builder.setWsaAction("http://www.onvif.org/ver20/ptz/wsdl/AbsoluteMove");
        builder.setWsaTo(xAddr);
        return builder.getAuthHeader()
                + builder.getPtzAbsoluteMoveBody(profileToken, position, speed,
                        uri(options.getAbsolutePanTiltSpace()), uri(options.getAbsoluteZoomSpace()),
                        uri(options.getPanTiltSpeedSpace()), uri(options.getZoomSpeedSpace()))
                + builder.getAuthEnd();
    }

    private static String uri(PtzSpace space) {
        return space == null ? null : space.getUri();
    }

    @Override
    public String getXAddr() {
        return xAddr;
    }

    @Override
    public PtzOperationType getPtzOperationType() {
        return PtzOperationType.ABSOLUTE_MOVE;
    }
}
