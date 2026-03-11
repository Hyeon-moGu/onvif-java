package io.github.hyeonmo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;

import io.github.hyeonmo.exceptions.OnvifExceptionFactory;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifServices;
import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.models.OnvifDeviceInformation;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.imaging.ImagingSettings;
import io.github.hyeonmo.parsers.device.GetCapabilitiesParser;
import io.github.hyeonmo.parsers.device.GetDeviceInformationParser;
import io.github.hyeonmo.parsers.device.GetServicesParser;
import io.github.hyeonmo.parsers.media.GetMediaProfilesParser;
import io.github.hyeonmo.parsers.media.GetMediaStreamParser;
import io.github.hyeonmo.parsers.media.GetSnapshotParser;
import io.github.hyeonmo.parsers.device.GetSystemDateAndTimeParser;
import io.github.hyeonmo.parsers.imaging.GetImagingSettingsParser;
import io.github.hyeonmo.requests.OnvifRequest;
import io.github.hyeonmo.requests.device.GetCapabilitiesRequest;
import io.github.hyeonmo.requests.device.GetDeviceInformationRequest;
import io.github.hyeonmo.requests.device.GetServicesRequest;
import io.github.hyeonmo.requests.media.GetMediaProfilesRequest;
import io.github.hyeonmo.requests.media.GetMediaStreamRequest;
import io.github.hyeonmo.requests.media.GetSnapshotRequest;
import io.github.hyeonmo.requests.device.GetSystemDateAndTimeRequest;
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
 * Executes ONVIF SOAP Requests asynchronously, returning CompletableFutures.
 *
 * Modified by Hyeonmo Gu for v2.0
 */
public class OnvifExecutor {

    public static final String TAG = OnvifExecutor.class.getSimpleName();

    private OkHttpClient baseClient;
    private MediaType reqBodyType;
    private Map<String, OkHttpClient> deviceClients = new ConcurrentHashMap<>();

    public OnvifExecutor() {
        baseClient = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .writeTimeout(100000, TimeUnit.MILLISECONDS)
                .readTimeout(10000, TimeUnit.MILLISECONDS)
                .build();

        reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8;");
    }

    private OkHttpClient getClientForDevice(String hostName, String username, String password) {
        String key = hostName + ":" + username + ":" + password;
        return deviceClients.computeIfAbsent(key, k -> {
            Credentials credentials = new Credentials(username, password);
            DigestAuthenticator authenticator = new DigestAuthenticator(credentials);
            Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();

            return baseClient.newBuilder()
                    .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                    .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                    .build();
        });
    }

    public <T> CompletableFuture<T> sendRequest(OnvifDevice device, OnvifRequest request) {
    	return sendRequest(device.getHostName(), device.getBaseUrl(), device.getUsername(), device.getPassword(), device.getTimeOffsetMs(), getPathForRequest(device, request), request);
    }
    
    public <T> CompletableFuture<T> sendRequest(String hostName, String baseUrl, String username, String password, long timeOffsetMs, String path, OnvifRequest request) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        AuthXMLBuilder builder = new AuthXMLBuilder(username, password, timeOffsetMs);
        RequestBody reqBody = RequestBody.create(reqBodyType, builder.getAuthHeader() + request.getXml() + builder.getAuthEnd());
        
        String url = (baseUrl == null || baseUrl.isEmpty()) ? hostName + path : baseUrl + path;
        
        Request xmlRequest = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "text/xml; charset=utf-8")
                .post(reqBody)
                .build();

        OkHttpClient client = getClientForDevice(hostName, username, password);

        client.newCall(xmlRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response xmlResponse) throws IOException {
                OnvifResponse response = new OnvifResponse(request);
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
                } else {
                    String errorMessage = xmlBody != null ? xmlBody.string() : "";
                    future.completeExceptionally(OnvifExceptionFactory.fromHttpError(xmlResponse.code(), errorMessage));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(OnvifExceptionFactory.fromHttpError(-1, e.getMessage()));
            }
        });

        return future;
    }

    @SuppressWarnings("unchecked")
	private <T> T parseResponseAsFutureResult(OnvifResponse response) {
        switch (response.request().getType()) {
            case GET_SERVICES:
                return (T) new GetServicesParser().parse(response);
            case GET_DEVICE_INFORMATION:
                return (T) new GetDeviceInformationParser().parse(response);
            case GET_MEDIA_PROFILES:
                return (T) new GetMediaProfilesParser().parse(response);
            case GET_STREAM_URI:
                return (T) new GetMediaStreamParser().parse(response);
            case GET_CAPABILITIES:
                return (T) new GetCapabilitiesParser().parse(response);
            case GET_SNAPSHOT_URI:
                return (T) new GetSnapshotParser().parse(response);
            case GET_SYSTEM_DATE_AND_TIME:
                return (T) new GetSystemDateAndTimeParser().parse(response);
            default:
                // For requests that don't need dedicated parsers (like PTZ move/stop)
                return (T) response;
        }
    }

    private String getPathForRequest(OnvifDevice device, OnvifRequest request) {
        switch (request.getType()) {
            case GET_SERVICES:
            case GET_CAPABILITIES:
            case GET_SNAPSHOT_URI:
                return device.getPath().getServicesPath();
            case GET_DEVICE_INFORMATION:
            case GET_SYSTEM_DATE_AND_TIME:
                return device.getPath().getDeviceInformationPath();
            case GET_MEDIA_PROFILES:
                return device.getPath().getProfilesPath();
            case GET_STREAM_URI:
                return device.getPath().getStreamURIPath();
            default:
                return device.getPath().getServicesPath();
        }
    }

    public void clear() {
        deviceClients.clear();
    }
}
