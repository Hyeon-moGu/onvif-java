package io.github.hyeonmo.operations;

import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import java.util.concurrent.CompletableFuture;

/**
 * Operations related to ONVIF Pan/Tilt/Zoom (PTZ) controls.
 */
public interface PtzOperations {

    /**
     * Start continuous movement in the given direction.
     */
    CompletableFuture<String> move(PtzType direction);

    /**
     * Stop all continuous movement.
     */
    CompletableFuture<String> stop();

    /**
     * Get the current PTZ coordinates and status.
     */
    CompletableFuture<String> getStatus();

    /**
     * Execute a PTZ Preset command (Save, Goto, Get).
     */
    CompletableFuture<String> preset(PresetCommand command);
}
