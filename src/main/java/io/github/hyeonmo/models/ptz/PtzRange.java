package io.github.hyeonmo.models.ptz;

import io.github.hyeonmo.exceptions.OnvifValidationException;

/** Inclusive numeric range reported by an ONVIF PTZ coordinate space. */
public final class PtzRange {
    private final double min;
    private final double max;

    public PtzRange(double min, double max) {
        if (Double.isNaN(min) || Double.isNaN(max) || min > max) {
            throw new OnvifValidationException("Invalid PTZ range: " + min + ".." + max);
        }
        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean contains(double value) {
        return value >= min && value <= max;
    }

    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }
}
