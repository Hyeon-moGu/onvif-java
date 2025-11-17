package io.github.hyeonmo.parsers.device;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import io.github.hyeonmo.models.OnvifCapabilities;
import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.responses.OnvifResponse;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public class GetCapabilitiesParser extends OnvifParser<OnvifCapabilities> {

    private static final String KEY_DEVICE = "Device";
    private static final String KEY_ANALYTICS = "Analytics";
    private static final String KEY_EVENTS = "Events";
    private static final String KEY_IMAGING = "Imaging";
    private static final String KEY_MEDIA = "Media";
    private static final String KEY_PTZ = "PTZ";
    private static final String KEY_XADDR = "XAddr";

    @Override
    public OnvifCapabilities parse(OnvifResponse response) {
        OnvifCapabilities capabilities = new OnvifCapabilities();
        String xml = response.getXml();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();

            // XAddr
            capabilities.setXaddr(getTagValue(doc, xPath, KEY_DEVICE, KEY_XADDR));
            capabilities.setAnalyticsXaddr(getTagValue(doc, xPath, KEY_ANALYTICS, KEY_XADDR));
            capabilities.setEventsXaddr(getTagValue(doc, xPath, KEY_EVENTS, KEY_XADDR));
            capabilities.setImagingXaddr(getTagValue(doc, xPath, KEY_IMAGING, KEY_XADDR));
            capabilities.setMediaXaddr(getTagValue(doc, xPath, KEY_MEDIA, KEY_XADDR));
            capabilities.setPtzXaddr(getTagValue(doc, xPath, KEY_PTZ, KEY_XADDR));

            // Events
            capabilities.setWsSubscriptionPolicySupport(getBooleanTag(doc, xPath, KEY_EVENTS, "WSSubscriptionPolicySupport"));
            capabilities.setWsPullPointSupport(getBooleanTag(doc, xPath, KEY_EVENTS, "WSPullPointSupport"));
            capabilities.setWsPausableSubscriptionManagerInterfaceSupport(getBooleanTag(doc, xPath, KEY_EVENTS, "WSPausableSubscriptionManagerInterfaceSupport"));

            // Media RTP
            capabilities.setRtpMulticast(getBooleanTag(doc, xPath, KEY_MEDIA, "StreamingCapabilities", "RTPMulticast"));
            capabilities.setRtpTcp(getBooleanTag(doc, xPath, KEY_MEDIA, "StreamingCapabilities", "RTP_TCP"));
            capabilities.setRtpRtspTcp(getBooleanTag(doc, xPath, KEY_MEDIA, "StreamingCapabilities", "RTP_RTSP_TCP"));

            // Media Profile
            String maxProfiles = getTagValue(doc, xPath, KEY_MEDIA, "Extension", "ProfileCapabilities", "MaximumNumberOfProfiles");
            capabilities.setMaximumNumberOfProfiles(maxProfiles.isEmpty() ? 0 : Integer.parseInt(maxProfiles));

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return capabilities;
    }

    private String getTagValue(Document doc, XPath xPath, String... tags) throws XPathExpressionException {
        if (tags == null || tags.length == 0) {
            return "";
        }

        StringBuilder expression = new StringBuilder("//*[local-name()='").append(tags[0]).append("']");
        for (int i = 1; i < tags.length; i++) {
            expression.append("/*[local-name()='").append(tags[i]).append("']");
        }

        Node node = (Node) xPath.evaluate(expression.toString(), doc, XPathConstants.NODE);
        return node != null ? node.getTextContent().trim() : "";
    }

    private boolean getBooleanTag(Document doc, XPath xPath, String... tags) throws XPathExpressionException {
        String val = getTagValue(doc, xPath, tags);
        return "true".equalsIgnoreCase(val);
    }
}
