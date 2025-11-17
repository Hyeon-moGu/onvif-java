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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.github.hyeonmo.OnvifUtils;
import io.github.hyeonmo.models.OnvifServices;
import io.github.hyeonmo.models.OnvifType;
import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.responses.OnvifResponse;

/**
 * Created by Tomas Verhelst on 06/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 *
 */
public class GetServicesParser extends OnvifParser<OnvifServices> {

    @Override
    public OnvifServices parse(OnvifResponse response) {
        OnvifServices services = new OnvifServices();
        String xml = response.getXml();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList namespaceNodes = (NodeList) xPath.evaluate("//*[local-name()='Namespace']", doc, XPathConstants.NODESET);

            for (int i = 0; i < namespaceNodes.getLength(); i++) {
                Node nsNode = namespaceNodes.item(i);
                String currentNamespace = nsNode.getTextContent().trim();

                Node xAddrNode = (Node) xPath.evaluate("following-sibling::*[local-name()='XAddr']", nsNode, XPathConstants.NODE);
                if (xAddrNode != null) {
                    String uri = xAddrNode.getTextContent().trim();

                    if (currentNamespace.equals(OnvifType.GET_DEVICE_INFORMATION.namespace)) {
                        services.setDeviceInformationPath(OnvifUtils.getPathFromURL(uri));
                    } else if (currentNamespace.equals(OnvifType.GET_MEDIA_PROFILES.namespace)
                            || currentNamespace.equals(OnvifType.GET_STREAM_URI.namespace)) {
                        String path = OnvifUtils.getPathFromURL(uri);
                        services.setProfilesPath(path);
                        services.setStreamURIPath(path);
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return services;
    }
}
