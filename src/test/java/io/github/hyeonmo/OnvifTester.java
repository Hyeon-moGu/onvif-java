package io.github.hyeonmo;

import java.util.Date;
import java.util.List;

import io.github.hyeonmo.listeners.DiscoveryListener;
import io.github.hyeonmo.listeners.device.OnvifCapabilitiesListener;
import io.github.hyeonmo.listeners.device.OnvifDeviceInformationListener;
import io.github.hyeonmo.listeners.device.OnvifSystemDateAndTimeListener;
import io.github.hyeonmo.listeners.media.OnvifMediaProfilesListener;
import io.github.hyeonmo.listeners.media.OnvifMediaStreamURIListener;
import io.github.hyeonmo.listeners.media.OnvifSnapshotURIListener;
import io.github.hyeonmo.listeners.OnvifResponseListener;
import io.github.hyeonmo.responses.OnvifResponse;
import io.github.hyeonmo.models.Device;
import io.github.hyeonmo.models.DeviceType;
import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifDeviceInformation;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.PtzManager;
import io.github.hyeonmo.listeners.ptz.PtzResponseListener;
import io.github.hyeonmo.responses.PtzResponse;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PresetCommand.PresetAction;
import io.github.hyeonmo.ImagingManager;
import io.github.hyeonmo.listeners.imaging.ImagingSettingsListener;
import io.github.hyeonmo.models.imaging.ImagingSettings;

/**
 * A safe, read-only ONVIF camera discovery and information retrieval test application.
 * (Excludes PTZ movement or any modification requests)
 */
public class OnvifTester {

    // TODO: Enter your camera's account credentials here.
    private static final String CAMERA_USERNAME = "admin";
    private static final String CAMERA_PASSWORD = "admin";

    public static void main(String[] args) {
        System.out.println("========== ONVIF Device Discovery Started ==========");

        OnvifManager onvifManager = new OnvifManager();
        onvifManager.setOnvifResponseListener(new OnvifResponseListener() {
            @Override
            public void onResponse(OnvifDevice onvifDevice, OnvifResponse response) {
            }

            @Override
            public void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage) {
                System.out.println("[Error] Camera communication error (" + errorCode + "): " + errorMessage);
            }
        });
        OnvifDiscovery discovery = new OnvifDiscovery(DiscoveryMode.ONVIF);
        discovery.setDiscoveryTimeout(20000);

        discovery.probe(DiscoveryMode.ONVIF, new DiscoveryListener() {
            @Override
            public void onDiscoveryStarted() {
                System.out.println("Scanning network... (Max 20 seconds)");
            }

            @Override
            public void onDevicesFound(List<Device> devices) {
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

                    fetchSystemDateAndTime(onvifManager, targetDevice);

                    new Thread(() -> {
                        try {
                            Thread.sleep(5000);
                            System.out.println("\n[Test Complete] Terminating JVM cleanly...");
                            System.exit(0);
                        } catch (InterruptedException ignored) {}
                    }).start();

                } else {
                    System.out.println("No ONVIF devices found on the network.");
                    System.exit(0);
                }
            }
        }, 0);
    }

    private static void fetchSystemDateAndTime(OnvifManager onvifManager, OnvifDevice device) {
        onvifManager.getSystemDateAndTime(device, new OnvifSystemDateAndTimeListener() {
            @Override
            public void onSystemDateAndTimeReceived(OnvifDevice device, Date date) {
                if (date != null) {
                    System.out.println("[Success] Camera Time: " + date.toString());
                    long offsetMs = date.getTime() - System.currentTimeMillis();
                    device.setTimeOffsetMs(offsetMs);
                    System.out.println("[Applied] Time Offset (Clock Sync): " + offsetMs + "ms");
                } else {
                    System.out.println("[Failed] Could not retrieve camera time.");
                }

                fetchDeviceInformation(onvifManager, device);
            }
        });
    }

    private static void fetchDeviceInformation(OnvifManager onvifManager, OnvifDevice device) {
        onvifManager.getDeviceInformation(device, new OnvifDeviceInformationListener() {
            @Override
            public void onDeviceInformationReceived(OnvifDevice device, OnvifDeviceInformation deviceInformation) {
                System.out.println("[Success] Device Info: Model=" + deviceInformation.getModel() + ", Manufacturer=" + deviceInformation.getManufacturer() + ", Firmware=" + deviceInformation.getFirmwareVersion());

                fetchMediaProfilesAndStreams(onvifManager, device);
                fetchCapabilities(onvifManager, device);
                fetchPtzStatus(device);
                fetchPtzPresets(device);
                fetchImagingSettings(device);
            }
        });
    }

    private static void fetchMediaProfilesAndStreams(OnvifManager onvifManager, OnvifDevice device) {
        onvifManager.getMediaProfiles(device, new OnvifMediaProfilesListener() {
            @Override
            public void onMediaProfilesReceived(OnvifDevice device, List<OnvifMediaProfile> profiles) {
                System.out.println("[Success] Found " + profiles.size() + " Media Profiles");
                for (OnvifMediaProfile profile : profiles) {
                    System.out.println("  - Profile: " + profile.getName() + " (Token: " + profile.getToken() + ")");
                }

                if (!profiles.isEmpty()) {
                    OnvifMediaProfile mainProfile = profiles.get(0);
                    fetchStreamUri(onvifManager, device, mainProfile);
                    fetchSnapshotUri(onvifManager, device, mainProfile);
                }
            }
        });
    }

    private static void fetchStreamUri(OnvifManager onvifManager, OnvifDevice device, OnvifMediaProfile profile) {
        onvifManager.getMediaStreamURI(device, profile, new OnvifMediaStreamURIListener() {
            @Override
            public void onMediaStreamURIReceived(OnvifDevice device, OnvifMediaProfile profile, String uri) {
                System.out.println("[Success] RTSP Stream URI: " + uri);
            }
        });
    }

    private static void fetchSnapshotUri(OnvifManager onvifManager, OnvifDevice device, OnvifMediaProfile profile) {
        onvifManager.getSnapshotURI(device, profile, new OnvifSnapshotURIListener() {
            @Override
            public void onMediaSnapshotReceived(OnvifDevice device, OnvifMediaProfile profile, String uri) {
                System.out.println("[Success] Snapshot JPEG URI: " + uri);
            }
        });
    }

    private static void fetchCapabilities(OnvifManager onvifManager, OnvifDevice device) {
        onvifManager.getCapabilities(device, new OnvifCapabilitiesListener() {
            @Override
            public void onDeviceCapabilitiesReceived(OnvifDevice device, OnvifCapabilities capabilities) {
                System.out.println("[Success] Capabilities Retrieved.");
            }
        });
    }

    private static void fetchPtzStatus(OnvifDevice device) {
        PtzManager ptzManager = new PtzManager();
        ptzManager.getStatus(device, new PtzResponseListener() {
            @Override
            public void onResponse(PtzResponse ptzResponse) {
                if (ptzResponse.isSuccess()) {
                    System.out.println("[Success] PTZ Status (Coordinates): " + ptzResponse.getMessage());
                } else {
                    System.out.println("[Failed] PTZ Status retrieval failed (Unsupported device?): " + ptzResponse.getMessage());
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                System.out.println("[Error] PTZ Status retrieval aborted: " + errorMessage);
            }
        });
    }

    private static void fetchPtzPresets(OnvifDevice device) {
        PtzManager ptzManager = new PtzManager();
        PresetCommand getPresetCommand = new PresetCommand(PresetAction.GET, null);

        ptzManager.preset(device, getPresetCommand, new PtzResponseListener() {
            @Override
            public void onResponse(PtzResponse ptzResponse) {
                if (ptzResponse.isSuccess()) {
                    System.out.println("[Success] PTZ Presets List: " + ptzResponse.getMessage());
                } else {
                    System.out.println("[Failed] PTZ Presets retrieval failed: " + ptzResponse.getMessage());
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                System.out.println("[Error] PTZ Presets retrieval aborted: " + errorMessage);
            }
        });
    }

    private static void fetchImagingSettings(OnvifDevice device) {
        ImagingManager imagingManager = new ImagingManager();
        
        imagingManager.getImagingSettings(device, new ImagingSettingsListener() {
            @Override
            public void onResponse(OnvifDevice onvifDevice, ImagingSettings imagingSettings) {
                if (imagingSettings != null) {
                    System.out.println("[Success] Imaging Settings: " + imagingSettings.toString());
                } else {
                    System.out.println("[Failed] Imaging Settings retrieval failed.");
                }
            }
            
            @Override
            public void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage) {
                System.out.println("[Error] Imaging Settings retrieval aborted: " + errorMessage);
            }
        });
    }
}
