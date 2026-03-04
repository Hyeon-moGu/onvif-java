package io.github.hyeonmo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import io.github.hyeonmo.listeners.OnvifResponseListener;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifServices;
import io.github.hyeonmo.parsers.device.GetCapabilitiesParser;
import io.github.hyeonmo.parsers.device.GetDeviceInformationParser;
import io.github.hyeonmo.parsers.device.GetServicesParser;
import io.github.hyeonmo.parsers.media.GetMediaProfilesParser;
import io.github.hyeonmo.parsers.media.GetMediaStreamParser;
import io.github.hyeonmo.parsers.media.GetSnapshotParser;
import io.github.hyeonmo.requests.OnvifRequest;
import io.github.hyeonmo.requests.device.GetCapabilitiesRequest;
import io.github.hyeonmo.requests.device.GetDeviceInformationRequest;
import io.github.hyeonmo.requests.device.GetServicesRequest;
import io.github.hyeonmo.requests.media.GetMediaProfilesRequest;
import io.github.hyeonmo.requests.media.GetMediaStreamRequest;
import io.github.hyeonmo.requests.media.GetSnapshotRequest;
import io.github.hyeonmo.responses.OnvifResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Tomas Verhelst on 03/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 *
 * Modified by Hyeonmo Gu on 29/09/2025
 */
public class OnvifExecutor {

    //Constants
    public static final String TAG = OnvifExecutor.class.getSimpleName();

    //Attributes
    private OkHttpClient baseClient;
    private MediaType reqBodyType;

    // Cache to hold OkHttpClients customized for specific devices to maintain thread-safety
    private Map<String, OkHttpClient> deviceClients = new ConcurrentHashMap<>();

    private OnvifResponseListener onvifResponseListener;

    //Constructors

    OnvifExecutor(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;

        baseClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .writeTimeout(100000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .build();

        reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8;");
    }

    //Methods
    private OkHttpClient getClientForDevice(OnvifDevice device) {
        String key = device.getHostName() + ":" + device.getUsername() + ":" + device.getPassword();
        return deviceClients.computeIfAbsent(key, k -> {
            Credentials credentials = new Credentials(device.getUsername(), device.getPassword());
            DigestAuthenticator authenticator = new DigestAuthenticator(credentials);
            Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();

            return baseClient.newBuilder()
                    .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                    .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                    .build();
        });
    }

    /**
     * Sends a request to the Onvif-compatible device.
     *
     * @param device
     * @param request
     */
    void sendRequest(OnvifDevice device, OnvifRequest request) {
        AuthXMLBuilder builder = new AuthXMLBuilder(device.getUsername(), device.getPassword());
        RequestBody reqBody = RequestBody.create(reqBodyType, builder.getAuthHeader() + request.getXml() + builder.getAuthEnd());
        performXmlRequest(device, request, buildOnvifRequest(device, request, reqBody));
    }

    /**
     * Clears up the resources.
     */
    void clear() {
        onvifResponseListener = null;
        deviceClients.clear();
    }

    //Properties

    public void setOnvifResponseListener(OnvifResponseListener onvifResponseListener) {
        this.onvifResponseListener = onvifResponseListener;
    }

    private void performXmlRequest(OnvifDevice device, OnvifRequest request, Request xmlRequest) {
        if (xmlRequest == null)
            return;

        OkHttpClient client = getClientForDevice(device);

        client.newCall(xmlRequest)
                .enqueue(new Callback() {

                    @Override
                    public void onResponse(Call call, Response xmlResponse) throws IOException {

                        OnvifResponse response = new OnvifResponse(request);
                        ResponseBody xmlBody = xmlResponse.body();

                        if (xmlResponse.code() == 200 && xmlBody != null) {
                            response.setSuccess(true);
                            response.setXml(xmlBody.string());
                            parseResponse(device, response);
                            return;
                        }

                        String errorMessage = "";
                        if (xmlBody != null)
                            errorMessage = xmlBody.string();

                        onvifResponseListener.onError(device, xmlResponse.code(), errorMessage);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        onvifResponseListener.onError(device, -1, e.getMessage());
                    }

                });
    }

    private void parseResponse(OnvifDevice device, OnvifResponse response) {
        switch (response.request().getType()) {
            case GET_SERVICES:
                OnvifServices path = new GetServicesParser().parse(response);
                device.setPath(path);
                ((GetServicesRequest) response.request()).getListener().onServicesReceived(device, path);
                break;
            case GET_DEVICE_INFORMATION:
                ((GetDeviceInformationRequest) response.request()).getListener().onDeviceInformationReceived(device,
                        new GetDeviceInformationParser().parse(response));
                break;
            case GET_MEDIA_PROFILES:
                ((GetMediaProfilesRequest) response.request()).getListener().onMediaProfilesReceived(device,
                        new GetMediaProfilesParser().parse(response));
                break;
            case GET_STREAM_URI:
                GetMediaStreamRequest streamRequest = (GetMediaStreamRequest) response.request();
                streamRequest.getListener().onMediaStreamURIReceived(device, streamRequest.getMediaProfile(),
                        new GetMediaStreamParser().parse(response));
                break;
            case GET_CAPABILITIES:
            	((GetCapabilitiesRequest) response.request()).getListener().onDeviceCapabilitiesReceived(device,
            			new GetCapabilitiesParser().parse(response));
            	break;
            case GET_SNAPSHOT_URI:
            	GetSnapshotRequest snapshotRequest = (GetSnapshotRequest) response.request();
            	snapshotRequest.getListener().onMediaSnapshotReceived(device, snapshotRequest.getMediaProfile(),
            			new GetSnapshotParser().parse(response));
            	break;
            default:
                if(onvifResponseListener != null) {
                   onvifResponseListener.onResponse(device, response);
                }
                break;
        }
    }

    private Request buildOnvifRequest(OnvifDevice device, OnvifRequest request, RequestBody body) {
        return new Request.Builder()
                .url(getUrlForRequest(device, request))
                .addHeader("Content-Type", "text/xml; charset=utf-8")
                .post(body)
                .build();
    }

    private String getUrlForRequest(OnvifDevice device, OnvifRequest request) {
    	if(device.getBaseUrl() == null || device.getBaseUrl().isEmpty()) return device.getHostName() + getPathForRequest(device, request);
    	return device.getBaseUrl() + getPathForRequest(device, request);
    }

    private String getPathForRequest(OnvifDevice device, OnvifRequest request) {
        switch (request.getType()) {
            case GET_SERVICES:
                return device.getPath().getServicesPath();
            case GET_DEVICE_INFORMATION:
                return device.getPath().getDeviceInformationPath();
            case GET_MEDIA_PROFILES:
                return device.getPath().getProfilesPath();
            case GET_STREAM_URI:
                return device.getPath().getStreamURIPath();
            case GET_CAPABILITIES:
            	return device.getPath().getServicesPath();
            case GET_SNAPSHOT_URI:
            	return device.getPath().getServicesPath();
        }

        return device.getPath().getServicesPath();
    }

}
