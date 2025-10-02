package io.github.hyeonmo.models;

/**
 * Created by Hyeonmo Gu on 17/09/2025.
 */
public class OnvifCapabilities {

	// Device
	private String xaddr;

	// Analytics
	private String analyticsXaddr;

	// Events
	private String eventsXaddr;
	private boolean wsSubscriptionPolicySupport;
	private boolean wsPullPointSupport;
	private boolean wsPausableSubscriptionManagerInterfaceSupport;

	// Imaging
	private String imagingXaddr;

	// Media
	private String mediaXaddr;
	private boolean rtpMulticast;
	private boolean rtpTcp;
	private boolean rtpRtspTcp;
	private int maximumNumberOfProfiles;

	// PTZ
	private String ptzXaddr;

	// Constructors
	public OnvifCapabilities() {
	}

	// Getters / Setters
	public String getXAddr() {
		return xaddr;
	}

	public void setXaddr(String xaddr) {
		this.xaddr = xaddr;
	}

	public String getAnalyticsXaddr() {
		return analyticsXaddr;
	}

	public void setAnalyticsXaddr(String analyticsXaddr) {
		this.analyticsXaddr = analyticsXaddr;
	}

	public String getEventsXaddr() {
		return eventsXaddr;
	}

	public void setEventsXaddr(String eventsXaddr) {
		this.eventsXaddr = eventsXaddr;
	}

	public boolean isWsSubscriptionPolicySupport() {
		return wsSubscriptionPolicySupport;
	}

	public void setWsSubscriptionPolicySupport(boolean wsSubscriptionPolicySupport) {
		this.wsSubscriptionPolicySupport = wsSubscriptionPolicySupport;
	}

	public boolean isWsPullPointSupport() {
		return wsPullPointSupport;
	}

	public void setWsPullPointSupport(boolean wsPullPointSupport) {
		this.wsPullPointSupport = wsPullPointSupport;
	}

	public boolean isWsPausableSubscriptionManagerInterfaceSupport() {
		return wsPausableSubscriptionManagerInterfaceSupport;
	}

	public void setWsPausableSubscriptionManagerInterfaceSupport(boolean wsPausableSubscriptionManagerInterfaceSupport) {
		this.wsPausableSubscriptionManagerInterfaceSupport = wsPausableSubscriptionManagerInterfaceSupport;
	}

	public String getImagingXaddr() {
		return imagingXaddr;
	}

	public void setImagingXaddr(String imagingXaddr) {
		this.imagingXaddr = imagingXaddr;
	}

	public String getMediaXaddr() {
		return mediaXaddr;
	}

	public void setMediaXaddr(String mediaXaddr) {
		this.mediaXaddr = mediaXaddr;
	}

	public boolean isRtpMulticast() {
		return rtpMulticast;
	}

	public void setRtpMulticast(boolean rtpMulticast) {
		this.rtpMulticast = rtpMulticast;
	}

	public boolean isRtpTcp() {
		return rtpTcp;
	}

	public void setRtpTcp(boolean rtpTcp) {
		this.rtpTcp = rtpTcp;
	}

	public boolean isRtpRtspTcp() {
		return rtpRtspTcp;
	}

	public void setRtpRtspTcp(boolean rtpRtspTcp) {
		this.rtpRtspTcp = rtpRtspTcp;
	}

	public int getMaximumNumberOfProfiles() {
		return maximumNumberOfProfiles;
	}

	public void setMaximumNumberOfProfiles(int maximumNumberOfProfiles) {
		this.maximumNumberOfProfiles = maximumNumberOfProfiles;
	}

	public String getPtzXaddr() {
		return ptzXaddr;
	}

	public void setPtzXaddr(String ptzXaddr) {
		this.ptzXaddr = ptzXaddr;
	}

	@Override
	public String toString() {
		return "OnvifCapabilities{" +
				"xaddr='" + xaddr + '\'' +
				", analyticsXaddr='" + analyticsXaddr + '\'' +
				", eventsXaddr='" + eventsXaddr + '\'' +
				", wsSubscriptionPolicySupport=" + wsSubscriptionPolicySupport +
				", wsPullPointSupport=" + wsPullPointSupport +
				", wsPausableSubscriptionManagerInterfaceSupport=" + wsPausableSubscriptionManagerInterfaceSupport +
				", imagingXaddr='" + imagingXaddr + '\'' +
				", mediaXaddr='" + mediaXaddr + '\'' +
				", rtpMulticast=" + rtpMulticast +
				", rtpTcp=" + rtpTcp +
				", rtpRtspTcp=" + rtpRtspTcp +
				", maximumNumberOfProfiles=" + maximumNumberOfProfiles +
				", ptzXaddr='" + ptzXaddr + '\'' +
				'}';
	}
}
