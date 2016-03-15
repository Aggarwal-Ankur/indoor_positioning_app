package com.aggarwalankur.indoor_positioning.core.trainingdata;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by Ankur on 14-Mar-16.
 */
public class TrainingDataManager {

    private static TrainingDataManager mInstance;

    private TrainingDataPOJO mData;

    private boolean isDataLoaded = false;

    private TrainingDataManager(){
        mData = new TrainingDataPOJO();
    }

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

    public boolean isDataLoaded() {
        return isDataLoaded;
    }
}
