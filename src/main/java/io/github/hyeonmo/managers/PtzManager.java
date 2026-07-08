package io.github.hyeonmo.managers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.github.hyeonmo.core.PtzExecutor;
import io.github.hyeonmo.exceptions.OnvifConfigurationException;
import io.github.hyeonmo.exceptions.OnvifValidationException;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.models.ptz.PtzConfigurationOptions;
import io.github.hyeonmo.models.ptz.PtzPosition;
import io.github.hyeonmo.models.ptz.PtzSpeed;
import io.github.hyeonmo.requests.ptz.PtzAbsoluteMoveRequest;
import io.github.hyeonmo.requests.ptz.PtzConfigurationOptionsRequest;
import io.github.hyeonmo.requests.ptz.PtzPositionRequest;
import io.github.hyeonmo.requests.ptz.PtzMoveRequest;
import io.github.hyeonmo.requests.ptz.PtzStopRequest;
import io.github.hyeonmo.requests.ptz.PtzPresetRequest;
import io.github.hyeonmo.requests.ptz.PtzStatusRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages PTZ (Pan-Tilt-Zoom) and preset commands for ONVIF devices.
 * Provides both device-based and direct URL-based operations.
 */
public class PtzManager {
    private static final Logger log = LoggerFactory.getLogger(PtzManager.class);

	private final OnvifManager onvifManager;
	private PtzExecutor ptzExecutor;
	private final ConcurrentMap<String, PtzConfigurationOptions> configurationOptionsCache = new ConcurrentHashMap<>();

	public PtzManager() {
		this(new OnvifManager());
	}

	public PtzManager(OnvifManager onvifManager) {
		this(onvifManager, new PtzExecutor());
	}

	PtzManager(OnvifManager onvifManager, PtzExecutor ptzExecutor) {
		this.onvifManager = onvifManager;
		this.ptzExecutor = ptzExecutor;
	}

	 /* -------------------- PTZ operations -------------------- */
	public CompletableFuture<String> move(OnvifDevice onvifDevice, PtzType ptzType) {
		if (ptzType == null) return failedFuture(new OnvifValidationException("ptzType must not be null."));
		return resolveXaddr(onvifDevice).thenCombine(resolveProfile(onvifDevice), (xaddr, profile) ->
				new PtzMoveRequest(xaddr, profile.getToken(), onvifDevice.getUsername(), onvifDevice.getPassword(), ptzType, onvifDevice.getTimeOffsetMs()))
				.thenCompose(ptzExecutor::sendRequest);
	}

	public CompletableFuture<String> stop(OnvifDevice onvifDevice) {
		return resolveXaddr(onvifDevice).thenCombine(resolveProfile(onvifDevice), (xaddr, profile) ->
				new PtzStopRequest(xaddr, profile.getToken(), onvifDevice.getUsername(), onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs()))
				.thenCompose(ptzExecutor::sendRequest);
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
		if (presetCommand == null) return failedFuture(new OnvifValidationException("presetCommand must not be null."));
		return resolveXaddr(onvifDevice).thenCombine(resolveProfile(onvifDevice), (xaddr, profile) ->
				new PtzPresetRequest(xaddr, profile.getToken(), onvifDevice.getUsername(), onvifDevice.getPassword(), presetCommand, onvifDevice.getTimeOffsetMs()))
				.thenCompose(ptzExecutor::sendRequest);
	}

	public CompletableFuture<String> preset(String xaddr, String profileToken, String userName, String password, PresetCommand presetCommand) {
		PtzPresetRequest request = new PtzPresetRequest(xaddr, profileToken, userName, password, presetCommand, 0);
		return ptzExecutor.sendRequest(request);
	}

	public CompletableFuture<String> getStatus(OnvifDevice onvifDevice) {
		return resolveXaddr(onvifDevice).thenCombine(resolveProfile(onvifDevice), (xaddr, profile) ->
				new PtzStatusRequest(xaddr, profile.getToken(), onvifDevice.getUsername(), onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs()))
				.thenCompose(ptzExecutor::sendRequest);
	}

	public CompletableFuture<String> getStatus(String xaddr, String profileToken, String userName, String password) {
		PtzStatusRequest request = new PtzStatusRequest(xaddr, profileToken, userName, password, 0);
		return ptzExecutor.sendRequest(request);
	}

	public CompletableFuture<PtzPosition> getPosition(OnvifDevice onvifDevice) {
		return resolveXaddr(onvifDevice).thenCombine(resolveProfile(onvifDevice), (xaddr, profile) ->
				new PtzPositionRequest(xaddr, profile.getToken(), onvifDevice.getUsername(),
						onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs()))
				.thenCompose(ptzExecutor::sendRequest);
	}

	public CompletableFuture<PtzConfigurationOptions> getConfigurationOptions(OnvifDevice onvifDevice) {
		return resolveProfile(onvifDevice).thenCompose(profile -> {
			String configurationToken = profile.getPtzConfigurationToken();
			if (configurationToken == null || configurationToken.isEmpty()) {
				throw new OnvifConfigurationException("The active media profile has no PTZ configuration token.");
			}
			String cacheKey = onvifDevice.getHostName() + "|" + configurationToken;
			PtzConfigurationOptions cached = configurationOptionsCache.get(cacheKey);
			if (cached != null) {
				log.debug("Using cached PTZ configuration options for token {}", configurationToken);
				return CompletableFuture.completedFuture(cached);
			}
			log.debug("Loading PTZ configuration options for token {}", configurationToken);
			return resolveXaddr(onvifDevice).thenCompose(xaddr -> {
				PtzConfigurationOptionsRequest request = new PtzConfigurationOptionsRequest(xaddr, configurationToken,
						onvifDevice.getUsername(), onvifDevice.getPassword(), onvifDevice.getTimeOffsetMs());
				return ptzExecutor.<PtzConfigurationOptions>sendRequest(request).thenApply(options -> {
					configurationOptionsCache.put(cacheKey, options);
					return options;
				});
			});
		});
	}

	public CompletableFuture<String> absoluteMove(OnvifDevice onvifDevice, PtzPosition position, PtzSpeed speed) {
		if (position == null) {
			CompletableFuture<String> future = new CompletableFuture<>();
			future.completeExceptionally(new OnvifValidationException("position must not be null."));
			return future;
		}
		return getConfigurationOptions(onvifDevice).thenCompose(options -> {
			options.validate(position, speed);
			return resolveXaddr(onvifDevice).thenCombine(resolveProfile(onvifDevice), (xaddr, profile) ->
					new PtzAbsoluteMoveRequest(xaddr, profile.getToken(), onvifDevice.getUsername(),
							onvifDevice.getPassword(), position, speed, options, onvifDevice.getTimeOffsetMs()))
					.thenCompose(ptzExecutor::sendRequest);
		});
	}

	/* -------------------- Utility methods -------------------- */
	private CompletableFuture<OnvifMediaProfile> resolveProfile(OnvifDevice onvifDevice) {
		List<OnvifMediaProfile> cachedProfiles = onvifDevice.getMediaProfiles();
		if (cachedProfiles != null && !cachedProfiles.isEmpty()) {
			return CompletableFuture.completedFuture(cachedProfiles.get(0));
		}
		return onvifManager.getMediaProfiles(onvifDevice).thenApply(profiles -> {
			if (profiles == null || profiles.isEmpty()) {
				throw new OnvifConfigurationException("No media profiles found.");
			}
			onvifDevice.setMediaProfiles(profiles);
			return profiles.get(0);
		});
	}

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
			throw new OnvifConfigurationException("No valid address or hostname found for PTZ request.");
		});
	}

	private static <T> CompletableFuture<T> failedFuture(Throwable error) {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(error);
		return future;
	}

	public void destroy() {
		configurationOptionsCache.clear();
	    if (ptzExecutor != null) {
	        ptzExecutor.destroy();
	        ptzExecutor = null;
	    }
	}
}
