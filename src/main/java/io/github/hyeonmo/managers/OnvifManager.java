package io.github.hyeonmo.managers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.core.OnvifExecutor;
import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifDeviceInformation;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifServices;
import io.github.hyeonmo.requests.OnvifRequest;
import io.github.hyeonmo.requests.device.GetCapabilitiesRequest;
import io.github.hyeonmo.requests.device.GetDeviceInformationRequest;
import io.github.hyeonmo.requests.device.GetServicesRequest;
import io.github.hyeonmo.requests.device.GetSystemDateAndTimeRequest;
import io.github.hyeonmo.requests.media.GetMediaProfilesRequest;
import io.github.hyeonmo.requests.media.GetMediaStreamRequest;
import io.github.hyeonmo.requests.media.GetSnapshotRequest;

/**
 * Executes core ONVIF requests returning CompletableFutures for easy chaining.
 */
public class OnvifManager {

    public final static String TAG = OnvifManager.class.getSimpleName();

    private OnvifExecutor executor;

    public OnvifManager() {
        executor = new OnvifExecutor();
    }

    public CompletableFuture<OnvifServices> getServices(OnvifDevice device) {
        OnvifRequest request = new GetServicesRequest();
        return executor.<OnvifServices>sendRequest(device, request)
                .thenApply(services -> {
                    device.setPath(services);
                    return services;
                });
    }

    public CompletableFuture<java.util.Date> getSystemDateAndTime(OnvifDevice device) {
        OnvifRequest request = new GetSystemDateAndTimeRequest();
        return executor.sendRequest(device, request);
    }

    public CompletableFuture<OnvifDeviceInformation> getDeviceInformation(OnvifDevice device) {
        OnvifRequest request = new GetDeviceInformationRequest();
        return executor.sendRequest(device, request);
    }

    public CompletableFuture<List<OnvifMediaProfile>> getMediaProfiles(OnvifDevice device) {
        if (device.getMediaProfiles() != null && !device.getMediaProfiles().isEmpty()) {
            return CompletableFuture.completedFuture(device.getMediaProfiles());
        }
        OnvifRequest request = new GetMediaProfilesRequest();
        return executor.<List<OnvifMediaProfile>>sendRequest(device, request)
                .thenApply(profiles -> {
                    device.setMediaProfiles(profiles);
                    return profiles;
                });
    }

    public CompletableFuture<String> getMediaStreamURI(OnvifDevice device, OnvifMediaProfile profile) {
        OnvifRequest request = new GetMediaStreamRequest(profile);
        return executor.sendRequest(device, request);
    }

    public CompletableFuture<OnvifCapabilities> getCapabilities(OnvifDevice device) {
        if (device.getCapabilities() != null) {
            return CompletableFuture.completedFuture(device.getCapabilities());
        }
        OnvifRequest request = new GetCapabilitiesRequest();
        return executor.<OnvifCapabilities>sendRequest(device, request)
                .thenApply(caps -> {
                    device.setCapabilities(caps);
                    return caps;
                });
    }

    public CompletableFuture<String> getSnapshotURI(OnvifDevice device, OnvifMediaProfile profile) {
        OnvifRequest request = new GetSnapshotRequest(profile);
        return executor.sendRequest(device, request);
    }

    public <T> CompletableFuture<T> sendOnvifRequest(OnvifDevice device, OnvifRequest request) {
        return executor.sendRequest(device, request);
    }

    public void destroy() {
        executor.clear();
    }
}
