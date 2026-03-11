package io.github.hyeonmo.requests.device;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

/**
 * Modified for v2.0 - callback removed.
 */
public class GetSystemDateAndTimeRequest implements OnvifRequest {

    public GetSystemDateAndTimeRequest() {
    }

    @Override
    public String getXml() {
        return "<GetSystemDateAndTime xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_SYSTEM_DATE_AND_TIME;
    }
}
