package com.aggarwalankur.indoor_positioning.core.stepdetection;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.aggarwalankur.indoor_positioning.core.listeners.StepDetectionListener;

import java.util.ArrayList;

/**
 * Created by Ankur on 16-Mar-16.
 */
public class StepDetector implements SensorEventListener {
    private static StepDetector mInstance;

    private static final String TAG = "StepDetector";

    private ArrayList<StepDetectionListener> mListenerList = new ArrayList<>();

    private StepDetector(){

    }



    private static final int EVENT_QUEUE_LENGTH = 10;
    // List of timestamps when sensor events occurred
    private float[] mEventDelays = new float[EVENT_QUEUE_LENGTH];

    // number of events in event list
    private int mEventLength = 0;
    // pointer to next entry in sensor event list
    private int mEventData = 0;

    // Steps counted in current session
    private int mSteps = 0;


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


    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();

        // store the delay of this event
        recordDelay(event);
        // A step detector event is received for each step.
        // This means we need to count steps ourselves

        mSteps = event.values.length;


        Log.i(TAG,
                "New step detected by STEP_DETECTOR sensor. Total step count: " + mSteps);


        //Send the step count to listeners

        if(mSteps > 0
                && !mListenerList.isEmpty()){
            for(StepDetectionListener currentListener : mListenerList){
                currentListener.onStepDetected(mSteps, timestamp);
            }
        }
    }


    private void recordDelay(SensorEvent event) {
        // Calculate the delay from when event was recorded until it was received here in ms
        // Event timestamp is recorded in us accuracy, but ms accuracy is sufficient here
        mEventDelays[mEventData] = System.currentTimeMillis() - (event.timestamp / 1000000L);

        // Increment length counter
        mEventLength = Math.min(EVENT_QUEUE_LENGTH, mEventLength + 1);
        // Move pointer to the next (oldest) location
        mEventData = (mEventData + 1) % EVENT_QUEUE_LENGTH;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
