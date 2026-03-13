package io.github.hyeonmo.operations;

import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDeviceInformation;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * Operations related to the ONVIF Device itself (info, time, capabilities).
 */
public interface DeviceOperations {

    /**
     * Gets the system date and time from the camera.
     */
    CompletableFuture<Date> getSystemDateAndTime();

    /**
     * Retrieves basic device information (Manufacturer, Model, Firmware).
     */
    CompletableFuture<OnvifDeviceInformation> getDeviceInformation();

    /**
     * Retrieves the device capabilities.
     */
    CompletableFuture<OnvifCapabilities> getCapabilities();
}
