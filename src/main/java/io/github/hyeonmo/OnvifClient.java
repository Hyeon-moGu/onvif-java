package io.github.hyeonmo;

import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.operations.MediaOperations;
import io.github.hyeonmo.operations.PtzOperations;

/**
 * Fluent Builder for creating and authenticating with an ONVIF Device.
 * Provides the main entry point for the ONVIF Java v2.0 API.
 */
public class OnvifClient {
    
    // Hidden constructor
    private OnvifClient() {}
    
    public static Builder connect(String hostName) {
        return new Builder(hostName);
    }
    
    public static class Builder {
        private String hostName;
        private String username = "";
        private String password = "";
        private int timeoutMs = 10000;
        
        protected Builder(String hostName) {
            this.hostName = hostName;
        }
        
        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }
        
        public Builder timeout(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }
        
        /**
         * Asynchronously builds the client by initializing the underlying OnvifDevice,
         * fetching capabilities, and performing time synchronization.
         */
        public CompletableFuture<OnvifDeviceWrapper> buildAsync() {
            OnvifDevice device = new OnvifDevice(hostName, username, password);
            OnvifManager manager = new OnvifManager();
            
            // Connect and fetch initial required data
            return manager.getServices(device)
                    .thenCompose(services -> manager.getCapabilities(device))
                    .thenApply(capabilities -> new OnvifDeviceWrapper(device, manager));
        }
    }
    
    /**
     * Wrapper class around the raw OnvifDevice to provide Fluent access to operations.
     */
    public static class OnvifDeviceWrapper {
        private final OnvifDevice device;
        private final OnvifManager manager;
        private final PtzOperations ptzOperations;
        private final MediaOperations mediaOperations;
        
        protected OnvifDeviceWrapper(OnvifDevice device, OnvifManager manager) {
            this.device = device;
            this.manager = manager;
            this.ptzOperations = new PtzOperations(device, manager);
            this.mediaOperations = new MediaOperations(device, manager);
        }
        
        public OnvifDevice getRawDevice() {
            return device;
        }
        
        public PtzOperations ptz() {
            return ptzOperations;
        }
        
        public MediaOperations media() {
            return mediaOperations;
        }
        
        public void destroy() {
            manager.destroy();
            ptzOperations.destroy();
        }
    }
}
