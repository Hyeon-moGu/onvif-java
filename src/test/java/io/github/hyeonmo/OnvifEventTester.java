package io.github.hyeonmo;

import io.github.hyeonmo.client.OnvifClient;
import io.github.hyeonmo.models.Device;
import io.github.hyeonmo.models.OnvifDevice;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class OnvifEventTester {

    private static final Logger log = LoggerFactory.getLogger(OnvifEventTester.class);
    private static final String DEFAULT_CAMERA_USERNAME = "admin";
    private static final String DEFAULT_CAMERA_PASSWORD = "admin";
    private static final int DISCOVERY_TIMEOUT_MS = 10000;

    private static final String CAMERA_IP = getConfig("onvif.camera.ip", "ONVIF_CAMERA_IP");
    private static final String CAMERA_USERNAME = getConfig("onvif.camera.username", "ONVIF_CAMERA_USERNAME");
    private static final String CAMERA_PASSWORD = getConfig("onvif.camera.password", "ONVIF_CAMERA_PASSWORD");

    private static OnvifDevice sharedDevice;

    @BeforeClass
    public static void setUp() {
        sharedDevice = initializeDevice();
        assertNotNull("Event test device initialization failed", sharedDevice);
        log.info("Connected to {} successfully.", sharedDevice.getHostName());
    }

    @Test
    public void testMotionEvents() {
        log.info("Testing Event Subscription and Motion Notifications...");
        log.info("Evidence - Device Capabilities: {}", sharedDevice.getCapabilities());

        if (sharedDevice.getCapabilities().getEventsXaddr() == null || sharedDevice.getCapabilities().getEventsXaddr().isEmpty()) {
            log.error("This camera does NOT support the Events service.");
            return;
        }

        if (!sharedDevice.getCapabilities().isWsPullPointSupport()) {
            log.warn("This camera may NOT support PullPoint (PullMessages) events, which we are using.");
        }

        log.info("Starting event subscription...");

        sharedDevice.event().subscribe("tns1:VideoSource/MotionAlarm", event -> {
            log.info("Evidence - Received Event: Topic={}, Source={}, Data={}",
                    event.getTopic(), event.getSource(), event.getData());

            if (event.isMotionRelated() && event.isMotionActive()) {
                log.info("Evidence - Motion active event detected.");
            } else if (event.isMotionRelated()) {
                log.info("Evidence - Motion-related event reported inactive state.");
            }
        }).thenAccept(session -> {
            log.info("Evidence - Subscribed to PullPoint events. Waiting for 60 seconds for events...");

            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            log.info("Evidence - Test period finished. Unsubscribing...");
            session.unsubscribe().join();
            log.info("Evidence - Unsubscribed and cleaned up.");
        }).join();
    }

    public static void main(String[] args) {
        setUp();
        new OnvifEventTester().testMotionEvents();
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

        log.info("Initializing ONVIF Shared Event Device (Discovery {} ms)...", DISCOVERY_TIMEOUT_MS);
        return OnvifClient.discover(DISCOVERY_TIMEOUT_MS)
                .thenCompose(devices -> connectFirstDiscovered(devices, username, password))
                .join();
    }

    private static java.util.concurrent.CompletableFuture<OnvifDevice> connectFirstDiscovered(List<Device> devices, String username, String password) {
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
