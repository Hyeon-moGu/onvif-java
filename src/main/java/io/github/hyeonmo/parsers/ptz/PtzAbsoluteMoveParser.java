package io.github.hyeonmo.parsers.ptz;

import io.github.hyeonmo.responses.PtzResponse;

public class PtzAbsoluteMoveParser extends PtzBaseParser {
    @Override
    public PtzResponse parse(String xml) {
        if (xml == null || xml.isEmpty())
            return new PtzResponse(false, "Response Empty", xml);
        if (containsTag(xml, "Fault"))
            return new PtzResponse(false, "SOAP Fault", xml);
        if (containsTag(xml, "AbsoluteMoveResponse"))
            return new PtzResponse(true, "AbsoluteMove success", xml);
        return new PtzResponse(false, "Unknown AbsoluteMove response: " + xml, xml);
    }
}
