package io.github.hyeonmo.exceptions;

/**
 * Thrown when an invalid SOAP response is received or a SOAP Fault occurs.
 */
public class OnvifSoapException extends OnvifException {
    
    private final String xmlPayload;

    public OnvifSoapException(String message, String xmlPayload) {
        super(message);
        this.xmlPayload = xmlPayload;
    }

    public String getXmlPayload() {
        return xmlPayload;
    }
}
