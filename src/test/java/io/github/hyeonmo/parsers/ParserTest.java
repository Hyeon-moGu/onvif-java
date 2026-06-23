package io.github.hyeonmo.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import io.github.hyeonmo.models.OnvifDeviceInformation;
import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.models.OnvifServices;
import io.github.hyeonmo.models.events.Event;
import io.github.hyeonmo.models.events.EventSubscription;
import io.github.hyeonmo.models.imaging.ImagingSettings;
import io.github.hyeonmo.parsers.device.GetDeviceInformationParser;
import io.github.hyeonmo.parsers.device.GetServicesParser;
import io.github.hyeonmo.parsers.device.GetSystemDateAndTimeParser;
import io.github.hyeonmo.parsers.events.CreatePullPointSubscriptionParser;
import io.github.hyeonmo.parsers.events.PullMessagesParser;
import io.github.hyeonmo.parsers.imaging.GetImagingSettingsParser;
import io.github.hyeonmo.parsers.media.GetMediaProfilesParser;
import io.github.hyeonmo.parsers.media.GetSnapshotParser;
import io.github.hyeonmo.parsers.ptz.PtzPresetParser;
import io.github.hyeonmo.parsers.ptz.PtzStatusParser;
import io.github.hyeonmo.responses.ImagingResponse;
import io.github.hyeonmo.responses.OnvifResponse;
import io.github.hyeonmo.responses.PtzResponse;
import io.github.hyeonmo.responses.events.PullMessagesResponse;

/**
 * Offline unit tests for the SOAP response parsers. They run against captured
 * ONVIF response payloads so no live camera is required.
 */
public class ParserTest {

    private static OnvifResponse<?> responseOf(String xml) {
        return new OnvifResponse<>(xml);
    }

    @Test
    public void parsesDeviceInformation() {
        String xml = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tds:GetDeviceInformationResponse xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\">"
                + "      <tds:Manufacturer>UNIVIEW</tds:Manufacturer>"
                + "      <tds:Model>IPC</tds:Model>"
                + "      <tds:FirmwareVersion>ONVIF_V3.0.0</tds:FirmwareVersion>"
                + "      <tds:SerialNumber>SN-12345</tds:SerialNumber>"
                + "      <tds:HardwareId>HW-1</tds:HardwareId>"
                + "    </tds:GetDeviceInformationResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        OnvifDeviceInformation info = new GetDeviceInformationParser().parse(responseOf(xml));

        assertEquals("UNIVIEW", info.getManufacturer());
        assertEquals("IPC", info.getModel());
        assertEquals("ONVIF_V3.0.0", info.getFirmwareVersion());
        assertEquals("SN-12345", info.getSerialNumber());
        assertEquals("HW-1", info.getHardwareId());
    }

    @Test
    public void parsesMediaProfiles() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <trt:GetProfilesResponse xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\""
                + "                             xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
                + "      <trt:Profiles token=\"Profile_1\" fixed=\"true\">"
                + "        <tt:Name>MainStream</tt:Name>"
                + "        <tt:VideoSourceConfiguration token=\"VSC_1\">"
                + "          <tt:SourceToken>VideoSource_1</tt:SourceToken>"
                + "        </tt:VideoSourceConfiguration>"
                + "      </trt:Profiles>"
                + "      <trt:Profiles token=\"Profile_2\">"
                + "        <tt:Name>SubStream</tt:Name>"
                + "        <tt:VideoSourceConfiguration token=\"VSC_1\">"
                + "          <tt:SourceToken>VideoSource_1</tt:SourceToken>"
                + "        </tt:VideoSourceConfiguration>"
                + "      </trt:Profiles>"
                + "    </trt:GetProfilesResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        List<OnvifMediaProfile> profiles = new GetMediaProfilesParser().parse(responseOf(xml));

        assertEquals(2, profiles.size());
        assertEquals("Profile_1", profiles.get(0).getToken());
        assertEquals("MainStream", profiles.get(0).getName());
        assertEquals("VideoSource_1", profiles.get(0).getVideoSourceToken());
        assertEquals("Profile_2", profiles.get(1).getToken());
        assertEquals("SubStream", profiles.get(1).getName());
    }

    @Test
    public void parsesSnapshotUri() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <trt:GetSnapshotUriResponse xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\""
                + "                                xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
                + "      <trt:MediaUri>"
                + "        <tt:Uri>http://192.168.1.100/onvif/snapshot</tt:Uri>"
                + "        <tt:InvalidAfterConnect>false</tt:InvalidAfterConnect>"
                + "      </trt:MediaUri>"
                + "    </trt:GetSnapshotUriResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        String uri = new GetSnapshotParser().parse(responseOf(xml));

        assertEquals("http://192.168.1.100/onvif/snapshot", uri);
    }

    @Test
    public void parsesSystemDateAndTime() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tds:GetSystemDateAndTimeResponse xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\""
                + "                                      xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
                + "      <tds:SystemDateAndTime>"
                + "        <tt:UTCDateTime>"
                + "          <tt:Time><tt:Hour>10</tt:Hour><tt:Minute>30</tt:Minute><tt:Second>15</tt:Second></tt:Time>"
                + "          <tt:Date><tt:Year>2026</tt:Year><tt:Month>6</tt:Month><tt:Day>24</tt:Day></tt:Date>"
                + "        </tt:UTCDateTime>"
                + "      </tds:SystemDateAndTime>"
                + "    </tds:GetSystemDateAndTimeResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        Date parsed = new GetSystemDateAndTimeParser().parse(responseOf(xml));
        assertNotNull(parsed);

        Calendar expected = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expected.clear();
        expected.set(2026, Calendar.JUNE, 24, 10, 30, 15);

        assertEquals(expected.getTimeInMillis(), parsed.getTime());
    }

    /**
     * Verifies the XXE hardening: a payload carrying a DOCTYPE / external entity
     * must NOT be resolved. With DOCTYPE declarations disallowed the parse fails
     * safely and the parser returns an empty result instead of leaking file
     * contents into the parsed value.
     */
    @Test
    public void rejectsXxePayload() {
        String malicious = ""
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"file:///etc/passwd\"> ]>"
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tds:GetDeviceInformationResponse xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\">"
                + "      <tds:Manufacturer>&xxe;</tds:Manufacturer>"
                + "    </tds:GetDeviceInformationResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        OnvifDeviceInformation info = new GetDeviceInformationParser().parse(responseOf(malicious));

        // The external entity must not have been expanded into the field.
        String manufacturer = info.getManufacturer();
        assertTrue("manufacturer should be empty, was: " + manufacturer,
                manufacturer == null || manufacturer.isEmpty());
        assertFalse("XXE entity must not be resolved",
                manufacturer != null && manufacturer.contains("root:"));
    }

    @Test
    public void parsesServicesPaths() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tds:GetServicesResponse xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\">"
                + "      <tds:Service>"
                + "        <tds:Namespace>http://www.onvif.org/ver10/device/wsdl</tds:Namespace>"
                + "        <tds:XAddr>http://192.168.1.100/onvif/device_service</tds:XAddr>"
                + "      </tds:Service>"
                + "      <tds:Service>"
                + "        <tds:Namespace>http://www.onvif.org/ver10/media/wsdl</tds:Namespace>"
                + "        <tds:XAddr>http://192.168.1.100/onvif/media_service</tds:XAddr>"
                + "      </tds:Service>"
                + "      <tds:Service>"
                + "        <tds:Namespace>http://www.onvif.org/ver20/ptz/wsdl</tds:Namespace>"
                + "        <tds:XAddr>http://192.168.1.100/onvif/ptz_service</tds:XAddr>"
                + "      </tds:Service>"
                + "      <tds:Service>"
                + "        <tds:Namespace>http://www.onvif.org/ver20/imaging/wsdl</tds:Namespace>"
                + "        <tds:XAddr>http://192.168.1.100/onvif/imaging_service</tds:XAddr>"
                + "      </tds:Service>"
                + "      <tds:Service>"
                + "        <tds:Namespace>http://www.onvif.org/ver10/events/wsdl</tds:Namespace>"
                + "        <tds:XAddr>http://192.168.1.100/onvif/event_service</tds:XAddr>"
                + "      </tds:Service>"
                + "    </tds:GetServicesResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        OnvifServices services = new GetServicesParser().parse(responseOf(xml));

        assertEquals("/onvif/device_service", services.getDeviceInformationPath());
        assertEquals("/onvif/media_service", services.getProfilesPath());
        assertEquals("/onvif/media_service", services.getStreamURIPath());
        assertEquals("/onvif/ptz_service", services.getPtzPath());
        assertEquals("/onvif/imaging_service", services.getImagingPath());
        assertEquals("/onvif/event_service", services.getEventsPath());
    }

    @Test
    public void parsesImagingSettings() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <timg:GetImagingSettingsResponse xmlns:timg=\"http://www.onvif.org/ver20/imaging/wsdl\""
                + "                                     xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
                + "      <timg:ImagingSettings>"
                + "        <tt:Brightness>55</tt:Brightness>"
                + "        <tt:ColorSaturation>60</tt:ColorSaturation>"
                + "        <tt:Contrast>50</tt:Contrast>"
                + "        <tt:Sharpness>40</tt:Sharpness>"
                + "        <tt:Exposure><tt:Mode>AUTO</tt:Mode></tt:Exposure>"
                + "        <tt:IrCutFilter>AUTO</tt:IrCutFilter>"
                + "        <tt:WhiteBalance><tt:Mode>AUTO</tt:Mode></tt:WhiteBalance>"
                + "      </timg:ImagingSettings>"
                + "    </timg:GetImagingSettingsResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        ImagingSettings settings = new GetImagingSettingsParser().parse(new ImagingResponse<>(xml));

        assertEquals(55, settings.getBrightness());
        assertEquals(60, settings.getColorSaturation());
        assertEquals(50, settings.getContrast());
        assertEquals(40, settings.getSharpness());
        assertEquals("AUTO", settings.getExposureMode());
        assertEquals("AUTO", settings.getIrCutFilter());
        assertEquals("AUTO", settings.getWhiteBalanceMode());
    }

    @Test
    public void parsesPtzStatus() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tptz:GetStatusResponse xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\""
                + "                            xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
                + "      <tptz:PTZStatus>"
                + "        <tt:Position>"
                + "          <tt:PanTilt x=\"0.5\" y=\"-0.3\"/>"
                + "          <tt:Zoom x=\"0.1\"/>"
                + "        </tt:Position>"
                + "        <tt:MoveStatus><tt:PanTilt>IDLE</tt:PanTilt><tt:Zoom>IDLE</tt:Zoom></tt:MoveStatus>"
                + "        <tt:UtcTime>2026-06-24T10:30:15Z</tt:UtcTime>"
                + "      </tptz:PTZStatus>"
                + "    </tptz:GetStatusResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        PtzResponse response = new PtzStatusParser().parse(xml);

        assertTrue(response.isSuccess());
        assertTrue("message should carry the parsed position, was: " + response.getMessage(),
                response.getMessage().contains("0.5") && response.getMessage().contains("-0.3"));
    }

    @Test
    public void parsesPtzPresets() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tptz:GetPresetsResponse xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\""
                + "                             xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
                + "      <tptz:Preset token=\"1\"><tt:Name>Home</tt:Name></tptz:Preset>"
                + "      <tptz:Preset token=\"2\"><tt:Name>Gate</tt:Name></tptz:Preset>"
                + "    </tptz:GetPresetsResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        PtzResponse response = new PtzPresetParser().parse(xml);

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Token='1'") && response.getMessage().contains("Name='Home'"));
        assertTrue(response.getMessage().contains("Token='2'") && response.getMessage().contains("Name='Gate'"));
    }

    @Test
    public void parsesPullMessagesEvents() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tev:PullMessagesResponse xmlns:tev=\"http://www.onvif.org/ver10/events/wsdl\""
                + "                              xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\""
                + "                              xmlns:tt=\"http://www.onvif.org/ver10/schema\">"
                + "      <tev:CurrentTime>2026-06-24T10:30:15Z</tev:CurrentTime>"
                + "      <tev:TerminationTime>2026-06-24T10:31:15Z</tev:TerminationTime>"
                + "      <wsnt:NotificationMessage>"
                + "        <wsnt:Topic>tns1:VideoSource/MotionAlarm</wsnt:Topic>"
                + "        <wsnt:Message>"
                + "          <tt:Message UtcTime=\"2026-06-24T10:30:14Z\" PropertyOperation=\"Changed\">"
                + "            <tt:Source><tt:SimpleItem Name=\"VideoSourceConfigurationToken\" Value=\"VSC_1\"/></tt:Source>"
                + "            <tt:Data><tt:SimpleItem Name=\"State\" Value=\"true\"/></tt:Data>"
                + "          </tt:Message>"
                + "        </wsnt:Message>"
                + "      </wsnt:NotificationMessage>"
                + "    </tev:PullMessagesResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        PullMessagesResponse response = new PullMessagesParser().parse(responseOf(xml));

        assertEquals("2026-06-24T10:30:15Z", response.getCurrentTime());
        assertEquals(1, response.getEvents().size());

        Event event = response.getEvents().get(0);
        assertEquals("tns1:VideoSource/MotionAlarm", event.getTopic());
        assertEquals("Changed", event.getPropertyOperation());
        assertTrue("event should be detected as motion-related", event.isMotionRelated());
        assertTrue("State=true should mark motion active", event.isMotionActive());
        assertTrue(event.getData().contains("State=true"));
    }

    @Test
    public void parsesPullPointSubscription() {
        String xml = ""
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "  <env:Body>"
                + "    <tev:CreatePullPointSubscriptionResponse xmlns:tev=\"http://www.onvif.org/ver10/events/wsdl\""
                + "                                             xmlns:wsa=\"http://www.w3.org/2005/08/addressing\""
                + "                                             xmlns:wsnt=\"http://docs.oasis-open.org/wsn/b-2\">"
                + "      <tev:SubscriptionReference>"
                + "        <wsa:Address>http://192.168.1.100/onvif/Subscription?Idx=1</wsa:Address>"
                + "      </tev:SubscriptionReference>"
                + "      <wsnt:CurrentTime>2026-06-24T10:30:15Z</wsnt:CurrentTime>"
                + "      <wsnt:TerminationTime>2026-06-24T10:40:15Z</wsnt:TerminationTime>"
                + "    </tev:CreatePullPointSubscriptionResponse>"
                + "  </env:Body>"
                + "</env:Envelope>";

        EventSubscription subscription = new CreatePullPointSubscriptionParser().parse(responseOf(xml));

        assertTrue(subscription.hasAddress());
        assertEquals("http://192.168.1.100/onvif/Subscription?Idx=1", subscription.getAddress());
        assertEquals("2026-06-24T10:30:15Z", subscription.getCurrentTime());
        assertEquals("2026-06-24T10:40:15Z", subscription.getTerminationTime());
    }
}
