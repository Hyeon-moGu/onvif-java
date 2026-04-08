package io.github.hyeonmo.models.events;

public class EventSubscriptionStatus {

    private final String currentTime;
    private final String terminationTime;
    private final String rawXml;

    public EventSubscriptionStatus(String currentTime, String terminationTime, String rawXml) {
        this.currentTime = currentTime;
        this.terminationTime = terminationTime;
        this.rawXml = rawXml;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public String getTerminationTime() {
        return terminationTime;
    }

    public String getRawXml() {
        return rawXml;
    }
}
