package io.github.hyeonmo.exceptions;

public class OnvifExceptionFactory {

    public static OnvifException fromHttpError(int statusCode, String errorMessage) {
        if (statusCode == 401 || statusCode == 403) {
            return new OnvifAuthException(statusCode, "Authentication failed: " + errorMessage);
        } else if (statusCode >= 500) {
            return new OnvifSoapException("Server returned an error: " + statusCode, errorMessage);
        } else if (statusCode == -1) { // Custom code we use for standard Java IO Exceptions
            return new OnvifNetworkException("Network error: " + errorMessage);
        }
        return new OnvifException("HTTP Error " + statusCode + ": " + errorMessage);
    }
}
