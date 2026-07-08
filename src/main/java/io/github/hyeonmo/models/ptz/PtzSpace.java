package io.github.hyeonmo.models.ptz;

/** Coordinate-space URI and one- or two-dimensional ranges. */
public final class PtzSpace {
    private final String uri;
    private final PtzRange xRange;
    private final PtzRange yRange;

    public PtzSpace(String uri, PtzRange xRange, PtzRange yRange) {
        this.uri = uri;
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public String getUri() {
        return uri;
    }

    public PtzRange getXRange() {
        return xRange;
    }

    public PtzRange getYRange() {
        return yRange;
    }
}
