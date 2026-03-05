package io.github.hyeonmo.parsers.device;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

public class GetSystemDateAndTimeParser extends OnvifParser<Date> {

    @Override
    public Date parse(OnvifResponse response) {
        Date deviceDate = null;
        String xml = response.getXml();

        try {
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            XPath xPath = XMLParserUtils.getXPath();

            // Find UTCDateTime > Date and Time
            Node utcDateTimeNode = (Node) xPath.evaluate("//*[local-name()='UTCDateTime']", doc, XPathConstants.NODE);
            if (utcDateTimeNode != null) {
                int year = getIntTag(utcDateTimeNode, xPath, "Date", "Year");
                int month = getIntTag(utcDateTimeNode, xPath, "Date", "Month");
                int day = getIntTag(utcDateTimeNode, xPath, "Date", "Day");
                int hour = getIntTag(utcDateTimeNode, xPath, "Time", "Hour");
                int minute = getIntTag(utcDateTimeNode, xPath, "Time", "Minute");
                int second = getIntTag(utcDateTimeNode, xPath, "Time", "Second");

                if (year > 0 && month > 0 && day > 0) {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month - 1); // Calendar month is 0-indexed
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                    cal.set(Calendar.SECOND, second);
                    cal.set(Calendar.MILLISECOND, 0);
                    deviceDate = cal.getTime();
                }
            }

        } catch (SAXException | IOException | XPathExpressionException e) {
            e.printStackTrace();
        }

        return deviceDate;
    }

    private int getIntTag(Node parentNode, XPath xPath, String type, String tag) throws XPathExpressionException {
        Node node = (Node) xPath.evaluate(".//*[local-name()='" + type + "']/*[local-name()='" + tag + "']", parentNode, XPathConstants.NODE);
        if (node != null) {
            try {
                return Integer.parseInt(node.getTextContent().trim());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return 0;
    }
}
