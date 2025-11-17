package io.github.hyeonmo.listeners.imaging;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.responses.ImagingResponse;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public interface ImagingResponseListener {

	void onResponse(OnvifDevice onvifDevice, ImagingResponse response);

	void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage);
}
