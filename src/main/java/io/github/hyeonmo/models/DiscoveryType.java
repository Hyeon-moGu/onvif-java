package io.github.hyeonmo.models;

public enum DiscoveryType {
    DEVICE(0, "Device"),
    NETWORK_VIDEO_TRANSMITTER(1, "NetworkVideoTransmitter");

    public final int id;
    public final String type;

    DiscoveryType(int id, String type) {
        this.id = id;
        this.type = type;
    }

}
