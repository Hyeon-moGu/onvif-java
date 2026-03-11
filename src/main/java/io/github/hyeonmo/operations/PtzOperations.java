package io.github.hyeonmo.operations;

import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.OnvifManager;
import io.github.hyeonmo.PtzManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;

/**
 * Fluent API wrapper for PTZ operations.
 */
public class PtzOperations {
    
    private final OnvifDevice device;
    private final PtzManager ptzManager;
    
    public PtzOperations(OnvifDevice device, OnvifManager onvifManager) {
        this.device = device;
        this.ptzManager = new PtzManager(onvifManager);
    }
    
    public CompletableFuture<Void> moveAsync(PtzType type) {
        return ptzManager.move(device, type).thenApply(r -> null);
    }
    
    public CompletableFuture<Void> stopAsync() {
        return ptzManager.stop(device).thenApply(r -> null);
    }
    
    public CompletableFuture<Void> presetAsync(PresetCommand command) {
        return ptzManager.preset(device, command).thenApply(r -> null);
    }
    
    public CompletableFuture<String> getStatusAsync() {
        return ptzManager.getStatus(device);
    }
    
    public void destroy() {
        ptzManager.destroy();
    }
}
