package io.github.hyeonmo.models;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public class PresetCommand {

    public enum PresetAction {
        SAVE,
        MOVE,
        REMOVE
    }

    private final PresetAction action;
    private final String presetToken;

    public PresetCommand(PresetAction action, String presetToken) {
        this.action = action;
        this.presetToken = presetToken;
    }

    public PresetAction getAction() {
        return action;
    }

    public String getPresetToken() {
        return presetToken;
    }

    @Override
    public String toString() {
        return "PresetCommand{" +
                "action=" + action +
                ", presetToken=" + presetToken +
                '}';
    }
}
