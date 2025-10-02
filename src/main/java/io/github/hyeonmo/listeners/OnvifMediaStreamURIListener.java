package io.github.hyeonmo.listeners;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;


/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public interface OnvifMediaStreamURIListener {

    void onMediaStreamURIReceived(OnvifDevice device, OnvifMediaProfile profile, String uri);

}
