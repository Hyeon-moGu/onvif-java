package io.github.hyeonmo.models.media;

public enum TransportProtocol {

    UDP("UDP"),
    TCP("TCP"),
    RTSP("RTSP"),
    HTTP("HTTP");

    public final String value;

    TransportProtocol(String value) {
        this.value = value;
    }
}
