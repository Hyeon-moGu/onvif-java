package io.github.hyeonmo.parsers;

import io.github.hyeonmo.responses.OnvifResponse;

/**
 * Created by Tomas Verhelst on 06/09/2018.
 * Copyright (c) 2018 TELETASK BVBA. All rights reserved.
 *
 */
public abstract class OnvifParser<T> {

    public static final String TAG = OnvifParser.class.getSimpleName();

    public abstract T parse(OnvifResponse response);
}
