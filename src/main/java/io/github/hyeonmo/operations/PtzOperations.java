package io.github.hyeonmo.operations;

import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.models.ptz.PtzConfigurationOptions;
import io.github.hyeonmo.models.ptz.PtzPosition;
import io.github.hyeonmo.models.ptz.PtzSpeed;
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

    /** Get the current absolute PTZ coordinates as a typed value. */
    CompletableFuture<PtzPosition> getPosition();

    /** Get the coordinate spaces and ranges supported by the active profile. */
    CompletableFuture<PtzConfigurationOptions> getConfigurationOptions();

    /** Move to an absolute position using the device's default speed. */
    CompletableFuture<String> absoluteMove(PtzPosition position);

    /** Move to an absolute position using an explicit, device-supported speed. */
    CompletableFuture<String> absoluteMove(PtzPosition position, PtzSpeed speed);

    /**
     * Execute a PTZ Preset command (Save, Goto, Get).
     */
    CompletableFuture<String> preset(PresetCommand command);
}
