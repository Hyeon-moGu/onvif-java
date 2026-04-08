package io.github.hyeonmo.requests;

import io.github.hyeonmo.models.OnvifType;

public interface OnvifRequest {

    String getXml();

    OnvifType getType();

    default String getAction() {
        return "";
    }

}
