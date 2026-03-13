package io.github.hyeonmo.models;

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

    //Constructors
    public OnvifServices() {
    }

    //Properties

    public String getServicesPath() {
        return servicesPath;
    }

    public void setServicesPath(String servicesPath) {
        this.servicesPath = servicesPath;
    }

    public String getDeviceInformationPath() {
        return deviceInformationPath;
    }

    public void setDeviceInformationPath(String deviceInformationPath) {
        this.deviceInformationPath = deviceInformationPath;
    }

    public String getProfilesPath() {
        return profilesPath;
    }

    public void setProfilesPath(String profilesPath) {
        this.profilesPath = profilesPath;
    }

    public String getStreamURIPath() {
        return streamURIPath;
    }

    public void setStreamURIPath(String streamURIPath) {
        this.streamURIPath = streamURIPath;
    }

    public String getPtzPath() {
        return ptzPath;
    }

    public void setPtzPath(String ptzPath) {
        this.ptzPath = ptzPath;
    }

    public String getImagingPath() {
        return imagingPath;
    }

    public void setImagingPath(String imagingPath) {
        this.imagingPath = imagingPath;
    }

    public String getEventsPath() {
        return eventsPath;
    }

    public void setEventsPath(String eventsPath) {
        this.eventsPath = eventsPath;
    }

}
