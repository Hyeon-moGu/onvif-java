package io.github.hyeonmo.requests.ptz;

import io.github.hyeonmo.models.ptz.PtzOperationType;

public interface PtzBaseRequest {
    String getXml();
    String getXAddr();
    PtzOperationType getPtzOperationType();
}
