package io.github.hyeonmo.listeners.media;

import java.util.List;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public interface OnvifMediaProfilesListener {

    void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> mediaProfiles);

}
