package io.github.hyeonmo.listeners;

import io.github.hyeonmo.models.ImagingSettings;
import io.github.hyeonmo.models.OnvifDevice;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public interface ImagingSettingsListener {

	void onResponse(OnvifDevice onvifDevice, ImagingSettings imagingSettings);

	void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage);
}
