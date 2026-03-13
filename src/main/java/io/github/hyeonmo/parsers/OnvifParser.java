package io.github.hyeonmo.parsers;

import io.github.hyeonmo.responses.OnvifResponse;

public abstract class OnvifParser<T> {

    public static final String TAG = OnvifParser.class.getSimpleName();

    public abstract T parse(OnvifResponse<?> response);
}
