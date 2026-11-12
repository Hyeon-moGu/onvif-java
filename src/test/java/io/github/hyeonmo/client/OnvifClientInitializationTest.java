package io.github.hyeonmo.client;

import io.github.hyeonmo.core.OnvifExecutor;
import io.github.hyeonmo.managers.OnvifManager;
import io.github.hyeonmo.exceptions.OnvifAuthException;
import io.github.hyeonmo.models.OnvifDevice;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

import static org.junit.Assert.*;

public class OnvifClientInitializationTest {
    @Test
    public void clockIsCorrectedBeforeAuthenticatedInitialization() throws Exception {
        try (Camera camera = new Camera(false, false, false)) {
            OnvifDevice device = OnvifClient.connect(camera.url()).credentials("user", "password")
                    .buildAsync(camera.manager).get(5, TimeUnit.SECONDS);
            assertTrue(camera.requests.get(0).contains("<GetSystemDateAndTime"));
            assertFalse(camera.requests.get(0).contains("<wsse:Security>"));
            assertTrue(camera.requests.get(1).contains("GetCapabilities"));
            long expected = Instant.parse("2030-02-03T04:05:06Z").toEpochMilli();
            for (String request : camera.requests.subList(1, camera.requests.size())) {
                assertTrue(request.contains("<wsse:Security>"));
                long created = Instant.parse(request.substring(request.indexOf("<wsu:Created>") + 13,
                        request.indexOf("</wsu:Created>"))).toEpochMilli();
                assertTrue("Authenticated requests must use camera time", Math.abs(created - expected) < 5000);
            }
            assertTrue(Math.abs(System.currentTimeMillis() + device.getTimeOffsetMs() - expected) < 5000);
            assertNotNull(device.getCapabilities());
            assertNotNull(device.getMediaProfiles());
        }
    }

    @Test
    public void failedTimeLookupStillAllowsInitialization() throws Exception {
        try (Camera camera = new Camera(true, false, false)) {
            OnvifDevice device = OnvifClient.connect(camera.url()).buildAsync(camera.manager).get(5, TimeUnit.SECONDS);
            assertEquals(0, device.getTimeOffsetMs());
            assertNotNull(device.getCapabilities());
            assertEquals(4, camera.requests.size());
        }
    }

    @Test
    public void capabilitiesAuthenticationFailureRemainsExceptional() throws Exception {
        try (Camera camera = new Camera(false, true, false)) {
            try {
                OnvifClient.connect(camera.url()).buildAsync(camera.manager).get(5, TimeUnit.SECONDS);
                fail("Expected authentication failure");
            } catch (ExecutionException e) {
                assertTrue(e.getCause() instanceof OnvifAuthException);
            }
            assertEquals(2, camera.requests.size());
        }
    }

    @Test
    public void optionalServiceAndProfileFailuresStillReturnDevice() throws Exception {
        try (Camera camera = new Camera(false, false, true)) {
            OnvifDevice device = OnvifClient.connect(camera.url()).buildAsync(camera.manager).get(5, TimeUnit.SECONDS);
            assertNotNull(device.getCapabilities());
            assertNull(device.getMediaProfiles());
            assertEquals(4, camera.requests.size());
        }
    }

    /** Intercepts real SOAP requests in memory without opening a network socket. */
    private static class Camera implements AutoCloseable {
        final OkHttpClient client;
        final OnvifManager manager = new OnvifManager();
        final List<String> requests = new CopyOnWriteArrayList<>();

        Camera(boolean failTime, boolean failCapabilities, boolean failOptional) throws Exception {
            client = new OkHttpClient.Builder().addInterceptor(chain -> {
                Buffer buffer = new Buffer();
                chain.request().body().writeTo(buffer);
                String request = buffer.readUtf8();
                requests.add(request);
                boolean time = request.contains("GetSystemDateAndTime");
                boolean capabilities = request.contains("GetCapabilities");
                int status = time && failTime || capabilities && failCapabilities || !time && !capabilities && failOptional ? 403 : 200;
                String xml = time ? "<GetSystemDateAndTimeResponse><UTCDateTime><Date>"
                        + "<Year>2030</Year><Month>2</Month><Day>3</Day></Date><Time>"
                        + "<Hour>4</Hour><Minute>5</Minute><Second>6</Second></Time></UTCDateTime></GetSystemDateAndTimeResponse>"
                        : "<Response/>";
                return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                        .code(status).message("test")
                        .body(ResponseBody.create(MediaType.parse("application/soap+xml"), xml)).build();
            }).build();
            Field executorField = OnvifManager.class.getDeclaredField("executor");
            executorField.setAccessible(true);
            OnvifExecutor executor = (OnvifExecutor) executorField.get(manager);
            Field clientField = OnvifExecutor.class.getDeclaredField("baseClient");
            clientField.setAccessible(true);
            clientField.set(executor, client);
        }

        String url() { return "http://camera.test"; }
        public void close() {
            manager.destroy();
            client.dispatcher().executorService().shutdownNow();
            client.connectionPool().evictAll();
        }
    }
}
