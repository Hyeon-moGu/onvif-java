package io.github.hyeonmo.parsers.media;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.github.hyeonmo.models.OnvifMediaProfile;
import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.responses.OnvifResponse;

/**
 * Created by Tomas Verhelst on 06/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 *
 */
public class GetMediaProfilesParser extends OnvifParser<List<OnvifMediaProfile>> {

    private static final String KEY_PROFILES = "Profiles";
    private static final String ATTR_TOKEN = "token";
    private static final String ATTR_NAME = "Name";
    private static final String SOURCE_TOKEN = "SourceToken";

    @Override
    public List<OnvifMediaProfile> parse(OnvifResponse response) {
        List<OnvifMediaProfile> profiles = new ArrayList<>();
        String xml = response.getXml();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();

            NodeList profileNodes = (NodeList) xPath.evaluate("//*[local-name()='" + KEY_PROFILES + "']", doc, XPathConstants.NODESET);

            for (int i = 0; i < profileNodes.getLength(); i++) {
                Node profileNode = profileNodes.item(i);
                NamedNodeMap attrs = profileNode.getAttributes();
                String token = "";
                String name = "";
                String videoSourceToken = "";

                if (attrs != null) {
                    Node tokenNode = attrs.getNamedItem(ATTR_TOKEN);
                    if (tokenNode != null) {
                        token = tokenNode.getNodeValue();
                    }
                }

                Node nameNode = (Node) xPath.evaluate("./*[local-name()='" + ATTR_NAME + "']", profileNode, XPathConstants.NODE);
                if (nameNode != null) {
                	name = nameNode.getTextContent().trim();
                }

                Node videoSourceTokenNode = (Node) xPath.evaluate("./*[local-name()='VideoSourceConfiguration']/*[local-name()='" + SOURCE_TOKEN + "']", profileNode, XPathConstants.NODE);
                if (videoSourceTokenNode != null) {
                	videoSourceToken = videoSourceTokenNode.getTextContent().trim();
                }

                NodeList children = profileNode.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);

                    if(!name.equals("") && !videoSourceToken.equals("")) {
                    	break;
                    }

                    // Profile Name
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(ATTR_NAME)) {
                        name = child.getTextContent().trim();
                    }

                    // Profile VideoSourceToken
                    if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(SOURCE_TOKEN)) {
                    	videoSourceToken = child.getTextContent().trim();
                    }
                }

                profiles.add(new OnvifMediaProfile(name, token, videoSourceToken));
            }

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return profiles;
    }
}
