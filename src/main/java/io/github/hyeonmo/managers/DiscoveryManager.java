package io.github.hyeonmo.managers;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.core.DiscoveryMode;
import io.github.hyeonmo.core.OnvifDiscovery;
import io.github.hyeonmo.models.Device;

public class DiscoveryManager {

    public static final String TAG = DiscoveryManager.class.getSimpleName();

    private OnvifDiscovery discovery;

    public DiscoveryManager() {
        discovery = new OnvifDiscovery();
    }

    /**
     * Discovers a list of ONVIF-compatible device on the LAN asynchronously.
     *
     * @return a CompletableFuture resolving to a list of found ONVIF Devices.
     */
    public CompletableFuture<List<Device>> discover() {
        return discover(DiscoveryMode.ONVIF);
    }

    /**
     * Discovers a list of network devices on the LAN using the specified discovery mode.
     *
     * @return a CompletableFuture resolving to a list of found Devices.
     */
    public CompletableFuture<List<Device>> discover(DiscoveryMode mode) {
        return discovery.probe(mode);
    }

    public List<InetAddress> getInterfaceAddresses() {
        return discovery.getInterfaceAddresses();
    }

    public List<InetAddress> getBroadcastAddresses() {
        return discovery.getBroadcastAddresses();
    }

    public String getLocalIpAddress() {
        return discovery.getLocalIpAddress();
    }

    public int getDiscoveryTimeout() {
        return discovery.getDiscoveryTimeout();
    }

    public void setDiscoveryTimeout(int timeoutMs) {
        discovery.setDiscoveryTimeout(timeoutMs);
    }

    public DiscoveryMode getDiscoveryMode() {
        return discovery.getDiscoveryMode();
    }

    public void setDiscoveryMode(DiscoveryMode mode) {
        discovery.setDiscoveryMode(mode);
    }

}
