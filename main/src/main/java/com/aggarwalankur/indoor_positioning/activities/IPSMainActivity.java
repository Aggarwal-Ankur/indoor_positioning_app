package com.aggarwalankur.indoor_positioning.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.core.logger.IPSLogger;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataManager;
import com.aggarwalankur.indoor_positioning.core.xml.MapXmlHelper;

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


        MapXmlHelper.getInstance(this).loadXml();

        IPSLogger ipsLogger = new IPSLogger();
        ipsLogger.setName("Logger_thread");
        ipsLogger.start();

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

                try {

                    String mapPath = TrainingDataManager.getInstance().getData().mapPath;

                    //Intent to map draw activity
                    Intent addAnchorsIntent = new Intent(this, MapActivity.class);
                    addAnchorsIntent.putExtra(IConstants.INTENT_EXTRAS.MAP_PATH, mapPath);
                    addAnchorsIntent.putExtra(IConstants.INTENT_EXTRAS.MODE, IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING);
                    startActivity(addAnchorsIntent);
                }catch(Exception e){
                    e.printStackTrace();

                    Toast.makeText(getApplicationContext(), "Invalid map data. Please check again", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
