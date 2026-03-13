package io.github.hyeonmo.operations;

import io.github.hyeonmo.models.imaging.ImagingSettings;
import java.util.concurrent.CompletableFuture;

/**
 * Operations related to ONVIF Imaging parameters (Focus, Brightness, Exposure).
 */
public interface ImagingOperations {

    /**
     * Retrieve the current imaging settings of the camera.
     */
    CompletableFuture<ImagingSettings> getImagingSettings();

    /**
     * Move camera focus continuously.
     */
    CompletableFuture<String> focusContinuousMove(float speed);

    /**
     * Stop camera focus movement.
     */
    CompletableFuture<String> focusStop();

    /**
     * Apply new imaging settings.
     */
    CompletableFuture<String> setImagingSettings(ImagingSettings settings);
}
