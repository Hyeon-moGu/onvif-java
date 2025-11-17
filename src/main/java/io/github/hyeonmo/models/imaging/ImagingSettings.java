package io.github.hyeonmo.models.imaging;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingSettings {

    // Backlight
    private String backlightCompensationMode;

    // Brightness/Contrast/Saturation
    private int brightness;
    private int colorSaturation;
    private int contrast;
    private int sharpness;

    // Exposure
    private String exposureMode;
    private double minExposureTime;
    private double maxExposureTime;
    private double minGain;
    private double maxGain;
    private double minIris;
    private double maxIris;

    // Focus
    private String autofocusMode;
    private double defaultFocusSpeed;

    // IR Cut
    private String irCutFilter;

    // Wide Dynamic Range
    private String wdrMode;

    // White Balance
    private String whiteBalanceMode;

    // Constructors
    public ImagingSettings() {
    }

    // Getters / Setters
    public String getBacklightCompensationMode() {
        return backlightCompensationMode;
    }

    public void setBacklightCompensationMode(String backlightCompensationMode) {
        this.backlightCompensationMode = backlightCompensationMode;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getColorSaturation() {
        return colorSaturation;
    }

    public void setColorSaturation(int colorSaturation) {
        this.colorSaturation = colorSaturation;
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public int getSharpness() {
        return sharpness;
    }

    public void setSharpness(int sharpness) {
        this.sharpness = sharpness;
    }

    public String getExposureMode() {
        return exposureMode;
    }

    public void setExposureMode(String exposureMode) {
        this.exposureMode = exposureMode;
    }

    public double getMinExposureTime() {
        return minExposureTime;
    }

    public void setMinExposureTime(double minExposureTime) {
        this.minExposureTime = minExposureTime;
    }

    public double getMaxExposureTime() {
        return maxExposureTime;
    }

    public void setMaxExposureTime(double maxExposureTime) {
        this.maxExposureTime = maxExposureTime;
    }

    public double getMinGain() {
        return minGain;
    }

    public void setMinGain(double minGain) {
        this.minGain = minGain;
    }

    public double getMaxGain() {
        return maxGain;
    }

    public void setMaxGain(double maxGain) {
        this.maxGain = maxGain;
    }

    public double getMinIris() {
        return minIris;
    }

    public void setMinIris(double minIris) {
        this.minIris = minIris;
    }

    public double getMaxIris() {
        return maxIris;
    }

    public void setMaxIris(double maxIris) {
        this.maxIris = maxIris;
    }

    public String getAutofocusMode() {
        return autofocusMode;
    }

    public void setAutofocusMode(String autofocusMode) {
        this.autofocusMode = autofocusMode;
    }

    public double getDefaultFocusSpeed() {
        return defaultFocusSpeed;
    }

    public void setDefaultFocusSpeed(double defaultFocusSpeed) {
        this.defaultFocusSpeed = defaultFocusSpeed;
    }

    public String getIrCutFilter() {
        return irCutFilter;
    }

    public void setIrCutFilter(String irCutFilter) {
        this.irCutFilter = irCutFilter;
    }

    public String getWdrMode() {
        return wdrMode;
    }

    public void setWdrMode(String wdrMode) {
        this.wdrMode = wdrMode;
    }

    public String getWhiteBalanceMode() {
        return whiteBalanceMode;
    }

    public void setWhiteBalanceMode(String whiteBalanceMode) {
        this.whiteBalanceMode = whiteBalanceMode;
    }

    @Override
    public String toString() {
        return "ImagingSettings{" +
                "backlightCompensationMode='" + backlightCompensationMode + '\'' +
                ", brightness=" + brightness +
                ", colorSaturation=" + colorSaturation +
                ", contrast=" + contrast +
                ", sharpness=" + sharpness +
                ", exposureMode='" + exposureMode + '\'' +
                ", minExposureTime=" + minExposureTime +
                ", maxExposureTime=" + maxExposureTime +
                ", minGain=" + minGain +
                ", maxGain=" + maxGain +
                ", minIris=" + minIris +
                ", maxIris=" + maxIris +
                ", autofocusMode='" + autofocusMode + '\'' +
                ", defaultFocusSpeed=" + defaultFocusSpeed +
                ", irCutFilter='" + irCutFilter + '\'' +
                ", wdrMode='" + wdrMode + '\'' +
                ", whiteBalanceMode='" + whiteBalanceMode + '\'' +
                '}';
    }
}
