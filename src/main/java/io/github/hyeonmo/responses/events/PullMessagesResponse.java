package io.github.hyeonmo.responses.events;

import io.github.hyeonmo.models.events.Event;
import java.util.List;

public class PullMessagesResponse {
    private final List<Event> events;
    private final String currentTime;
    private final String terminationTime;
    private final String rawXml;

    public PullMessagesResponse(List<Event> events, String currentTime, String terminationTime, String rawXml) {
        this.events = events;
        this.currentTime = currentTime;
        this.terminationTime = terminationTime;
        this.rawXml = rawXml;
    }

    public List<Event> getEvents() {
        return events;
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
