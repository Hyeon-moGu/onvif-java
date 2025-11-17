package io.github.hyeonmo.responses;

import io.github.hyeonmo.requests.imaging.ImagingRequest;

/**
 * Created by Hyeonmo Gu on 24/09/2025.
 */
public class ImagingResponse<T> {

    //Constants
    public static final String TAG = ImagingResponse.class.getSimpleName();

    //Attributes
    private boolean success;
    private int errorCode;
    private String errorMessage;
    private String xml;

    private ImagingRequest imagingReq;

    public ImagingResponse(String xml) {
    	this.xml = xml;
    }

    public ImagingResponse(ImagingRequest imagingReq) {
    	this.imagingReq = imagingReq;
    }

    //Properties

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public ImagingRequest request() {
    	return imagingReq;
    }
}
