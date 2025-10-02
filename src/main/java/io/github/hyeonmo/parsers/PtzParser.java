package io.github.hyeonmo.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.hyeonmo.responses.PtzResponse;

public class PtzParser {

	public PtzResponse parser(String type, String xml) {

		if (xml == null || xml.isEmpty()) {
			return new PtzResponse(false, "Response Empty", xml);
		}

		if (xml.contains("<SOAP-ENV:Fault>")) {
			return new PtzResponse(false, "SOAP Fault", xml);
		}

		if (type.equals("move")) {
			return moveParser(xml);
		} else if (type.equals("stop")) {
			return stopParser(xml);
		} else if (type.equals("preset")) {
			return presetParser(xml);
		} else {
			return new PtzResponse(false, "Unknown response", xml);
		}
	}

	private PtzResponse moveParser(String xml) {
		if (xml.contains("ContinuousMoveResponse")) {
			return new PtzResponse(true, "PTZ success", xml);
		}

		return new PtzResponse(false, "Unknown Move response", xml);
	}

	private PtzResponse stopParser(String xml) {
		if (xml.contains("StopResponse")) {
			return new PtzResponse(true, "Stop success", xml);
		}

		return new PtzResponse(false, "Unknown Stop response", xml);
	}

	private PtzResponse presetParser(String xml) {
		if (xml.contains("GotoPresetResponse")) {
			return new PtzResponse(true, "MovePreset success", xml);
		}

		if (xml.contains("SetPresetResponse")) {
			String token = extractValue(xml, "tptz:PresetToken");
			return new PtzResponse(true, "SavePreset success, token=" + token, xml);
		}

		if (xml.contains("RemovePresetResponse")) {
			return new PtzResponse(true, "RemovePreset success", xml);
		}

		return new PtzResponse(false, "Unknown Preset response", xml);
	}

	private String extractValue(String xml, String tagName) {
		String openTag = "<" + tagName + ">";
		String closeTag = "</" + tagName + ">";
		int start = xml.indexOf(openTag);
		int end = xml.indexOf(closeTag);
		if (start != -1 && end != -1 && start < end) {
			return xml.substring(start + openTag.length(), end);
		}
		return "";
	}

    public String parseVideoSourceTokenFromProfile(String xmlResponse) {
        Pattern tagPattern = Pattern.compile("<tt:SourceToken>(.*?)</tt:SourceToken>");
        Matcher tagMatcher = tagPattern.matcher(xmlResponse);
        if (tagMatcher.find()) {
            return tagMatcher.group(1);
        }
        return null;
    }
}
