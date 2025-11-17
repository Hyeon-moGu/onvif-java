package io.github.hyeonmo.listeners.imaging;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.imaging.ImagingSettings;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public interface ImagingSettingsListener {

	void onResponse(OnvifDevice onvifDevice, ImagingSettings imagingSettings);

	void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage);
}
