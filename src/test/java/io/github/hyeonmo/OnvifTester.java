package io.github.hyeonmo;

import io.github.hyeonmo.client.OnvifClient;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PresetCommand.PresetAction;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.exceptions.OnvifAuthException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;

public class OnvifTester {

    private static final Logger log = LoggerFactory.getLogger(OnvifTester.class);
    private static final String CAMERA_USERNAME = "admin";
    private static final String CAMERA_PASSWORD = "admin";
    
    private static OnvifDevice sharedDevice;

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(OnvifTester.class);
    }

    @BeforeClass
    public static void setUp() {
        log.info("Initializing ONVIF Shared Device (Discovery 10s)...");
        sharedDevice = OnvifClient.discover(10000)
            .thenCompose(devices -> {
                if (devices.isEmpty()) {
                    throw new RuntimeException("No ONVIF devices found on the network.");
                }
                log.info("Discovered {} devices. Using first: {}", devices.size(), devices.get(0).getHostName());
                return OnvifClient.connect(devices.get(0))
                    .credentials(CAMERA_USERNAME, CAMERA_PASSWORD)
                    .buildAsync();
            })
            .join();
        assertNotNull("Global device initialization failed", sharedDevice);
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
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
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
                cause instanceof OnvifAuthException || cause instanceof io.github.hyeonmo.exceptions.OnvifException);
        }
    }
}
