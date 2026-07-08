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
import io.github.hyeonmo.models.ptz.PtzPosition;
import io.github.hyeonmo.parsers.XMLParserUtils;

public class PtzPositionParser implements PtzResponseParser<PtzPosition> {
    @Override
    public PtzPosition parse(String xml) {
        if (xml == null || xml.isEmpty())
            throw new OnvifSoapException("PTZ status response is empty.", xml);
        try {
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            Document doc =
                    builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            XPath xpath = XMLParserUtils.getXPath();
            Node response = (Node) xpath.evaluate("//*[local-name()='GetStatusResponse']", doc,
                    XPathConstants.NODE);
            if (response == null)
                throw new OnvifSoapException("GetStatusResponse was not found.", xml);

            Node panTilt =
                    (Node) xpath.evaluate(".//*[local-name()='Position']/*[local-name()='PanTilt']",
                            response, XPathConstants.NODE);
            Node zoom =
                    (Node) xpath.evaluate(".//*[local-name()='Position']/*[local-name()='Zoom']",
                            response, XPathConstants.NODE);
            if (panTilt == null && zoom == null)
                throw new OnvifSoapException("PTZ position was not found.", xml);

            Double pan = attributeAsDouble(panTilt, "x");
            Double tilt = attributeAsDouble(panTilt, "y");
            Double zoomValue = attributeAsDouble(zoom, "x");
            return new PtzPosition(pan, tilt, zoomValue, attribute(panTilt, "space"),
                    attribute(zoom, "space"));
        } catch (OnvifException e) {
            throw e;
        } catch (Exception e) {
            throw new OnvifSoapException("Failed to parse PTZ position.", xml, e);
        }
    }

    private static Double attributeAsDouble(Node node, String name) {
        String value = attribute(node, name);
        if (value == null || value.isEmpty())
            return null;
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new OnvifSoapException("Invalid PTZ " + name + " coordinate: " + value, null, e);
        }
    }

    private static String attribute(Node node, String name) {
        if (node == null || node.getAttributes() == null
                || node.getAttributes().getNamedItem(name) == null)
            return null;
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }
}
