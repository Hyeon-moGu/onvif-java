package io.github.hyeonmo.models.ptz;

import io.github.hyeonmo.exceptions.OnvifUnsupportedException;
import io.github.hyeonmo.exceptions.OnvifValidationException;

/** Absolute-position and speed spaces selected from GetConfigurationOptions. */
public final class PtzConfigurationOptions {
    private final PtzSpace absolutePanTiltSpace;
    private final PtzSpace absoluteZoomSpace;
    private final PtzSpace panTiltSpeedSpace;
    private final PtzSpace zoomSpeedSpace;

    public PtzConfigurationOptions(PtzSpace absolutePanTiltSpace, PtzSpace absoluteZoomSpace,
            PtzSpace panTiltSpeedSpace, PtzSpace zoomSpeedSpace) {
        this.absolutePanTiltSpace = absolutePanTiltSpace;
        this.absoluteZoomSpace = absoluteZoomSpace;
        this.panTiltSpeedSpace = panTiltSpeedSpace;
        this.zoomSpeedSpace = zoomSpeedSpace;
    }

    public PtzSpace getAbsolutePanTiltSpace() {
        return absolutePanTiltSpace;
    }

    public PtzSpace getAbsoluteZoomSpace() {
        return absoluteZoomSpace;
    }

    public PtzSpace getPanTiltSpeedSpace() {
        return panTiltSpeedSpace;
    }

    public PtzSpace getZoomSpeedSpace() {
        return zoomSpeedSpace;
    }

    public boolean supportsAbsolutePanTilt() {
        return absolutePanTiltSpace != null;
    }

    public boolean supportsAbsoluteZoom() {
        return absoluteZoomSpace != null;
    }

    public void validate(PtzPosition position, PtzSpeed speed) {
        if (speed != null && speed.hasPanTilt() && !position.hasPanTilt()) {
            throw new OnvifValidationException("Pan/Tilt speed requires a Pan/Tilt target.");
        }
        if (speed != null && speed.hasZoom() && !position.hasZoom()) {
            throw new OnvifValidationException("Zoom speed requires a Zoom target.");
        }
        if (position.hasPanTilt()) {
            requireSupported(absolutePanTiltSpace, "Absolute Pan/Tilt");
            requireInRange(position.getPan(), absolutePanTiltSpace.getXRange(), "pan");
            requireInRange(position.getTilt(), absolutePanTiltSpace.getYRange(), "tilt");
        }
        if (position.hasZoom()) {
            requireSupported(absoluteZoomSpace, "Absolute Zoom");
            requireInRange(position.getZoom(), absoluteZoomSpace.getXRange(), "zoom");
        }
        if (speed != null && speed.hasPanTilt()) {
            requireSupported(panTiltSpeedSpace, "Pan/Tilt speed");
            requireInRange(speed.getPan(), panTiltSpeedSpace.getXRange(), "pan speed");
            // PanTiltSpeedSpace is normally a Space1DDescription. Apply its
            // scalar range to both Vector2D components; accept YRange only as
            // a vendor extension when one is present.
            PtzRange tiltRange =
                    panTiltSpeedSpace.getYRange() == null ? panTiltSpeedSpace.getXRange()
                            : panTiltSpeedSpace.getYRange();
            requireInRange(speed.getTilt(), tiltRange, "tilt speed");
        }
        if (speed != null && speed.hasZoom()) {
            requireSupported(zoomSpeedSpace, "Zoom speed");
            requireInRange(speed.getZoom(), zoomSpeedSpace.getXRange(), "zoom speed");
        }
    }

    private static void requireSupported(PtzSpace space, String name) {
        if (space == null)
            throw new OnvifUnsupportedException(name + " is not supported by this device.");
    }

    private static void requireInRange(double value, PtzRange range, String name) {
        if (range == null || !range.contains(value)) {
            throw new OnvifValidationException(
                    name + " " + value + " is outside supported range " + range);
        }
    }
}
