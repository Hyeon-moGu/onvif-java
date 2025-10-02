package io.github.hyeonmo.models;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public enum PtzType {

    UP(0.0, 0.7, 0.0),
    DOWN(0.0, -0.7, 0.0),
    LEFT(-0.7, 0.0, 0.0),
    RIGHT(0.7, 0.0, 0.0),
    UP_LEFT(-0.7, 0.7, 0.0),
    UP_RIGHT(0.7, 0.7, 0.0),
    DOWN_LEFT(-0.7, -0.7, 0.0),
    DOWN_RIGHT(0.7, -0.7, 0.0),

    ZOOM_IN(0.0, 0.0, 0.7),
    ZOOM_OUT(0.0, 0.0, -0.7);

    private final double pan;
    private final double tilt;
    private final double zoom;

    PtzType(double pan, double tilt, double zoom) {
        this.pan = pan;
        this.tilt = tilt;
        this.zoom = zoom;
    }

    public double getPan() {
        return pan;
    }

    public double getTilt() {
        return tilt;
    }

    public double getZoom() {
        return zoom;
    }

    @Override
    public String toString() {
    	return "PtzType{" +
    			"pan=" + pan +
    			", tilt=" + tilt +
    			", zoom=" + zoom +
    			"}";
    }
}