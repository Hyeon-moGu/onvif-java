package io.github.hyeonmo.requests.media;

import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

/**
 * Created by Tomas Verhelst on 04/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 * Modified for v2.0 - callback removed.
 */
public class GetMediaStreamRequest implements OnvifRequest {

    public static final String TAG = GetMediaStreamRequest.class.getSimpleName();

    private final OnvifMediaProfile mediaProfile;

    public GetMediaStreamRequest(OnvifMediaProfile mediaProfile) {
        super();
        this.mediaProfile = mediaProfile;
    }

    public OnvifMediaProfile getMediaProfile() {
        return mediaProfile;
    }

    @Override
    public String getXml() {
        return "<GetStreamUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\">"
                + "<StreamSetup>"
                + "<Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream>"
                + "<Transport xmlns=\"http://www.onvif.org/ver10/schema\"><Protocol>RTSP</Protocol></Transport>"
                + "</StreamSetup>"
                + "<ProfileToken>" + mediaProfile.getToken() + "</ProfileToken>"
                + "</GetStreamUri>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_STREAM_URI;
    }
}
