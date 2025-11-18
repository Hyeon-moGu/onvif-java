package io.github.hyeonmo.parsers.ptz;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.hyeonmo.models.ptz.Preset;
import io.github.hyeonmo.responses.PtzResponse;

public class PtzParser {

	private static final Pattern PRESET_PATTERN = Pattern.compile("<tptz:Preset\\s+token=\"(.*?)\">\\s*<tt:Name>(.*?)</tt:Name>\\s*</tptz:Preset>");

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
		} else if (type.equals("status")) {
			return statusParser(xml);
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

		if (xml.contains("GetPresetsResponse")) {
			return getPresetsToPtzResponse(xml);
		}

		return new PtzResponse(false, "Unknown Preset response", xml);
	}

	public PtzResponse getPresetsToPtzResponse(String xml) {
		List<Preset> presets = getPresetsParser(xml);

		StringBuilder sb = new StringBuilder();
		for (Preset p : presets) {
			sb.append("{Token='").append(p.getToken()).append("', Name='").append(p.getName()).append("'}\n");
		}

		return new PtzResponse(true, sb.toString(), xml);
	}

	public List<Preset> getPresetsParser(String xml) {
		List<Preset> presets = new ArrayList<Preset>();

		if (!xml.contains("GetPresetsResponse")) {
			return presets;
		}

		Matcher matcher = PRESET_PATTERN.matcher(xml);

		while (matcher.find()) {
			String token = matcher.group(1);
			String name = matcher.group(2);

			presets.add(new Preset(token, name));
		}

		return presets;
	}

	private PtzResponse statusParser(String xml) {
		if (!xml.contains("GetStatusResponse")) {
			return new PtzResponse(false, "Unknown Status response", xml);
		}

		String panTiltX = extractAttribute(xml, "tt:PanTilt", "x");
		String panTiltY = extractAttribute(xml, "tt:PanTilt", "y");

		String zoomX = extractAttribute(xml, "tt:Zoom", "x");

		String moveStatusPanTilt = extractValueFromTag(xml, "tt:MoveStatus", "tt:PanTilt");
		String moveStatusZoom = extractValueFromTag(xml, "tt:MoveStatus", "tt:Zoom");

		String utcTime = extractValue(xml, "tt:UtcTime");

		String message = String.format("Position: (Pan: %s, Tilt: %s, Zoom: %s), MoveStatus: (PanTilt: %s, Zoom: %s), Time: %s", panTiltX, panTiltY, zoomX, moveStatusPanTilt, moveStatusZoom, utcTime);

		return new PtzResponse(true, message, xml);
	}

	private String extractAttribute(String xml, String tagName, String attributeName) {
		String patternStr = "<" + tagName + "[^>]*" + attributeName + "=\"(.*?)\"";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(xml);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "N/A";
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

	private String extractValueFromTag(String xml, String parentTagName, String childTagName) {
		String parentOpenTag = "<" + parentTagName + ">";
		String parentCloseTag = "</" + parentTagName + ">";

		int parentStart = xml.indexOf(parentOpenTag);
		int parentEnd = xml.indexOf(parentCloseTag);

		if (parentStart != -1 && parentEnd != -1) {
			String parentContent = xml.substring(parentStart + parentOpenTag.length(), parentEnd);

			String childOpenTag = "<" + childTagName + ">";
			String childCloseTag = "</" + childTagName + ">";

			int childStart = parentContent.indexOf(childOpenTag);
			int childEnd = parentContent.indexOf(childCloseTag);

			if (childStart != -1 && childEnd != -1) {
				return parentContent.substring(childStart + childOpenTag.length(), childEnd);
			}
		}
		return "N/A";
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
