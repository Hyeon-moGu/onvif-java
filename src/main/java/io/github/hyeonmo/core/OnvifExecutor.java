package io.github.hyeonmo.core;

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
import io.github.hyeonmo.parsers.device.GetCapabilitiesParser;
import io.github.hyeonmo.parsers.device.GetDeviceInformationParser;
import io.github.hyeonmo.parsers.device.GetServicesParser;
import io.github.hyeonmo.parsers.events.EventSubscriptionStatusParser;
import io.github.hyeonmo.parsers.media.GetMediaProfilesParser;
import io.github.hyeonmo.parsers.media.GetMediaStreamParser;
import io.github.hyeonmo.parsers.media.GetSnapshotParser;
import io.github.hyeonmo.parsers.device.GetSystemDateAndTimeParser;
import io.github.hyeonmo.parsers.events.CreatePullPointSubscriptionParser;
import io.github.hyeonmo.parsers.events.PullMessagesParser;
import io.github.hyeonmo.requests.OnvifRequest;
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
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .build();

        reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8");
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

    public <T> CompletableFuture<T> sendRequest(OnvifDevice device, String fullUrl, OnvifRequest request) {
        return sendRequest(device, fullUrl, "", request);
    }

    public <T> CompletableFuture<T> sendRequest(OnvifDevice device, String fullUrl, String additionalHeaderXml, OnvifRequest request) {
        return sendRequest(device.getHostName(), null, device.getUsername(), device.getPassword(), device.getTimeOffsetMs(), fullUrl, true, additionalHeaderXml, request);
    }
    
    public <T> CompletableFuture<T> sendRequest(String hostName, String baseUrl, String username, String password, long timeOffsetMs, String path, OnvifRequest request) {
        return sendRequest(hostName, baseUrl, username, password, timeOffsetMs, path, false, "", request);
    }

    public <T> CompletableFuture<T> sendRequest(String hostName, String baseUrl, String username, String password, long timeOffsetMs, String pathOrUrl, boolean isFullUrl, String additionalHeaderXml, OnvifRequest request) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        String url;
        if (isFullUrl) {
            url = pathOrUrl;
        } else {
            url = (baseUrl == null || baseUrl.isEmpty()) ? hostName + pathOrUrl : baseUrl + pathOrUrl;
        }

        AuthXMLBuilder builder = new AuthXMLBuilder(username, password, timeOffsetMs);
        String action = request.getAction();
        if (action != null && !action.isEmpty()) {
            builder.setWsaAction(action);
            builder.setWsaTo(url);
        }
        if (additionalHeaderXml != null && !additionalHeaderXml.isEmpty()) {
            builder.setAdditionalHeaderXml(additionalHeaderXml);
        }

        String xmlPayload = builder.getAuthHeader() + request.getXml() + builder.getAuthEnd();
        RequestBody reqBody = RequestBody.create(reqBodyType, xmlPayload);

        Request xmlRequest = new Request.Builder()
                .url(url)
                .post(reqBody)
                .build();

        OkHttpClient client = getClientForDevice(hostName, username, password);

        client.newCall(xmlRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response xmlResponse) throws IOException {
                OnvifResponse<?> response = new OnvifResponse<>(request);
                ResponseBody xmlBody = xmlResponse.body();
                String responseBody = xmlBody != null ? xmlBody.string() : "";

                if (xmlResponse.isSuccessful()) {
                    response.setSuccess(true);
                    response.setXml(responseBody);
                    try {
                    	T result = parseResponseAsFutureResult(response);
                        future.complete(result);
                    } catch (Exception e) {
                    	future.completeExceptionally(e);
                    }
                } else {
                    future.completeExceptionally(OnvifExceptionFactory.fromHttpError(xmlResponse.code(), responseBody));
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
	private <T> T parseResponseAsFutureResult(OnvifResponse<?> response) {
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
            case CREATE_PULLPOINT_SUBSCRIPTION:
                return (T) new CreatePullPointSubscriptionParser().parse(response);
            case PULL_MESSAGES:
                return (T) new PullMessagesParser().parse(response);
            case RENEW:
                return (T) new EventSubscriptionStatusParser().parse(response);
            case UNSUBSCRIBE:
                return null;
            default:
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
            case GET_PTZ:
                return device.getPath().getPtzPath();
            case GET_IMAGING:
                return device.getPath().getImagingPath();
            case GET_EVENTS:
            case CREATE_PULLPOINT_SUBSCRIPTION:
            case PULL_MESSAGES:
            case RENEW:
            case UNSUBSCRIBE:
                return device.getPath().getEventsPath();
            default:
                return device.getPath().getServicesPath();
        }
    }

    public void clear() {
        deviceClients.clear();
    }
}
