package io.github.hyeonmo.requests.ptz;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.AuthXMLBuilder;
import io.github.hyeonmo.OnvifManager;
import io.github.hyeonmo.exceptions.OnvifExceptionFactory;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.ptz.PresetCommand;
import io.github.hyeonmo.models.ptz.PtzType;
import io.github.hyeonmo.parsers.ptz.PtzParser;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 * Modified for v2.0 - CompletableFuture based, callbacks removed.
 */
public class PtzRequest {

    private static int PTZ_REQUEST_TIMEOUT = 5000;
    private int ptzRequestTimeout = PTZ_REQUEST_TIMEOUT;

    private final PtzParser ptzParser;
    private final OnvifManager onvifManager;
    private OkHttpClient httpClient;

    private static final MediaType SOAP_MEDIA_TYPE = MediaType.parse("application/soap+xml; charset=utf-8");
    private static final int ERROR_CODE_PROFILE = -100;

    public PtzRequest(OnvifManager onvifManager) {
        this.onvifManager = onvifManager;
        this.ptzParser = new PtzParser();
        this.httpClient = buildHttpClient();
    }

    public CompletableFuture<String> move(OnvifDevice onvifDevice, PtzType ptzType) {
        String xaddr = onvifDevice.getAddresses().get(0);
        return onvifManager.getMediaProfiles(onvifDevice)
            .thenCompose(profiles -> {
                if (profiles == null || profiles.isEmpty()) {
                    CompletableFuture<String> f = new CompletableFuture<>();
                    f.completeExceptionally(OnvifExceptionFactory.fromHttpError(ERROR_CODE_PROFILE, "No media profiles found."));
                    return f;
                }
                String profileToken = profiles.get(0).getToken();
                AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
                String soapXml = builder.getAuthHeader() + builder.getPtzMoveBody(profileToken, ptzType) + builder.getAuthEnd();
                return sendSoap(xaddr, soapXml, "move");
            });
    }

    public CompletableFuture<String> stop(OnvifDevice onvifDevice) {
        String xaddr = onvifDevice.getAddresses().get(0);
        return onvifManager.getMediaProfiles(onvifDevice)
            .thenCompose(profiles -> {
                if (profiles == null || profiles.isEmpty()) {
                    CompletableFuture<String> f = new CompletableFuture<>();
                    f.completeExceptionally(OnvifExceptionFactory.fromHttpError(ERROR_CODE_PROFILE, "No media profiles found."));
                    return f;
                }
                String profileToken = profiles.get(0).getToken();
                AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
                String soapXml = builder.getAuthHeader() + builder.getPtzStopBody(profileToken) + builder.getAuthEnd();
                return sendSoap(xaddr, soapXml, "stop");
            });
    }

    public CompletableFuture<String> move(String xaddr, String profileToken, String userName, String password, PtzType ptzType) {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);
        String soapXml = builder.getAuthHeader() + builder.getPtzMoveBody(profileToken, ptzType) + builder.getAuthEnd();
        return sendSoap(xaddr, soapXml, "move");
    }

    public CompletableFuture<String> stop(String xaddr, String profileToken, String userName, String password) {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);
        String soapXml = builder.getAuthHeader() + builder.getPtzStopBody(profileToken) + builder.getAuthEnd();
        return sendSoap(xaddr, soapXml, "stop");
    }

    public CompletableFuture<String> preset(OnvifDevice onvifDevice, PresetCommand presetCommand) {
        String xaddr = onvifDevice.getAddresses().get(0);
        return onvifManager.getMediaProfiles(onvifDevice)
            .thenCompose(profiles -> {
                if (profiles == null || profiles.isEmpty()) {
                    CompletableFuture<String> f = new CompletableFuture<>();
                    f.completeExceptionally(OnvifExceptionFactory.fromHttpError(ERROR_CODE_PROFILE, "No media profiles found."));
                    return f;
                }
                String profileToken = profiles.get(0).getToken();
                AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
                String soapXml = builder.getAuthHeader() + builder.getPresetBody(profileToken, presetCommand) + builder.getAuthEnd();
                return sendSoap(xaddr, soapXml, "preset");
            });
    }

    public CompletableFuture<String> preset(String xaddr, String profileToken, String userName, String password, PresetCommand presetCommand) {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);
        String soapXml = builder.getAuthHeader() + builder.getPresetBody(profileToken, presetCommand) + builder.getAuthEnd();
        return sendSoap(xaddr, soapXml, "preset");
    }

    public CompletableFuture<String> getStatus(OnvifDevice onvifDevice) {
        String xaddr = onvifDevice.getAddresses().get(0);
        return onvifManager.getMediaProfiles(onvifDevice)
            .thenCompose(profiles -> {
                if (profiles == null || profiles.isEmpty()) {
                    CompletableFuture<String> f = new CompletableFuture<>();
                    f.completeExceptionally(OnvifExceptionFactory.fromHttpError(ERROR_CODE_PROFILE, "No media profiles found."));
                    return f;
                }
                String profileToken = profiles.get(0).getToken();
                AuthXMLBuilder builder = new AuthXMLBuilder(onvifDevice.getUsername(), onvifDevice.getPassword());
                String soapXml = builder.getAuthHeader() + builder.getPtzStatusBody(profileToken) + builder.getAuthEnd();
                return sendSoap(xaddr, soapXml, "status");
            });
    }

    public CompletableFuture<String> getStatus(String xaddr, String profileToken, String userName, String password) {
        AuthXMLBuilder builder = new AuthXMLBuilder(userName, password);
        String soapXml = builder.getAuthHeader() + builder.getPtzStatusBody(profileToken) + builder.getAuthEnd();
        return sendSoap(xaddr, soapXml, "status");
    }

    private CompletableFuture<String> sendSoap(String urlStr, String soapXml, String type) {
        CompletableFuture<String> future = new CompletableFuture<>();
        RequestBody body = RequestBody.create(SOAP_MEDIA_TYPE, soapXml);
        Request request = new Request.Builder()
            .url(urlStr)
            .addHeader("Content-Type", SOAP_MEDIA_TYPE.toString())
            .post(body)
            .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(OnvifExceptionFactory.fromHttpError(-1, e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    future.completeExceptionally(OnvifExceptionFactory.fromHttpError(response.code(), responseBody));
                } else {
                    io.github.hyeonmo.responses.PtzResponse ptzResponse = ptzParser.parser(type, responseBody);
                    if (ptzResponse.isSuccess()) {
                        future.complete(ptzResponse.getMessage());
                    } else {
                        future.completeExceptionally(new io.github.hyeonmo.exceptions.OnvifException(ptzResponse.getMessage()));
                    }
                }
            }
        });
        return future;
    }

    public int getPtzRequestTimeout() {
        return ptzRequestTimeout;
    }

    public void setPtzRequestTimeout(int to) {
        ptzRequestTimeout = to;
        this.httpClient = buildHttpClient();
    }

    private OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(ptzRequestTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.dispatcher().executorService().shutdown();
                httpClient.connectionPool().evictAll();
            } catch (Exception ignored) {
            }
            httpClient = null;
        }
    }
}
