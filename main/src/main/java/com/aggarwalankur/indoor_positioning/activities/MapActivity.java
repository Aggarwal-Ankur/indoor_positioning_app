package com.aggarwalankur.indoor_positioning.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.fragments.MapFragment;

public class MapActivity extends AppCompatActivity {

    private String mapPath = "";

    private final String TAG = "MapActivity";

    private final String MAP_FRAGMENT_TAG = "map_fragment";

    private MapFragment mapFragment;

    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        if(intent != null){
            Bundle extras = intent.getExtras();

            if(extras != null){
                mapPath = extras.getString(IConstants.INTENT_EXTRAS.MAP_PATH);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString(IConstants.INTENT_EXTRAS.MAP_PATH, mapPath);
        mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);

        fm = getSupportFragmentManager();

        fm.beginTransaction().add(mapFragment, MAP_FRAGMENT_TAG);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged");
    }
}
