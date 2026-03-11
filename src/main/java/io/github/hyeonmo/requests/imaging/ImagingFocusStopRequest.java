package io.github.hyeonmo.requests.imaging;

import io.github.hyeonmo.models.imaging.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 * Modified for v2.0 - callback removed.
 */
public class ImagingFocusStopRequest implements ImagingRequest {

    public static final String TAG = ImagingFocusStopRequest.class.getSimpleName();

    private final String token;
    private final String xaddr;

    public ImagingFocusStopRequest(String token, String xaddr) {
        super();
        this.token = token;
        this.xaddr = xaddr;
    }

    @Override
    public String getXml() {
        return "<timg:Stop>" +
                "<timg:VideoSourceToken>" + token + "</timg:VideoSourceToken>" +
                "</timg:Stop>";
    }

    @Override
    public String getXAddr() {
        return xaddr;
    }

    @Override
    public ImagingType getImagingType() {
        return ImagingType.FOCUS_STOP;
    }
}
