package io.github.hyeonmo.requests.imaging;

import io.github.hyeonmo.models.imaging.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 * Modified for v2.0 - callback removed.
 */
public class GetImagingSettingsRequest implements ImagingRequest {

    public static final String TAG = GetImagingSettingsRequest.class.getSimpleName();

    private final String token;
    private final String xaddr;

    public GetImagingSettingsRequest(String token, String xaddr) {
        super();
        this.token = token;
        this.xaddr = xaddr;
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
