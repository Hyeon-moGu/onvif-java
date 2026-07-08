package io.github.hyeonmo.exceptions;

/** Thrown when required device, profile, or service configuration is unavailable. */
public class OnvifConfigurationException extends OnvifException {
    public OnvifConfigurationException(String message) {
        super(message);
    }
}
