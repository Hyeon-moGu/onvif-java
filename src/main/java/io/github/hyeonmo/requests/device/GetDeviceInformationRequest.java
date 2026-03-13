package io.github.hyeonmo.requests.device;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

public class GetDeviceInformationRequest implements OnvifRequest {

    public static final String TAG = GetDeviceInformationRequest.class.getSimpleName();

    public GetDeviceInformationRequest() {
        super();
    }

    @Override
    public String getXml() {
        return "<GetDeviceInformation xmlns=\"http://www.onvif.org/ver10/device/wsdl\">" +
                "</GetDeviceInformation>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_DEVICE_INFORMATION;
    }
}
