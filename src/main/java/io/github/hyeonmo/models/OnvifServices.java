package io.github.hyeonmo.models;

import io.github.hyeonmo.utils.OnvifUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Discovered service addresses and legacy paths. Setting a legacy path clears
 * the corresponding full-address override so manually configured paths take effect.
 */
public class OnvifServices {

    //Constants
    public static final String TAG = OnvifServices.class.getSimpleName();
    public static final String ONVIF_PATH_SERVICES = "/onvif/device_service";
    public static final String ONVIF_PATH_DEVICE_INFORMATION = "/onvif/device_service";
    public static final String ONVIF_PATH_PROFILES = "/onvif/device_service";
    public static final String ONVIF_PATH_STREAM_URI = "/onvif/device_service";

    //Attributes
    private String servicesPath = ONVIF_PATH_SERVICES;
    private String deviceInformationPath = ONVIF_PATH_DEVICE_INFORMATION;
    private String profilesPath = ONVIF_PATH_PROFILES;
    private String streamURIPath = ONVIF_PATH_STREAM_URI;
    private String ptzPath = ONVIF_PATH_SERVICES;
    private String imagingPath = ONVIF_PATH_SERVICES;
    private String eventsPath = ONVIF_PATH_SERVICES;
    private String deviceXaddr;
    private String mediaXaddr;
    private String ptzXaddr;
    private String imagingXaddr;
    private String eventsXaddr;

    //Constructors
    public OnvifServices() {
    }

    //Properties

    /** Returns the advertised device service address, or null when only a path is configured. */
    public String getDeviceXaddr() {
        return deviceXaddr;
    }

    /**
     * Stores the advertised device address and updates the legacy path accessors.
     * Null or empty addresses clear the override without changing the paths.
     * @throws IllegalArgumentException if a nonempty address is not an absolute HTTP(S) URL
     */
    public void setDeviceXaddr(String address) {
        String path = servicePath(address);
        deviceXaddr = address;
        if (address != null && !address.isEmpty()) {
            servicesPath = path;
            deviceInformationPath = path;
        }
    }

    /** Returns the advertised media service address, or null when only a path is configured. */
    public String getMediaXaddr() {
        return mediaXaddr;
    }

    /**
     * Stores the advertised media address and updates the legacy path accessors.
     * Null or empty addresses clear the override without changing the paths.
     * @throws IllegalArgumentException if a nonempty address is not an absolute HTTP(S) URL
     */
    public void setMediaXaddr(String address) {
        String path = servicePath(address);
        mediaXaddr = address;
        if (address != null && !address.isEmpty()) {
            profilesPath = path;
            streamURIPath = path;
        }
    }

    /** Returns the advertised ptz service address, or null when only a path is configured. */
    public String getPtzXaddr() {
        return ptzXaddr;
    }

    /**
     * Stores the advertised ptz address and updates the legacy path accessors.
     * Null or empty addresses clear the override without changing the paths.
     * @throws IllegalArgumentException if a nonempty address is not an absolute HTTP(S) URL
     */
    public void setPtzXaddr(String address) {
        String path = servicePath(address);
        ptzXaddr = address;
        if (address != null && !address.isEmpty()) {
            ptzPath = path;
        }
    }

    /** Returns the advertised imaging service address, or null when only a path is configured. */
    public String getImagingXaddr() {
        return imagingXaddr;
    }

    /**
     * Stores the advertised imaging address and updates the legacy path accessors.
     * Null or empty addresses clear the override without changing the paths.
     * @throws IllegalArgumentException if a nonempty address is not an absolute HTTP(S) URL
     */
    public void setImagingXaddr(String address) {
        String path = servicePath(address);
        imagingXaddr = address;
        if (address != null && !address.isEmpty()) {
            imagingPath = path;
        }
    }

    /** Returns the advertised events service address, or null when only a path is configured. */
    public String getEventsXaddr() {
        return eventsXaddr;
    }

    /**
     * Stores the advertised events address and updates the legacy path accessors.
     * Null or empty addresses clear the override without changing the paths.
     * @throws IllegalArgumentException if a nonempty address is not an absolute HTTP(S) URL
     */
    public void setEventsXaddr(String address) {
        String path = servicePath(address);
        eventsXaddr = address;
        if (address != null && !address.isEmpty()) {
            eventsPath = path;
        }
    }

    public String getServicesPath() {
        return servicesPath;
    }

    private static String servicePath(String address) {
        if (address == null || address.isEmpty()) return null;
        try {
            URL url = new URL(address);
            if (("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()))
                    && !url.getHost().isEmpty()) {
                return OnvifUtils.getPathFromURL(address);
            }
        } catch (MalformedURLException ignored) {
            // Report an invalid model argument without logging potentially sensitive URLs.
        }
        throw new IllegalArgumentException("Service address must be an absolute HTTP(S) URL");
    }

    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
        deviceXaddr = null;
    }

    public String getDeviceInformationPath() {
        return deviceInformationPath;
    }

    public void setDeviceInformationPath(String deviceInformationPath) {
        this.deviceInformationPath = deviceInformationPath;
        deviceXaddr = null;
    }

    public String getProfilesPath() {
        return profilesPath;
    }

    public void setProfilesPath(String profilesPath) {
        this.profilesPath = profilesPath;
        mediaXaddr = null;
    }

    public String getStreamURIPath() {
        return streamURIPath;
    }

    public void setStreamURIPath(String streamURIPath) {
        this.streamURIPath = streamURIPath;
        mediaXaddr = null;
    }

    public String getPtzPath() {
        return ptzPath;
    }

    public void setPtzPath(String ptzPath) {
        this.ptzPath = ptzPath;
        ptzXaddr = null;
    }

    public String getImagingPath() {
        return imagingPath;
    }

    public void setImagingPath(String imagingPath) {
        this.imagingPath = imagingPath;
        imagingXaddr = null;
    }

    public String getEventsPath() {
        return eventsPath;
    }

    public void setEventsPath(String eventsPath) {
        this.eventsPath = eventsPath;
        eventsXaddr = null;
    }

}
