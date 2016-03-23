package com.aggarwalankur.indoor_positioning.core.listeners;

/**
 * Created by Ankur on 23/03/2016.
 */
public interface BleScanListener {
    void onBleDeviceScanned(String id, long timestamp, int distanceInMeters);
}
