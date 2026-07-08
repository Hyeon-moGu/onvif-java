package io.github.hyeonmo.exceptions;

/** Thrown when an ONVIF device does not advertise support for an operation or coordinate space. */
public class OnvifUnsupportedException extends OnvifException {
    public OnvifUnsupportedException(String message) {
        super(message);
    }
}
