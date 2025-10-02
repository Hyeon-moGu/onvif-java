package io.github.hyeonmo.requests;

import io.github.hyeonmo.listeners.OnvifSnapshotURIListener;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifType;

/**
 * Created by Hyeonmo Gu on 30/09/2025.
 */
public class GetSnapshotRequest implements OnvifRequest{

    //Constants
    public static final String TAG = GetSnapshotRequest.class.getSimpleName();

    //Attributes
    private final OnvifMediaProfile mediaProfile;
    private final OnvifSnapshotURIListener listener;

    //Constructors
    public GetSnapshotRequest(OnvifMediaProfile mediaProfile, OnvifSnapshotURIListener listener) {
        super();
        this.mediaProfile = mediaProfile;
        this.listener = listener;
    }

    //Properties

    public OnvifMediaProfile getMediaProfile() {
        return mediaProfile;
    }

    public OnvifSnapshotURIListener getListener() {
        return listener;
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
