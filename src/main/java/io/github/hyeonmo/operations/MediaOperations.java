package io.github.hyeonmo.operations;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.OnvifManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;

/**
 * Fluent API wrapper for Media operations.
 */
public class MediaOperations {
    
    private final OnvifDevice device;
    private final OnvifManager manager;
    
    public MediaOperations(OnvifDevice device, OnvifManager manager) {
        this.device = device;
        this.manager = manager;
    }
    
    public CompletableFuture<List<OnvifMediaProfile>> getProfilesAsync() {
        return manager.getMediaProfiles(device);
    }
    
    public CompletableFuture<String> getStreamUriAsync() {
        return manager.getMediaProfiles(device)
            .thenCompose(profiles -> {
                if(profiles == null || profiles.isEmpty()) {
                    return CompletableFuture.failedFuture(new RuntimeException("No profiles found"));
                }
                return manager.getMediaStreamURI(device, profiles.get(0));
            });
    }

    public CompletableFuture<String> getStreamUriAsync(OnvifMediaProfile profile) {
        return manager.getMediaStreamURI(device, profile);
    }
    
    public CompletableFuture<String> getSnapshotUriAsync() {
        return manager.getMediaProfiles(device)
            .thenCompose(profiles -> {
                if(profiles == null || profiles.isEmpty()) {
                    return CompletableFuture.failedFuture(new RuntimeException("No profiles found"));
                }
                return manager.getSnapshotURI(device, profiles.get(0));
            });
    }

    public CompletableFuture<String> getSnapshotUriAsync(OnvifMediaProfile profile) {
        return manager.getSnapshotURI(device, profile);
    }
}
