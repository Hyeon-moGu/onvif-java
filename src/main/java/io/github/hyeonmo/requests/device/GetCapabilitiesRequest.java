package io.github.hyeonmo.requests.device;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

public class GetCapabilitiesRequest implements OnvifRequest {

    public static final String TAG = GetCapabilitiesRequest.class.getSimpleName();

    public GetCapabilitiesRequest() {
        super();
    }

    @Override
    public String getXml() {
        return "<GetCapabilities xmlns=\"http://www.onvif.org/ver10/device/wsdl\">" +
                "<Category>All</Category>" +
                "</GetCapabilities>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_CAPABILITIES;
    }
}
