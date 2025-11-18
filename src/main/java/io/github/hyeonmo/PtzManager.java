package io.github.hyeonmo;

import io.github.hyeonmo.listeners.ptz.PtzResponseListener;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.requests.ptz.PtzRequest;

/**
 * Manages PTZ (Pan-Tilt-Zoom) and preset commands for ONVIF devices.
 * Provides both device-based and direct URL-based operations.
 *
 * Created by Hyeonmo Gu on 17/09/2025.
 *
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

    /**
     * Sends a PTZ move command using the first available media profile
     *
     * <p>When using {@link OnvifDevice}, this method automatically retrieves
     * media profiles and uses the first available profile token. </p>
     *
     * @param onvifDevice the target ONVIF device
     * @param ptzType     the PTZ movement type (e.g. UP, DOWN_LEFT)
     * @param listener    callback listener for the response
     */
	public void move(OnvifDevice onvifDevice, PtzType ptzType, PtzResponseListener ptzResponseListener) {
		ptzRequest.move(onvifDevice, ptzType, ptzResponseListener);
	}

	/**
     * Sends a PTZ stop command to halt ongoing movement
     *
     * <p>When using {@link OnvifDevice}, this method automatically retrieves
     * media profiles and uses the first available profile token. </p>
     *
     * @param onvifDevice the target ONVIF device
     * @param listener    callback listener for the response
     */
	public void stop(OnvifDevice onvifDevice, PtzResponseListener ptzResponseListener) {
		ptzRequest.stop(onvifDevice,  ptzResponseListener);
	}

    /**
     * Sends a PTZ move command using the first available media profile
     *
     * @param xaddr        PTZ Service URI
     * @param profileToken Profile Token
     * @param userName     device username
     * @param password     device password
     * @param ptzType      the PTZ movement type (e.g. UP, DOWN_LEFT)
     * @param listener     callback listener for the response
     */
	public void move(String xaddr, String profileToken, String userName, String password, PtzType ptzType, PtzResponseListener ptzResponseListener) {
		ptzRequest.move(xaddr, profileToken, userName, password, ptzType, ptzResponseListener);
	}

    /**
     * Sends a PTZ move command using the first available media profile
     *
     * @param xaddr        PTZ Service URI
     * @param profileToken Profile Token
     * @param userName     device username
     * @param password     device password
     * @param listener     callback listener for the response
     */
	public void stop(String xaddr, String profileToken, String userName, String password, PtzResponseListener ptzResponseListener) {
		ptzRequest.stop(xaddr, profileToken, userName, password, ptzResponseListener);
	}

	/* -------------------- Preset operations -------------------- */

    /**
     * Executes a preset command (MOVE, SAVE, REMOVE) on the given device
     *
     * <p>When using {@link OnvifDevice}, this method automatically retrieves
     * media profiles and uses the first available profile token. </p>
     *
     * @param onvifDevice the target ONVIF device
     * @param command     the preset command (MOVE, SAVE, REMOVE)
     * @param listener    callback listener for the response
     */
	public void preset(OnvifDevice onvifDevice, PresetCommand presetCommand, PtzResponseListener ptzResponseListener) {
		ptzRequest.preset(onvifDevice, presetCommand, ptzResponseListener);
	}

    /**
     * Executes a preset command (MOVE, SAVE, REMOVE) on the given device
     *
     * @param xaddr        PTZ Service URI
     * @param profileToken Profile Token
     * @param userName     device username
     * @param password     device password
     * @param command      the preset command (MOVE, SAVE, REMOVE)
     * @param listener     callback listener for the response
     */
	public void preset(String xaddr, String profileToken, String userName, String password, PresetCommand presetCommand, PtzResponseListener ptzResponseListener) {
		ptzRequest.preset(xaddr, profileToken, userName, password, presetCommand, ptzResponseListener);
	}

	public void getStatus(OnvifDevice onvifDevice, PtzResponseListener ptzResponseListener) {
		ptzRequest.getStatus(onvifDevice, ptzResponseListener);
	}

	public void getStatus(String xaddr, String profileToken, String userName, String password, PtzResponseListener ptzResponseListener) {
		ptzRequest.getStatus(xaddr, profileToken, userName, password, ptzResponseListener);
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
