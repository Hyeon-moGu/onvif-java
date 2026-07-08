package io.github.hyeonmo.core;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.github.hyeonmo.exceptions.OnvifExceptionFactory;
import io.github.hyeonmo.exceptions.OnvifSoapException;
import io.github.hyeonmo.parsers.ptz.PtzMoveParser;
import io.github.hyeonmo.parsers.ptz.PtzStopParser;
import io.github.hyeonmo.parsers.ptz.PtzPresetParser;
import io.github.hyeonmo.parsers.ptz.PtzStatusParser;
import io.github.hyeonmo.parsers.ptz.PtzAbsoluteMoveParser;
import io.github.hyeonmo.parsers.ptz.PtzConfigurationOptionsParser;
import io.github.hyeonmo.parsers.ptz.PtzPositionParser;
import io.github.hyeonmo.parsers.ptz.PtzResponseParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes PTZ requests asynchronously.
 */
public class PtzExecutor {
    private static final Logger log = LoggerFactory.getLogger(PtzExecutor.class);

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
        log.debug("Sending PTZ {} request to {}", request.getPtzOperationType(), request.getXAddr());
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
                        log.debug("PTZ {} request completed", request.getPtzOperationType());
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
        PtzResponseParser<?> parser;
        switch (request.getPtzOperationType()) {
            case MOVE:
                parser = new PtzMoveParser();
                break;
            case STOP:
                parser = new PtzStopParser();
                break;
            case PRESET:
                parser = new PtzPresetParser();
                break;
            case STATUS:
                parser = new PtzStatusParser();
                break;
            case ABSOLUTE_MOVE:
                parser = new PtzAbsoluteMoveParser();
                break;
            case POSITION:
                parser = new PtzPositionParser();
                break;
            case CONFIGURATION_OPTIONS:
                parser = new PtzConfigurationOptionsParser();
                break;
            default:
                throw new OnvifSoapException("Unknown PTZ operation type: " + request.getPtzOperationType(), xml);
        }

        Object parsed = parser.parse(xml);
        if (parsed instanceof PtzResponse) {
            PtzResponse response = (PtzResponse) parsed;
            if (!response.isSuccess()) {
                throw new OnvifSoapException(response.getMessage(), response.getRawXml());
            }
            return response.getMessage();
        }
        return parsed;
    }

    public void destroy() {
        if (client != null) {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
            client = null;
        }
    }
}
