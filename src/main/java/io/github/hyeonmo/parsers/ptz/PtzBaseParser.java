package io.github.hyeonmo.parsers.ptz;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.github.hyeonmo.responses.PtzResponse;

public abstract class PtzBaseParser {
    public abstract PtzResponse parse(String xml);

    protected String extractAttribute(String xml, String tagName, String attributeName) {
        String cleanTagName = tagName.replaceFirst(".*:", "");
        String patternStr = "<(?:[\\w-]*:)?" + cleanTagName + "[^>]*\\s+" + attributeName + "=\"(.*?)\"";
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "N/A";
    }

    protected String extractValue(String xml, String tagName) {
        // Namespace-agnostic regex
        String cleanTagName = tagName.replaceFirst(".*:", "");
        String patternStr = "<(?:[\\w-]*:)?(" + cleanTagName + ")>(.*?)</(?:[\\w-]*:)?\\1>";
        Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(2).trim();
        }
        return "";
    }

    protected boolean containsTag(String xml, String tagName) {
        String cleanTagName = tagName.replaceFirst(".*:", "");
        String patternStr = "<(?:[\\w-]*:)?(" + cleanTagName + ")[\\s/>]";
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(xml).find();
    }

    protected String extractValueFromTag(String xml, String parentTagName, String childTagName) {
        String cleanParent = parentTagName.replaceFirst(".*:", "");
        String cleanChild = childTagName.replaceFirst(".*:", "");
        
        // Find parent block
        String parentPatternStr = "<(?:[\\w-]*:)?(" + cleanParent + ")[^>]*>(.*?)</(?:[\\w-]*:)?\\1>";
        Pattern parentPattern = Pattern.compile(parentPatternStr, Pattern.DOTALL);
        Matcher parentMatcher = parentPattern.matcher(xml);
        
        if (parentMatcher.find()) {
            String parentContent = parentMatcher.group(2);
            // Find child inside parent
            return extractValue(parentContent, cleanChild);
        }
        return "N/A";
    }
}
