package io.github.hyeonmo.upnp;

import io.github.hyeonmo.models.UPnPDevice;


/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public interface UPnPDeviceInformationListener {

    void onDeviceInformationReceived(UPnPDevice device, UPnPDeviceInformation deviceInformation);

    void onError(UPnPDevice onvifDevice, int errorCode, String errorMessage);

}
