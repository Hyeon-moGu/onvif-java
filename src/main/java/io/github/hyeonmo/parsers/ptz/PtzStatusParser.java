package io.github.hyeonmo.parsers.ptz;

import io.github.hyeonmo.responses.PtzResponse;

public class PtzStatusParser extends PtzBaseParser {
    @Override
    public PtzResponse parse(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new PtzResponse(false, "Response Empty", xml);
        }
        if (xml.contains("<SOAP-ENV:Fault>") || xml.contains("<SOAP:Fault>")) {
            return new PtzResponse(false, "SOAP Fault", xml);
        }
        if (!containsTag(xml, "GetStatusResponse")) {
            return new PtzResponse(false, "Unknown Status response: " + xml, xml);
        }

        String panTiltX = extractAttribute(xml, "PanTilt", "x");
        String panTiltY = extractAttribute(xml, "PanTilt", "y");
        String zoomX = extractAttribute(xml, "Zoom", "x");
        String moveStatusPanTilt = extractValueFromTag(xml, "MoveStatus", "PanTilt");
        String moveStatusZoom = extractValueFromTag(xml, "MoveStatus", "Zoom");
        String utcTime = extractValue(xml, "UtcTime");

        String message = String.format("Position: (Pan: %s, Tilt: %s, Zoom: %s), MoveStatus: (PanTilt: %s, Zoom: %s), Time: %s", panTiltX, panTiltY, zoomX, moveStatusPanTilt, moveStatusZoom, utcTime);

        return new PtzResponse(true, message, xml);
    }
}
