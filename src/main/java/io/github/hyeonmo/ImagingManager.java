package io.github.hyeonmo;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.github.hyeonmo.listeners.ImagingFocusResponseListener;
import io.github.hyeonmo.listeners.ImagingResponseListener;
import io.github.hyeonmo.listeners.ImagingSettingsListener;
import io.github.hyeonmo.listeners.OnvifCapabilitiesListener;
import io.github.hyeonmo.listeners.OnvifMediaProfilesListener;
import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.requests.GetImagingSettingsRequest;
import io.github.hyeonmo.requests.ImagingFocusRequest;
import io.github.hyeonmo.requests.ImagingFocusStopRequest;
import io.github.hyeonmo.requests.ImagingRequest;
import io.github.hyeonmo.responses.ImagingResponse;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingManager implements ImagingResponseListener{

    //Constants
    public final static String TAG = ImagingManager.class.getSimpleName();

    private static final ExecutorService TOKEN_EXECUTOR = Executors.newCachedThreadPool();

    //Attributes
    private ImagingExecutor executor;
	private ImagingResponseListener imagingResponseListener;

	//Constructors
	public ImagingManager() {
		this(null);
	}

	public ImagingManager(ImagingResponseListener imagingResponseListener) {
		this.imagingResponseListener = imagingResponseListener;
		executor = new ImagingExecutor(this);
	}

	//Methods
	public void getImagingSettings(OnvifDevice device, ImagingSettingsListener listener) {
	    getToken(device, (token, xaddr) -> {
	        ImagingRequest request = new GetImagingSettingsRequest(listener ,token, xaddr);
	        executor.sendRequest(device, request);
	    });
	}

	public void getImagingSettings(String videoSourceToken, String imagingXaddr, String userName, String password, ImagingSettingsListener listener) {
		ImagingRequest request = new GetImagingSettingsRequest(listener, videoSourceToken, imagingXaddr);
		executor.sendRequestUser(userName, password, request);
	}

	public void focusContinuousMove(OnvifDevice device, double focus, ImagingFocusResponseListener listener) {
		getToken(device, (token, xaddr) -> {
			ImagingRequest request = new ImagingFocusRequest(listener, token, xaddr, focus);
			executor.sendRequest(device, request);
		});
	}

	public void focusContinuousMove(String videoSourceToken, String imagingXaddr, String userName, String password, double focus, ImagingFocusResponseListener listener) {
		ImagingRequest request = new ImagingFocusRequest(listener, videoSourceToken, imagingXaddr, focus);
		executor.sendRequestUser(userName, password, request);
	}

	public void focusStop(OnvifDevice device, ImagingFocusResponseListener listener) {
		getToken(device, (token, xaddr) -> {
			ImagingRequest request = new ImagingFocusStopRequest(listener, token, xaddr);
			executor.sendRequest(device, request);
		});
	}

	public void focusStop(String videoSourceToken, String imagingXaddr, String userName, String password, ImagingFocusResponseListener listener) {
		ImagingRequest request = new ImagingFocusStopRequest(listener, videoSourceToken, imagingXaddr);
		executor.sendRequestUser(userName, password, request);
	}

    /**
     * Clear up the resources.
     */
    public void destroy() {
    	imagingResponseListener = null;

        if (executor != null) {
            executor.destroy();
            executor = null;
        }

        TOKEN_EXECUTOR.shutdown();
        try {
            if (!TOKEN_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                TOKEN_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            TOKEN_EXECUTOR.shutdownNow();
        }
    }

	protected void getToken(OnvifDevice device, TokenCallback callback) {
	    OnvifManager om = new OnvifManager();

	    final AtomicReference<String> token = new AtomicReference<>();
	    final AtomicReference<String> xaddr = new AtomicReference<>();
	    CountDownLatch latch = new CountDownLatch(2);

	    om.getMediaProfiles(device, new OnvifMediaProfilesListener() {
	        @Override
	        public void onMediaProfilesReceived(OnvifDevice dev, List<OnvifMediaProfile> profiles) {
	            token.set(profiles.get(0).getVideoSourceToken());
	            latch.countDown();
	        }
	    });

	    om.getCapabilities(device, new OnvifCapabilitiesListener() {
	        @Override
	        public void onDeviceCapabilitiesReceived(OnvifDevice dev, OnvifCapabilities caps) {
	            xaddr.set(caps.getImagingXaddr());
	            latch.countDown();
	        }
	    });

	    TOKEN_EXECUTOR.submit(() -> {
	        try {
	            if (!latch.await(5, TimeUnit.SECONDS) || token.get() == null || xaddr.get() == null) {
	                String message = "Token or XAddr acquisition failed within timeout";
	                onError(device, -2, message);
	            } else {
	                callback.onReady(token.get(), xaddr.get());
	            }
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            onError(device, -3, "Token acquisition interrupted: " + e.getMessage());
	        }
	    });
	}

	protected interface TokenCallback {
	    void onReady(String token, String xaddr);
	}

	@Override
	public void onResponse(OnvifDevice onvifDevice, ImagingResponse response) {
		if(imagingResponseListener != null) {
			imagingResponseListener.onResponse(onvifDevice, response);
		}

	}

	@Override
	public void onError(OnvifDevice onvifDevice, int errorCode, String errorMessage) {
		if(imagingResponseListener != null) {
			imagingResponseListener.onError(onvifDevice, errorCode, errorMessage);
		}

	}
}
