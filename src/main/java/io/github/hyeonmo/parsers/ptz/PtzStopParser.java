package io.github.hyeonmo.parsers.ptz;

import io.github.hyeonmo.responses.PtzResponse;

public class PtzStopParser extends PtzBaseParser {
    @Override
    public PtzResponse parse(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new PtzResponse(false, "Response Empty", xml);
        }
        if (xml.contains("<SOAP-ENV:Fault>") || xml.contains("<SOAP:Fault>")) {
            return new PtzResponse(false, "SOAP Fault", xml);
        }
        if (containsTag(xml, "StopResponse")) {
            return new PtzResponse(true, "Stop success", xml);
        }
        return new PtzResponse(false, "Unknown Stop response: " + xml, xml);
    }
}
