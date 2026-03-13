package io.github.hyeonmo.managers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.hyeonmo.core.ImagingExecutor;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.imaging.ImagingSettings;
import io.github.hyeonmo.requests.imaging.GetImagingSettingsRequest;
import io.github.hyeonmo.requests.imaging.ImagingFocusRequest;
import io.github.hyeonmo.requests.imaging.ImagingFocusStopRequest;
import io.github.hyeonmo.requests.imaging.ImagingRequest;
import io.github.hyeonmo.requests.imaging.ImagingSettingRequest;

/**
 * Manages Imaging settings and Focus commands for ONVIF devices asynchronously.
 */
public class ImagingManager {

    public final static String TAG = ImagingManager.class.getSimpleName();

    private ImagingExecutor executor;
    private OnvifManager onvifManager;

    public ImagingManager() {
        this(new OnvifManager());
    }

    public ImagingManager(OnvifManager onvifManager) {
        this.onvifManager = onvifManager;
        this.executor = new ImagingExecutor();
    }

    public CompletableFuture<ImagingSettings> getImagingSettings(OnvifDevice device) {
        return getToken(device).thenCompose(tokenData -> {
            ImagingRequest request = new GetImagingSettingsRequest(tokenData.token, tokenData.xaddr);
            return executor.sendRequest(device, request);
        });
    }

    public CompletableFuture<ImagingSettings> getImagingSettings(String videoSourceToken, String imagingXaddr, String userName, String password) {
        ImagingRequest request = new GetImagingSettingsRequest(videoSourceToken, imagingXaddr);
        return executor.sendRequestUser(userName, password, request);
    }

    public CompletableFuture<String> focusContinuousMove(OnvifDevice device, double focus) {
        return getToken(device).thenCompose(tokenData -> {
            ImagingRequest request = new ImagingFocusRequest(tokenData.token, tokenData.xaddr, focus);
            return executor.sendRequest(device, request);
        });
    }

    public CompletableFuture<String> focusContinuousMove(String videoSourceToken, String imagingXaddr, String userName, String password, double focus) {
        ImagingRequest request = new ImagingFocusRequest(videoSourceToken, imagingXaddr, focus);
        return executor.sendRequestUser(userName, password, request);
    }

    public CompletableFuture<String> focusStop(OnvifDevice device) {
        return getToken(device).thenCompose(tokenData -> {
            ImagingRequest request = new ImagingFocusStopRequest(tokenData.token, tokenData.xaddr);
            return executor.sendRequest(device, request);
        });
    }

    public CompletableFuture<String> focusStop(String videoSourceToken, String imagingXaddr, String userName, String password) {
        ImagingRequest request = new ImagingFocusStopRequest(videoSourceToken, imagingXaddr);
        return executor.sendRequestUser(userName, password, request);
    }

    public CompletableFuture<String> setImagingSettings(OnvifDevice device, ImagingSettings imagingSettings) {
        return getToken(device).thenCompose(tokenData -> {
            ImagingRequest request = new ImagingSettingRequest(tokenData.token, tokenData.xaddr, imagingSettings);
            return executor.sendRequest(device, request);
        });
    }

    public CompletableFuture<String> setImagingSettings(String videoSourceToken, String imagingXaddr, String userName, String password, ImagingSettings imagingSettings) {
        ImagingRequest request = new ImagingSettingRequest(videoSourceToken, imagingXaddr, imagingSettings);
        return executor.sendRequestUser(userName, password, request);
    }

    public void destroy() {
        if (executor != null) {
            executor.destroy();
            executor = null;
        }
    }

    private CompletableFuture<TokenData> getToken(OnvifDevice device) {
        List<OnvifMediaProfile> cachedProfiles = device.getMediaProfiles();
        io.github.hyeonmo.models.OnvifCapabilities cachedCaps = device.getCapabilities();

        if (cachedProfiles != null && !cachedProfiles.isEmpty() && cachedCaps != null && cachedCaps.getImagingXaddr() != null) {
            return CompletableFuture.completedFuture(new TokenData(cachedProfiles.get(0).getVideoSourceToken(), cachedCaps.getImagingXaddr()));
        }

        CompletableFuture<String> tokenFuture = (cachedProfiles != null && !cachedProfiles.isEmpty()) 
            ? CompletableFuture.completedFuture(cachedProfiles.get(0).getVideoSourceToken())
            : onvifManager.getMediaProfiles(device)
                .thenApply(profiles -> {
                    if (profiles == null || profiles.isEmpty()) {
                        throw new RuntimeException("No profiles found to get video source token");
                    }
                    return profiles.get(0).getVideoSourceToken();
                });

        CompletableFuture<String> xaddrFuture = (cachedCaps != null && cachedCaps.getImagingXaddr() != null)
            ? CompletableFuture.completedFuture(cachedCaps.getImagingXaddr())
            : onvifManager.getCapabilities(device)
                .thenApply(caps -> {
                    if(caps == null || caps.getImagingXaddr() == null) {
                        throw new RuntimeException("Imaging XAddr is not supported or null");
                    }
                    return caps.getImagingXaddr();
                });

        return tokenFuture.thenCombine(xaddrFuture, TokenData::new);
    }

    private static class TokenData {
        final String token;
        final String xaddr;
        TokenData(String token, String xaddr) {
            this.token = token;
            this.xaddr = xaddr;
        }
    }
}
