package io.github.hyeonmo.core;

import io.github.hyeonmo.exceptions.OnvifAuthException;
import io.github.hyeonmo.exceptions.OnvifNetworkException;
import io.github.hyeonmo.exceptions.OnvifSoapException;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifServices;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.parsers.device.GetServicesParser;
import io.github.hyeonmo.requests.OnvifRequest;
import io.github.hyeonmo.requests.device.GetServicesRequest;
import io.github.hyeonmo.requests.device.GetSystemDateAndTimeRequest;
import io.github.hyeonmo.requests.imaging.GetImagingSettingsRequest;
import io.github.hyeonmo.requests.media.GetSnapshotRequest;
import io.github.hyeonmo.responses.OnvifResponse;
import okhttp3.*;
import okio.*;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/** All HTTP calls are intercepted in memory; no camera or external network is used. */
public class OnvifTransportTest {
    private final List<OkHttpClient> clients = new ArrayList<>();
    private final OnvifDevice device = new OnvifDevice("camera.test", "user", "password");

    @After
    public void closeClients() {
        for (OkHttpClient client : clients) {
            client.dispatcher().executorService().shutdownNow();
            client.connectionPool().evictAll();
        }
    }

    @Test
    public void onvifBodyReadFailureCompletesFutureAndClosesBody() throws Exception {
        assertReadFailure(false, 200);
    }

    @Test
    public void imagingBodyReadFailureCompletesFutureAndClosesBody() throws Exception {
        assertReadFailure(true, 200);
    }

    @Test
    public void errorResponseBodyReadFailureAlsoCompletesFuture() throws Exception {
        assertReadFailure(false, 500);
        assertReadFailure(true, 500);
    }

    private void assertReadFailure(boolean imaging, int status) throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        IOException readFailure = new IOException("simulated truncated body");
        ResponseBody body = new ResponseBody() {
            private final BufferedSource source = Okio.buffer(new Source() {
                public long read(Buffer sink, long count) throws IOException { throw readFailure; }
                public Timeout timeout() { return Timeout.NONE; }
                public void close() { closed.set(true); }
            });
            public MediaType contentType() { return MediaType.parse("application/soap+xml"); }
            public long contentLength() { return -1; }
            public BufferedSource source() { return source; }
        };
        OkHttpClient client = client(chain -> response(chain.request(), status, body));
        CompletableFuture<?> future;
        if (imaging) {
            ImagingExecutor executor = new ImagingExecutor();
            inject(executor, "client", client);
            future = executor.sendRequest(device, new GetImagingSettingsRequest("source", "http://camera.test/imaging"));
        } else {
            OnvifExecutor executor = new OnvifExecutor();
            inject(executor, "baseClient", client);
            future = executor.sendRequest(device, new GetServicesRequest());
        }
        Throwable failure = failure(future);
        assertTrue(failure instanceof OnvifNetworkException);
        assertSame(readFailure, failure.getCause());
        assertTrue("Body must close even if reading fails", closed.get());
    }

    @Test
    public void httpErrorClassificationIsUnchanged() throws Exception {
        for (int status : new int[]{403, 500}) {
            OkHttpClient client = client(chain -> response(chain.request(), status, xml("<Fault/>")));
            OnvifExecutor executor = new OnvifExecutor();
            inject(executor, "baseClient", client);
            Throwable cause = failure(executor.sendRequest(device, new GetServicesRequest()));
            assertTrue(status == 403 ? cause instanceof OnvifAuthException : cause instanceof OnvifSoapException);
            ImagingExecutor imaging = new ImagingExecutor();
            inject(imaging, "client", client);
            cause = failure(imaging.sendRequest(device, new GetImagingSettingsRequest("source", "http://camera.test/imaging")));
            assertTrue(status == 403 ? cause instanceof OnvifAuthException : cause instanceof OnvifSoapException);
        }
    }

    @Test
    public void snapshotUsesMediaServiceInsteadOfDeviceService() throws Exception {
        device.getPath().setStreamURIPath("/media");
        AtomicReference<Request> sent = new AtomicReference<>();
        OnvifExecutor executor = capturingExecutor(sent);
        String uri = executor.<String>sendRequest(device, new GetSnapshotRequest(profile())).get(3, TimeUnit.SECONDS);
        assertEquals("http://camera.test/media", sent.get().url().toString());
        assertEquals("http://camera.test/image.jpg", uri);
    }

    @Test
    public void parsedMediaAddressPreservesSchemeHostPortAndQuery() throws Exception {
        String address = "https://media.test:8443/service?channel=1&mode=main";
        OnvifResponse<?> response = new OnvifResponse<>(new GetServicesRequest());
        response.setXml("<GetServicesResponse><Service><Namespace>http://www.onvif.org/ver10/media/wsdl</Namespace>"
                + "<XAddr>" + address.replace("&", "&amp;") + "</XAddr></Service></GetServicesResponse>");
        device.setPath(new GetServicesParser().parse(response));
        AtomicReference<Request> sent = new AtomicReference<>();
        capturingExecutor(sent).sendRequest(device, new GetSnapshotRequest(profile())).get(3, TimeUnit.SECONDS);
        assertEquals(address, sent.get().url().toString());
        assertEquals("/service?channel=1&mode=main", device.getPath().getProfilesPath());
    }

    @Test
    public void relativePathsStillUseBaseUrl() throws Exception {
        device.setBaseUrl("http://gateway.test:8000");
        device.getPath().setStreamURIPath("/media?channel=2");
        AtomicReference<Request> sent = new AtomicReference<>();
        capturingExecutor(sent).sendRequest(device, new GetSnapshotRequest(profile())).get(3, TimeUnit.SECONDS);
        assertEquals("http://gateway.test:8000/media?channel=2", sent.get().url().toString());
    }

    @Test
    public void explicitFullUrlAndReferenceHeadersRemainSupported() throws Exception {
        AtomicReference<Request> sent = new AtomicReference<>();
        capturingExecutor(sent).sendRequest(device, "http://subscription.test:8081/pull?key=abc",
                "<ref:Id xmlns:ref=\"urn:test\">123</ref:Id>", new GetServicesRequest()).get(3, TimeUnit.SECONDS);
        assertEquals("http://subscription.test:8081/pull?key=abc", sent.get().url().toString());
        assertTrue(body(sent.get()).contains("<ref:Id xmlns:ref=\"urn:test\">123</ref:Id>"));
    }

    @Test
    public void serviceRoutingUsesEachAdvertisedAddress() throws Exception {
        OnvifServices services = device.getPath();
        services.setDeviceXaddr("https://device.test:8443/device");
        services.setMediaXaddr("https://media.test:8444/media");
        services.setPtzXaddr("https://ptz.test:8445/ptz");
        services.setImagingXaddr("https://imaging.test:8446/imaging");
        services.setEventsXaddr("https://events.test:8447/events");
        OnvifType[] types = {OnvifType.GET_SERVICES, OnvifType.GET_DEVICE_INFORMATION,
                OnvifType.GET_MEDIA_PROFILES, OnvifType.GET_STREAM_URI, OnvifType.GET_PTZ,
                OnvifType.GET_IMAGING, OnvifType.GET_EVENTS};
        String[] addresses = {services.getDeviceXaddr(), services.getDeviceXaddr(),
                services.getMediaXaddr(), services.getMediaXaddr(), services.getPtzXaddr(),
                services.getImagingXaddr(), services.getEventsXaddr()};
        AtomicReference<Request> sent = new AtomicReference<>();
        OnvifExecutor executor = capturingExecutor(sent);
        for (int i = 0; i < types.length; i++) {
            final OnvifType type = types[i];
            executor.sendRequest(device, new OnvifRequest() {
                public String getXml() { return "<Request/>"; }
                public OnvifType getType() { return type; }
            }).get(3, TimeUnit.SECONDS);
            assertEquals(addresses[i], sent.get().url().toString());
        }
    }

    @Test
    public void imagingUsesDeviceClockOffsetAndLegacyOverloadUsesLocalTime() throws Exception {
        device.setTimeOffsetMs(TimeUnit.HOURS.toMillis(3));
        AtomicReference<Request> sent = new AtomicReference<>();
        ImagingExecutor executor = new ImagingExecutor();
        inject(executor, "client", client(chain -> {
            sent.set(chain.request());
            return response(chain.request(), 200, xml("<GetImagingSettingsResponse/>"));
        }));
        GetImagingSettingsRequest request = new GetImagingSettingsRequest("source", "http://camera.test/imaging");
        long before = System.currentTimeMillis();
        executor.sendRequest(device, request).get(3, TimeUnit.SECONDS);
        assertCreatedBetween(sent.get(), before + device.getTimeOffsetMs(), System.currentTimeMillis() + device.getTimeOffsetMs());
        before = System.currentTimeMillis();
        executor.sendRequestUser("user", "password", request).get(3, TimeUnit.SECONDS);
        assertCreatedBetween(sent.get(), before, System.currentTimeMillis());
    }

    @Test
    public void timeLookupOmitsWsSecurityButOtherRequestsKeepIt() throws Exception {
        AtomicReference<Request> sent = new AtomicReference<>();
        OnvifExecutor executor = capturingExecutor(sent);
        executor.sendRequest(device, new GetSystemDateAndTimeRequest()).get(3, TimeUnit.SECONDS);
        assertFalse(body(sent.get()).contains("<wsse:Security>"));
        executor.sendRequest(device, new GetServicesRequest()).get(3, TimeUnit.SECONDS);
        assertTrue(body(sent.get()).contains("<wsse:Security>"));
    }

    @Test
    public void addressingHeaderEscapesServiceQueryParameters() throws Exception {
        device.getPath().setEventsXaddr("https://events.test/service?channel=1&mode=pull");
        AtomicReference<Request> sent = new AtomicReference<>();
        capturingExecutor(sent).sendRequest(device, new OnvifRequest() {
            public String getXml() { return "<Request/>"; }
            public OnvifType getType() { return OnvifType.GET_EVENTS; }
            public String getAction() { return "urn:test:action"; }
        }).get(3, TimeUnit.SECONDS);
        String requestXml = body(sent.get());
        org.w3c.dom.Document document = io.github.hyeonmo.parsers.XMLParserUtils.getDocumentBuilder().parse(
                new java.io.ByteArrayInputStream(requestXml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertEquals(device.getPath().getEventsXaddr(), document.getElementsByTagNameNS(
                "http://www.w3.org/2005/08/addressing", "To").item(0).getTextContent());
    }

    private OnvifExecutor capturingExecutor(AtomicReference<Request> sent) throws Exception {
        OnvifExecutor executor = new OnvifExecutor();
        inject(executor, "baseClient", client(chain -> {
            sent.set(chain.request());
            return response(chain.request(), 200, xml("<Response><Uri>http://camera.test/image.jpg</Uri></Response>"));
        }));
        return executor;
    }

    @Test
    public void cancellingFutureCancelsUnderlyingHttpCall() throws Exception {
        java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        AtomicReference<Call> call = new AtomicReference<>();
        OnvifExecutor executor = new OnvifExecutor();
        inject(executor, "baseClient", client(chain -> {
            call.set(chain.call());
            entered.countDown();
            try {
                if (!release.await(3, TimeUnit.SECONDS)) throw new IOException("Test interceptor timed out");
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                throw new IOException(error);
            }
            return response(chain.request(), 200, xml("<Response/>"));
        }));
        CompletableFuture<?> result = executor.sendRequest(device, new GetServicesRequest());
        try {
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            result.cancel(true);
            assertTrue(call.get().isCanceled());
        } finally {
            release.countDown();
        }
    }

    private OkHttpClient client(Interceptor interceptor) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        clients.add(client);
        return client;
    }

    private static void inject(Object target, String name, OkHttpClient client) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, client);
    }

    private static Response response(Request request, int status, ResponseBody body) {
        return new Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(status).message("test").body(body).build();
    }

    private static ResponseBody xml(String text) {
        return ResponseBody.create(MediaType.parse("application/soap+xml"), text);
    }

    private static OnvifMediaProfile profile() {
        return new OnvifMediaProfile("Main", "profile", "source");
    }

    private static Throwable failure(CompletableFuture<?> future) throws Exception {
        try {
            future.get(3, TimeUnit.SECONDS);
            throw new AssertionError("Expected exceptional completion");
        } catch (ExecutionException e) {
            return e.getCause();
        }
    }

    private static String body(Request request) throws IOException {
        Buffer buffer = new Buffer();
        request.body().writeTo(buffer);
        return buffer.readUtf8();
    }

    private static void assertCreatedBetween(Request request, long before, long after) throws IOException {
        String xml = body(request);
        String created = xml.substring(xml.indexOf("<wsu:Created>") + 13, xml.indexOf("</wsu:Created>"));
        long time = Instant.parse(created).toEpochMilli();
        assertTrue("WS-Security timestamp must use the expected clock", time >= before && time <= after);
    }
}
