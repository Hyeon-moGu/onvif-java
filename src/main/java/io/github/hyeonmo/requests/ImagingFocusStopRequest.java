package io.github.hyeonmo.requests;

import io.github.hyeonmo.listeners.ImagingFocusResponseListener;
import io.github.hyeonmo.models.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingFocusStopRequest implements ImagingRequest{

    //Constants
    public static final String TAG = ImagingFocusStopRequest.class.getSimpleName();

    //Attributes
    private final ImagingFocusResponseListener listener;
    private String token;
    private String xaddr;

    public ImagingFocusStopRequest(ImagingFocusResponseListener listener, String token, String xaddr) {
    	super();
    	this.listener = listener;
    	this.token = token;
    	this.xaddr = xaddr;
    }

    public ImagingFocusResponseListener getListener() {
    	return listener;
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
