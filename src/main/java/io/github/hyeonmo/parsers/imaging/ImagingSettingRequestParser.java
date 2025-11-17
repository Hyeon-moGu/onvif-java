package io.github.hyeonmo.parsers.imaging;

import io.github.hyeonmo.responses.ImagingResponse;

/**
 * Created by Hyeonmo Gu on 17/11/2025.
 */
public class ImagingSettingRequestParser {

	public ImagingResponse parser(ImagingResponse response) {
		String xml = response.getXml();

		if (xml == null || xml.isEmpty()) {
			response.setSuccess(false);
			response.setErrorMessage("Response Empty");
			return response;
		}

		if (xml.contains("<SOAP-ENV:Fault>") || xml.contains("<SOAP:Fault>")) {
			response.setSuccess(false);
			response.setErrorMessage("SOAP Fault");
			return response;
		}

		if (xml.contains("SetImagingSettingsResponse")) {
            response.setSuccess(true);
			return response;
		} else {
			response.setSuccess(false);
			response.setErrorMessage("Unknown response");
		}

		return response;
	}
}