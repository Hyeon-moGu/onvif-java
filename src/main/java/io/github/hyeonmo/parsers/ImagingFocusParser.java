package io.github.hyeonmo.parsers;

import io.github.hyeonmo.responses.ImagingResponse;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingFocusParser {

	public ImagingResponse parser(ImagingResponse response) {
		String xml = response.getXml();

		if (xml == null || xml.isEmpty()) {
			response.setSuccess(false);
			response.setErrorMessage("Response Empty");
			return response;
		}

		if (xml.contains("<SOAP-ENV:Fault>")) {
			response.setSuccess(false);
			response.setErrorMessage("SOAP Fault");
			return response;
		}

		if(xml.contains("MoveResponse") || xml.contains("StopResponse")) {
			return response;
		} else {
			response.setSuccess(false);
			response.setErrorMessage("Unknown response");
		}

		return response;
	}
}
