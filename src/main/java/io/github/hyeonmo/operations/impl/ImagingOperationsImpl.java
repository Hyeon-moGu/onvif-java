package io.github.hyeonmo.operations.impl;

import io.github.hyeonmo.managers.ImagingManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.imaging.ImagingSettings;
import io.github.hyeonmo.operations.ImagingOperations;

import java.util.concurrent.CompletableFuture;

public class ImagingOperationsImpl implements ImagingOperations {

    private final OnvifDevice device;
    private final ImagingManager manager;

    public ImagingOperationsImpl(OnvifDevice device) {
        this.device = device;
        this.manager = new ImagingManager();
    }

    @Override
    public CompletableFuture<ImagingSettings> getImagingSettings() {
        return manager.getImagingSettings(device);
    }

    @Override
    public CompletableFuture<String> focusContinuousMove(float speed) {
        return manager.focusContinuousMove(device, speed);
    }

    @Override
    public CompletableFuture<String> focusStop() {
        return manager.focusStop(device);
    }

    @Override
    public CompletableFuture<String> setImagingSettings(ImagingSettings settings) {
        return manager.setImagingSettings(device, settings);
    }
}
