package io.github.hyeonmo.operations;

import io.github.hyeonmo.models.OnvifMediaProfile;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Operations related to ONVIF Media (Profiles, Stream URLs, Snapshots).
 */
public interface MediaOperations {

    /**
     * Retrieves the media profiles configured on the device.
     */
    CompletableFuture<List<OnvifMediaProfile>> getMediaProfiles();

    /**
     * Retrieves the RTSP stream URI for a specific profile.
     */
    CompletableFuture<String> getMediaStreamURI(OnvifMediaProfile profile);

    /**
     * Retrieves the HTTP Snapshot JPEG URI for a specific profile.
     */
    CompletableFuture<String> getSnapshotURI(OnvifMediaProfile profile);
}
