package io.github.hyeonmo.requests.media;

import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.requests.OnvifRequest;

/**
 * Created by Hyeonmo Gu on 30/09/2025.
 * Modified for v2.0 - callback removed.
 */
public class GetSnapshotRequest implements OnvifRequest {

    public static final String TAG = GetSnapshotRequest.class.getSimpleName();

    private final OnvifMediaProfile mediaProfile;

    public GetSnapshotRequest(OnvifMediaProfile mediaProfile) {
        super();
        this.mediaProfile = mediaProfile;
    }

    public OnvifMediaProfile getMediaProfile() {
        return mediaProfile;
    }

    @Override
    public String getXml() {
        return "<trt:GetSnapshotUri>" +
                "<trt:ProfileToken>" + mediaProfile.getToken() + "</trt:ProfileToken>" +
                "</trt:GetSnapshotUri>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_SNAPSHOT_URI;
    }
}
