package io.github.hyeonmo.listeners.imaging;

import io.github.hyeonmo.responses.ImagingResponse;

public interface ImagingSettingRequestListener {

	void onResponse(ImagingResponse imagingResponse);

	void onError(int errorCode, String errorMessage);
}
