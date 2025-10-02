package io.github.hyeonmo.requests;

import io.github.hyeonmo.listeners.OnvifCapabilitiesListener;
import io.github.hyeonmo.models.OnvifType;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public class GetCapabilitiesRequest implements OnvifRequest {

	//Constants
    public static final String TAG = GetCapabilitiesRequest.class.getSimpleName();

    //Attributes
    private final OnvifCapabilitiesListener listener;

    //Constructors
    public GetCapabilitiesRequest(OnvifCapabilitiesListener listener) {
    	super();
    	this.listener = listener;
    }

    public OnvifCapabilitiesListener getListener() {
    	return listener;
    }

	@Override
	public String getXml() {
		return "<tds:GetCapabilities>" +
				"<tds:Category>All</tds:Category>" +
				"</tds:GetCapabilities>";
	}

	@Override
	public OnvifType getType() {
		return OnvifType.GET_CAPABILITIES;
	}

}
