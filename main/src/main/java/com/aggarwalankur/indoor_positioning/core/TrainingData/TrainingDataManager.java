package com.aggarwalankur.indoor_positioning.core.trainingdata;

import android.graphics.PointF;

import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.core.wifi.WiFiListener;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiHelper;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiScanResult;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ankur on 14-Mar-16.
 */
public class TrainingDataManager implements WiFiListener{

    private static TrainingDataManager mInstance;

    private TrainingDataPOJO mData;

    private boolean isDataLoaded = false;

    private boolean collectWifitrainingData = false;

    private TrainingDataManager(){
        mData = new TrainingDataPOJO();
    }

    private HashMap<String, Float> tempWifiData;
    private int tempWifiDataSampleCount;

    public static synchronized TrainingDataManager getInstance(){
        if(mInstance == null){
            mInstance = new TrainingDataManager();
        }

        return mInstance;
    }

    public TrainingDataPOJO getData(){
        return mData;
    }

    public void setData(TrainingDataPOJO data){
        this.mData = data;
        isDataLoaded = true;
    }


    public void addMapBasicData(String path, int mapHeight, int mapWidth, int bearing, int strideLength){
        mData.mapPath = path;
        mData.mapHeight = mapHeight;
        mData.mapWidth = mapWidth;
        mData.mapBearing = bearing;
        mData.strideLength = strideLength;

        mData.stepLength = (float)strideLength/2;
    }

    public void resetMapData(){
        mData.resetAllData();
    }


    public void addAnchor(PointF point, String id, int type){
        mData.addAnchor(point, id, type);
    }

    public void addWifiDataPoint(PointF point){
        mData.addWifiDataPoint(point, tempWifiData);
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    @Override
    public synchronized void onWifiScanResultsReceived(ArrayList<WifiScanResult> scanResults, long timestamp) {
        ArrayList<AnchorPOJO> anchorList = mData.anchorList;
        if(anchorList.isEmpty() || scanResults.isEmpty() || !collectWifitrainingData){
            //Nothing to add
            return;
        }
        for(WifiScanResult currentWifiResult : scanResults){
            for(AnchorPOJO currentAnchor : anchorList){
                if(currentAnchor.type == IConstants.ANCHOR_TYPE.WIFI
                        && currentWifiResult.bssid.equalsIgnoreCase(currentAnchor.id)){

                    if(tempWifiData.containsKey(currentWifiResult.bssid)){
                        float currentRssi = tempWifiData.get(currentWifiResult.bssid);

                        float newRssi = ((currentRssi * tempWifiDataSampleCount) + currentWifiResult.level)/(tempWifiDataSampleCount + 1);

                        tempWifiData.put(currentWifiResult.bssid, newRssi);
                    }else{
                        tempWifiData.put(currentWifiResult.bssid, (float)currentWifiResult.level);
                    }

                }
            }


        }

        tempWifiDataSampleCount++;
    }

    public synchronized void setCollectWifitrainingData(boolean collectWifitrainingData) {
        this.collectWifitrainingData = collectWifitrainingData;

        //TODO : Prevent NPE
        if(collectWifitrainingData){
            //Register for Wifi data
            WifiHelper.getInstance().addListener(this, null);

            tempWifiData = new HashMap<>();
            tempWifiDataSampleCount = 0;
        }else{
            WifiHelper.getInstance().removeListener(this, null);
        }
    }


    //This is the function that adds to the
}
