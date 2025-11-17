package io.github.hyeonmo.parsers.imaging;

import io.github.hyeonmo.responses.ImagingResponse;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public abstract class ImagingParser<T> {

	public static final String TAG = ImagingParser.class.getSimpleName();

	public abstract T parse(ImagingResponse response);
}
