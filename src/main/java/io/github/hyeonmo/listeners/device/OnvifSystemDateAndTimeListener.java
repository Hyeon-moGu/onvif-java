package io.github.hyeonmo.listeners.device;

import java.util.Date;

import io.github.hyeonmo.models.OnvifDevice;

public interface OnvifSystemDateAndTimeListener {
    void onSystemDateAndTimeReceived(OnvifDevice device, Date date);
}
