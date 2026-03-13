package io.github.hyeonmo.managers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.core.PtzExecutor;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.requests.ptz.PtzMoveRequest;
import io.github.hyeonmo.requests.ptz.PtzStopRequest;
import io.github.hyeonmo.requests.ptz.PtzPresetRequest;
import io.github.hyeonmo.requests.ptz.PtzStatusRequest;

/**
 * Manages PTZ (Pan-Tilt-Zoom) and preset commands for ONVIF devices.
 * Provides both device-based and direct URL-based operations.
 */
public class PtzManager {

	private final OnvifManager onvifManager;
	private PtzExecutor ptzExecutor;

	public PtzManager() {
		this(new OnvifManager());
	}

	public PtzManager(OnvifManager onvifManager) {
		this.onvifManager = onvifManager;
		this.ptzExecutor = new PtzExecutor();
	}

	 /* -------------------- PTZ operations -------------------- */
	public CompletableFuture<String> move(OnvifDevice onvifDevice, PtzType ptzType) {
		return resolveXaddr(onvifDevice).thenCompose(xaddr -> {
			List<OnvifMediaProfile> cachedProfiles = onvifDevice.getMediaProfiles();
			if (cachedProfiles != null && !cachedProfiles.isEmpty()) {
				String profileToken = cachedProfiles.get(0).getToken();
				PtzMoveRequest request = new PtzMoveRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), ptzType, onvifDevice.getTimeOffsetMs());
				return ptzExecutor.sendRequest(request);
			}

			return onvifManager.getMediaProfiles(onvifDevice)
				.thenCompose(profiles -> {
					if (profiles == null || profiles.isEmpty()) {
						CompletableFuture<String> f = new CompletableFuture<>();
						f.completeExceptionally(new RuntimeException("No media profiles found."));
						return f;
					}
					String profileToken = profiles.get(0).getToken();
					PtzMoveRequest request = new PtzMoveRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), ptzType, onvifDevice.getTimeOffsetMs());
					return ptzExecutor.sendRequest(request);
				});
		});
	}

	public CompletableFuture<String> stop(OnvifDevice onvifDevice) {
		return resolveXaddr(onvifDevice).thenCompose(xaddr -> {
			List<OnvifMediaProfile> cachedProfiles = onvifDevice.getMediaProfiles();
			if (cachedProfiles != null && !cachedProfiles.isEmpty()) {
				String profileToken = cachedProfiles.get(0).getToken();
				PtzStopRequest request = new PtzStopRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs());
				return ptzExecutor.sendRequest(request);
			}

			return onvifManager.getMediaProfiles(onvifDevice)
				.thenCompose(profiles -> {
					if (profiles == null || profiles.isEmpty()) {
						CompletableFuture<String> f = new CompletableFuture<>();
						f.completeExceptionally(new RuntimeException("No media profiles found."));
						return f;
					}
					String profileToken = profiles.get(0).getToken();
					PtzStopRequest request = new PtzStopRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs());
					return ptzExecutor.sendRequest(request);
				});
		});
	}

	public CompletableFuture<String> move(String xaddr, String profileToken, String userName, String password, PtzType ptzType) {
		PtzMoveRequest request = new PtzMoveRequest(xaddr, profileToken, userName, password, ptzType, 0);
		return ptzExecutor.sendRequest(request);
	}

	public CompletableFuture<String> stop(String xaddr, String profileToken, String userName, String password) {
		PtzStopRequest request = new PtzStopRequest(xaddr, profileToken, userName, password, 0);
		return ptzExecutor.sendRequest(request);
	}

	/* -------------------- Preset operations -------------------- */
	public CompletableFuture<String> preset(OnvifDevice onvifDevice, PresetCommand presetCommand) {
		return resolveXaddr(onvifDevice).thenCompose(xaddr -> {
			List<OnvifMediaProfile> cachedProfiles = onvifDevice.getMediaProfiles();
			if (cachedProfiles != null && !cachedProfiles.isEmpty()) {
				String profileToken = cachedProfiles.get(0).getToken();
				PtzPresetRequest request = new PtzPresetRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), presetCommand, onvifDevice.getTimeOffsetMs());
				return ptzExecutor.sendRequest(request);
			}

			return onvifManager.getMediaProfiles(onvifDevice)
				.thenCompose(profiles -> {
					if (profiles == null || profiles.isEmpty()) {
						CompletableFuture<String> f = new CompletableFuture<>();
						f.completeExceptionally(new RuntimeException("No media profiles found."));
						return f;
					}
					String profileToken = profiles.get(0).getToken();
					PtzPresetRequest request = new PtzPresetRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), presetCommand, onvifDevice.getTimeOffsetMs());
					return ptzExecutor.sendRequest(request);
				});
		});
	}

	public CompletableFuture<String> preset(String xaddr, String profileToken, String userName, String password, PresetCommand presetCommand) {
		PtzPresetRequest request = new PtzPresetRequest(xaddr, profileToken, userName, password, presetCommand, 0);
		return ptzExecutor.sendRequest(request);
	}

	public CompletableFuture<String> getStatus(OnvifDevice onvifDevice) {
		return resolveXaddr(onvifDevice).thenCompose(xaddr -> {
			List<OnvifMediaProfile> cachedProfiles = onvifDevice.getMediaProfiles();
			if (cachedProfiles != null && !cachedProfiles.isEmpty()) {
				String profileToken = cachedProfiles.get(0).getToken();
				PtzStatusRequest request = new PtzStatusRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs());
				return ptzExecutor.sendRequest(request);
			}

			return onvifManager.getMediaProfiles(onvifDevice)
				.thenCompose(profiles -> {
					if (profiles == null || profiles.isEmpty()) {
						CompletableFuture<String> f = new CompletableFuture<>();
						f.completeExceptionally(new RuntimeException("No media profiles found."));
						return f;
					}
					String profileToken = profiles.get(0).getToken();
					PtzStatusRequest request = new PtzStatusRequest(xaddr, profileToken, onvifDevice.getUsername(), onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs());
					return ptzExecutor.sendRequest(request);
				});
		});
	}

	public CompletableFuture<String> getStatus(String xaddr, String profileToken, String userName, String password) {
		PtzStatusRequest request = new PtzStatusRequest(xaddr, profileToken, userName, password, 0);
		return ptzExecutor.sendRequest(request);
	}

	/* -------------------- Utility methods -------------------- */
	private CompletableFuture<String> resolveXaddr(OnvifDevice onvifDevice) {
		return CompletableFuture.supplyAsync(() -> {
			if (onvifDevice.getCapabilities() != null && onvifDevice.getCapabilities().getPtzXaddr() != null && !onvifDevice.getCapabilities().getPtzXaddr().isEmpty()) {
				return onvifDevice.getCapabilities().getPtzXaddr();
			}

			if (onvifDevice.getPath() != null && onvifDevice.getPath().getPtzPath() != null) {
				String baseUrl = onvifDevice.getBaseUrl();
				if (baseUrl == null || baseUrl.isEmpty()) {
					baseUrl = onvifDevice.getHostName();
				}
				return baseUrl + onvifDevice.getPath().getPtzPath();
			}

			if (onvifDevice.getAddresses() != null && !onvifDevice.getAddresses().isEmpty()) {
				return onvifDevice.getAddresses().get(0);
			}
			if (onvifDevice.getHostName() != null && !onvifDevice.getHostName().isEmpty()) {
				return onvifDevice.getHostName();
			}
			throw new RuntimeException("No valid address or hostname found for PTZ request.");
		});
	}

	public void destroy() {
	    if (ptzExecutor != null) {
	        ptzExecutor.destroy();
	        ptzExecutor = null;
	    }
	}
}
