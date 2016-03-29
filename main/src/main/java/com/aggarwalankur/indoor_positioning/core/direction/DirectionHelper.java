package com.aggarwalankur.indoor_positioning.core.direction;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.core.listeners.DirectionListener;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Ankur on 18-Mar-16.
 */
public class DirectionHelper implements SensorEventListener {

    public static final String TAG = "DirectionHelper";

    public static DirectionHelper mInstance;

    private ArrayList<DirectionListener> mDirectionListeners;


    private int lastDirection = -1;
    private int lastPitch;
    private int lastRoll;
    private boolean firstReading = true;
    private Handler mHandler;
    private CountDownLatch mStartCountdown = new CountDownLatch(1);

    private DirectionHelper(){
        mDirectionListeners = new ArrayList<>();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                mStartCountdown.countDown();
                Looper.loop();
            }
        });

        t.start();
    }

    private static Context mContext;

    public synchronized static DirectionHelper getInstance(Context context){
        if(mInstance == null){
            mInstance = new DirectionHelper();

            mContext = context.getApplicationContext();
        }

        return mInstance;
    }

    public void addListener(DirectionListener listener){
        if(!mDirectionListeners.contains(listener)){
            mDirectionListeners.add(listener);
        }
    }

    public void removeListener(DirectionListener listener){
        if(mDirectionListeners.contains(listener)){
            mDirectionListeners.remove(listener);
        }
    }

    public void resetFirstReading(){
        firstReading = true;
    }


    float accelerometerValues[] = null;
    float geomagneticMatrix[] = null;
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values.clone();

                //Initial adjustment in gravity.
                // This is not picked from any document, but comes from actual testing on Nexus 6 device
                //Value MUST be changed for testing on any other device
                accelerometerValues[2] = (float)(accelerometerValues[2] - (10.2537-9.8));

                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                geomagneticMatrix = event.values.clone();
                break;
        }

        if (geomagneticMatrix != null && accelerometerValues != null && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            float[] R = new float[16];
            float[] I = new float[16];
            float[] outR = new float[16];

            //Get the rotation matrix, then remap it from camera surface to world coordinates
            SensorManager.getRotationMatrix(R, I, accelerometerValues, geomagneticMatrix);
            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            float values[] = new float[4];
            SensorManager.getOrientation(outR,values);

            boolean success = SensorManager.getRotationMatrix(R, I, accelerometerValues, geomagneticMatrix);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                int azimut =(int) orientation[0]; // orientation contains: azimut, pitch and roll

            }

            int direction = normalizeDegrees(filterChange((int)Math.toDegrees(values[0])));
            int pitch = normalizeDegrees(Math.toDegrees(values[1]));
            int roll = normalizeDegrees(Math.toDegrees(values[2]));

            if(lastDirection == -1){
                try {
                    mStartCountdown.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.post(mDirectionSendRunnable);
            }

            if((int)direction != (int)lastDirection){
                lastDirection = (int)direction;
                lastPitch = (int)pitch;
                lastRoll = (int)roll;

                Log.d(TAG, "Direction = " + lastDirection);
            }
        }
    }


    private Runnable mDirectionSendRunnable = new Runnable() {
        @Override
        public void run() {
            long timestamp = System.currentTimeMillis();

            if(mDirectionListeners != null &&
                    !mDirectionListeners.isEmpty()){
                Log.d(TAG, "Sending Direction = "+lastDirection );

                //Toast.makeText(mContext, "Direction = "+lastDirection, Toast.LENGTH_SHORT).show();
                for(DirectionListener currentListener : mDirectionListeners){
                    currentListener.onDirectionListener(lastDirection, timestamp);
                }
            }

            mHandler.postDelayed(mDirectionSendRunnable, 3000);
        }
    };



    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int normalizeDegrees(double rads){
        return (int)((rads+360)%360);
    }


    private static final int MAX_CHANGE = 3;
    private int filterChange(int newDir){
        newDir = normalizeDegrees(newDir);
        //On the first reading, assume it's right.  Otherwise NW readings take forever to ramp up
        if(firstReading){
            firstReading = false;
            return newDir;
        }

        //Figure out how many degrees to move
        int delta = newDir - lastDirection;
        int normalizedDelta = normalizeDegrees(delta);
        int change = Math.min(Math.abs(delta),MAX_CHANGE);

        if( normalizedDelta > 180 ){
            change = -change;
        }

        return lastDirection+change;
    }


}
