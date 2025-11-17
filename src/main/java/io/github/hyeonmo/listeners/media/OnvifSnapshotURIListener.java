package io.github.hyeonmo.listeners.media;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;

/**
 * Created by Hyeonmo Gu on 30/09/2025.
 */
public interface OnvifSnapshotURIListener {

	void onMediaSnapshotReceived(OnvifDevice device, OnvifMediaProfile profile, String uri);
}
