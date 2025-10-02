package io.github.hyeonmo.requests;

import io.github.hyeonmo.models.ImagingType;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public interface ImagingRequest {

	String getXml();

	String getXAddr();

	ImagingType getImagingType();

}
