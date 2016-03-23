package com.aggarwalankur.indoor_positioning.core.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.aggarwalankur.indoor_positioning.core.listeners.BleScanListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankur on 23/03/2016.
 */
public class BLEScanHelper extends ScanCallback {

    private static final String TAG = "BLEScanHelper";

    private static BLEScanHelper mInstance;

    private ArrayList<BleScanListener> mListeners;

    private BLEScanHelper(){
        mListeners = new ArrayList<>();
    }

    public synchronized static BLEScanHelper getInstance(){
        if(mInstance == null){
            mInstance = new BLEScanHelper();
        }

        return mInstance;
    }

    public void addListener(BleScanListener listener){
        if(mListeners != null && !mListeners.contains(listener)){
            mListeners.add(listener);
        }
    }

    public void removeListener(BleScanListener listener){
        if(mListeners != null && mListeners.contains(listener)){
            mListeners.remove(listener);
        }
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        Log.d(TAG, "onScanResult");
        processResult(result);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        Log.d(TAG, "onBatchScanResults: "+results.size()+" results");
        for (ScanResult result : results) {
            processResult(result);
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.w(TAG, "LE Scan Failed: "+errorCode);
    }

    private void processResult(ScanResult result) {
        Log.i(TAG, "New LE Device: " + result.getDevice().getName() + " @ " + result.getRssi());

    }
}

