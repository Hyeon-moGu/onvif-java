package io.github.hyeonmo.parsers.device;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

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
import org.xml.sax.SAXException;

import io.github.hyeonmo.models.OnvifDeviceInformation;
import io.github.hyeonmo.parsers.OnvifParser;
import io.github.hyeonmo.responses.OnvifResponse;

public class GetDeviceInformationParser extends OnvifParser<OnvifDeviceInformation> {

	private static final String KEY_MANUFACTURER = "Manufacturer";
	private static final String KEY_MODEL = "Model";
	private static final String KEY_FIRMWARE_VERSION = "FirmwareVersion";
	private static final String KEY_SERIAL_NUMBER = "SerialNumber";
	private static final String KEY_HARDWARE_ID = "HardwareId";

	@Override
	public OnvifDeviceInformation parse(OnvifResponse response) {
		OnvifDeviceInformation deviceInformation = new OnvifDeviceInformation();
		String xml = response.getXml();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

			XPath xPath = XPathFactory.newInstance().newXPath();

			xPath.setNamespaceContext(new DeviceNamespaceContext());

			deviceInformation.setManufacturer(getTagValue(doc, xPath, KEY_MANUFACTURER));
			deviceInformation.setModel(getTagValue(doc, xPath, KEY_MODEL));
			deviceInformation.setFirmwareVersion(getTagValue(doc, xPath, KEY_FIRMWARE_VERSION));
			deviceInformation.setSerialNumber(getTagValue(doc, xPath, KEY_SERIAL_NUMBER));
			deviceInformation.setHardwareId(getTagValue(doc, xPath, KEY_HARDWARE_ID));

		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			e.printStackTrace();
		}

		return deviceInformation;
	}

	private String getTagValue(Document doc, XPath xPath, String tagName) throws XPathExpressionException {
		String xpathQuery = "//tds:" + tagName;

		Node node = (Node) xPath.evaluate(xpathQuery, doc, XPathConstants.NODE);
		return node != null ? node.getTextContent().trim() : "";
	}

	private static class DeviceNamespaceContext implements NamespaceContext {
		private static final String TDS_URI = "http://www.onvif.org/ver10/device/wsdl";

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix.equals("tds")) {
				return TDS_URI;
			}
			return null;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			if (namespaceURI.equals(TDS_URI)) {
				return "tds";
			}
			return null;
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			return null;
		}
	}
}