package io.github.hyeonmo.requests.imaging;

import io.github.hyeonmo.models.imaging.ImagingType;

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
