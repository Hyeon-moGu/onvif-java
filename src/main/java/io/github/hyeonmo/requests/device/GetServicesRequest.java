package io.github.hyeonmo.requests.device;

import io.github.hyeonmo.listeners.device.OnvifServicesListener;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;


/**
 * Created by Tomas Verhelst on 04/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class GetServicesRequest implements OnvifRequest {

    //Constants
    public static final String TAG = GetServicesRequest.class.getSimpleName();

    //Attributes
    private final OnvifServicesListener listener;

    //Constructors
    public GetServicesRequest(OnvifServicesListener listener) {
        super();
        this.listener = listener;
    }

    //Properties

    public OnvifServicesListener getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        return "<GetServices xmlns=\"http://www.onvif.org/ver10/device/wsdl\">" +
                "<IncludeCapability>true</IncludeCapability>" +
                "</GetServices>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_SERVICES;
    }

}
