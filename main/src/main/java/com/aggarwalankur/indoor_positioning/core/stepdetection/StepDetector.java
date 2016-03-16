package com.aggarwalankur.indoor_positioning.core.stepdetection;

import com.aggarwalankur.indoor_positioning.core.listeners.StepDetectionListener;

import java.util.ArrayList;

/**
 * Created by Ankur on 16-Mar-16.
 */
public class StepDetector {
    private static StepDetector mInstance;

    private static final String TAG = "StepDetector";

    private ArrayList<StepDetectionListener> mListenerList = new ArrayList<>();

    private StepDetector(){

    }

    public synchronized static StepDetector getInstance(){
        if(mInstance == null){
            mInstance = new StepDetector();
        }

        return mInstance;
    }

    public void addListener(StepDetectionListener listener){
        if(!mListenerList.contains(listener)){
            mListenerList.add(listener);
        }
    }

    public void removeListener(StepDetectionListener listener){
        if(mListenerList.contains(listener)){
            mListenerList.remove(listener);
        }
    }

}
