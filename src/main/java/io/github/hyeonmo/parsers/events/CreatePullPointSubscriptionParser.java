package io.github.hyeonmo.parsers.events;

import io.github.hyeonmo.models.events.EventSubscription;
import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.parsers.XMLParserUtils;
import io.github.hyeonmo.responses.OnvifResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class CreatePullPointSubscriptionParser extends OnvifParser<EventSubscription> {
    @Override
    public EventSubscription parse(OnvifResponse<?> response) {
        String xml = response.getXml();
        try {
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            XPath xPath = XMLParserUtils.getXPath();

            Node addressNode = (Node) xPath.evaluate("//*[local-name()='SubscriptionReference']/*[local-name()='Address']", doc, XPathConstants.NODE);
            Node referenceParametersNode = (Node) xPath.evaluate("//*[local-name()='SubscriptionReference']/*[local-name()='ReferenceParameters']", doc, XPathConstants.NODE);
            String currentTime = (String) xPath.evaluate("//*[local-name()='CurrentTime']", doc, XPathConstants.STRING);
            String terminationTime = (String) xPath.evaluate("//*[local-name()='TerminationTime']", doc, XPathConstants.STRING);

            return new EventSubscription(
                    addressNode != null ? addressNode.getTextContent().trim() : "",
                    currentTime != null ? currentTime.trim() : "",
                    terminationTime != null ? terminationTime.trim() : "",
                    serializeChildren(referenceParametersNode),
                    xml);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse pull point subscription response", e);
        }
    }

    private String serializeChildren(Node parent) throws Exception {
        if (parent == null) {
            return "";
        }

        StringBuilder xml = new StringBuilder();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            xml.append(nodeToXml(child));
        }
        return xml.toString();
    }

    private String nodeToXml(Node node) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }
}
