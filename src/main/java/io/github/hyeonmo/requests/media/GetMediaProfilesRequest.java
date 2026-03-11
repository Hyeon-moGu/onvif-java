package io.github.hyeonmo.requests.media;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

/**
 * Created by Tomas Verhelst on 04/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 * Modified for v2.0 - callback removed.
 */
public class GetMediaProfilesRequest implements OnvifRequest {

    public static final String TAG = GetMediaProfilesRequest.class.getSimpleName();

    public GetMediaProfilesRequest() {
        super();
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
