package io.github.hyeonmo.operations;

import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.media.StreamType;
import io.github.hyeonmo.models.media.TransportProtocol;
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
     * Retrieves the RTSP stream URI for a specific profile (RTP unicast over RTSP).
     */
    CompletableFuture<String> getMediaStreamURI(OnvifMediaProfile profile);

    /**
     * Retrieves the stream URI for a specific profile with an explicit stream type and transport.
     */
    CompletableFuture<String> getMediaStreamURI(OnvifMediaProfile profile,
                                                StreamType streamType,
                                                TransportProtocol transportProtocol);

    /**
     * Retrieves the HTTP Snapshot JPEG URI for a specific profile.
     */
    CompletableFuture<String> getSnapshotURI(OnvifMediaProfile profile);
}
