package io.github.hyeonmo.parsers.ptz;

/** Common parser contract for all PTZ responses. */
public interface PtzResponseParser<T> {
    T parse(String xml);
}
