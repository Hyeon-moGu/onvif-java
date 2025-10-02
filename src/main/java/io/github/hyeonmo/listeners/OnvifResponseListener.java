package io.github.hyeonmo.listeners;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.responses.OnvifResponse;


/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public interface OnvifResponseListener {

    void onResponse(OnvifDevice onvifDevice, OnvifResponse response);

    void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage);
}
