package io.github.hyeonmo.models;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public enum ImagingType {
	 GET_IMAGING_SETTINGS("IMAGINGSETTINGS"),
	 FOCUS_MOVE("FOCUSMOVE"),
	 FOCUS_STOP("FOCUSSTOP");

    public final String namespace;

    ImagingType(String namespace) {
        this.namespace = namespace;
    }
}
