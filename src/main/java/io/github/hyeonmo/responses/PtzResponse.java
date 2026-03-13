package io.github.hyeonmo.responses;

public class PtzResponse {
	private final boolean success;
	private final String message;
	private final String rawXml;

	public PtzResponse(boolean success, String message, String rawXml) {
		this.success = success;
		this.message = message;
		this.rawXml = rawXml;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getRawXml() {
		return rawXml;
	}
}
