package com.aggarwalankur.indoor_positioning.core.positioning;

import com.aggarwalankur.indoor_positioning.core.listeners.NfcListener;
import com.aggarwalankur.indoor_positioning.core.listeners.StepDetectionListener;
import com.aggarwalankur.indoor_positioning.core.nfc.NfcHelper;
import com.aggarwalankur.indoor_positioning.core.stepdetection.StepDetector;
import com.aggarwalankur.indoor_positioning.core.wifi.WiFiListener;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiHelper;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiScanResult;

import java.util.ArrayList;

/**
 * Created by Ankur on 16-Mar-16.
 *
 *
 * This is the heart of the positioning. It listens from all the sensor managers, cross references
 * the wifi training data and provides a position
 *
 * For ease, I have made this singleton as well
 */
public class PositioningManager implements NfcListener, WiFiListener, StepDetectionListener{
    private static final String TAG = "PositioningManager";


    private static PositioningManager mInstance;

    private PositioningManager(){

    }

    public synchronized static PositioningManager getInstance(){
        if(mInstance == null){
            mInstance = new PositioningManager();
        }

        return mInstance;
    }

    public void startLocationTracking(){
        //Register to receive various events

        NfcHelper.getInstance().addListener(this);

        //Make sure that this does not get called before the wifi is initialized
        WifiHelper.getInstance().addListener(this, null);

        StepDetector.getInstance().addListener(this);
    }

    public void stopLocationTracking(){
        //De-Register all listeners

        NfcHelper.getInstance().removeListener(this);
        WifiHelper.getInstance().removeListener(this, null);
        StepDetector.getInstance().removeListener(this);
    }


    @Override
    public void onNfcTagScanned(String id, long timestamp) {
        //CrossReference with training data
    }

    @Override
    public void onWifiScanResultsReceived(ArrayList<WifiScanResult> scanResults, long timestamp) {

    }

    @Override
    public void onStepDetected(int count, long timestamp) {

    }
}
