package io.github.hyeonmo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.listeners.ImagingResponseListener;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.parsers.GetImagingSettingsParser;
import io.github.hyeonmo.parsers.ImagingFocusParser;
import io.github.hyeonmo.requests.GetImagingSettingsRequest;
import io.github.hyeonmo.requests.ImagingFocusRequest;
import io.github.hyeonmo.requests.ImagingFocusStopRequest;
import io.github.hyeonmo.requests.ImagingRequest;
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

    //Constants
    public static final String TAG = ImagingExecutor.class.getSimpleName();

    //Attributes
    private OkHttpClient client;
    private MediaType reqBodyType;

    private ImagingResponseListener imagingResponseListener;

    //Constructors

    ImagingExecutor(ImagingResponseListener imagingResponseListener){
    	this.imagingResponseListener = imagingResponseListener;

        client = new OkHttpClient.Builder()
        		.connectTimeout(30, TimeUnit.SECONDS)
        		.writeTimeout(15, TimeUnit.SECONDS)
        		.readTimeout(30, TimeUnit.SECONDS)
                .build();

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

    //Properties

    private void xmlRequest(OnvifDevice onvifDevice, ImagingRequest request, Request xml) {
    	if(xml == null)
    		return;

    	client.newCall(xml)
    			.enqueue(new Callback() {

    				@Override
                    public void onResponse(Call call, Response xmlResponse) throws IOException {
    					ImagingResponse response = new ImagingResponse(request);
                        ResponseBody xmlBody = xmlResponse.body();

                        if(xmlResponse.code() == 200 && xmlBody != null) {
                        	response.setSuccess(true);
                        	response.setXml(xmlBody.string());

                        	parseResponse(onvifDevice, response);
                        	return;
                        }

                        String errorMessage = "";
                        if(xmlBody != null)
                        	errorMessage = xmlBody.string();

                        imagingResponseListener.onError(onvifDevice, xmlResponse.code(), errorMessage);
    				}

    				@Override
					public void onFailure(Call call, IOException e) {
						imagingResponseListener.onError(onvifDevice, -1, e.getMessage());
					}

    			});
    }

    private void parseResponse(OnvifDevice device, ImagingResponse response) {
    	switch(response.request().getImagingType()) {
    		case GET_IMAGING_SETTINGS:
    			((GetImagingSettingsRequest) response.request()).getListener().onResponse(device,
    					new GetImagingSettingsParser().parse(response));
    			break;
    		case FOCUS_MOVE:
    			((ImagingFocusRequest) response.request()).getListener().onResponse(new ImagingFocusParser().parser(response));
    			break;
    		case FOCUS_STOP:
    			((ImagingFocusStopRequest) response.request()).getListener().onResponse(new ImagingFocusParser().parser(response));
    			break;
    		default:
    			imagingResponseListener.onResponse(device, response);
    			break;
    	}
    }

    private Request buildOnvifRequest(ImagingRequest request, RequestBody reqBody) {
        return new Request.Builder()
                .url(request.getXAddr())
                .addHeader("Content-Type", "text/xml; charset=utf-8")
                .post(reqBody)
                .build();
    }

}
