package com.aggarwalankur.indoor_positioning.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;

import java.util.ArrayList;

public class TrainingSettings extends AppCompatActivity implements View.OnClickListener{

    private static final int CODE_SD = 0;

    private TextView mMapFile;
    private Button mMapFileSelector, mAddAnchorsButton, mTrainWifiButton;
    private EditText mMapHeight, mMapWidth, mMapBearing, mStrideLength;

    private String mMapPath="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_settings);

        mMapFile = (TextView) findViewById(R.id.map_file_name);

        mMapFileSelector = (Button) findViewById(R.id.btn_select_mapfile);
        mAddAnchorsButton = (Button) findViewById(R.id.btn_add_anchors);
        mTrainWifiButton = (Button) findViewById(R.id.btn_train_wifi);


        mMapFileSelector.setOnClickListener(this);
        mAddAnchorsButton.setOnClickListener(this);
        mTrainWifiButton.setOnClickListener(this);

        mMapHeight = (EditText) findViewById(R.id.map_height);
        mMapWidth = (EditText) findViewById(R.id.map_width);
        mMapBearing = (EditText) findViewById(R.id.map_bearing);
        mStrideLength = (EditText) findViewById(R.id.stride_length);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_select_mapfile :
                Intent filePickerIntent = new Intent(this, FilePickerActivity.class);
                ArrayList<String> extensions = new ArrayList<String>();
                extensions.add(".jpeg");
                extensions.add(".gif");
                extensions.add(".png");
                extensions.add(".jpg");
                extensions.add(".JPEG");
                extensions.add(".GIF");
                extensions.add(".PNG");
                extensions.add(".JPG");
                filePickerIntent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS,
                        extensions);
                filePickerIntent.putExtra(FilePickerActivity.EXTRA_FILE_PATH,
                        "/sdcard/");
                startActivityForResult(filePickerIntent, IConstants.FILE_PICKER_CONSTANTS.PICK_FILE);
                break;

            case R.id.btn_add_anchors:
                //Intent to map draw activity

                Intent addAnchorsIntent = new Intent(this, MapActivity.class);
                addAnchorsIntent.putExtra(IConstants.INTENT_EXTRAS.MAP_PATH, mMapPath);
                startActivity(addAnchorsIntent);

                break;

            case R.id.btn_train_wifi :
                //Intent to select training Wi-Fi's

                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == IConstants.FILE_PICKER_CONSTANTS.SAVE_FILE) && (resultCode == RESULT_OK)) {
        } else if ((requestCode == IConstants.FILE_PICKER_CONSTANTS.PICK_FILE) && (resultCode == RESULT_OK)) {
            mMapPath = data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH);
            mMapFile.setText("Map File : "+mMapPath);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
