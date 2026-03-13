package io.github.hyeonmo.upnp;

import io.github.hyeonmo.models.UPnPDevice;


public interface UPnPResponseListener {

    void onResponse(UPnPDevice onvifDevice);

    void onError(UPnPDevice onvifDevice, int errorCode, String errorMessage);
}
