package io.github.hyeonmo.upnp;

import io.github.hyeonmo.models.UPnPDevice;


public interface UPnPDeviceInformationListener {

    void onDeviceInformationReceived(UPnPDevice device, UPnPDeviceInformation deviceInformation);

    void onError(UPnPDevice onvifDevice, int errorCode, String errorMessage);

}
