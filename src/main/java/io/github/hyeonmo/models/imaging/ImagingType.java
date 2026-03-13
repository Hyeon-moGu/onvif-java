package io.github.hyeonmo.models.imaging;

public enum ImagingType {
	 GET_IMAGING_SETTINGS("IMAGINGSETTINGS"),
	 FOCUS_MOVE("FOCUSMOVE"),
	 FOCUS_STOP("FOCUSSTOP"),
	 SET_SETTINGS("SETSETTINGS");

    public final String namespace;

    ImagingType(String namespace) {
        this.namespace = namespace;
    }
}
