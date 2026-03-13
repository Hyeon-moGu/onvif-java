package io.github.hyeonmo.models;

public enum OnvifType {
    CUSTOM(""),
    GET_SERVICES("http://www.onvif.org/ver10/device/wsdl"),
    GET_CAPABILITIES("http://www.onvif.org/ver10/device/wsdl"),
    GET_DEVICE_INFORMATION("http://www.onvif.org/ver10/device/wsdl"),
    GET_MEDIA_PROFILES("http://www.onvif.org/ver10/media/wsdl"),
    GET_STREAM_URI("http://www.onvif.org/ver10/media/wsdl"),
	GET_SNAPSHOT_URI("http://www.onvif.org/ver10/media/wsdl"),
    GET_SYSTEM_DATE_AND_TIME("http://www.onvif.org/ver10/device/wsdl"),
    GET_PTZ("http://www.onvif.org/ver20/ptz/wsdl"),
    GET_IMAGING("http://www.onvif.org/ver20/imaging/wsdl"),
    GET_EVENTS("http://www.onvif.org/ver10/events/wsdl");

    public final String namespace;

    OnvifType(String namespace) {
        this.namespace = namespace;
    }

}
