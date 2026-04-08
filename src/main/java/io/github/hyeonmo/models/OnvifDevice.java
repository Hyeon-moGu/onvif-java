package io.github.hyeonmo.models;



import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import io.github.hyeonmo.operations.DeviceOperations;
import io.github.hyeonmo.operations.MediaOperations;
import io.github.hyeonmo.operations.PtzOperations;
import io.github.hyeonmo.operations.ImagingOperations;
import io.github.hyeonmo.operations.impl.DeviceOperationsImpl;
import io.github.hyeonmo.operations.impl.MediaOperationsImpl;
import io.github.hyeonmo.operations.impl.PtzOperationsImpl;
import io.github.hyeonmo.operations.impl.ImagingOperationsImpl;
import io.github.hyeonmo.operations.EventOperations;
import io.github.hyeonmo.operations.impl.EventOperationsImpl;

public class OnvifDevice extends Device {

    //Constants
    public static final String TAG = OnvifDevice.class.getSimpleName();

    //Attributes
    private OnvifServices path;
    private List<String> addresses;
    private List<HashMap<String, Object>> scopes;

    private String baseUrl;
    private long timeOffsetMs;
    private OnvifCapabilities capabilities;
    private List<OnvifMediaProfile> mediaProfiles;

    private transient DeviceOperations deviceOperations;
    private transient MediaOperations mediaOperations;
    private transient PtzOperations ptzOperations;
    private transient ImagingOperations imagingOperations;
    private transient EventOperations eventOperations;

    //Constructors
    public OnvifDevice(String hostName) {
        this(hostName, "", "");
    }

    public OnvifDevice(String hostName, String username, String password) {
        super(hostName, username, password);
        path = new OnvifServices();
        addresses = new ArrayList<>();
        scopes = new ArrayList<>();
        timeOffsetMs = 0;
    }

    //Methods
    public void addAddress(String address) {
        addresses.add(address);
    }

    public void addScope(HashMap<String, Object> map) {
    	scopes.add(map);
    }

    //Properties
    public OnvifServices getPath() {
        return path;
    }

    public void setPath(OnvifServices path) {
        this.path = path;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public List<HashMap<String, Object>> getScopes() {
    	return scopes;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public String getBaseUrl() {
    	return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
    	this.baseUrl = baseUrl;
    }

    public long getTimeOffsetMs() {
        return timeOffsetMs;
    }

    public void setTimeOffsetMs(long timeOffsetMs) {
        this.timeOffsetMs = timeOffsetMs;
    }

    public DeviceOperations device() {
        if (deviceOperations == null) deviceOperations = new DeviceOperationsImpl(this);
        return deviceOperations;
    }

    public MediaOperations media() {
        if (mediaOperations == null) mediaOperations = new MediaOperationsImpl(this);
        return mediaOperations;
    }

    public PtzOperations ptz() {
        if (ptzOperations == null) ptzOperations = new PtzOperationsImpl(this);
        return ptzOperations;
    }

    public ImagingOperations imaging() {
        if (imagingOperations == null) imagingOperations = new ImagingOperationsImpl(this);
        return imagingOperations;
    }

    public EventOperations event() {
        if (eventOperations == null) eventOperations = new EventOperationsImpl(this);
        return eventOperations;
    }

    public OnvifCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(OnvifCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public List<OnvifMediaProfile> getMediaProfiles() {
        return mediaProfiles;
    }

    public void setMediaProfiles(List<OnvifMediaProfile> mediaProfiles) {
        this.mediaProfiles = mediaProfiles;
    }

    @Override
    public DeviceType getType() {
        return DeviceType.ONVIF;
    }

}
