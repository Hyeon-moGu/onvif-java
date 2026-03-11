package io.github.hyeonmo;

import java.util.List;
import io.github.hyeonmo.models.Device;

public interface DiscoveryCallback {
    void onDiscoveryStarted();
    void onDevicesFound(List<Device> devices);
    void onDiscoveryFinished();
}
