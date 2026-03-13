package io.github.hyeonmo.operations.impl;

import io.github.hyeonmo.managers.OnvifManager;
import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifDeviceInformation;
import io.github.hyeonmo.operations.DeviceOperations;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class DeviceOperationsImpl implements DeviceOperations {

    private final OnvifDevice device;
    private final OnvifManager manager;

    public DeviceOperationsImpl(OnvifDevice device) {
        this.device = device;
        this.manager = new OnvifManager();
    }

    @Override
    public CompletableFuture<Date> getSystemDateAndTime() {
        return manager.getSystemDateAndTime(device);
    }

    @Override
    public CompletableFuture<OnvifDeviceInformation> getDeviceInformation() {
        return manager.getDeviceInformation(device);
    }

    @Override
    public CompletableFuture<OnvifCapabilities> getCapabilities() {
        return manager.getCapabilities(device);
    }
}
