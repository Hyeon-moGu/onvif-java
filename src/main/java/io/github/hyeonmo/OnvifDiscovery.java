package io.github.hyeonmo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import io.github.hyeonmo.DiscoveryCallback;
import io.github.hyeonmo.models.Device;
import io.github.hyeonmo.DiscoveryCallback;
import io.github.hyeonmo.models.DiscoveryPacket;
import io.github.hyeonmo.models.OnvifPacket;

/**
 * The OnvifDiscovery class uses the Web Services Dynamic Discovery (WS-Discovery).
 *
 * Modified by Hyeonmo Gu for v2.0
 */
public class OnvifDiscovery {

    public static final String TAG = OnvifDiscovery.class.getSimpleName();
    private static final String MULTICAST_ADDRESS_IPV4 = "239.255.255.250"; 
    private static final String MULTICAST_ADDRESS_IPV6 = "[FF02::C]";
    private static int DISCOVERY_TIMEOUT = 10000;

    private static final Random random = new SecureRandom();

    private int discoveryTimeout = DISCOVERY_TIMEOUT;
    private DiscoveryMode mode;

    OnvifDiscovery() {
        this(DiscoveryMode.ONVIF);
    }

    public OnvifDiscovery(DiscoveryMode mode) {
        this.mode = mode;
    }

    int getDiscoveryTimeout() {
        return discoveryTimeout;
    }

    void setDiscoveryTimeout(int timeoutMs) {
        discoveryTimeout = timeoutMs;
    }

    DiscoveryMode getDiscoveryMode() {
        return mode;
    }

    void setDiscoveryMode(DiscoveryMode mode) {
        this.mode = mode;
    }

    CompletableFuture<List<Device>> probe(DiscoveryMode mode) {
        this.mode = mode;
        List<InetAddress> addresses = getInterfaceAddresses();
        return broadcast(addresses);
    }

    private CompletableFuture<List<Device>> broadcast(List<InetAddress> addresses) {
        CompletableFuture<List<Device>> future = new CompletableFuture<>();
        Collection<Device> devices = new ConcurrentSkipListSet<>();
        List<DiscoveryThread> threads = new ArrayList<>();

        ExecutorService executorService = Executors.newCachedThreadPool();
        ExecutorService monitor = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(addresses.size());
        List<Runnable> runnables = new ArrayList<>();

        for (InetAddress address : addresses) {
            runnables.add(() -> {
                try {
                    OnvifPacket packet = createDiscoveryPacket();
                    byte[] data = packet.getData();

                    DatagramSocket client = new DatagramSocket(new InetSocketAddress(address, 0));

                    DiscoveryThread thread = new DiscoveryThread(client, discoveryTimeout, mode, new DiscoveryCallback() {

                        @Override
                        public void onDiscoveryStarted() {
                            try {
                                if (address instanceof Inet4Address) {
                                    client.send(new DatagramPacket(data, data.length, InetAddress.getByName(MULTICAST_ADDRESS_IPV4), mode.port));
                                } else {
                                    client.send(new DatagramPacket(data, data.length, InetAddress.getByName(MULTICAST_ADDRESS_IPV6), mode.port));
                                }
                            } catch (IOException e) {
                            }
                        }

                        @Override
                        public void onDevicesFound(List<Device> onvifDevices) {
                            devices.addAll(onvifDevices);
                        }

                        @Override
                        public void onDiscoveryFinished() {
                            latch.countDown();
                        }

                    });
                    thread.setName("DiscoveryThread-" + address.getHostAddress());
                    threads.add(thread);
                    thread.start();

                } catch (Exception e) {
                	e.printStackTrace();
                }

            });
        }

        monitor.submit(() -> {
            for (Runnable runnable : runnables) {
                executorService.submit(runnable);
            }

            try {
                executorService.shutdown();
                latch.await(discoveryTimeout, TimeUnit.MILLISECONDS);
                boolean cleanShutdown = executorService.awaitTermination(discoveryTimeout, TimeUnit.MILLISECONDS);

                if (!cleanShutdown) {
                    executorService.shutdownNow();
                }

                future.complete(new ArrayList<>(devices));
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        });
        monitor.shutdown();
        return future;
    }

    private OnvifPacket createDiscoveryPacket() {
        String uuid = UUID.randomUUID().toString();
        return new DiscoveryPacket(uuid, mode);
    }

    List<InetAddress> getInterfaceAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; 
                }

                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    addresses.add(address.getAddress());
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return addresses;
    }

    List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> addresses = new ArrayList<>();
        try {
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue; 
                }

                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = address.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }

                    addresses.add(broadcast);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return addresses;
    }

    String getLocalIpAddress() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {

        }
        return null;
    }
}
