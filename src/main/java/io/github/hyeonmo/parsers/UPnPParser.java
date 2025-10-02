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
import io.github.hyeonmo.upnp.UPnPDeviceInformation;

/**
 * Created by Tomas Verhelst on 06/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 *
 */
public class UPnPParser extends OnvifParser<UPnPDeviceInformation> {

    @Override
    public UPnPDeviceInformation parse(OnvifResponse response) {
    	UPnPDeviceInformation deviceInformation = new UPnPDeviceInformation();
        String xml = response.getXml();

        String[] keys = {
                "deviceType", "friendlyName", "manufacturer", "manufacturerURL",
                "modelDescription", "modelName", "modelNumber", "modelURL",
                "serialNumber", "UDN", "presentationURL", "URLBase"
        };

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();

            for (String key : keys) {
                Node node = (Node) xPath.evaluate("//*[local-name()='" + key + "']", doc, XPathConstants.NODE);
                if (node != null) {
                    String value = node.getTextContent().trim();
                    switch (key) {
                        case "deviceType":
                            deviceInformation.setDeviceType(value);
                            break;
                        case "friendlyName":
                            deviceInformation.setFriendlyName(value);
                            break;
                        case "manufacturer":
                            deviceInformation.setManufacturer(value);
                            break;
                        case "manufacturerURL":
                            deviceInformation.setManufacturerURL(value);
                            break;
                        case "modelDescription":
                            deviceInformation.setModelDescription(value);
                            break;
                        case "modelName":
                            deviceInformation.setModelName(value);
                            break;
                        case "modelNumber":
                            deviceInformation.setModelNumber(value);
                            break;
                        case "modelURL":
                            deviceInformation.setModelURL(value);
                            break;
                        case "serialNumber":
                            deviceInformation.setSerialNumber(value);
                            break;
                        case "UDN":
                            deviceInformation.setUDN(value);
                            break;
                        case "presentationURL":
                            deviceInformation.setPresentationURL(value);
                            break;
                        case "URLBase":
                            deviceInformation.setUrlBase(value);
                            break;
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return deviceInformation;
    }
}
