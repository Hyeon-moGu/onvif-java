package io.github.hyeonmo.listeners;

import io.github.hyeonmo.responses.PtzResponse;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public interface PtzResponseListener {

	void onResponse(PtzResponse ptzResponse);

}
