package io.github.hyeonmo.core;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hyeonmo.parsers.DiscoveryParser;
import io.github.hyeonmo.responses.OnvifResponse;

public class DiscoveryThread extends Thread {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryThread.class);

    //Constants
    public static final String TAG = DiscoveryThread.class.getSimpleName();

    //Attributes
    private DatagramSocket server;
    private int timeout;
    private DiscoveryParser parser;
    private DiscoveryCallback callback;

    //Constructors
    DiscoveryThread(DatagramSocket server, int timeout, DiscoveryMode mode, DiscoveryCallback callback) {
        super();
        this.server = server;
        this.timeout = timeout;
        this.callback = callback;
        parser = new DiscoveryParser(mode);
    }

    @Override
    public void run() {
        try {
            boolean started = false;
            DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
            server.setSoTimeout(timeout);
            long timerStarted = System.currentTimeMillis();
            while (System.currentTimeMillis() - timerStarted < timeout) {
                if (!started) {
                    callback.onDiscoveryStarted();
                    started = true;
                }
                server.receive(packet);
                String response = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                parser.setHostName(packet.getAddress().getHostName());
                callback.onDevicesFound(parser.parse(new OnvifResponse<>(response)));
            }

        }catch (SocketTimeoutException ignore) {
            log.debug("Discovery receive timed out after {} ms", timeout);
        }catch (Exception e) {
            log.warn("Discovery receive failed", e);
        } finally {
            server.close();
            callback.onDiscoveryFinished();
        }
    }
}
