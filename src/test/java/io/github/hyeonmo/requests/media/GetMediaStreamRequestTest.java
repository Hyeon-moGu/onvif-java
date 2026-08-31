package io.github.hyeonmo.requests.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.models.media.StreamType;
import io.github.hyeonmo.models.media.TransportProtocol;
import org.junit.Test;

/**
 * Offline tests for the GetStreamUri SOAP body: default setup and explicit transport options.
 */
public class GetMediaStreamRequestTest {

    private static OnvifMediaProfile profile() {
        return new OnvifMediaProfile("Main", "profile-token-1", "vs-token");
    }

    @Test
    public void defaultConstructorEmitsRtpUnicastOverRtsp() {
        String xml = new GetMediaStreamRequest(profile()).getXml();

        assertTrue(xml, xml.contains(">RTP-Unicast</Stream>"));
        assertTrue(xml, xml.contains("<Protocol>RTSP</Protocol>"));
        assertTrue(xml, xml.contains("<ProfileToken>profile-token-1</ProfileToken>"));
        assertEquals(OnvifType.GET_STREAM_URI, new GetMediaStreamRequest(profile()).getType());
    }

    @Test
    public void tcpTransportIsSerializedIntoProtocolElement() {
        String xml = new GetMediaStreamRequest(profile(),
                StreamType.RTP_UNICAST, TransportProtocol.TCP).getXml();

        assertTrue(xml, xml.contains(">RTP-Unicast</Stream>"));
        assertTrue(xml, xml.contains("<Protocol>TCP</Protocol>"));
    }

    @Test
    public void multicastStreamTypeIsSerializedIntoStreamElement() {
        String xml = new GetMediaStreamRequest(profile(),
                StreamType.RTP_MULTICAST, TransportProtocol.UDP).getXml();

        assertTrue(xml, xml.contains(">RTP-Multicast</Stream>"));
        assertTrue(xml, xml.contains("<Protocol>UDP</Protocol>"));
    }

    @Test
    public void nullOptionsFallBackToDefaultsWithoutEmittingEmptyElements() {
        String xml = new GetMediaStreamRequest(profile(), null, null).getXml();

        assertTrue(xml, xml.contains(">RTP-Unicast</Stream>"));
        assertTrue(xml, xml.contains("<Protocol>RTSP</Protocol>"));
    }
}
