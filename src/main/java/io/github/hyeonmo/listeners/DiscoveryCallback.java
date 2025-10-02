package io.github.hyeonmo.listeners;

import java.util.List;

import io.github.hyeonmo.models.Device;

/**
 * Created by Tomas Verhelst on 04/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public interface DiscoveryCallback {

    void onDiscoveryStarted();

    void onDevicesFound(List<Device> devices);

    void onDiscoveryFinished();

}
