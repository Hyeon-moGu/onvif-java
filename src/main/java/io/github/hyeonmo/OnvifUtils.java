package io.github.hyeonmo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

/**
 * Created by Tomas Verhelst on 06/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 *
 */
public class OnvifUtils {

    /**
     * Util method to retrieve a path from an URL (without IP address and port)
     *
     * @param uri example input: `http://192.168.1.0:8791/cam/realmonitor?audio=1`
     * @example:
     * @result example output: `cam/realmonitor?audio=1`
     */
    public static String getPathFromURL(String uri) {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return "";
        }

        String result = url.getPath();
        if (url.getQuery() != null) {
            result += url.getQuery();
        }

        return result;
    }

    public static String retrieveXAddr(Node parentNode) {
        return retrieveTagValue(parentNode, "XAddr");
    }

    public static String retrieveXAddrs(Node parentNode) {
        return retrieveTagValue(parentNode, "XAddrs");
    }

    private static String retrieveTagValue(Node parentNode, String tagName) {
        if (parentNode == null) return "";

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.newDocument();
            Node importedNode = doc.importNode(parentNode, true);
            doc.appendChild(importedNode);

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate("//" + tagName, doc, XPathConstants.NODE);
            if (node != null) {
                return node.getTextContent().trim();
            }
        } catch (ParserConfigurationException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String retrieveXAddrsFromXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate("//XAddrs", doc, XPathConstants.NODE);
            if (node != null) {
                return node.getTextContent().trim();
            }

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String retrieveXAddrFromXml(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate("//XAddr", doc, XPathConstants.NODE);
            if (node != null) {
                return node.getTextContent().trim();
            }

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return "";
    }
}
