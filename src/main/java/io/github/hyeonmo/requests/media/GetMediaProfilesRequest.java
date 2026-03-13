package io.github.hyeonmo.requests.media;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

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
