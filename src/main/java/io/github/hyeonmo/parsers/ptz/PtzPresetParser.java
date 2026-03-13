package io.github.hyeonmo.parsers.ptz;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.github.hyeonmo.models.ptz.Preset;
import io.github.hyeonmo.responses.PtzResponse;

public class PtzPresetParser extends PtzBaseParser {
    // Namespace-agnostic patterns
    private static final String PRESET_TAG_PATTERN = "<(?:[\\w-]*:)?Preset\\s+token=\"(.*?)\">";
    private static final String NAME_TAG_PATTERN = "<(?:[\\w-]*:)?Name>(.*?)</(?:[\\w-]*:)?Name>";
    private static final String PRESET_END_PATTERN = "</(?:[\\w-]*:)?Preset>";

    @Override
    public PtzResponse parse(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new PtzResponse(false, "Response Empty", xml);
        }
        if (xml.contains("<SOAP-ENV:Fault>")) {
            return new PtzResponse(false, "SOAP Fault", xml);
        }

        if (xml.contains("GotoPresetResponse")) {
            return new PtzResponse(true, "MovePreset success", xml);
        }

        if (xml.contains("SetPresetResponse")) {
            String token = extractValue(xml, "PresetToken");
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

    private PtzResponse getPresetsToPtzResponse(String xml) {
        List<Preset> presets = getPresetsParser(xml);
        StringBuilder sb = new StringBuilder();
        for (Preset p : presets) {
            sb.append("{Token='").append(p.getToken()).append("', Name='").append(p.getName()).append("'}\n");
        }
        return new PtzResponse(true, sb.toString(), xml);
    }

    private List<Preset> getPresetsParser(String xml) {
        List<Preset> presets = new ArrayList<>();
        // Extract each <Preset> block and then parse token and name from it
        Pattern blockPattern = Pattern.compile(PRESET_TAG_PATTERN + "(.*?)" + PRESET_END_PATTERN, Pattern.DOTALL);
        Matcher blockMatcher = blockPattern.matcher(xml);
        
        while (blockMatcher.find()) {
            String token = blockMatcher.group(1);
            String innerContent = blockMatcher.group(2);
            
            Pattern namePattern = Pattern.compile(NAME_TAG_PATTERN, Pattern.DOTALL);
            Matcher nameMatcher = namePattern.matcher(innerContent);
            String name = "N/A";
            if (nameMatcher.find()) {
                name = nameMatcher.group(1).trim();
            }
            presets.add(new Preset(token, name));
        }
        return presets;
    }
}
