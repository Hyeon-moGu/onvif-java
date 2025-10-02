package io.github.hyeonmo.requests;

import io.github.hyeonmo.listeners.ImagingSettingsListener;
import io.github.hyeonmo.models.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class GetImagingSettingsRequest implements ImagingRequest{

    //Constants
    public static final String TAG = GetImagingSettingsRequest.class.getSimpleName();

    //Attributes
    private final ImagingSettingsListener listener;
    private String token;
    private String xaddr;

    public GetImagingSettingsRequest(ImagingSettingsListener listener, String token, String xaddr) {
        super();
        this.listener = listener;
        this.token = token;
        this.xaddr = xaddr;
    }

    public ImagingSettingsListener getListener() {
        return listener;
    }

	@Override
	public String getXml() {
		return "<timg:GetImagingSettings>" +
				"<timg:VideoSourceToken>" + token + "</timg:VideoSourceToken>" +
				"</timg:GetImagingSettings>";
	}

	@Override
	public String getXAddr() {
		return xaddr;
	}

	@Override
	public ImagingType getImagingType() {
		return ImagingType.GET_IMAGING_SETTINGS;
	}


}
