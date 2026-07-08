package io.github.hyeonmo.models.ptz;

import java.util.Objects;
import io.github.hyeonmo.exceptions.OnvifValidationException;

/** Absolute PTZ position. Pan and tilt must be supplied together; zoom is optional. */
public final class PtzPosition {
    private final Double pan;
    private final Double tilt;
    private final Double zoom;
    private final String panTiltSpace;
    private final String zoomSpace;

    public PtzPosition(double pan, double tilt, double zoom) {
        this(pan, tilt, zoom, null, null);
    }

    public PtzPosition(Double pan, Double tilt, Double zoom) {
        this(pan, tilt, zoom, null, null);
    }

    public PtzPosition(Double pan, Double tilt, Double zoom, String panTiltSpace,
            String zoomSpace) {
        if ((pan == null) != (tilt == null)) {
            throw new OnvifValidationException("Pan and tilt must be supplied together.");
        }
        if (pan == null && zoom == null) {
            throw new OnvifValidationException("At least pan/tilt or zoom must be supplied.");
        }
        requireFinite(pan, "pan");
        requireFinite(tilt, "tilt");
        requireFinite(zoom, "zoom");
        this.pan = pan;
        this.tilt = tilt;
        this.zoom = zoom;
        this.panTiltSpace = panTiltSpace;
        this.zoomSpace = zoomSpace;
    }

    private static void requireFinite(Double value, String name) {
        if (value != null && (value.isNaN() || value.isInfinite())) {
            throw new OnvifValidationException(name + " must be finite.");
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

    public String getPanTiltSpace() {
        return panTiltSpace;
    }

    public String getZoomSpace() {
        return zoomSpace;
    }

    public boolean hasPanTilt() {
        return pan != null;
    }

    public boolean hasZoom() {
        return zoom != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PtzPosition))
            return false;
        PtzPosition that = (PtzPosition) o;
        return Objects.equals(pan, that.pan) && Objects.equals(tilt, that.tilt)
                && Objects.equals(zoom, that.zoom)
                && Objects.equals(panTiltSpace, that.panTiltSpace)
                && Objects.equals(zoomSpace, that.zoomSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pan, tilt, zoom, panTiltSpace, zoomSpace);
    }

    @Override
    public String toString() {
        return "PtzPosition{pan=" + pan + ", tilt=" + tilt + ", zoom=" + zoom + '}';
    }
}
