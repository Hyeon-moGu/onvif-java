package io.github.hyeonmo.client;

import io.github.hyeonmo.managers.OnvifManager;
import io.github.hyeonmo.managers.DiscoveryManager;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.Device;
import io.github.hyeonmo.core.DiscoveryMode;
import io.github.hyeonmo.operations.DeviceOperations;
import io.github.hyeonmo.operations.ImagingOperations;
import io.github.hyeonmo.operations.MediaOperations;
import io.github.hyeonmo.operations.PtzOperations;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Fluent API Builder for connecting and initializing an ONVIF Device.
 */
public class OnvifClient {

    private String hostName;
    private String username = "";
    private String password = "";

    private OnvifDevice existingDevice;

    private OnvifClient(String hostName) {
        this.hostName = hostName;
    }
    
    private OnvifClient(OnvifDevice device) {
        this.existingDevice = device;
        this.hostName = device.getHostName();
    }

    /**
     * Start configuring a connection to a specific ONVIF device by IP or host.
     */
    public static OnvifClient connect(String hostName) {
        return new OnvifClient(hostName);
    }

    /**
     * Start configuring a connection using an already discovered ONVIF Device object.
     */
    public static OnvifClient connect(io.github.hyeonmo.models.Device discoveredDevice) {
        OnvifDevice onvifDevice = new OnvifDevice(discoveredDevice.getHostName());
        if (discoveredDevice instanceof OnvifDevice) {
            onvifDevice.setBaseUrl(((OnvifDevice) discoveredDevice).getBaseUrl());
            onvifDevice.getAddresses().addAll(((OnvifDevice) discoveredDevice).getAddresses());
        } else {
            onvifDevice.addAddress(discoveredDevice.getHostName());
        }
        return new OnvifClient(onvifDevice);
    }

    /**
     * Provide WS-Security credentials.
     */
    public OnvifClient credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    /**
     * Asynchronously connects to the device, retrieves initial paths, and syncs the clock.
     * @return A CompletableFuture containing the fully initialized OnvifDevice.
     */
    public CompletableFuture<OnvifDevice> buildAsync() {
        OnvifDevice device = (existingDevice != null) ? existingDevice : new OnvifDevice(hostName, username, password);
        
        if (!username.isEmpty() || !password.isEmpty()) {
            device.setUsername(username);
            device.setPassword(password);
        }
        
        OnvifManager manager = new OnvifManager();

        return manager.getCapabilities(device)
                .thenCompose(caps -> {
                    device.setCapabilities(caps);
                    return manager.getServices(device)
                            .handle((services, ex) -> {
                                if (ex != null) {
                                    return null;
                                }
                                return services;
                            });
                })
                .thenCompose(ignored -> manager.getSystemDateAndTime(device)
                        .handle((date, ex) -> {
                            if (ex != null) {
                                return null;
                            }
                            return date;
                        }))
                .thenCompose(date -> {
                    if (date != null) {
                        long offsetMs = date.getTime() - System.currentTimeMillis();
                        device.setTimeOffsetMs(offsetMs);
                    }
                    return manager.getMediaProfiles(device)
                        .handle((profiles, ex) -> {
                            if (profiles != null) {
                                device.setMediaProfiles(profiles);
                            }
                            return device;
                        });
                });
    }

    /**
     * Start discovering ONVIF devices on the local network.
     */
    public static CompletableFuture<List<Device>> discover() {
        return new DiscoveryManager().discover();
    }

    /**
     * Start discovering ONVIF devices with a specific DiscoveryMode.
     */
    public static CompletableFuture<List<Device>> discover(DiscoveryMode mode) {
        return new DiscoveryManager().discover(mode);
    }

    /**
     * Start discovering ONVIF devices with a specific timeout.
     */
    public static CompletableFuture<List<Device>> discover(int timeoutMs) {
        DiscoveryManager manager = new DiscoveryManager();
        manager.setDiscoveryTimeout(timeoutMs);
        return manager.discover();
    }

    private void checkDeviceInitialized() {
        if (existingDevice == null) {
            throw new IllegalStateException("Device is not fully initialized. Ensure buildAsync() has completed.");
        }
    }

    public DeviceOperations device() {
        checkDeviceInitialized();
        return existingDevice.device();
    }

    public MediaOperations media() {
        checkDeviceInitialized();
        return existingDevice.media();
    }

    public PtzOperations ptz() {
        checkDeviceInitialized();
        return existingDevice.ptz();
    }

    public ImagingOperations imaging() {
        checkDeviceInitialized();
        return existingDevice.imaging();
    }
}
