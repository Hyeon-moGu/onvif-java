package io.github.hyeonmo.core;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.exceptions.OnvifExceptionFactory;
import io.github.hyeonmo.parsers.ptz.PtzMoveParser;
import io.github.hyeonmo.parsers.ptz.PtzStopParser;
import io.github.hyeonmo.parsers.ptz.PtzPresetParser;
import io.github.hyeonmo.parsers.ptz.PtzStatusParser;
import io.github.hyeonmo.requests.ptz.PtzBaseRequest;
import io.github.hyeonmo.responses.PtzResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Executes PTZ requests asynchronously.
 */
public class PtzExecutor {

    private OkHttpClient client;
    private MediaType reqBodyType;

    public PtzExecutor() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        reqBodyType = MediaType.parse("application/soap+xml; charset=utf-8");
    }

    public <T> CompletableFuture<T> sendRequest(PtzBaseRequest request) {
        RequestBody reqBody = RequestBody.create(reqBodyType, request.getXml());
        Request xmlRequest = new Request.Builder()
                .url(request.getXAddr())
                .addHeader("Content-Type", "application/soap+xml; charset=utf-8")
                .post(reqBody)
                .build();
        
        return execute(request, xmlRequest);
    }

    @SuppressWarnings("unchecked")
    private <T> CompletableFuture<T> execute(PtzBaseRequest request, Request xmlRequest) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        client.newCall(xmlRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response xmlResponse) throws IOException {
                ResponseBody xmlBody = xmlResponse.body();
                String responseXml = "";
                try {
                    responseXml = xmlBody != null ? xmlBody.string() : "";
                } catch (IOException e) {
                    future.completeExceptionally(e);
                    return;
                }

                if (xmlResponse.isSuccessful()) {
                    try {
                        T result = (T) parseResponse(request, responseXml);
                        future.complete(result);
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                } else {
                    future.completeExceptionally(OnvifExceptionFactory.fromHttpError(xmlResponse.code(), responseXml));
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(OnvifExceptionFactory.fromHttpError(-1, e.getMessage()));
            }
        });
        
        return future;
    }

    private Object parseResponse(PtzBaseRequest request, String xml) {
        PtzResponse ptzResponse;
        switch (request.getPtzOperationType()) {
            case MOVE:
                ptzResponse = new PtzMoveParser().parse(xml);
                break;
            case STOP:
                ptzResponse = new PtzStopParser().parse(xml);
                break;
            case PRESET:
                ptzResponse = new PtzPresetParser().parse(xml);
                break;
            case STATUS:
                ptzResponse = new PtzStatusParser().parse(xml);
                break;
            default:
                throw new IllegalArgumentException("Unknown PTZ operation type");
        }

        if (ptzResponse.isSuccess()) {
            return ptzResponse.getMessage();
        } else {
            throw new RuntimeException(ptzResponse.getMessage());
        }
    }

    public void destroy() {
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            client = null;
        }
    }
}
