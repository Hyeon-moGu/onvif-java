package io.github.hyeonmo.listeners;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifDeviceInformation;


/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public interface OnvifDeviceInformationListener {

    void onDeviceInformationReceived(OnvifDevice device, OnvifDeviceInformation deviceInformation);

}
