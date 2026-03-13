package io.github.hyeonmo.operations.impl;

import io.github.hyeonmo.managers.PtzManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.operations.PtzOperations;

import java.util.concurrent.CompletableFuture;

public class PtzOperationsImpl implements PtzOperations {

    private final OnvifDevice device;
    private final PtzManager manager;

    public PtzOperationsImpl(OnvifDevice device) {
        this.device = device;
        this.manager = new PtzManager();
    }

    @Override
    public CompletableFuture<String> move(PtzType direction) {
        return manager.move(device, direction);
    }

    @Override
    public CompletableFuture<String> stop() {
        return manager.stop(device);
    }

    @Override
    public CompletableFuture<String> getStatus() {
        return manager.getStatus(device);
    }

    @Override
    public CompletableFuture<String> preset(PresetCommand command) {
        return manager.preset(device, command);
    }
}
