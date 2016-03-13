package com.aggarwalankur.indoor_positioning.core.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.common.IConstants;

import java.util.ArrayList;
import java.util.List;

public class WifiHelper {

    private static final String TAG = "WifiHelper";
    private ArrayList<WifiScanResult> wifiResults;

	private ArrayList<WiFiListener> wifiScanListeners;
	private boolean isScanStarted = false;
    private String connectedBssid = null;
    private Handler mHandler;
    
    private static WifiHelper instance;
    private BroadcastReceiver wifiScanReceiver;

    private Context mContext;

	private WifiHelper(){
		wifiScanListeners = new ArrayList<>();

        Looper looper = Looper.myLooper();
        mHandler = new Handler(looper);
        Looper.loop();
	}

    public static WifiHelper getInstance() {
        if (instance == null) {
            instance = new WifiHelper();
        }
        return instance;
    }

	public synchronized void addListener(WiFiListener listener, Context context){
		wifiScanListeners.add(listener);

        if(mContext == null){
            mContext = context.getApplicationContext();
        }

		if(!isScanStarted){
			initWifiScan();
			isScanStarted = true;
		}
	}

	public synchronized void removeListener(WiFiListener listener, Context context){
		wifiScanListeners.remove(listener);

		if(wifiScanListeners.isEmpty()){
			deInitWifiScan(context);
			isScanStarted = false;
		}
	}


    public void deInitWifiScan(Context context) {
        mHandler.removeCallbacksAndMessages(null);
    	context.unregisterReceiver(wifiScanReceiver);
    }

    public void initWifiScan() {
        final WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        final ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        Log.d(TAG, "initWifiScan");
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Broadcast received..");
                long timestamp = System.currentTimeMillis();

                connectedBssid = null;
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected()) {
                    connectedBssid = wifi.getConnectionInfo().getBSSID();
                }
                Log.d(TAG, "connectedBssid" +connectedBssid);
                List<ScanResult> results = wifi.getScanResults();
                if(results != null)	{
                     wifiResults = new ArrayList<WifiScanResult>();
                    //check for connected AP
                    for(int i=0; i<results.size(); i++){
                        if(results.get(i).BSSID != null){
                            if(results.get(i).BSSID.equals(connectedBssid)) {
                                Log.i(TAG, "Connected BSSID added in wifi scan result list");
                                wifiResults.add(new WifiScanResult(results.get(i).BSSID, results.get(i).SSID, results.get(i).frequency, results.get(i).level, results.get(i).capabilities));
                                break;
                            }
                        }else{
                            Log.d(TAG, "BSSID was found null in wifi scan result.");
                        }
                    }
                    //Add other detected points.
                    for(int i = 0; i<results.size() && i< IConstants.WIFI_CONSTANTS.ITEM_LIMIT; i++){
                        if(results.get(i).BSSID != null){
                            if(!results.get(i).BSSID.equals(connectedBssid)) {
                                wifiResults.add(new WifiScanResult(results.get(i).BSSID, results.get(i).SSID, results.get(i).frequency, results.get(i).level, results.get(i).capabilities));
                            }
                        }else{
                            Log.d(TAG, "BSSID was found null in wifi scan result.");
                        }

                    }
                    Log.d(TAG, "WifiResult list size : " + wifiResults.size());

                    if(isScanStarted && !wifiScanListeners.isEmpty()){
                        Log.v(TAG, "WifiScanResult Size before recording: " + wifiResults.size());

                        for(WiFiListener currentListener : wifiScanListeners){
                            currentListener.onWifiScanResultsReceived(wifiResults, timestamp);
                        }
                    }

                    mHandler.postDelayed(mWifiRunnable, IConstants.WIFI_CONSTANTS.WIFI_DELAY);
                }
            }
        };

        mContext.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mHandler.post(mWifiRunnable);
    }

    private Runnable mWifiRunnable = new Runnable() {
        @Override
        public void run() {
            final WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            final ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi;
            while (true) {
                mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        /*
                         * Using Wifi manager to check if wifi is On
                         * If it is not connected, mark connectedBssid null
                         * to reset connected params and call update.
                         * */
                if (wifi.isWifiEnabled()) {
                    if(!(mWifi.isConnected()) && connectedBssid!=null){
                        connectedBssid = null;
                    }
                    wifi.startScan();
                    Log.d(TAG, "Start WifiScan");
                }
                else{
                    Toast.makeText(mContext, "Wi-Fi disabled. Please enable from settings", Toast.LENGTH_SHORT).show();
                }
            }

        }
    };

}
