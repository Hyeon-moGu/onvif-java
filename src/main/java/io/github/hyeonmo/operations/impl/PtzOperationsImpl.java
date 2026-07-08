package io.github.hyeonmo.operations.impl;

import io.github.hyeonmo.managers.PtzManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.models.ptz.PtzConfigurationOptions;
import io.github.hyeonmo.models.ptz.PtzPosition;
import io.github.hyeonmo.models.ptz.PtzSpeed;
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
    public CompletableFuture<PtzPosition> getPosition() {
        return manager.getPosition(device);
    }

    @Override
    public CompletableFuture<PtzConfigurationOptions> getConfigurationOptions() {
        return manager.getConfigurationOptions(device);
    }

    @Override
    public CompletableFuture<String> absoluteMove(PtzPosition position) {
        return manager.absoluteMove(device, position, null);
    }

    @Override
    public CompletableFuture<String> absoluteMove(PtzPosition position, PtzSpeed speed) {
        return manager.absoluteMove(device, position, speed);
    }

    @Override
    public CompletableFuture<String> preset(PresetCommand command) {
        return manager.preset(device, command);
    }
}
