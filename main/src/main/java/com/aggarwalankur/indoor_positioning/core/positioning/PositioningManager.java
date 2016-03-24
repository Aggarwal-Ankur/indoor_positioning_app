package com.aggarwalankur.indoor_positioning.core.positioning;

import android.graphics.PointF;
import android.util.Log;

import com.aggarwalankur.indoor_positioning.core.ble.BLEScanHelper;
import com.aggarwalankur.indoor_positioning.core.direction.DirectionHelper;
import com.aggarwalankur.indoor_positioning.core.listeners.BleScanListener;
import com.aggarwalankur.indoor_positioning.core.listeners.DirectionListener;
import com.aggarwalankur.indoor_positioning.core.listeners.NfcListener;
import com.aggarwalankur.indoor_positioning.core.listeners.StepDetectionListener;
import com.aggarwalankur.indoor_positioning.core.nfc.NfcHelper;
import com.aggarwalankur.indoor_positioning.core.stepdetection.StepDetector;
import com.aggarwalankur.indoor_positioning.core.trainingdata.AnchorPOJO;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataManager;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataPOJO;
import com.aggarwalankur.indoor_positioning.core.trainingdata.WiFiDataPoint;
import com.aggarwalankur.indoor_positioning.core.trainingdata.WifiDataPOJO;
import com.aggarwalankur.indoor_positioning.core.wifi.WiFiListener;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiHelper;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiScanResult;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Ankur on 16-Mar-16.
 *
 *
 * This is the heart of the positioning. It listens from all the sensor managers, cross references
 * the wifi training data and provides a position
 *
 * For ease, I have made this singleton as well
 *
 * Algo:
 * 1. Always keep storing direction updates
 * 2. If position = null, don't consider step updates
 * 3. If position = null and received Wi-Fi scan, initiate cross-referencing
 * 4. If received WiFi scan, keep accumulating. Accumulate for 3 readings, and then initiate cross-referencing
 * 5. If received step count, flush old WiFi data
 * 6. If received BLE data, and position != null, calculate new position with "pull back"
 */
public class PositioningManager implements NfcListener, WiFiListener
        , StepDetectionListener, DirectionListener, BleScanListener{
    private static final String TAG = "PositioningManager";


    private static PositioningManager mInstance;

    private TrainingDataPOJO mTrainingData;

    private WiFiDataPoint wifiAccumulatedPoint;

    private int currentWifiScanCount = 0;
    private PointF currentPosition = null;

    private int direction = -1;

    private PositioningManager(){
        wifiAccumulatedPoint = new WiFiDataPoint();
    }

    public synchronized static PositioningManager getInstance(){
        if(mInstance == null){
            mInstance = new PositioningManager();
        }

        return mInstance;
    }

    public void startLocationTracking(){
        //Fetch the latest training Data
        mTrainingData = TrainingDataManager.getInstance().getData();


        //Register to receive various events

        NfcHelper.getInstance().addListener(this);

        //Make sure that this does not get called before the wifi is initialized
        WifiHelper.getInstance().addListener(this, null);

        StepDetector.getInstance().addListener(this);

        DirectionHelper.getInstance(null).addListener(this);

        BLEScanHelper.getInstance().addListener(this);
    }

    public void stopLocationTracking(){
        //De-Register all listeners

        NfcHelper.getInstance().removeListener(this);
        WifiHelper.getInstance().removeListener(this, null);
        StepDetector.getInstance().removeListener(this);
        DirectionHelper.getInstance(null).removeListener(this);

        BLEScanHelper.getInstance().removeListener(this);

        currentPosition = null;
        direction = -1;
    }

    @Override
    public void onWifiScanResultsReceived(ArrayList<WifiScanResult> scanResults, long timestamp) {
        Log.d(TAG, "onWifiScanResultsReceived. Timestamp="+ timestamp);

        if(mTrainingData == null || mTrainingData.wiFiDataPoints== null || mTrainingData.wiFiDataPoints.isEmpty()){
            return;
        }

        if(currentWifiScanCount == 0){
            //First, lets get only what we want to track
            Iterator<WifiScanResult> iter = scanResults.iterator();

            while (iter.hasNext()){
                WifiScanResult currentScanResult = iter.next();
                boolean tracking = false;
                for(AnchorPOJO currentAnchor : mTrainingData.anchorList){
                    if(currentAnchor.id.equalsIgnoreCase(currentScanResult.bssid)){
                        WifiDataPOJO currentWifiDataPojo = new WifiDataPOJO();
                        currentWifiDataPojo.bssid = currentScanResult.bssid;
                        currentWifiDataPojo.rssi = currentScanResult.level;

                        wifiAccumulatedPoint.wifiData.add(currentWifiDataPojo);

                        tracking = true;
                        break;
                    }
                }

                if(!tracking){
                    iter.remove();
                }
            }
        }else{
            //Accumulate the Wifi data

            for(WifiDataPOJO accumulatedWifiDataPojo : wifiAccumulatedPoint.wifiData){
                for(WifiScanResult currentScanResult : scanResults){
                    if(currentScanResult.bssid.equalsIgnoreCase(accumulatedWifiDataPojo.bssid)){
                        double totalRssi = accumulatedWifiDataPojo.rssi * currentWifiScanCount + currentScanResult.level;

                        double averageRssi = totalRssi / (currentWifiScanCount + 1);

                        accumulatedWifiDataPojo.rssi = averageRssi;
                    }
                }
            }


            currentWifiScanCount = currentWifiScanCount + 1;
        }

        if(currentPosition != null && currentWifiScanCount< 3){
            return;
        }


        /*//CrossReference with training data

        ArrayList<WifiScanResult> scanResultsTemp = (ArrayList<WifiScanResult>)scanResults.clone();

        //First, lets get only what we want to track
        Iterator<WifiScanResult> iter = scanResultsTemp.iterator();

        while (iter.hasNext()){
            WifiScanResult currentScanResult = iter.next();
            boolean tracking = false;
            for(AnchorPOJO currentAnchor : mTrainingData.anchorList){
                if(currentAnchor.id.equalsIgnoreCase(currentScanResult.bssid)){
                    tracking = true;
                    break;
                }
            }

            if(!tracking){
                iter.remove();
            }
        }*/

        //Now, let us calculate the top 2 points with minimum deviation in rssi

        float firstDeviationValue = 0, secondDeviationValue = 0;

        WiFiDataPoint firstPoint, secondPoint;

        firstPoint = mTrainingData.wiFiDataPoints.get(0);
        secondPoint = mTrainingData.wiFiDataPoints.get(0);

        for(WiFiDataPoint currentWifiDataPoint : mTrainingData.wiFiDataPoints){
            float currentDeviation = 0;

            for(WifiDataPOJO currentWifiPojo : currentWifiDataPoint.wifiData){
                innerFor: for(WifiDataPOJO accumulatedWifiData : wifiAccumulatedPoint.wifiData){
                    if(accumulatedWifiData.bssid.equalsIgnoreCase(currentWifiPojo.bssid)){
                        //Calculate deviation

                        currentDeviation += Math.abs(accumulatedWifiData.rssi - currentWifiPojo.rssi);
                        break innerFor;
                    }
                }
            }

            if(currentDeviation >= firstDeviationValue){
                secondDeviationValue = firstDeviationValue;
                firstDeviationValue = currentDeviation;

                secondPoint = firstPoint;
                firstPoint = currentWifiDataPoint;

            }else if(currentDeviation >secondDeviationValue){
                secondDeviationValue = currentDeviation;

                secondPoint = currentWifiDataPoint;
            }
        }


        //Calculate distance between first and second point
        double distanceBetweenWiFiAnchors = Math.sqrt(Math.pow((firstPoint.x - secondPoint.x), 2)
                + Math.pow((firstPoint.y - secondPoint.y), 2));


        //Right now, lets just log everything
        Log.d(TAG, "onWifiScanResultsReceived. distanceBetweenWiFiAnchors="+distanceBetweenWiFiAnchors+
                " :firstDeviationValue="+firstDeviationValue+" :secondDeviationValue="+secondDeviationValue);

        // distanceBetweenWiFiAnchors is in metres

        /*if(distanceBetweenWiFiAnchors > 3){

        }*/


        //Generate an event for positioning
    }

    @Override
    public void onStepDetected(int count, long timestamp) {

        if(currentPosition == null
                || direction < 0){
            return;
        }

        float d = count * mTrainingData.stepLength;

        //Dead reckoning!!
        PointF position = new PointF();

        //orienatation in radians
        double orientation = (direction - mTrainingData.mapBearing) * Math.PI/ 180;

        position.x   = (float)(currentPosition.x +  d* Math.sin(orientation));
        position.y   = (float)(currentPosition.y +  d* Math.cos(orientation));

        //Flush the old WiFi data
        currentWifiScanCount = 0;

        Log.d(TAG, "onStepDetected. Position.x ="+position.x+"  :Position.y="+position.y);

    }

    @Override
    public void onDirectionListener(int direction, long timeStamp) {

        this.direction = direction;

    }

    @Override
    public void onNfcTagScanned(String id, long timestamp) {
        //CrossReference with training data
    }

    @Override
    public void onBleDeviceScanned(String id, long timestamp, double distanceInMetres) {

    }
}
