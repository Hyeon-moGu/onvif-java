package io.github.hyeonmo.requests.imaging;

import io.github.hyeonmo.models.imaging.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 * Modified for v2.0 - callback removed.
 */
public class ImagingFocusRequest implements ImagingRequest {

    public static final String TAG = ImagingFocusRequest.class.getSimpleName();

    private final String token;
    private final String xaddr;
    private final double focus;

    public ImagingFocusRequest(String token, String xaddr, double focus) {
        super();
        this.token = token;
        this.xaddr = xaddr;
        this.focus = focus;
    }

    @Override
    public String getXml() {
        return "<timg:Move>" +
                "<timg:VideoSourceToken>" + token + "</timg:VideoSourceToken>" +
                "<timg:Focus>" +
                "<tt:Continuous>" +
                "<tt:Speed>" + focus + "</tt:Speed>" +
                "</tt:Continuous>" +
                "</timg:Focus>" +
                "</timg:Move>";
    }

    @Override
    public String getXAddr() {
        return xaddr;
    }

    @Override
    public ImagingType getImagingType() {
        return ImagingType.FOCUS_MOVE;
    }
}
