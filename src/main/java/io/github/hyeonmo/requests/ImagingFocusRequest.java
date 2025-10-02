package io.github.hyeonmo.requests;

import io.github.hyeonmo.listeners.ImagingFocusResponseListener;
import io.github.hyeonmo.models.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingFocusRequest implements ImagingRequest{

    //Constants
    public static final String TAG = ImagingFocusRequest.class.getSimpleName();

    //Attributes
    private final ImagingFocusResponseListener listener;
    private String token;
    private String xaddr;
    private double focus;

    public ImagingFocusRequest(ImagingFocusResponseListener listener, String token, String xaddr, double focus) {
    	super();
    	this.listener = listener;
    	this.token = token;
    	this.xaddr = xaddr;
    	this.focus = focus;
    }

    public ImagingFocusResponseListener getListener() {
    	return listener;
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
