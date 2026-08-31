package io.github.hyeonmo.models.media;

public enum StreamType {

    RTP_UNICAST("RTP-Unicast"),
    RTP_MULTICAST("RTP-Multicast");

    public final String value;

    StreamType(String value) {
        this.value = value;
    }
}
