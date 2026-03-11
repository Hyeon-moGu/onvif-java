package io.github.hyeonmo.exceptions;

/**
 * Thrown when authentication with the ONVIF device fails (e.g., 401 Unauthorized).
 */
public class OnvifAuthException extends OnvifException {
    
    private final int statusCode;

    public OnvifAuthException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
