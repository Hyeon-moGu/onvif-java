package io.github.hyeonmo.requests.media;

import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.models.media.StreamType;
import io.github.hyeonmo.models.media.TransportProtocol;
import io.github.hyeonmo.requests.OnvifRequest;

public class GetMediaStreamRequest implements OnvifRequest {

    public static final String TAG = GetMediaStreamRequest.class.getSimpleName();

    private static final StreamType DEFAULT_STREAM = StreamType.RTP_UNICAST;
    private static final TransportProtocol DEFAULT_TRANSPORT = TransportProtocol.RTSP;

    private final OnvifMediaProfile mediaProfile;
    private final StreamType streamType;
    private final TransportProtocol transportProtocol;

    public GetMediaStreamRequest(OnvifMediaProfile mediaProfile) {
        this(mediaProfile, DEFAULT_STREAM, DEFAULT_TRANSPORT);
    }

    public GetMediaStreamRequest(OnvifMediaProfile mediaProfile,
                                 StreamType streamType,
                                 TransportProtocol transportProtocol) {
        super();
        this.mediaProfile = mediaProfile;
        // Fall back to defaults on null so we never emit an empty StreamSetup.
        this.streamType = (streamType != null) ? streamType : DEFAULT_STREAM;
        this.transportProtocol = (transportProtocol != null) ? transportProtocol : DEFAULT_TRANSPORT;
    }

    public OnvifMediaProfile getMediaProfile() {
        return mediaProfile;
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public TransportProtocol getTransportProtocol() {
        return transportProtocol;
    }

    @Override
    public String getXml() {
        return "<GetStreamUri xmlns=\"http://www.onvif.org/ver10/media/wsdl\">"
                + "<StreamSetup>"
                + "<Stream xmlns=\"http://www.onvif.org/ver10/schema\">" + streamType.value + "</Stream>"
                + "<Transport xmlns=\"http://www.onvif.org/ver10/schema\"><Protocol>"
                + transportProtocol.value + "</Protocol></Transport>"
                + "</StreamSetup>"
                + "<ProfileToken>" + mediaProfile.getToken() + "</ProfileToken>"
                + "</GetStreamUri>";
    }

    @Override
    public OnvifType getType() {
        return OnvifType.GET_STREAM_URI;
    }
}
