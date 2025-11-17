package io.github.hyeonmo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.listeners.imaging.ImagingFocusResponseListener;
import io.github.hyeonmo.listeners.imaging.ImagingResponseListener;
import io.github.hyeonmo.listeners.imaging.ImagingSettingRequestListener;
import io.github.hyeonmo.listeners.imaging.ImagingSettingsListener;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.parsers.imaging.GetImagingSettingsParser;
import io.github.hyeonmo.parsers.imaging.ImagingFocusParser;
import io.github.hyeonmo.parsers.imaging.ImagingSettingRequestParser;
import io.github.hyeonmo.requests.imaging.GetImagingSettingsRequest;
import io.github.hyeonmo.requests.imaging.ImagingFocusRequest;
import io.github.hyeonmo.requests.imaging.ImagingFocusStopRequest;
import io.github.hyeonmo.requests.imaging.ImagingRequest;
import io.github.hyeonmo.requests.imaging.ImagingSettingRequest;
import io.github.hyeonmo.responses.ImagingResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingExecutor {

	// Constants
	public static final String TAG = ImagingExecutor.class.getSimpleName();

	// Attributes
	private OkHttpClient client;
	private MediaType reqBodyType;

	private ImagingResponseListener imagingResponseListener;

	// Constructors

	ImagingExecutor(ImagingResponseListener imagingResponseListener) {
		this.imagingResponseListener = imagingResponseListener;

		client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

		reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8;");
	}

	/**
	 * Sends a request to the Onvif-compatible device.
	 *
	 * @param device
	 * @param request
	 */
	void sendRequest(OnvifDevice onvifDevice, ImagingRequest request) {
		AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
		RequestBody reqBody = RequestBody.create(reqBodyType, builder.getAuthHeader() + request.getXml() + builder.getAuthEnd());
		xmlRequest(onvifDevice, request, buildOnvifRequest(request, reqBody));
	}

	void sendRequestUser(String userName, String password, ImagingRequest request) {
		AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);
		RequestBody reqBody = RequestBody.create(reqBodyType, builder.getAuthHeader() + request.getXml() + builder.getAuthEnd());
		xmlRequest(null, request, buildOnvifRequest(request, reqBody));
	}

	/**
	 * Clears up the resources.
	 */
	void destroy() {
		imagingResponseListener = null;

		if (client != null) {
			client.dispatcher().executorService().shutdown();
			client.connectionPool().evictAll();

			client = null;
		}
	}

	// Properties
	private void xmlRequest(OnvifDevice onvifDevice, ImagingRequest request, Request xml) {
		if (xml == null) return;

		client.newCall(xml).enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response xmlResponse) throws IOException {
				ImagingResponse response = new ImagingResponse(request);
				ResponseBody xmlBody = xmlResponse.body();

				if (xmlResponse.code() == 200 && xmlBody != null) {
					response.setSuccess(true);
					response.setXml(xmlBody.string());
					parseResponse(onvifDevice, response, true, 200, null);
					return;
				}

				String errorMessage = "";
				if (xmlBody != null) errorMessage = xmlBody.string();

				parseResponse(onvifDevice, response, false, xmlResponse.code(), errorMessage);
			}

			@Override
			public void onFailure(Call call, IOException e) {
				ImagingResponse response = new ImagingResponse(request);
				parseResponse(onvifDevice, response, false, -1, e.getMessage());
			}

		});
	}

	private void parseResponse(OnvifDevice device, ImagingResponse response, boolean success, int errorCode, String errorMessage) {
	    switch (response.request().getImagingType()) {
	        case GET_IMAGING_SETTINGS:
	            if (response.request() instanceof GetImagingSettingsRequest) {
	                ImagingSettingsListener listener = ((GetImagingSettingsRequest) response.request()).getListener();
	                if (success) {
	                    listener.onResponse(device, new GetImagingSettingsParser().parse(response));
	                } else {
	                    listener.onError(device, errorCode, errorMessage);
	                }
	            }
	            break;

	        case FOCUS_MOVE:
	            if (response.request() instanceof ImagingFocusRequest) {
	                ImagingFocusResponseListener listener = ((ImagingFocusRequest) response.request()).getListener();
	                if (success) {
	                    listener.onResponse(new ImagingFocusParser().parser(response));
	                } else {
	                    listener.onError(errorCode, errorMessage);
	                }
	            }
	            break;

	        case FOCUS_STOP:
	            if (response.request() instanceof ImagingFocusStopRequest) {
	                ImagingFocusResponseListener listener = ((ImagingFocusStopRequest) response.request()).getListener();
	                if (success) {
	                    listener.onResponse(new ImagingFocusParser().parser(response));
	                } else {
	                    listener.onError(errorCode, errorMessage);
	                }
	            }
	            break;

	        case SET_SETTINGS:
	        	if (response.request() instanceof ImagingSettingRequest) {
	        		ImagingSettingRequestListener listener = ((ImagingSettingRequest) response.request()).getListener();
	        		if(success) {
	        			listener.onResponse(new ImagingSettingRequestParser().parser(response));
	        		} else {
	        			listener.onError(errorCode, errorMessage);
	        		}
	        	}
	        	break;

	        default:
	            if (success) {
	                imagingResponseListener.onResponse(device, response);
	            } else {
	                imagingResponseListener.onError(device, errorCode, errorMessage);
	            }
	            break;
	    }
	}

	private Request buildOnvifRequest(ImagingRequest request, RequestBody reqBody) {
		return new Request.Builder().url(request.getXAddr()).addHeader("Content-Type", "application/soap+xml; charset=utf-8").post(reqBody).build();
	}
}
