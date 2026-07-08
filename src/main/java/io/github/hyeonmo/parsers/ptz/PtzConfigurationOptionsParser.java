package io.github.hyeonmo.parsers.ptz;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.github.hyeonmo.exceptions.OnvifException;
import io.github.hyeonmo.exceptions.OnvifSoapException;
import io.github.hyeonmo.models.ptz.PtzConfigurationOptions;
import io.github.hyeonmo.models.ptz.PtzRange;
import io.github.hyeonmo.models.ptz.PtzSpace;
import io.github.hyeonmo.parsers.XMLParserUtils;

public class PtzConfigurationOptionsParser implements PtzResponseParser<PtzConfigurationOptions> {
    @Override
    public PtzConfigurationOptions parse(String xml) {
        if (xml == null || xml.isEmpty())
            throw new OnvifSoapException("PTZ configuration options response is empty.", xml);
        try {
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            Document doc =
                    builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            XPath xpath = XMLParserUtils.getXPath();
            Node response =
                    (Node) xpath.evaluate("//*[local-name()='GetConfigurationOptionsResponse']",
                            doc, XPathConstants.NODE);
            if (response == null)
                throw new OnvifSoapException("GetConfigurationOptionsResponse was not found.", xml);

            return new PtzConfigurationOptions(
                    parseSpace(xpath, response, "AbsolutePanTiltPositionSpace", true),
                    parseSpace(xpath, response, "AbsoluteZoomPositionSpace", false),
                    // ONVIF models PanTiltSpeedSpace as a one-dimensional speed
                    // magnitude range even though PTZSpeed carries a Vector2D.
                    parseSpace(xpath, response, "PanTiltSpeedSpace", false),
                    parseSpace(xpath, response, "ZoomSpeedSpace", false));
        } catch (OnvifException e) {
            throw e;
        } catch (Exception e) {
            throw new OnvifSoapException("Failed to parse PTZ configuration options.", xml, e);
        }
    }

    private static PtzSpace parseSpace(XPath xpath, Node parent, String localName,
            boolean twoDimensional) throws Exception {
        Node space = (Node) xpath.evaluate(".//*[local-name()='" + localName + "'][1]", parent,
                XPathConstants.NODE);
        if (space == null)
            return null;
        String uri = text(xpath, space, "URI");
        PtzRange xRange = parseRange(xpath, space, "XRange");
        PtzRange yRange = twoDimensional ? parseRange(xpath, space, "YRange") : null;
        if (xRange == null || (twoDimensional && yRange == null)) {
            throw new OnvifSoapException(localName + " has incomplete range information.", null);
        }
        return new PtzSpace(uri, xRange, yRange);
    }

    private static PtzRange parseRange(XPath xpath, Node space, String rangeName) throws Exception {
        Node range = (Node) xpath.evaluate("./*[local-name()='" + rangeName + "']", space,
                XPathConstants.NODE);
        if (range == null)
            return null;
        String min = text(xpath, range, "Min");
        String max = text(xpath, range, "Max");
        if (min == null || max == null)
            return null;
        return new PtzRange(Double.parseDouble(min), Double.parseDouble(max));
    }

    private static String text(XPath xpath, Node parent, String localName) throws Exception {
        Node node = (Node) xpath.evaluate("./*[local-name()='" + localName + "']", parent,
                XPathConstants.NODE);
        return node == null ? null : node.getTextContent().trim();
    }
}
