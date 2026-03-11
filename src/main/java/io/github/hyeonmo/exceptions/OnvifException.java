package io.github.hyeonmo.exceptions;

/**
 * Base exception class for all ONVIF related errors.
 */
public class OnvifException extends RuntimeException {
    
    public OnvifException(String message) {
        super(message);
    }
    
    public OnvifException(String message, Throwable cause) {
        super(message, cause);
    }
}
