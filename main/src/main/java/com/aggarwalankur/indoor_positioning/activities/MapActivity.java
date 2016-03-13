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
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.fragments.MapFragment;

public class MapActivity extends AppCompatActivity {

    private String mapPath = "";

    private final String TAG = "MapActivity";

    private final String MAP_FRAGMENT_TAG = "map_fragment";

    private MapFragment mapFragment;

    private FragmentManager fm;

    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_map);

        mLayout = findViewById(R.id.map_activity_layout);

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
        }

        return true;
    }
}
