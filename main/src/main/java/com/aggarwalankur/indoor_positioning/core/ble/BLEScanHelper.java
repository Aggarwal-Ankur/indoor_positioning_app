package com.aggarwalankur.indoor_positioning.core.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.aggarwalankur.indoor_positioning.core.listeners.BleScanListener;

import java.util.ArrayList;

/**
 * Created by Ankur on 23/03/2016.
 */
public class BLEScanHelper implements BluetoothAdapter.LeScanCallback{

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
    public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

    }
}

