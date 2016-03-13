package com.aggarwalankur.indoor_positioning.core.wifi;

import java.util.ArrayList;

/**
 * Created by Ankur on 13-Mar-16.
 */
public interface WiFiListener {
    void onWifiScanResultsReceived(ArrayList<WifiScanResult> scanResults, long timestamp);
}
