package io.github.hyeonmo.core;

public enum DiscoveryMode {
    ONVIF(3702),
    UPNP(1900);

    public final int port;

    DiscoveryMode(int port) {
        this.port = port;
    }

}
