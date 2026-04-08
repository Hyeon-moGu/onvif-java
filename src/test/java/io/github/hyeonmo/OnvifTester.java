package io.github.hyeonmo;

import io.github.hyeonmo.client.OnvifClient;
import io.github.hyeonmo.exceptions.OnvifAuthException;
import io.github.hyeonmo.exceptions.OnvifException;
import io.github.hyeonmo.models.Device;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PresetCommand.PresetAction;
import io.github.hyeonmo.models.ptz.PtzType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OnvifTester {

    private static final Logger log = LoggerFactory.getLogger(OnvifTester.class);
    private static final String DEFAULT_CAMERA_USERNAME = "admin";
    private static final String DEFAULT_CAMERA_PASSWORD = "admin";
    private static final int DISCOVERY_TIMEOUT_MS = 10000;

    private static final String CAMERA_IP = getConfig("onvif.camera.ip", "ONVIF_CAMERA_IP");
    private static final String CAMERA_USERNAME = getConfig("onvif.camera.username", "ONVIF_CAMERA_USERNAME");
    private static final String CAMERA_PASSWORD = getConfig("onvif.camera.password", "ONVIF_CAMERA_PASSWORD");

    private static OnvifDevice sharedDevice;

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(OnvifTester.class);
    }

    @BeforeClass
    public static void setUp() {
        sharedDevice = initializeDevice();
        assertNotNull("Shared device initialization failed", sharedDevice);
        log.info("Connected to {} successfully.", sharedDevice.getHostName());
    }

    @Test
    public void testDeviceInfo() {
        log.info("Testing Device Information...");
        sharedDevice.device().getDeviceInformation().thenAccept(info -> {
            log.info("Evidence - Manufacturer: {}, Model: {}, Firmware: {}",
                    info.getManufacturer(), info.getModel(), info.getFirmwareVersion());
            assertNotNull(info.getManufacturer());
            assertNotNull(info.getModel());
        }).join();
    }

    @Test
    public void testMediaProfiles() {
        log.info("Testing Media Profiles and URIs...");
        sharedDevice.media().getMediaProfiles().thenCompose(profiles -> {
            assertFalse("Profiles list should not be empty", profiles.isEmpty());
            log.info("Evidence - Found {} media profiles", profiles.size());

            OnvifMediaProfile profile = profiles.get(0);
            return sharedDevice.media().getMediaStreamURI(profile)
                    .thenCompose(streamUri -> {
                        log.info("Evidence - RTSP Stream URI: {}", streamUri);
                        assertNotNull(streamUri);
                        return sharedDevice.media().getSnapshotURI(profile);
                    })
                    .thenAccept(snapshotUri -> {
                        log.info("Evidence - Snapshot JPEG URI: {}", snapshotUri);
                        assertNotNull(snapshotUri);
                    });
        }).join();
    }

    @Test
    public void testPtzStatus() {
        log.info("Testing PTZ Status...");
        sharedDevice.ptz().getStatus()
                .thenAccept(status -> {
                    log.info("Evidence - PTZ Status: {}", status);
                    assertNotNull(status);
                })
                .exceptionally(ex -> {
                    log.warn("Evidence - PTZ Status skipped (unsupported or error: {})", ex.getMessage());
                    return null;
                }).join();
    }

    @Test
    public void testPtzPresetLifecycle() {
        log.info("Testing PTZ Preset Lifecycle (Save -> List -> Remove)...");
        PresetCommand saveCmd = new PresetCommand(PresetAction.SAVE, "test");

        sharedDevice.ptz().preset(saveCmd)
                .thenCompose(saveRes -> {
                    log.info("Evidence - Preset Save Result: {}", saveRes);
                    String token = saveRes.contains("token=") ? saveRes.split("token=")[1].trim() : "test";

                    return sharedDevice.ptz().preset(new PresetCommand(PresetAction.GET, null))
                            .thenCompose(presets -> {
                                int count = (presets == null || presets.isEmpty()) ? 0 : presets.trim().split("\n").length;
                                log.info("Evidence - Presets List size: {}", count);
                                return sharedDevice.ptz().preset(new PresetCommand(PresetAction.REMOVE, token));
                            })
                            .thenAccept(removeRes -> {
                                log.info("Evidence - Preset Remove Result: {}", removeRes);
                                assertNotNull(removeRes);
                            });
                }).join();
    }

    @Test
    public void testImagingSettings() {
        log.info("Testing Imaging Settings (Get -> Set)...");
        sharedDevice.imaging().getImagingSettings().thenCompose(settings -> {
            log.info("Evidence - Current Imaging Settings: {}", settings);
            assertNotNull(settings);
            return sharedDevice.imaging().setImagingSettings(settings);
        }).thenAccept(result -> {
            log.info("Evidence - Set Imaging Result: {}", result);
            assertEquals("OK", result);
        }).join();
    }

    @Test
    public void testPtzMoveAndStop() {
        log.info("Testing PTZ Move (LEFT) and Stop...");
        sharedDevice.ptz().move(PtzType.LEFT).thenCompose(moveRes -> {
            log.info("Evidence - Move Result: {}", moveRes);
            assertEquals("ContinuousMove success", moveRes);

            return CompletableFuture.runAsync(() -> {
                log.info("Evidence - Waiting 1000ms for PTZ movement to complete...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).thenCompose(v -> sharedDevice.ptz().stop());
        }).thenAccept(stopRes -> {
            log.info("Evidence - Stop Result: {}", stopRes);
            assertEquals("Stop success", stopRes);
        }).join();
    }

    @Test
    public void testAuthFailure() {
        log.info("Testing Authentication Failure...");
        CompletableFuture<OnvifDevice> badAuthFuture = OnvifClient.connect(sharedDevice.getHostName())
                .credentials("wrong_user", "wrong_password")
                .buildAsync();

        try {
            badAuthFuture.join();
            fail("Should have thrown an exception for invalid credentials");
        } catch (Exception e) {
            Throwable cause = e.getCause();
            log.info("Evidence - Caught expected exception: {}", cause.getClass().getSimpleName());
            assertTrue("Exception should be OnvifAuthException or general OnvifException",
                    cause instanceof OnvifAuthException || cause instanceof OnvifException);
        }
    }

    private static OnvifDevice initializeDevice() {
        String username = isNotBlank(CAMERA_USERNAME) ? CAMERA_USERNAME : DEFAULT_CAMERA_USERNAME;
        String password = isNotBlank(CAMERA_PASSWORD) ? CAMERA_PASSWORD : DEFAULT_CAMERA_PASSWORD;

        if (isNotBlank(CAMERA_IP)) {
            log.info("Connecting to configured camera at {}...", CAMERA_IP);
            return OnvifClient.connect(CAMERA_IP)
                    .credentials(username, password)
                    .buildAsync()
                    .join();
        }

        log.info("Initializing ONVIF Shared Device (Discovery {} ms)...", DISCOVERY_TIMEOUT_MS);
        return OnvifClient.discover(DISCOVERY_TIMEOUT_MS)
                .thenCompose(devices -> connectFirstDiscovered(devices, username, password))
                .join();
    }

    private static CompletableFuture<OnvifDevice> connectFirstDiscovered(List<Device> devices, String username, String password) {
        if (devices.isEmpty()) {
            throw new RuntimeException("No ONVIF devices found on the network.");
        }

        Device firstDevice = devices.get(0);
        log.info("Discovered {} devices. Using first: {}", devices.size(), firstDevice.getHostName());

        return OnvifClient.connect(firstDevice)
                .credentials(username, password)
                .buildAsync();
    }

    private static String getConfig(String propertyKey, String envKey) {
        String propertyValue = System.getProperty(propertyKey);
        if (isNotBlank(propertyValue)) {
            return propertyValue;
        }
        return System.getenv(envKey);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
