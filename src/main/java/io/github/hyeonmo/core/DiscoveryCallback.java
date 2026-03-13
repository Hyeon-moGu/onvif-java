package io.github.hyeonmo.core;

import java.util.List;
import io.github.hyeonmo.models.Device;

public interface DiscoveryCallback {
    void onDiscoveryStarted();
    void onDevicesFound(List<Device> devices);
    void onDiscoveryFinished();
}
