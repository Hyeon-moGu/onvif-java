package io.github.hyeonmo.listeners;

import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDevice;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public interface OnvifCapabilitiesListener {

	void onDeviceCapabilitiesReceived(OnvifDevice device, OnvifCapabilities onvifCapabilities);
}
