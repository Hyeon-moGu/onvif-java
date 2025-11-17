package  io.github.hyeonmo.requests.imaging;

import io.github.hyeonmo.listeners.imaging.ImagingSettingRequestListener;
import io.github.hyeonmo.models.imaging.ImagingSettings;
import io.github.hyeonmo.models.imaging.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingSettingRequest implements ImagingRequest {

    //Constants
    public static final String TAG = ImagingSettingRequest.class.getSimpleName();

    //Attributes
    private final ImagingSettingRequestListener listener;
    private final String token;
    private final String xaddr;
    private final ImagingSettings imagingSettings;

    private static final String FORCE_PERSISTENCE = "true";

    public ImagingSettingRequest(ImagingSettingRequestListener listener, String token, String xaddr, ImagingSettings imagingSettings) {
        this.listener = listener;
        this.token = token;
        this.xaddr = xaddr;
        this.imagingSettings = imagingSettings;
    }

    public ImagingSettingRequestListener getListener() {
        return listener;
    }

    @Override
    public String getXml() {
        StringBuilder imagingSettingsContent = new StringBuilder();

        // 1. Brightness/Contrast/Saturation/Sharpness (int)
        if (imagingSettings.getBrightness() != 0) {
            imagingSettingsContent.append("<tt:Brightness>").append(imagingSettings.getBrightness()).append("</tt:Brightness>");
        }
        if (imagingSettings.getColorSaturation() != 0) {
            imagingSettingsContent.append("<tt:ColorSaturation>").append(imagingSettings.getColorSaturation()).append("</tt:ColorSaturation>");
        }
        if (imagingSettings.getContrast() != 0) {
            imagingSettingsContent.append("<tt:Contrast>").append(imagingSettings.getContrast()).append("</tt:Contrast>");
        }
        if (imagingSettings.getSharpness() != 0) {
            imagingSettingsContent.append("<tt:Sharpness>").append(imagingSettings.getSharpness()).append("</tt:Sharpness>");
        }

        // 2. Backlight Compensation (String)
        if (imagingSettings.getBacklightCompensationMode() != null) {
            imagingSettingsContent.append("<tt:BacklightCompensation>");
            imagingSettingsContent.append("<tt:Mode>").append(imagingSettings.getBacklightCompensationMode()).append("</tt:Mode>");
            imagingSettingsContent.append("</tt:BacklightCompensation>");
        }

        // 3. Wide Dynamic Range (String)
        if (imagingSettings.getWdrMode() != null) {
            imagingSettingsContent.append("<tt:WideDynamicRange>");
            imagingSettingsContent.append("<tt:Mode>").append(imagingSettings.getWdrMode()).append("</tt:Mode>");
            imagingSettingsContent.append("</tt:WideDynamicRange>");
        }

        // 4. IR Cut Filter (String)
        if (imagingSettings.getIrCutFilter() != null) {
            imagingSettingsContent.append("<tt:IrCutFilter>").append(imagingSettings.getIrCutFilter()).append("</tt:IrCutFilter>");
        }

        // 5. Exposure (String/double)
        if (imagingSettings.getExposureMode() != null) {
            imagingSettingsContent.append("<tt:Exposure>");
            imagingSettingsContent.append("<tt:Mode>").append(imagingSettings.getExposureMode()).append("</tt:Mode>");

            if (imagingSettings.getMinExposureTime() != 0.0) {
                imagingSettingsContent.append("<tt:MinExposureTime>").append(imagingSettings.getMinExposureTime()).append("</tt:MinExposureTime>");
            }
            if (imagingSettings.getMaxExposureTime() != 0.0) {
                imagingSettingsContent.append("<tt:MaxExposureTime>").append(imagingSettings.getMaxExposureTime()).append("</tt:MaxExposureTime>");
            }
            if (imagingSettings.getMinGain() != 0.0) {
                imagingSettingsContent.append("<tt:MinGain>").append(imagingSettings.getMinGain()).append("</tt:MinGain>");
            }
            if (imagingSettings.getMaxGain() != 0.0) {
                imagingSettingsContent.append("<tt:MaxGain>").append(imagingSettings.getMaxGain()).append("</tt:MaxGain>");
            }
            if (imagingSettings.getMinIris() != 0.0) {
                imagingSettingsContent.append("<tt:MinIris>").append(imagingSettings.getMinIris()).append("</tt:MinIris>");
            }
            if (imagingSettings.getMaxIris() != 0.0) {
                imagingSettingsContent.append("<tt:MaxIris>").append(imagingSettings.getMaxIris()).append("</tt:MaxIris>");
            }

            imagingSettingsContent.append("</tt:Exposure>");
        }

        // 6. Focus (String/double)
        if (imagingSettings.getAutofocusMode() != null || imagingSettings.getDefaultFocusSpeed() != 0.0) {
            imagingSettingsContent.append("<tt:Focus>");

            if (imagingSettings.getAutofocusMode() != null) {
                imagingSettingsContent.append("<tt:AutoFocusMode>").append(imagingSettings.getAutofocusMode()).append("</tt:AutoFocusMode>");
            }
            if (imagingSettings.getDefaultFocusSpeed() != 0.0) {
                imagingSettingsContent.append("<tt:DefaultSpeed>").append(imagingSettings.getDefaultFocusSpeed()).append("</tt:DefaultSpeed>");
            }

            imagingSettingsContent.append("</tt:Focus>");
        }

        // 7. White Balance (String)
        if (imagingSettings.getWhiteBalanceMode() != null) {
            imagingSettingsContent.append("<tt:WhiteBalance>");
            imagingSettingsContent.append("<tt:Mode>").append(imagingSettings.getWhiteBalanceMode()).append("</tt:Mode>");
            imagingSettingsContent.append("</tt:WhiteBalance>");
        }

        if (imagingSettingsContent.length() == 0) {
            return "";
        }

        return "<timg:SetImagingSettings>" +
                "<timg:VideoSourceToken>" + token + "</timg:VideoSourceToken>" +
                "<timg:ImagingSettings>" +
                imagingSettingsContent.toString() +
                "</timg:ImagingSettings>" +
                "<timg:ForcePersistence>" + FORCE_PERSISTENCE + "</timg:ForcePersistence>" +
                "</timg:SetImagingSettings>";
    }

	@Override
	public String getXAddr() {
		return xaddr;
	}

	@Override
	public ImagingType getImagingType() {
		return ImagingType.SET_SETTINGS;
	}
}