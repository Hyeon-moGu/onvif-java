package io.github.hyeonmo.parsers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
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

import io.github.hyeonmo.DiscoveryMode;
import io.github.hyeonmo.models.Device;
import io.github.hyeonmo.models.DiscoveryType;
import io.github.hyeonmo.models.OnvifDevice;
import io.github.hyeonmo.models.ScopeType;
import io.github.hyeonmo.models.UPnPDevice;
import io.github.hyeonmo.responses.OnvifResponse;

/**
 * Created by Tomas Verhelst on 06/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 *
 *  Modified by Hyeonmo Gu on 17/09/2025
 */
public class DiscoveryParser extends OnvifParser<List<Device>> {

	private static final String LINE_END = "\r\n";
	private static final String KEY_UPNP_LOCATION = "LOCATION: ";
	private static final String KEY_UPNP_SERVER = "SERVER: ";
	private static final String KEY_UPNP_USN = "USN: ";
	private static final String KEY_UPNP_ST = "ST: ";

	private DiscoveryMode mode;
	private String hostName;

	public DiscoveryParser(DiscoveryMode mode) {
		this.mode = mode;
		this.hostName = "";
	}

	@Override
	public List<Device> parse(OnvifResponse response) {
		List<Device> devices = new ArrayList<>();

		switch (mode) {
			case ONVIF:
				devices.addAll(parseOnvif(response));
				break;
			case UPNP:
				devices.add(parseUPnP(response));
				break;
		}

		return devices;
	}

	private List<Device> parseOnvif(OnvifResponse response) {
		List<Device> devices = new ArrayList<>();
		String xml = response.getXml();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

			XPath xPath = XPathFactory.newInstance().newXPath();

			xPath.setNamespaceContext(new NamespaceContext() {
				@Override
				public String getNamespaceURI(String prefix) {
					switch (prefix) {
						case "tns":
							return "http://schemas.xmlsoap.org/ws/2005/04/discovery";
						case "SOAP-ENV":
							return "http://www.w3.org/2003/05/soap-envelope";
						case "wsa":
							return "http://schemas.xmlsoap.org/ws/2004/08/addressing";
						default:
							return null;
					}
				}

				@Override
				public String getPrefix(String namespaceURI) {
					return null;
				}

				@Override
				public Iterator getPrefixes(String namespaceURI) {
					return null;
				}
			});

			NodeList probeMatchNodes = (NodeList) xPath.evaluate("//tns:ProbeMatch", doc, XPathConstants.NODESET);

			for (int i = 0; i < probeMatchNodes.getLength(); i++) {
			    Node probeMatch = probeMatchNodes.item(i);

			    String typeText = xPath.evaluate("tns:Types", probeMatch);
			    String scopes = xPath.evaluate("tns:Scopes", probeMatch);
			    String xAddrs = xPath.evaluate("tns:XAddrs", probeMatch);
			    String metadataVersion = xPath.evaluate("tns:MetadataVersion", probeMatch);

			    if (mode.equals(DiscoveryMode.ONVIF) && typeText.contains(DiscoveryType.NETWORK_VIDEO_TRANSMITTER.type)) {
			    	devices.addAll(parseDevicesFromUri(xAddrs, scopes));
			    }
			}


		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			e.printStackTrace();
		}

		return devices;
	}

	private Device parseUPnP(OnvifResponse response) {
		String header = response.getXml();
		String location = parseUPnPHeader(header, KEY_UPNP_LOCATION);
		String server = parseUPnPHeader(header, KEY_UPNP_SERVER);
		String usn = parseUPnPHeader(header, KEY_UPNP_USN);
		String st = parseUPnPHeader(header, KEY_UPNP_ST);
		return new UPnPDevice(getHostName(), header, location, server, usn, st);
	}

	private List<OnvifDevice> parseDevicesFromUri(String uri, String scope) {
	    List<OnvifDevice> devices = new ArrayList<>();
	    OnvifDevice device = new OnvifDevice(getHostName());

	    String baseUrl = uri.replaceAll("(:\\d+)/.*", "$1");
	    device.setBaseUrl(baseUrl);

	    for (String address : uri.split("\\s+")) {
	        device.addAddress(address);
	    }

	    HashMap<String, Object> map = new HashMap<>();
	    for (String sc : scope.split("\\s+")) {
	        ScopeType type = ScopeType.fromString(sc);
	        if (type != null) {
	            map.put(type.getKeyword(), type.extractValue(sc));
	        }
	    }

	    device.addScope(map);
	    devices.add(device);
	    return devices;
	}

	private String parseUPnPHeader(String header, String whatSearch) {
		int pos = header.indexOf(whatSearch);
		if (pos != -1) {
			pos += whatSearch.length();
			int end = header.indexOf(LINE_END, pos);
			return header.substring(pos, end);
		}
		return "";
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
}
