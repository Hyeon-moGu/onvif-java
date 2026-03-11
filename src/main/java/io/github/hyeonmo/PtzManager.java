package io.github.hyeonmo;

import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.requests.ptz.PtzRequest;

/**
 * Manages PTZ (Pan-Tilt-Zoom) and preset commands for ONVIF devices.
 * Provides both device-based and direct URL-based operations.
 *
 * Created by Hyeonmo Gu on 17/09/2025.
 * Modified by Hyeonmo Gu for v2.0
 */
public class PtzManager {

	private PtzRequest ptzRequest;

	public PtzManager() {
		this(new OnvifManager());
	}

	public PtzManager(OnvifManager onvifManager) {
		this.ptzRequest = new PtzRequest(onvifManager);
	}

	 /* -------------------- PTZ operations -------------------- */
	public CompletableFuture<String> move(OnvifDevice onvifDevice, PtzType ptzType) {
		return ptzRequest.move(onvifDevice, ptzType);
	}

	public CompletableFuture<String> stop(OnvifDevice onvifDevice) {
		return ptzRequest.stop(onvifDevice);
	}

	public CompletableFuture<String> move(String xaddr, String profileToken, String userName, String password, PtzType ptzType) {
		return ptzRequest.move(xaddr, profileToken, userName, password, ptzType);
	}

	public CompletableFuture<String> stop(String xaddr, String profileToken, String userName, String password) {
		return ptzRequest.stop(xaddr, profileToken, userName, password);
	}

	/* -------------------- Preset operations -------------------- */

	public CompletableFuture<String> preset(OnvifDevice onvifDevice, PresetCommand presetCommand) {
		return ptzRequest.preset(onvifDevice, presetCommand);
	}

	public CompletableFuture<String> preset(String xaddr, String profileToken, String userName, String password, PresetCommand presetCommand) {
		return ptzRequest.preset(xaddr, profileToken, userName, password, presetCommand);
	}

	public CompletableFuture<String> getStatus(OnvifDevice onvifDevice) {
		return ptzRequest.getStatus(onvifDevice);
	}

	public CompletableFuture<String> getStatus(String xaddr, String profileToken, String userName, String password) {
		return ptzRequest.getStatus(xaddr, profileToken, userName, password);
	}

	/* -------------------- Utility methods -------------------- */

	public void setPtzRequestTimeout(int to) {
		ptzRequest.setPtzRequestTimeout(to);
	}

	public int getPtzRequestTimeout() {
		return ptzRequest.getPtzRequestTimeout();
	}

	public void destroy() {
	    if (ptzRequest != null) {
	        ptzRequest.destroy();
	        ptzRequest = null;
	    }
	}
}
