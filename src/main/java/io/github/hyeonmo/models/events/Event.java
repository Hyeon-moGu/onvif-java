package io.github.hyeonmo.models.events;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Event {
    private final String topic;
    private final List<EventItem> sourceItems;
    private final List<EventItem> dataItems;
    private final String utcTime;
    private final String propertyOperation;
    private final String rawMessageXml;
    private final long timestamp;

    public Event(String topic, List<EventItem> sourceItems, List<EventItem> dataItems, String utcTime, String propertyOperation, String rawMessageXml) {
        this.topic = topic;
        this.sourceItems = sourceItems == null ? Collections.emptyList() : Collections.unmodifiableList(sourceItems);
        this.dataItems = dataItems == null ? Collections.emptyList() : Collections.unmodifiableList(dataItems);
        this.utcTime = utcTime;
        this.propertyOperation = propertyOperation;
        this.rawMessageXml = rawMessageXml;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTopic() {
        return topic;
    }

    public List<EventItem> getSourceItems() {
        return sourceItems;
    }

    public List<EventItem> getDataItems() {
        return dataItems;
    }

    public String getUtcTime() {
        return utcTime;
    }

    public String getPropertyOperation() {
        return propertyOperation;
    }

    public String getRawMessageXml() {
        return rawMessageXml;
    }

    public String getSource() {
        return formatItems(sourceItems);
    }

    public String getData() {
        return formatItems(dataItems);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isMotionRelated() {
        return topic != null && topic.toLowerCase().contains("motion");
    }

    public boolean isMotionActive() {
        return hasTrueValue("IsMotion") || hasTrueValue("State");
    }

    private String formatItems(List<EventItem> items) {
        return items.stream()
                .map(EventItem::toString)
                .collect(Collectors.joining(", "));
    }

    private boolean hasTrueValue(String itemName) {
        return dataItems.stream()
                .anyMatch(item -> itemName.equalsIgnoreCase(item.getName()) && "true".equalsIgnoreCase(item.getValue()));
    }

    @Override
    public String toString() {
        return "Event{" +
                "topic='" + topic + '\'' +
                ", source='" + getSource() + '\'' +
                ", data='" + getData() + '\'' +
                ", utcTime='" + utcTime + '\'' +
                ", propertyOperation='" + propertyOperation + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
