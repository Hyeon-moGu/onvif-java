package io.github.hyeonmo;

import io.github.hyeonmo.listeners.OnvifResponseListener;
import io.github.hyeonmo.listeners.device.OnvifCapabilitiesListener;
import io.github.hyeonmo.listeners.device.OnvifDeviceInformationListener;
import io.github.hyeonmo.listeners.device.OnvifServicesListener;
import io.github.hyeonmo.listeners.media.OnvifMediaProfilesListener;
import io.github.hyeonmo.listeners.media.OnvifMediaStreamURIListener;
import io.github.hyeonmo.listeners.media.OnvifSnapshotURIListener;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.requests.OnvifRequest;
import io.github.hyeonmo.requests.device.GetCapabilitiesRequest;
import io.github.hyeonmo.requests.device.GetDeviceInformationRequest;
import io.github.hyeonmo.requests.device.GetServicesRequest;
import io.github.hyeonmo.requests.media.GetMediaProfilesRequest;
import io.github.hyeonmo.requests.media.GetMediaStreamRequest;
import io.github.hyeonmo.requests.media.GetSnapshotRequest;
import io.github.hyeonmo.responses.OnvifResponse;


/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class OnvifManager implements OnvifResponseListener {

    //Constants
    public final static String TAG = OnvifManager.class.getSimpleName();

    //Attributes
    private OnvifExecutor executor;
    private OnvifResponseListener onvifResponseListener;

    //Constructors
    public OnvifManager() {
        this(null);
    }

    private OnvifManager(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
        executor = new OnvifExecutor(this);
    }

    //Methods
    public void getServices(OnvifDevice device, OnvifServicesListener listener) {
        OnvifRequest request = new GetServicesRequest(listener);
        executor.sendRequest(device, request);
    }

    public void getDeviceInformation(OnvifDevice device, OnvifDeviceInformationListener listener) {
        OnvifRequest request = new GetDeviceInformationRequest(listener);
        executor.sendRequest(device, request);
    }

    public void getMediaProfiles(OnvifDevice device, OnvifMediaProfilesListener listener) {
        OnvifRequest request = new GetMediaProfilesRequest(listener);
        executor.sendRequest(device, request);
    }

    public void getMediaStreamURI(OnvifDevice device, OnvifMediaProfile profile, OnvifMediaStreamURIListener listener) {
        OnvifRequest request = new GetMediaStreamRequest(profile, listener);
        executor.sendRequest(device, request);
    }

    public void getCapabilities(OnvifDevice device, OnvifCapabilitiesListener listener) {
    	OnvifRequest request = new GetCapabilitiesRequest(listener);
    	executor.sendRequest(device, request);
    }

    public void sendOnvifRequest(OnvifDevice device, OnvifRequest request) {
        executor.sendRequest(device, request);
    }

    public void setOnvifResponseListener(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
    }

    public void getSnapshotURI(OnvifDevice device, OnvifMediaProfile profile, OnvifSnapshotURIListener listener) {
    	OnvifRequest request = new GetSnapshotRequest(profile, listener);
    	executor.sendRequest(device, request);
    }

    /**
     * Clear up the resources.
     */
    public void destroy() {
        onvifResponseListener = null;
        executor.clear();
    }

    @Override
    public void onResponse(OnvifDevice onvifDevice, OnvifResponse response) {
        if (onvifResponseListener != null)
            onvifResponseListener.onResponse(onvifDevice, response);
    }

    @Override
    public void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage) {
        if (onvifResponseListener != null)
            onvifResponseListener.onError(onvifDevice, errorCode, errorMessage);
    }

}
