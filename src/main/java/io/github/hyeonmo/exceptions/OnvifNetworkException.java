package io.github.hyeonmo.exceptions;

/**
 * Thrown when a network error occurs during communication with the ONVIF device.
 */
public class OnvifNetworkException extends OnvifException {
    
    public OnvifNetworkException(String message) {
        super(message);
    }

    public OnvifNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
