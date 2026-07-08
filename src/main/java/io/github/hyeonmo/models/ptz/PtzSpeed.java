package io.github.hyeonmo.models.ptz;

import io.github.hyeonmo.exceptions.OnvifValidationException;

/** Optional absolute-move speed. Values are validated against device options before use. */
public final class PtzSpeed {
    private final Double pan;
    private final Double tilt;
    private final Double zoom;

    public PtzSpeed(double pan, double tilt, double zoom) {
        this(Double.valueOf(pan), Double.valueOf(tilt), Double.valueOf(zoom));
    }

    public PtzSpeed(Double pan, Double tilt, Double zoom) {
        if ((pan == null) != (tilt == null)) {
            throw new OnvifValidationException("Pan and tilt speed must be supplied together.");
        }
        if (pan == null && zoom == null) {
            throw new OnvifValidationException("At least pan/tilt or zoom speed must be supplied.");
        }
        requireFiniteNonNegative(pan, "pan");
        requireFiniteNonNegative(tilt, "tilt");
        requireFiniteNonNegative(zoom, "zoom");
        this.pan = pan;
        this.tilt = tilt;
        this.zoom = zoom;
    }

    private static void requireFiniteNonNegative(Double value, String name) {
        if (value != null && (value.isNaN() || value.isInfinite() || value < 0)) {
            throw new OnvifValidationException(name + " speed must be finite and non-negative.");
        }
    }

    public Double getPan() {
        return pan;
    }

    public Double getTilt() {
        return tilt;
    }

    public Double getZoom() {
        return zoom;
    }

    public boolean hasPanTilt() {
        return pan != null;
    }

    public boolean hasZoom() {
        return zoom != null;
    }
}
