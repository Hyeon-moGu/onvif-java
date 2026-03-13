package io.github.hyeonmo.parsers.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.parsers.XMLParserUtils;
import io.github.hyeonmo.responses.OnvifResponse;

public class GetSnapshotParser extends OnvifParser<String>{

	private static final String KEY_URI = "Uri";

	@Override
	public String parse(OnvifResponse response) {
		String uri = "";
		String xml = response.getXml();

        try {
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XMLParserUtils.getXPath();
            Node node = (Node) xPath.evaluate("//*[local-name()='" + KEY_URI + "']", doc, XPathConstants.NODE);
            if (node != null) {
                uri = node.getTextContent().trim();
            }

        } catch (SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

		return uri;
	}

}
