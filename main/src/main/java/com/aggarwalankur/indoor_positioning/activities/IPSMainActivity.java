package com.aggarwalankur.indoor_positioning.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.aggarwalankur.indoor_positioning.R;
import com.nononsenseapps.filepicker.FilePickerActivity;

public class IPSMainActivity extends AppCompatActivity implements View.OnClickListener {

    Button mTrainingButton, mPositioningStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTrainingButton = (Button) findViewById(R.id.start_training);
        mPositioningStartButton = (Button) findViewById(R.id.start_positioning);

        mTrainingButton.setOnClickListener(this);
        mPositioningStartButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.start_training:
                Intent i = new Intent(IPSMainActivity.this.getApplicationContext(), TrainingSettings.class);
                startActivity(i);

                break;
            case R.id.start_positioning:
                mTrainingButton.setText("Bore");
                break;
        }
    }
}
