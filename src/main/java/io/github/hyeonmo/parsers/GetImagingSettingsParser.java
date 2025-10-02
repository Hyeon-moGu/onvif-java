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

import io.github.hyeonmo.models.ImagingSettings;
import io.github.hyeonmo.responses.ImagingResponse;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class GetImagingSettingsParser extends ImagingParser<ImagingSettings> {

    @Override
    public ImagingSettings parse(ImagingResponse response) {
        ImagingSettings imagingSettings = new ImagingSettings();
        String xml = response.getXml();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XPathFactory.newInstance().newXPath();

            // Backlight
            imagingSettings.setBacklightCompensationMode(getTagValue(doc, xPath, "BacklightCompensation", "Mode"));

            // Brightness/Contrast/Saturation/Sharpness
            imagingSettings.setBrightness(getIntTag(doc, xPath, "Brightness"));
            imagingSettings.setColorSaturation(getIntTag(doc, xPath, "ColorSaturation"));
            imagingSettings.setContrast(getIntTag(doc, xPath, "Contrast"));
            imagingSettings.setSharpness(getIntTag(doc, xPath, "Sharpness"));

            // Exposure
            imagingSettings.setExposureMode(getTagValue(doc, xPath, "Exposure", "Mode"));
            imagingSettings.setMinExposureTime(getDoubleTag(doc, xPath, "Exposure", "MinExposureTime"));
            imagingSettings.setMaxExposureTime(getDoubleTag(doc, xPath, "Exposure", "MaxExposureTime"));
            imagingSettings.setMinGain(getDoubleTag(doc, xPath, "Exposure", "MinGain"));
            imagingSettings.setMaxGain(getDoubleTag(doc, xPath, "Exposure", "MaxGain"));
            imagingSettings.setMinIris(getDoubleTag(doc, xPath, "Exposure", "MinIris"));
            imagingSettings.setMaxIris(getDoubleTag(doc, xPath, "Exposure", "MaxIris"));

            // Focus
            imagingSettings.setAutofocusMode(getTagValue(doc, xPath, "Focus", "AutoFocusMode"));
            imagingSettings.setDefaultFocusSpeed(getDoubleTag(doc, xPath, "Focus", "DefaultSpeed"));

            // IR Cut
            imagingSettings.setIrCutFilter(getTagValue(doc, xPath, "IrCutFilter"));

            // WDR
            imagingSettings.setWdrMode(getTagValue(doc, xPath, "WideDynamicRange", "Mode"));

            // White Balance
            imagingSettings.setWhiteBalanceMode(getTagValue(doc, xPath, "WhiteBalance", "Mode"));

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return imagingSettings;
    }

    private String getTagValue(Document doc, XPath xPath, String... tags) throws XPathExpressionException {
        if (tags == null || tags.length == 0) {
            return "";
        }

        StringBuilder expression = new StringBuilder("//*[local-name()='").append(tags[0]).append("']");
        for (int i = 1; i < tags.length; i++) {
            expression.append("/*[local-name()='").append(tags[i]).append("']");
        }

        Node node = (Node) xPath.evaluate(expression.toString(), doc, XPathConstants.NODE);
        return node != null ? node.getTextContent().trim() : "";
    }

    private int getIntTag(Document doc, XPath xPath, String... tags) throws XPathExpressionException {
        String val = getTagValue(doc, xPath, tags);
        return val.isEmpty() ? 0 : Integer.parseInt(val);
    }

    private double getDoubleTag(Document doc, XPath xPath, String... tags) throws XPathExpressionException {
        String val = getTagValue(doc, xPath, tags);
		return val.isEmpty() ? 0.0 : Double.parseDouble(val);
	}
}
