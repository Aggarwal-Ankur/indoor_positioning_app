package com.aggarwalankur.indoor_positioning.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.core.listeners.SelectedAnchorListener;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataManager;
import com.aggarwalankur.indoor_positioning.core.wifi.WiFiListener;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiHelper;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiScanResult;
import com.aggarwalankur.indoor_positioning.fragments.MapFragment;
import com.aggarwalankur.indoor_positioning.fragments.WifiListDialogFragment;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements WiFiListener, View.OnClickListener, SelectedAnchorListener {

    private String mapPath = "";

    private final String TAG = "MapActivity";

    private final String MAP_FRAGMENT_TAG = "map_fragment";

    private MapFragment mapFragment;

    private FragmentManager fm;

    private View mButtonContainer;

    private ArrayList<WifiScanResult> mScanResults;

    private Button mOKButton, mCancelButton;


    private String mSelectedAnchor = "";
    private int selectedAnchorType = IConstants.ANCHOR_TYPE.UNDEFINED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_map);

        mButtonContainer = findViewById(R.id.button_container);

        mOKButton = (Button)findViewById(R.id.button_ok);
        mCancelButton = (Button)findViewById(R.id.button_cancel);
        mOKButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);


        mOKButton.setEnabled(false);
        mCancelButton.setEnabled(false);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        if(intent != null){
            Bundle extras = intent.getExtras();

            if(extras != null){
                mapPath = extras.getString(IConstants.INTENT_EXTRAS.MAP_PATH);
            }
        }



        fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString(IConstants.INTENT_EXTRAS.MAP_PATH, mapPath);
            mapFragment = new MapFragment();
            mapFragment.setArguments(bundle);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.map_activity_layout, mapFragment).commit();
        }


        //Cehck mode and then add listener
        WifiHelper.getInstance().addListener(this, this);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.place_bt:
                Toast.makeText(this.getApplicationContext(), "BT", Toast.LENGTH_SHORT).show();
                break;

            case R.id.place_wifi:
                //Show the WiFi results dialog
                FragmentManager manager = getSupportFragmentManager();

                WifiListDialogFragment dialog = new WifiListDialogFragment();
                dialog.setWiFiList(mScanResults);
                dialog.setSelectedAnchorListener(this);
                dialog.show(manager, "wifiListDialog");

                mOKButton.setEnabled(true);
                mCancelButton.setEnabled(true);
                break;

            case R.id.place_nfc:
                //Show the scan dialog
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        WifiHelper.getInstance().removeListener(this, this);
        super.onDestroy();
    }

    @Override
    public void onWifiScanResultsReceived(ArrayList<WifiScanResult> scanResults, long timestamp) {
        Log.i(TAG, "onWifiScanResultsReceived. size ="+scanResults.size());

        mScanResults = (ArrayList<WifiScanResult>)scanResults.clone();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.button_ok:
                TrainingDataManager.getInstance().addAnchor(mapFragment.getPanelData().getCurrentLoc(), mSelectedAnchor, selectedAnchorType);
                mOKButton.setEnabled(false);
                mCancelButton.setEnabled(false);
                refreshAnchorData();
                break;

            case R.id.button_cancel:
                mOKButton.setEnabled(false);
                mCancelButton.setEnabled(false);
                refreshAnchorData();
                break;
        }

    }

    private void refreshAnchorData(){
        mSelectedAnchor = "";
        selectedAnchorType = IConstants.ANCHOR_TYPE.UNDEFINED;
    }

    @Override
    public void onAnchorSelected(String id, int type) {
        mSelectedAnchor = id;
        selectedAnchorType = type;
    }
}
