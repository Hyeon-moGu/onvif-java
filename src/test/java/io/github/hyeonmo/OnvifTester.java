package io.github.hyeonmo;

import java.util.Date;
import java.util.List;

import io.github.hyeonmo.models.Device;
import io.github.hyeonmo.models.DeviceType;
import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifDeviceInformation;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PresetCommand.PresetAction;
import io.github.hyeonmo.models.imaging.ImagingSettings;
import io.github.hyeonmo.DiscoveryMode;

/**
 * A safe, read-only ONVIF camera discovery and information retrieval test application.
 * (Excludes PTZ movement or any modification requests)
 *
 * Modified for v2.0 - CompletableFuture implementation
 */
public class OnvifTester {

    private static final String CAMERA_USERNAME = "admin";
    private static final String CAMERA_PASSWORD = "admin";

    public static void main(String[] args) {
        System.out.println("========== ONVIF Device Discovery Started ==========");

        OnvifManager onvifManager = new OnvifManager();
        OnvifDiscovery discovery = new OnvifDiscovery();

        System.out.println("Scanning network... (Max 20 seconds)");

        discovery.probe(DiscoveryMode.ONVIF)
            .thenAccept(devices -> {
                System.out.println("\nScan complete! Found " + devices.size() + " devices.\n");

                OnvifDevice targetDevice = null;
                for (Device genericDevice : devices) {
                    if (genericDevice.getType() == DeviceType.ONVIF) {
                        targetDevice = (OnvifDevice) genericDevice;
                        break;
                    }
                }

                if (targetDevice != null) {
                    targetDevice.setUsername(CAMERA_USERNAME);
                    targetDevice.setPassword(CAMERA_PASSWORD);

                    System.out.println("------------------------------------------------");
                    System.out.println("Proceeding with single camera test...");
                    System.out.println("Target IP: " + targetDevice.getHostName());
                    System.out.println("Base URL: " + targetDevice.getBaseUrl());
                    System.out.println("Username: " + targetDevice.getUsername());
                    System.out.println("------------------------------------------------");

                    runDeviceTests(onvifManager, targetDevice);

                    new Thread(() -> {
                        try {
                            Thread.sleep(10000); // Allow time for async tests to complete
                            System.out.println("\n[Test Complete] Terminating JVM cleanly...");
                            System.exit(0);
                        } catch (InterruptedException ignored) {}
                    }).start();

                } else {
                    System.out.println("No ONVIF devices found on the network.");
                    System.exit(0);
                }
            })
            .exceptionally(ex -> {
                System.out.println("[Error] Discovery failed: " + ex.getMessage());
                System.exit(1);
                return null;
            });
    }

    private static void runDeviceTests(OnvifManager onvifManager, OnvifDevice device) {
        // Fetch Date & Time
        onvifManager.getSystemDateAndTime(device)
            .thenCompose(dateTime -> {
                System.out.println("[Success] Camera Time: " + dateTime.toString());
                if (dateTime != null) {
                    long offsetMs = dateTime.getTime() - System.currentTimeMillis();
                    System.out.println("[Applied] Time Offset (Clock Sync): " + offsetMs + "ms");
                }
                
                // Fetch Device Information
                return onvifManager.getDeviceInformation(device);
            })
            .thenCompose(deviceInfo -> {
                System.out.println("[Success] Device Info: Model=" + deviceInfo.getModel() + ", Manufacturer=" + deviceInfo.getManufacturer() + ", Firmware=" + deviceInfo.getFirmwareVersion());
                
                // Fetch Capabilities
                return onvifManager.getCapabilities(device);
            })
            .thenCompose(capabilities -> {
                System.out.println("[Success] Capabilities Retrieved.");
                
                // Fetch Media Profiles
                return onvifManager.getMediaProfiles(device);
            })
            .thenCompose(profiles -> {
                System.out.println("[Success] Found " + profiles.size() + " Media Profiles");
                for (OnvifMediaProfile profile : profiles) {
                    System.out.println("  - Profile: " + profile.getName() + " (Token: " + profile.getToken() + ")");
                }

                if (!profiles.isEmpty()) {
                    OnvifMediaProfile mainProfile = profiles.get(0);
                    
                    onvifManager.getMediaStreamURI(device, mainProfile)
                        .thenAccept(uri -> System.out.println("[Success] RTSP Stream URI: " + uri));

                    onvifManager.getSnapshotURI(device, mainProfile)
                        .thenAccept(uri -> System.out.println("[Success] Snapshot JPEG URI: " + uri));
                }
                
                fetchPtzStatus(device);
                fetchPtzPresets(device);
                fetchImagingSettings(device);
                
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            })
            .exceptionally(ex -> {
                System.out.println("[Error] Test chain aborted: " + ex.getMessage());
                return null;
            });
    }

    private static void fetchPtzStatus(OnvifDevice device) {
        PtzManager ptzManager = new PtzManager();
        ptzManager.getStatus(device)
            .thenAccept(status -> System.out.println("[Success] PTZ Status: " + status))
            .exceptionally(ex -> {
                System.out.println("[Failed] PTZ Status retrieval failed: " + ex.getMessage());
                return null;
            });
    }

    private static void fetchPtzPresets(OnvifDevice device) {
        PtzManager ptzManager = new PtzManager();
        PresetCommand getPresetCommand = new PresetCommand(PresetAction.GET, null);
        ptzManager.preset(device, getPresetCommand)
            .thenAccept(presets -> System.out.println("[Success] PTZ Presets List: " + presets))
            .exceptionally(ex -> {
                System.out.println("[Failed] PTZ Presets retrieval failed: " + ex.getMessage());
                return null;
            });
    }

    private static void fetchImagingSettings(OnvifDevice device) {
        ImagingManager imagingManager = new ImagingManager();
        imagingManager.getImagingSettings(device)
            .thenAccept(settings -> System.out.println("[Success] Imaging Settings: " + settings.toString()))
            .exceptionally(ex -> {
                System.out.println("[Failed] Imaging Settings retrieval failed: " + ex.getMessage());
                return null;
            });
    }
}
