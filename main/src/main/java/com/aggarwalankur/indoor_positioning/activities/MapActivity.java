package com.aggarwalankur.indoor_positioning.activities;

import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.nfc.NfcAdapter;
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
import com.aggarwalankur.indoor_positioning.core.nfc.NfcHelper;
import com.aggarwalankur.indoor_positioning.core.trainingdata.AnchorPOJO;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataManager;
import com.aggarwalankur.indoor_positioning.core.wifi.WiFiListener;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiHelper;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiScanResult;
import com.aggarwalankur.indoor_positioning.fragments.MapFragment;
import com.aggarwalankur.indoor_positioning.fragments.WifiListDialogFragment;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements WiFiListener, View.OnClickListener, SelectedAnchorListener {

    private final String TAG = "MapActivity";

    private final String MAP_FRAGMENT_TAG = "map_fragment";

    private String mapPath = "";
    private int mMode = IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING;

    private MapFragment mapFragment;

    private FragmentManager fm;

    private View mButtonContainer;

    private ArrayList<WifiScanResult> mScanResults;

    private Button mOKButton, mCancelButton, mIAMHereButton, mStopTrainingWifiButton;


    private String mSelectedAnchor = "";
    private int selectedAnchorType = IConstants.ANCHOR_TYPE.UNDEFINED;


    private PendingIntent nfcPendingIntent;
    private IntentFilter[] nfcIntentFiltersArray;
    private NfcAdapter nfcAdpt;

    private PointF mIAmHereLocation;

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

        mIAMHereButton = (Button)findViewById(R.id.button_i_am_here);
        mStopTrainingWifiButton = (Button)findViewById(R.id.button_wifi_train_end);
        mIAMHereButton.setOnClickListener(this);
        mStopTrainingWifiButton.setOnClickListener(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        if(intent != null){
            Bundle extras = intent.getExtras();

            if(extras != null){
                mapPath = extras.getString(IConstants.INTENT_EXTRAS.MAP_PATH);
                mMode = extras.getInt(IConstants.INTENT_EXTRAS.MODE);
            }
        }



        fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putString(IConstants.INTENT_EXTRAS.MAP_PATH, mapPath);
            bundle.putInt(IConstants.INTENT_EXTRAS.MODE, mMode);
            mapFragment = new MapFragment();
            mapFragment.setArguments(bundle);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.map_activity_layout, mapFragment).commit();
        }

        //Cehck mode and then add listener
        WifiHelper.getInstance().addListener(this, this);


        //Check mode and add
        if((mMode == IConstants.MAP_ACTIVITY_MODES.MODE_SET_ANCHORS
                || mMode == IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING)) {
            nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            // Check if the smartphone has NFC
            if (nfcAdpt == null) {
                Toast.makeText(this, "NFC not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            // Check if NFC is enabled
            if (!nfcAdpt.isEnabled()) {
                Toast.makeText(this, "Enable NFC before using the app", Toast.LENGTH_LONG).show();
            }

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            nfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);

            IntentFilter tagIntentFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                tagIntentFilter.addDataType("text/plain");
                nfcIntentFiltersArray = new IntentFilter[]{tagIntentFilter};
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }


        //Do mode specific tasks
        switch (mMode){
            case IConstants.MAP_ACTIVITY_MODES.MODE_SET_ANCHORS :
                setTitle("Set Anchors");

                mOKButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.VISIBLE);
                mIAMHereButton.setVisibility(View.GONE);
                mStopTrainingWifiButton.setVisibility(View.GONE);

                mOKButton.setEnabled(false);
                mCancelButton.setEnabled(false);
                mIAMHereButton.setEnabled(false);
                mStopTrainingWifiButton.setEnabled(false);

                break;

            case IConstants.MAP_ACTIVITY_MODES.MODE_TRAIN_WIFI :
                setTitle("Add Wi-Fi datapoints");

                mOKButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
                mIAMHereButton.setVisibility(View.VISIBLE);
                mStopTrainingWifiButton.setVisibility(View.GONE);

                mOKButton.setEnabled(false);
                mCancelButton.setEnabled(false);
                mIAMHereButton.setEnabled(true);
                mStopTrainingWifiButton.setEnabled(false);
                break;

            case IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING :
                setTitle("Indoor Positioning");
                break;
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if((mMode == IConstants.MAP_ACTIVITY_MODES.MODE_SET_ANCHORS
                || mMode == IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING)) {
            nfcAdpt.enableForegroundDispatch(
                    this,
                    nfcPendingIntent,
                    null,
                    null);
        }
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if ((mMode == IConstants.MAP_ACTIVITY_MODES.MODE_SET_ANCHORS
                || mMode == IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING)
                && (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))) {

            mSelectedAnchor = NfcHelper.getInstance().getNfcTag(intent);
            Toast.makeText(this, "Tag found: "+ mSelectedAnchor, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu on basis of mode
        switch (mMode){
            case IConstants.MAP_ACTIVITY_MODES.MODE_SET_ANCHORS :
                getMenuInflater().inflate(R.menu.menu_map, menu);
                return true;

            case IConstants.MAP_ACTIVITY_MODES.MODE_TRAIN_WIFI :
                getMenuInflater().inflate(R.menu.menu_train_wifi, menu);
                return true;
            case IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING :
                getMenuInflater().inflate(R.menu.menu_map, menu);
                return true;

            default :
                return false;
        }

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
                selectedAnchorType = IConstants.ANCHOR_TYPE.NFC;
                mOKButton.setEnabled(true);
                mCancelButton.setEnabled(true);
                break;

            case R.id.clear_all_wifi_data:
                //Clear all Wifi training data
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
                if(!mSelectedAnchor.isEmpty() && selectedAnchorType != IConstants.ANCHOR_TYPE.UNDEFINED) {

                    TrainingDataManager.getInstance().addAnchor(mapFragment.getPanelData().getCurrentLoc(), mSelectedAnchor, selectedAnchorType);
                    mOKButton.setEnabled(false);
                    mCancelButton.setEnabled(false);
                    refreshAnchorData();

                    mapFragment.getPanelData().setAnchorList((ArrayList<AnchorPOJO>) TrainingDataManager.getInstance().getData().anchorList.clone());
                    mapFragment.getPanelData().invalidate();
                }else{
                    Toast.makeText(this, "Invalid Anchor data", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.button_cancel:
                if(mMode == IConstants.MAP_ACTIVITY_MODES.MODE_SET_ANCHORS) {

                    mOKButton.setVisibility(View.VISIBLE);
                    mCancelButton.setVisibility(View.VISIBLE);
                    mIAMHereButton.setVisibility(View.GONE);
                    mStopTrainingWifiButton.setVisibility(View.GONE);

                    mOKButton.setEnabled(false);
                    mCancelButton.setEnabled(false);
                    mIAMHereButton.setEnabled(false);
                    mStopTrainingWifiButton.setEnabled(false);

                    refreshAnchorData();
                }else if(mMode == IConstants.MAP_ACTIVITY_MODES.MODE_TRAIN_WIFI){
                    mOKButton.setVisibility(View.GONE);
                    mCancelButton.setVisibility(View.VISIBLE);
                    mIAMHereButton.setVisibility(View.VISIBLE);
                    mStopTrainingWifiButton.setVisibility(View.GONE);

                    mOKButton.setEnabled(false);
                    mCancelButton.setEnabled(false);
                    mIAMHereButton.setEnabled(true);
                    mStopTrainingWifiButton.setEnabled(false);

                    mIAmHereLocation = null;
                }
                break;

            case R.id.button_i_am_here:

                mIAmHereLocation = mapFragment.getPanelData().getCurrentLoc();

                if(mIAmHereLocation != null) {
                    mOKButton.setVisibility(View.GONE);
                    mCancelButton.setVisibility(View.VISIBLE);
                    mIAMHereButton.setVisibility(View.GONE);
                    mStopTrainingWifiButton.setVisibility(View.VISIBLE);

                    mOKButton.setEnabled(false);
                    mCancelButton.setEnabled(true);
                    mIAMHereButton.setEnabled(false);
                    mStopTrainingWifiButton.setEnabled(true);


                    TrainingDataManager.getInstance().setCollectWifitrainingData(true);
                }else{
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.button_wifi_train_end:
                mOKButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
                mIAMHereButton.setVisibility(View.VISIBLE);
                mStopTrainingWifiButton.setVisibility(View.GONE);

                mOKButton.setEnabled(false);
                mCancelButton.setEnabled(false);
                mIAMHereButton.setEnabled(true);
                mStopTrainingWifiButton.setEnabled(false);

                //Put data into Training DB

                TrainingDataManager.getInstance().addWifiDataPoint(mIAmHereLocation);
                TrainingDataManager.getInstance().setCollectWifitrainingData(false);
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
