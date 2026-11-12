package io.github.hyeonmo.parsers.device;

import io.github.hyeonmo.models.OnvifServices;
import io.github.hyeonmo.requests.device.GetServicesRequest;
import io.github.hyeonmo.responses.OnvifResponse;
import io.github.hyeonmo.utils.OnvifUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetServicesParserTest {
    @Test
    public void namespacePrefixesDoNotAffectAddressOrLegacyPath() {
        OnvifServices services = parse("<d:GetServicesResponse xmlns:d=\"urn:device\"><d:Service>"
                + "<d:Namespace>http://www.onvif.org/ver10/media/wsdl</d:Namespace>"
                + "<d:XAddr>https://media.test:8443/media?x=1&amp;y=2</d:XAddr>"
                + "</d:Service></d:GetServicesResponse>");
        assertEquals("https://media.test:8443/media?x=1&y=2", services.getMediaXaddr());
        assertEquals("/media?x=1&y=2", services.getProfilesPath());
        assertEquals(services.getProfilesPath(), services.getStreamURIPath());
    }

    @Test
    public void missingAndEmptyAddressesKeepDefaultPaths() {
        for (String address : new String[]{"", "<XAddr> </XAddr>"}) {
            OnvifServices services = parse("<Response><Service><Namespace>http://www.onvif.org/ver10/media/wsdl</Namespace>"
                    + address + "</Service></Response>");
            assertNull(services.getMediaXaddr());
            assertEquals(OnvifServices.ONVIF_PATH_PROFILES, services.getProfilesPath());
        }
    }

    @Test
    public void malformedXmlAndDoctypeRetainSafeFallback() {
        for (String xml : new String[]{"<broken>", "<!DOCTYPE r [<!ENTITY address 'https://unexpected.test/media'>]>"
                + "<r><Service><Namespace>http://www.onvif.org/ver10/media/wsdl</Namespace>"
                + "<XAddr>&address;</XAddr></Service></r>"}) {
            OnvifServices services = parse(xml);
            assertNull(services.getMediaXaddr());
            assertEquals(OnvifServices.ONVIF_PATH_PROFILES, services.getProfilesPath());
        }
    }

    @Test
    public void pathExtractionPreservesQueryDelimiterAndEncoding() {
        assertEquals("/media?x=a%20b&y=2", OnvifUtils.getPathFromURL("https://camera.test:8443/media?x=a%20b&y=2"));
        assertEquals("/media", OnvifUtils.getPathFromURL("http://camera.test/media"));
    }

    @Test
    public void explicitLegacyPathOverridesClearAdvertisedAddress() {
        OnvifServices services = new OnvifServices();
        services.setMediaXaddr("http://camera.test/media");
        services.setStreamURIPath("/custom-media");
        assertNull(services.getMediaXaddr());
        assertEquals("/custom-media", services.getStreamURIPath());
        services.setDeviceXaddr("http://camera.test/device");
        assertEquals("/device", services.getServicesPath());
        assertEquals("/device", services.getDeviceInformationPath());
        services.setServicesPath("/custom-device");
        assertNull(services.getDeviceXaddr());
    }

    @Test
    public void clearingAddressLeavesLegacyPathAvailable() {
        OnvifServices services = new OnvifServices();
        services.setMediaXaddr("http://camera.test/media");
        services.setMediaXaddr(null);
        assertNull(services.getMediaXaddr());
        assertEquals("/media", services.getProfilesPath());
        services.setMediaXaddr("");
        assertEquals("/media", services.getProfilesPath());
    }

    private static OnvifServices parse(String xml) {
        OnvifResponse<?> response = new OnvifResponse<>(new GetServicesRequest());
        response.setXml(xml);
        return new GetServicesParser().parse(response);
    }

    @Test
    public void invalidAddressDoesNotReplacePreviouslyConfiguredAddress() {
        OnvifServices services = new OnvifServices();
        services.setMediaXaddr("https://camera.test/media");
        for (String invalid : new String[]{"/relative", "file:///tmp/media", "http:///missing-host"}) {
            try {
                services.setMediaXaddr(invalid);
                fail("Expected invalid address to be rejected");
            } catch (IllegalArgumentException expected) {
                assertEquals("https://camera.test/media", services.getMediaXaddr());
                assertEquals("/media", services.getProfilesPath());
            }
        }
    }

    @Test
    public void invalidServiceAddressDoesNotPreventParsingOtherServices() {
        OnvifServices services = parse("<Response><Service><Namespace>http://www.onvif.org/ver10/media/wsdl</Namespace>"
                + "<XAddr>not-a-url</XAddr></Service><Service>"
                + "<Namespace>http://www.onvif.org/ver10/events/wsdl</Namespace>"
                + "<XAddr>https://camera.test/events</XAddr></Service></Response>");
        assertNull(services.getMediaXaddr());
        assertEquals("https://camera.test/events", services.getEventsXaddr());
    }
}
