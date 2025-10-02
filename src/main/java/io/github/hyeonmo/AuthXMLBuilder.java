package io.github.hyeonmo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Random;

import io.github.hyeonmo.models.PresetCommand;
import io.github.hyeonmo.models.PtzType;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public class AuthXMLBuilder {

	private String userName;
	private String password;

	public AuthXMLBuilder(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public String getAuthHeader() {
		byte[] nonceBytes = new byte[16];
		new Random().nextBytes(nonceBytes);
		String nonceBase64 = Base64.getEncoder().encodeToString(nonceBytes);
		String created = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

		String passwordDigest = "";
		try {
	        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
	        sha1.update(nonceBytes);
	        sha1.update(created.getBytes(StandardCharsets.UTF_8));
	        sha1.update(password.getBytes(StandardCharsets.UTF_8));
	        passwordDigest = Base64.getEncoder().encodeToString(sha1.digest());
		} catch(Exception e) {
			e.getStackTrace();
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" ");
		sb.append("xmlns:tt=\"http://www.onvif.org/ver10/schema\" ");
		sb.append("xmlns:tds=\"http://www.onvif.org/ver10/device/wsdl\" ");
		sb.append("xmlns:tptz=\"http://www.onvif.org/ver20/ptz/wsdl\" ");
		sb.append("xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" ");
		sb.append("xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" ");
		sb.append("xmlns:trt=\"http://www.onvif.org/ver10/media/wsdl\" ");
		sb.append("xmlns:timg=\"http://www.onvif.org/ver20/imaging/wsdl\">");
		sb.append("<s:Header>");
		sb.append("<wsse:Security>");
		sb.append("<wsse:UsernameToken>");
		sb.append("<wsse:Username>");
		sb.append(userName);
		sb.append("</wsse:Username>");
		sb.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">");
		sb.append(passwordDigest);
		sb.append("</wsse:Password>");
		sb.append("<wsse:Nonce EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">");
		sb.append(nonceBase64);
		sb.append("</wsse:Nonce>");
		sb.append("<wsu:Created>");
		sb.append(created);
		sb.append("</wsu:Created>");
		sb.append("</wsse:UsernameToken>");
		sb.append("</wsse:Security>");
		sb.append("</s:Header>");
		sb.append("<s:Body>");

		return sb.toString();
	}

	public String getAuthEnd() {
		return "</s:Body></s:Envelope>";
	}

	public String getPtzMoveBody(String profileToken, PtzType ptzType) {
		StringBuilder sb = new StringBuilder();

		sb.append("<tptz:ContinuousMove>");
		sb.append("<tptz:ProfileToken>");
		sb.append(profileToken);
		sb.append("</tptz:ProfileToken>");
		sb.append("<tptz:Velocity>");
		sb.append("<tt:PanTilt x=\"");
		sb.append(ptzType.getPan());
		sb.append("\" y=\"");
		sb.append(ptzType.getTilt());
		sb.append("\"/>");
		sb.append("<tt:Zoom x=\"");
		sb.append(ptzType.getZoom());
		sb.append("\"/>");
		sb.append("</tptz:Velocity>");
		sb.append("</tptz:ContinuousMove>");

		return sb.toString();
	}

	public String getPtzStopBody(String profileToken) {
		StringBuilder sb = new StringBuilder();
		sb.append("<tptz:Stop>");
		sb.append("<tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>");
		sb.append("<tptz:PanTilt>true</tptz:PanTilt>");
		sb.append("<tptz:Zoom>true</tptz:Zoom>");
		sb.append("</tptz:Stop>");

		return sb.toString();
	}

	public String getPresetBody(String profileToken, PresetCommand presetCommand) {
	    if (presetCommand == null) {
	        return "";
	    }

	    String token = presetCommand.getPresetToken();
	    switch (presetCommand.getAction()) {
	        case MOVE:
	            return "<tptz:GotoPreset>\r\n" +
	                   "  <tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>\r\n" +
	                   "  <tptz:PresetToken>" + token + "</tptz:PresetToken>\r\n" +
	                   "</tptz:GotoPreset>";
	        case SAVE:
	            return "<tptz:SetPreset>\r\n" +
	                   "  <tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>\r\n" +
	                   "  <tptz:PresetName>" + token + "</tptz:PresetName>\r\n" +
	                   "</tptz:SetPreset>";
	        case REMOVE:
	            return "<tptz:RemovePreset>\r\n" +
	                   "  <tptz:ProfileToken>" + profileToken + "</tptz:ProfileToken>\r\n" +
	                   "  <tptz:PresetToken>" + token + "</tptz:PresetToken>\r\n" +
	                   "</tptz:RemovePreset>";
	        default:
	            return "";
	    }
	}

}
