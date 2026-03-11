package io.github.hyeonmo;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.exceptions.OnvifExceptionFactory;
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
 * Executes Imaging requests synchronously via CompletableFuture. 
 * Created by Hyeonmo Gu on 24/09/2025.
 * Modified by Hyeonmo Gu for v2.0
 */
public class ImagingExecutor {

	public static final String TAG = ImagingExecutor.class.getSimpleName();

	private OkHttpClient client;
	private MediaType reqBodyType;

	public ImagingExecutor() {
		client = new OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(15, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build();

		reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8;");
	}

	public <T> CompletableFuture<T> sendRequest(OnvifDevice onvifDevice, ImagingRequest request) {
		AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
		RequestBody reqBody = RequestBody.create(reqBodyType, builder.getAuthHeader() + request.getXml() + builder.getAuthEnd());
		return xmlRequest(request, buildOnvifRequest(request, reqBody));
	}

	public <T> CompletableFuture<T> sendRequestUser(String userName, String password, ImagingRequest request) {
		AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);
		RequestBody reqBody = RequestBody.create(reqBodyType, builder.getAuthHeader() + request.getXml() + builder.getAuthEnd());
		return xmlRequest(request, buildOnvifRequest(request, reqBody));
	}

	void destroy() {
		if (client != null) {
			client.dispatcher().executorService().shutdown();
			client.connectionPool().evictAll();
			client = null;
		}
	}

	private <T> CompletableFuture<T> xmlRequest(ImagingRequest request, Request xml) {
		CompletableFuture<T> future = new CompletableFuture<>();
		if (xml == null) {
			future.completeExceptionally(new IllegalArgumentException("Request is null"));
			return future;
		}

		client.newCall(xml).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, Response xmlResponse) throws IOException {
				ImagingResponse response = new ImagingResponse(request);
				ResponseBody xmlBody = xmlResponse.body();

				if (xmlResponse.code() == 200 && xmlBody != null) {
					response.setSuccess(true);
					response.setXml(xmlBody.string());
					try {
						T result = parseResponseAsFutureResult(response);
						future.complete(result);
					} catch (Exception e) {
						future.completeExceptionally(e);
					}
					return;
				}

				String errorMessage = xmlBody != null ? xmlBody.string() : "";
				future.completeExceptionally(OnvifExceptionFactory.fromHttpError(xmlResponse.code(), errorMessage));
			}

			@Override
			public void onFailure(Call call, IOException e) {
				future.completeExceptionally(OnvifExceptionFactory.fromHttpError(-1, e.getMessage()));
			}
		});
		return future;
	}

	@SuppressWarnings("unchecked")
	private <T> T parseResponseAsFutureResult(ImagingResponse response) {
	    switch (response.request().getImagingType()) {
	        case GET_IMAGING_SETTINGS:
	            return (T) new GetImagingSettingsParser().parse(response);
	        case FOCUS_MOVE:
	        case FOCUS_STOP:
	            return (T) new ImagingFocusParser().parser(response);
	        case SET_SETTINGS:
	        	return (T) new ImagingSettingRequestParser().parser(response);
	        default:
	            return (T) response;
	    }
	}

	private Request buildOnvifRequest(ImagingRequest request, RequestBody reqBody) {
		return new Request.Builder().url(request.getXAddr()).addHeader("Content-Type", "application/soap+xml; charset=utf-8").post(reqBody).build();
	}
}
