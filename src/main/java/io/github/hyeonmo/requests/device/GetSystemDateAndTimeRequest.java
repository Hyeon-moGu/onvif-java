package io.github.hyeonmo.requests.device;

import io.github.hyeonmo.listeners.device.OnvifSystemDateAndTimeListener;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

public class GetSystemDateAndTimeRequest implements OnvifRequest {

    private OnvifSystemDateAndTimeListener listener;

    public GetSystemDateAndTimeRequest(OnvifSystemDateAndTimeListener listener) {
        this.listener = listener;
    }

    @Override
    public String getXml() {
        return "<GetSystemDateAndTime xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_SYSTEM_DATE_AND_TIME;
    }

    public OnvifSystemDateAndTimeListener getListener() {
        return listener;
    }
}
