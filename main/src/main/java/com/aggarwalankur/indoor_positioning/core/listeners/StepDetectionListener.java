package com.aggarwalankur.indoor_positioning.core.listeners;

/**
 * Created by Ankur on 16-Mar-16.
 */
public interface StepDetectionListener {
    void onStepDetected(int count, long timestamp);
}
