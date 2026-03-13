package io.github.hyeonmo.requests.imaging;

import io.github.hyeonmo.models.imaging.ImagingType;

public interface ImagingRequest {

	String getXml();

	String getXAddr();

	ImagingType getImagingType();

}
