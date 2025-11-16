package io.github.hyeonmo.listeners;

import io.github.hyeonmo.responses.ImagingResponse;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public interface ImagingFocusResponseListener {

	void onResponse(ImagingResponse imagingResponse);

	void onError(int errorCode, String errorMessage);
}
