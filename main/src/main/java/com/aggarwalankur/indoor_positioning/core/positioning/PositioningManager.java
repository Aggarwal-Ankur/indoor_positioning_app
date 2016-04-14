package com.aggarwalankur.indoor_positioning.core.positioning;

import android.content.Context;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.Log;

import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.core.ble.BLEScanHelper;
import com.aggarwalankur.indoor_positioning.core.direction.DirectionHelper;
import com.aggarwalankur.indoor_positioning.core.listeners.BleScanListener;
import com.aggarwalankur.indoor_positioning.core.listeners.DirectionListener;
import com.aggarwalankur.indoor_positioning.core.listeners.NfcListener;
import com.aggarwalankur.indoor_positioning.core.listeners.PositionListener;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
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

    private ArrayList<PositionListener> mListeners;

    private AsyncTask mWifiAsyncTask ;


    private String urlString = IConstants.WEBSERVICE_CONSTANTS.URL + ":"
            + IConstants.WEBSERVICE_CONSTANTS.PORTNO
            + IConstants.WEBSERVICE_CONSTANTS.WEBSERVICE_URI;

    private PositioningManager(){
        wifiAccumulatedPoint = new WiFiDataPoint();
        mListeners = new ArrayList<>();
    }

    public synchronized static PositioningManager getInstance(){
        if(mInstance == null){
            mInstance = new PositioningManager();
        }

        return mInstance;
    }

    public synchronized void addListener(PositionListener listener){
        if(mListeners != null && !mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    public synchronized void removeListener(PositionListener listener){
        if(mListeners != null && mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }

    public void startLocationTracking(Context context){
        //Fetch the latest training Data
        mTrainingData = TrainingDataManager.getInstance().getData();


        //Register to receive various events

        NfcHelper.getInstance().addListener(this);

        //Make sure that this does not get called before the wifi is initialized
        WifiHelper.getInstance().addListener(this, context);

        StepDetector.getInstance().addListener(this);

        DirectionHelper.getInstance(context).addListener(this);

        BLEScanHelper.getInstance().addListener(this);
    }

    public void stopLocationTracking(){
        //Stop Wifi tracking as well
        if(mWifiAsyncTask != null && mWifiAsyncTask.getStatus()== AsyncTask.Status.RUNNING){
            mWifiAsyncTask.cancel(true);
        }


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

            currentWifiScanCount = 1;
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


        //reference web service for position

        //Step 1: convert accumulated data to String

        String wifiDataJson = getWiFiDataString(wifiAccumulatedPoint.wifiData);
        mWifiAsyncTask = new WiFiAsyncTask();
        mWifiAsyncTask.execute(new String[]{wifiDataJson});

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

        currentPosition = new PointF(position.x, position.y);

        Log.d(TAG, "onStepDetected. Position.x ="+position.x+"  :Position.y="+position.y);

        sendPosition(currentPosition);

    }

    @Override
    public void onDirectionListener(int direction, long timeStamp) {

        this.direction = direction;

    }

    @Override
    public void onNfcTagScanned(String id, long timestamp) {
        if(currentPosition == null ){
            return;
        }

        //CrossReference with training data

        AnchorPOJO currentAnchor = null;

        for(AnchorPOJO anchor : mTrainingData.anchorList){
            if(anchor.id.equalsIgnoreCase(id) && anchor.type == IConstants.ANCHOR_TYPE.NFC){
                currentAnchor = anchor;
                break;
            }
        }

        if(currentAnchor == null){
            //No matching anchor found in training data
            return;
        }

        PointF anchorLocation = new PointF(currentAnchor.x, currentAnchor.y);

        PointF newLocation = pullBack(anchorLocation, currentPosition, 0.1F);

        Log.d(TAG, "onNfcTagScanned. Position.x ="+newLocation.x+"  :Position.y="+newLocation.y);

        sendPosition(newLocation);

    }

    @Override
    public void onBleDeviceScanned(String id, long timestamp, double distanceInMetres) {
        if(currentPosition == null ){
            return;
        }

        //CrossReference with training data
        AnchorPOJO currentAnchor = null;

        for(AnchorPOJO anchor : mTrainingData.anchorList){
            if(anchor.id.equalsIgnoreCase(id) && anchor.type == IConstants.ANCHOR_TYPE.BLE){
                currentAnchor = anchor;
                break;
            }
        }

        if(currentAnchor == null){
            //No matching anchor found in training data
            return;
        }

        PointF anchorLocation = new PointF(currentAnchor.x, currentAnchor.y);

        PointF newLocation = pullBack(anchorLocation, currentPosition, (float)distanceInMetres);

        Log.d(TAG, "onNfcTagScanned. Position.x ="+newLocation.x+"  :Position.y="+newLocation.y);

        sendPosition(newLocation);
    }

    private PointF pullBack(PointF anchorLocation, PointF currentLocation, float distanceInMteres){
        //Get the distance between current and anchorLocation

        double originalDistance = Math.sqrt(Math.pow((anchorLocation.x - currentLocation.x), 2)
                +Math.pow((anchorLocation.y - currentLocation.y), 2));

        if(originalDistance == 0 || originalDistance< distanceInMteres){
            //No change in pulled-back location
            return currentLocation;
        }

        PointF pulledBackLocation = new PointF();
        pulledBackLocation.x = anchorLocation.x + (float)((currentLocation.x - anchorLocation.x)/originalDistance)* distanceInMteres;
        pulledBackLocation.y = anchorLocation.y + (float)((currentLocation.y - anchorLocation.y)/originalDistance)* distanceInMteres;

        return pulledBackLocation;

    }

    /**
     * Helper function to send the calculated position to UI
     */
    private void sendPosition(PointF position){
        for(PositionListener currentListener : mListeners){
            currentListener.onPositionChanged(position);
        }
    }

    /**
     *  Helper function to convert Wi-Fi Data to String
     */
    public String getWiFiDataString(ArrayList<WifiDataPOJO> wifiData){

        /*
        Structure is
        { "wifiDataPointList" :{
            "wifiDataPoint" : [
                {"bssid":"bssidValue","rssi":"rssivalue"},
                {"bssid":"bssidValue","rssi":"rssivalue"},
                {"bssid":"bssidValue","rssi":"rssivalue"}
            ]
            }
        }
         */

        StringBuffer buf = new StringBuffer();
        buf.append("{\"wifiDataPointList\" : { \"wifiDataPoint\" : [");

        for(int i=0; i<wifiData.size(); i++){
            WifiDataPOJO currentWifidata = wifiData.get(i);

            buf.append("{\"bssid\":\""+currentWifidata.bssid+"\", \"rssi\":\""+ currentWifidata.rssi+"\"}");

            if(i<wifiData.size() -1){
                buf.append(",");
            }
        }

        buf.append("]}}");

        String returnString = buf.toString();
        return returnString;
    }


    private class WiFiAsyncTask extends AsyncTask<String, Void, PointF>{
        @Override
        protected PointF doInBackground(String... strings) {



            PointF position = null;

            try{
                //Make the URL connection first
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();

                String wifiJsonString = strings [0];

                connection.setDoOutput(true);
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(wifiJsonString);
                out.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String returnString="";


                while ((returnString = in.readLine()) != null){
                    //returnString would contain the position. Format is:

                    /* {"positionData" :
                            { "x" : "xValue", "y":"yValue" }
                       }
                    */
                    final JSONObject obj = new JSONObject(returnString);
                    final JSONObject positionData = obj.getJSONObject("positionData");

                    float x= (float)positionData.getDouble("x");
                    float y= (float)positionData.getDouble("y");

                    Log.d(TAG, "x="+x);
                    Log.d(TAG, "y="+y);

                    position = new PointF(x, y);
                }
                in.close();
            }catch (Exception e){
                e.printStackTrace();
            }


            return position;
        }

        @Override
        protected void onPostExecute(PointF position) {
            if(isCancelled() || position == null){
                return;
            }

            //Else, set the position
            if(currentPosition.x == position.x && currentPosition.y == position.y){
                return;
            }

            currentPosition = position;

            sendPosition(currentPosition);
        }
    }



}
