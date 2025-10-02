package io.github.hyeonmo.models;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class OnvifDevice extends Device {

    //Constants
    public static final String TAG = OnvifDevice.class.getSimpleName();

    //Attributes
    private OnvifServices path;
    private List<String> addresses;
    private List<HashMap<String, Object>> scopes;

    private String baseUrl;
    private String userName;
    private String password;

    //Constructors
    public OnvifDevice(String hostName) {
        this(hostName, "", "");
    }

    public OnvifDevice(String hostName, String username, String password) {
        super(hostName, username, password);
        path = new OnvifServices();
        addresses = new ArrayList<>();
        scopes = new ArrayList<>();
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

    @Override
    public DeviceType getType() {
        return DeviceType.ONVIF;
    }

}
