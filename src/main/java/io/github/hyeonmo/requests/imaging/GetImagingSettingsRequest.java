package io.github.hyeonmo.requests.imaging;

import io.github.hyeonmo.models.imaging.ImagingType;

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
