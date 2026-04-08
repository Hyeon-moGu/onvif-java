package io.github.hyeonmo.parsers.events;

import io.github.hyeonmo.models.events.Event;
import io.github.hyeonmo.models.events.EventItem;
import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.parsers.XMLParserUtils;
import io.github.hyeonmo.responses.OnvifResponse;
import io.github.hyeonmo.responses.events.PullMessagesResponse;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
import java.util.ArrayList;
import java.util.List;

public class PullMessagesParser extends OnvifParser<PullMessagesResponse> {
    @Override
    public PullMessagesResponse parse(OnvifResponse<?> response) {
        String xml = response.getXml();
        List<Event> events = new ArrayList<>();
        String currentTime = "";
        String terminationTime = "";

        try {
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            XPath xPath = XMLParserUtils.getXPath();

            currentTime = (String) xPath.evaluate("//*[local-name()='CurrentTime']", doc, XPathConstants.STRING);
            terminationTime = (String) xPath.evaluate("//*[local-name()='TerminationTime']", doc, XPathConstants.STRING);

            NodeList messageNodes = (NodeList) xPath.evaluate("//*[local-name()='NotificationMessage']", doc, XPathConstants.NODESET);
            for (int i = 0; i < messageNodes.getLength(); i++) {
                Node msgNode = messageNodes.item(i);
                String topic = (String) xPath.evaluate(".//*[local-name()='Topic']", msgNode, XPathConstants.STRING);
                Node messageNode = (Node) xPath.evaluate(".//*[local-name()='Message']", msgNode, XPathConstants.NODE);
                String utcTime = getAttribute(messageNode, "UtcTime");
                String propertyOperation = getAttribute(messageNode, "PropertyOperation");
                Node sourceNode = (Node) xPath.evaluate(".//*[local-name()='Source']", msgNode, XPathConstants.NODE);
                Node dataNode = (Node) xPath.evaluate(".//*[local-name()='Data']", msgNode, XPathConstants.NODE);

                events.add(new Event(
                        topic != null ? topic.trim() : "",
                        readSimpleItems(sourceNode),
                        readSimpleItems(dataNode),
                        utcTime,
                        propertyOperation,
                        nodeToXml(msgNode)));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PullMessages response", e);
        }

        return new PullMessagesResponse(events, currentTime, terminationTime, xml);
    }

    private List<EventItem> readSimpleItems(Node parent) {
        List<EventItem> items = new ArrayList<>();
        if (parent == null) {
            return items;
        }

        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE || !"SimpleItem".equals(child.getLocalName())) {
                continue;
            }
            items.add(new EventItem(getAttribute(child, "Name"), getAttribute(child, "Value")));
        }

        return items;
    }

    private String getAttribute(Node node, String name) {
        if (node == null) {
            return "";
        }

        NamedNodeMap attrs = node.getAttributes();
        if (attrs == null) {
            return "";
        }

        Node attr = attrs.getNamedItem(name);
        return attr != null ? attr.getNodeValue().trim() : "";
    }

    private String nodeToXml(Node node) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }
}
