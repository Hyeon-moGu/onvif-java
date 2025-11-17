package io.github.hyeonmo.requests.media;

import io.github.hyeonmo.listeners.media.OnvifMediaProfilesListener;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;


/**
 * Created by Tomas Verhelst on 04/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 */
public class GetMediaProfilesRequest implements OnvifRequest {

    //Constants
    public static final String TAG = GetMediaProfilesRequest.class.getSimpleName();

    //Attributes
    private final OnvifMediaProfilesListener listener;

    //Constructors
    public GetMediaProfilesRequest(OnvifMediaProfilesListener listener) {
        super();
        this.listener = listener;
    }

    //Properties

    public OnvifMediaProfilesListener getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        return "<GetProfiles xmlns=\"http://www.onvif.org/ver10/media/wsdl\"/>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_MEDIA_PROFILES;
    }

}
