package io.github.hyeonmo.exceptions;

/** Thrown when a requested ONVIF operation contains invalid input. */
public class OnvifValidationException extends OnvifException {
    public OnvifValidationException(String message) {
        super(message);
    }

    public OnvifValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
