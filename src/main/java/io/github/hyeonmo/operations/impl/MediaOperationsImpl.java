package io.github.hyeonmo.operations.impl;

import io.github.hyeonmo.managers.OnvifManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.operations.MediaOperations;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MediaOperationsImpl implements MediaOperations {

    private final OnvifDevice device;
    private final OnvifManager manager;

    public MediaOperationsImpl(OnvifDevice device) {
        this.device = device;
        this.manager = new OnvifManager();
    }

    @Override
    public CompletableFuture<List<OnvifMediaProfile>> getMediaProfiles() {
        return manager.getMediaProfiles(device);
    }

    @Override
    public CompletableFuture<String> getMediaStreamURI(OnvifMediaProfile profile) {
        return manager.getMediaStreamURI(device, profile);
    }

    @Override
    public CompletableFuture<String> getSnapshotURI(OnvifMediaProfile profile) {
        return manager.getSnapshotURI(device, profile);
    }
}
