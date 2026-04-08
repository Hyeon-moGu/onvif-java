package io.github.hyeonmo.parsers.events;

import io.github.hyeonmo.models.events.EventSubscriptionStatus;
import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.parsers.XMLParserUtils;
import io.github.hyeonmo.responses.OnvifResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;

public class EventSubscriptionStatusParser extends OnvifParser<EventSubscriptionStatus> {

    @Override
    public EventSubscriptionStatus parse(OnvifResponse<?> response) {
        String xml = response.getXml();
        try {
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            XPath xPath = XMLParserUtils.getXPath();

            String currentTime = (String) xPath.evaluate("//*[local-name()='CurrentTime']", doc, XPathConstants.STRING);
            String terminationTime = (String) xPath.evaluate("//*[local-name()='TerminationTime']", doc, XPathConstants.STRING);

            return new EventSubscriptionStatus(
                    currentTime != null ? currentTime.trim() : "",
                    terminationTime != null ? terminationTime.trim() : "",
                    xml);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse event subscription status response", e);
        }
    }
}
