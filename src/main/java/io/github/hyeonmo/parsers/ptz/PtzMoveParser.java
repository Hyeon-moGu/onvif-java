package io.github.hyeonmo.parsers.ptz;

import io.github.hyeonmo.responses.PtzResponse;

public class PtzMoveParser extends PtzBaseParser {
    @Override
    public PtzResponse parse(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new PtzResponse(false, "Response Empty", xml);
        }
        if (xml.contains("<SOAP-ENV:Fault>") || xml.contains("<SOAP:Fault>")) {
            return new PtzResponse(false, "SOAP Fault", xml);
        }
        if (containsTag(xml, "ContinuousMoveResponse")) {
            return new PtzResponse(true, "ContinuousMove success", xml);
        }
        return new PtzResponse(false, "Unknown Move response: " + xml, xml);
    }
}
