package io.github.hyeonmo.models.events;

public class EventSubscription {

    private final String address;
    private final String currentTime;
    private final String terminationTime;
    private final String referenceParametersXml;
    private final String rawXml;

    public EventSubscription(String address, String currentTime, String terminationTime, String referenceParametersXml, String rawXml) {
        this.address = address;
        this.currentTime = currentTime;
        this.terminationTime = terminationTime;
        this.referenceParametersXml = referenceParametersXml;
        this.rawXml = rawXml;
    }

    public String getAddress() {
        return address;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public String getTerminationTime() {
        return terminationTime;
    }

    public String getReferenceParametersXml() {
        return referenceParametersXml;
    }

    public String getRawXml() {
        return rawXml;
    }

    public boolean hasAddress() {
        return address != null && !address.trim().isEmpty();
    }

    public EventSubscription withTimes(String newCurrentTime, String newTerminationTime) {
        return new EventSubscription(address, newCurrentTime, newTerminationTime, referenceParametersXml, rawXml);
    }
}
