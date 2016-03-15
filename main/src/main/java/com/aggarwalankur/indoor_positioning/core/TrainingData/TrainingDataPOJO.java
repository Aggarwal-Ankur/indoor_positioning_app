package com.aggarwalankur.indoor_positioning.core.trainingdata;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Ankur on 14-Mar-16.
 */
public class TrainingDataPOJO {

    public String mapPath = "";
    public int mapHeight = -1;
    public int mapWidth = -1;
    public int mapBearing = -1;
    public int strideLength = -1;
    public float stepLength = -1;


    public ArrayList<AnchorPOJO> anchorList = new ArrayList<>();

    public ArrayList<WiFiDataPoint> wiFiDataPoints = new ArrayList<>();

    public void resetAllData(){
        mapPath = "";
        mapHeight = 1;
        mapWidth = 1;
        mapBearing = 1;
        strideLength = 1;
        stepLength = 1;


        anchorList = new ArrayList<>();
        wiFiDataPoints = new ArrayList<>();
    }

    public void addAnchor(PointF point, String id, int type){
        AnchorPOJO anchor = new AnchorPOJO();

        anchor.x = point.x;
        anchor.y = point.y;
        anchor.id = id;
        anchor.type = type;

        boolean alreadyExists = false;

        //No duplicates
        for( AnchorPOJO currentAnchor : anchorList){
            if(currentAnchor.id.equalsIgnoreCase(anchor.id)
                    && currentAnchor.type == anchor.type){
                alreadyExists = true;
                currentAnchor.x = point.x;
                currentAnchor.y = point.y;
                break;
            }
        }

        if(!alreadyExists) {
            anchorList.add(anchor);
        }
    }

    public void addWifiDataPoint(PointF point, HashMap<String, Float> wifiDataMap){
        ArrayList<WifiDataPOJO> wifiData = new ArrayList<>();

        //Get arraylist from map
        Iterator it = wifiDataMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            WifiDataPOJO currentWifiData = new WifiDataPOJO();
            currentWifiData.bssid = (String)pair.getKey();
            currentWifiData.rssi = (float)pair.getValue();

            it.remove(); // avoids a ConcurrentModificationException

            wifiData.add(currentWifiData);
        }


        WiFiDataPoint currentDatapoint = new WiFiDataPoint();

        currentDatapoint.x = point.x;
        currentDatapoint.y = point.y;

        currentDatapoint.wifiData = wifiData;

        wiFiDataPoints.add(currentDatapoint);
    }
}
