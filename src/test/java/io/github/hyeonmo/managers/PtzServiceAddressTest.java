package io.github.hyeonmo.managers;

import io.github.hyeonmo.core.PtzExecutor;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.requests.ptz.PtzBaseRequest;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class PtzServiceAddressTest {
    @Test
    public void ptzFallbackUsesFullServiceAddressAndHonorsLegacyOverride() throws Exception {
        OnvifDevice device = new OnvifDevice("device.test");
        device.setMediaProfiles(Collections.singletonList(new OnvifMediaProfile("Main", "profile", "source")));
        device.getPath().setPtzXaddr("https://ptz.test:8443/control?channel=2");
        AtomicReference<String> address = new AtomicReference<>();
        PtzExecutor executor = new PtzExecutor() {
            @Override
            public <T> CompletableFuture<T> sendRequest(PtzBaseRequest request) {
                address.set(request.getXAddr());
                return CompletableFuture.completedFuture(null);
            }
        };
        PtzManager manager = new PtzManager(new OnvifManager(), executor);
        try {
            manager.stop(device).get(3, TimeUnit.SECONDS);
            assertEquals("https://ptz.test:8443/control?channel=2", address.get());
            device.getPath().setPtzPath("/custom-ptz");
            manager.stop(device).get(3, TimeUnit.SECONDS);
            assertEquals("http://device.test/custom-ptz", address.get());
        } finally {
            manager.destroy();
        }
    }
}
