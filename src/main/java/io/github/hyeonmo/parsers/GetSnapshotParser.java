package io.github.hyeonmo.parsers;

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

import io.github.hyeonmo.responses.OnvifResponse;

/**
 * Created by Hyeonmo Gu on 30/09/2025.
 */
public class GetSnapshotParser extends OnvifParser<String>{

	private static final String KEY_URI = "Uri";

	@Override
	public String parse(OnvifResponse response) {
		String uri = "";
		String xml = response.getXml();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate("//*[local-name()='" + KEY_URI + "']", doc, XPathConstants.NODE);
            if (node != null) {
                uri = node.getTextContent().trim();
            }

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

		return uri;
	}

}
