package io.github.hyeonmo.models;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public enum ScopeType {
    PTZ("ptz", true),
    VIDEO_ENCODER("video_encoder", true),
    AUDIO_ENCODER("audio_encoder", true),

    NAME("name", false),
    VERSION("version", false),
    SERIAL("serial", false),
    HARDWARE("hardware", false);

    private final String keyword;
    private final boolean isBoolean;

    ScopeType(String keyword, boolean isBoolean) {
        this.keyword = keyword;
        this.isBoolean = isBoolean;
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public static ScopeType fromString(String s) {
        String lower = s.toLowerCase();
        for (ScopeType type : values()) {
            if (type.isBoolean()) {
                if (lower.contains("/type/" + type.keyword.toLowerCase())) {
                    return type;
                }
            } else {
                if (lower.contains("/" + type.keyword.toLowerCase() + "/")) {
                    return type;
                }
            }
        }
        return null;
    }

    public Object extractValue(String scopeString) {
        if (isBoolean) {
            return true;
        } else {
            int lastSlash = scopeString.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash + 1 < scopeString.length()) {
                return scopeString.substring(lastSlash + 1);
            }
            return "";
        }
    }
}

