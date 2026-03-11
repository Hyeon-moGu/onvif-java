package io.github.hyeonmo.requests.device;

import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 * Modified for v2.0 - callback removed.
 */
public class GetCapabilitiesRequest implements OnvifRequest {

    public static final String TAG = GetCapabilitiesRequest.class.getSimpleName();

    public GetCapabilitiesRequest() {
        super();
    }

    @Override
    public String getXml() {
        return "<tds:GetCapabilities>" +
                "<tds:Category>All</tds:Category>" +
                "</tds:GetCapabilities>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_CAPABILITIES;
    }
}
