package com.aggarwalankur.indoor_positioning.core.listeners;

/**
 * Created by Ankur on 16-Mar-16.
 */
public interface NfcListener {
    public void onNfcTagScanned(String id, long timestamp);
}
