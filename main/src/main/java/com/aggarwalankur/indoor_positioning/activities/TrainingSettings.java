package com.aggarwalankur.indoor_positioning.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aggarwalankur.indoor_positioning.R;

import java.util.ArrayList;

public class TrainingSettings extends AppCompatActivity implements View.OnClickListener{

    private static final int CODE_SD = 0;

    private TextView mMapFile;
    private Button mMapFileSelector;
    private EditText mMapHeight, mMapWidth, mMapBearing, mStrideLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training_settings);

        mMapFile = (TextView) findViewById(R.id.map_file_name);

        mMapFileSelector = (Button) findViewById(R.id.btn_select_mapfile);
        mMapFileSelector.setOnClickListener(this);

        mMapHeight = (EditText) findViewById(R.id.map_height);
        mMapWidth = (EditText) findViewById(R.id.map_width);
        mMapBearing = (EditText) findViewById(R.id.map_bearing);
        mStrideLength = (EditText) findViewById(R.id.stride_length);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_select_mapfile :
                Intent filePickerDialogIntent = new Intent(this, FilePickerActivity.class);
                filePickerDialogIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
                filePickerDialogIntent.putExtra(FilePickerActivity.REQUEST, Request.FILE);
                startActivityForResult(filePickerDialogIntent, REQUEST_FILE);
                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_DIRECTORY) && (resultCode == RESULT_OK)) {
            Toast.makeText(this, "Directory Selected: " + data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH), Toast.LENGTH_LONG).show();
        } else if ((requestCode == REQUEST_FILE) && (resultCode == RESULT_OK)) {
            Toast.makeText(this, "File Selected: " + data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH), Toast.LENGTH_LONG).show();
        }
    }
}
