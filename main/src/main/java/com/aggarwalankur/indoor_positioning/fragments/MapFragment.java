package com.aggarwalankur.indoor_positioning.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.core.trainingdata.AnchorPOJO;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataManager;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataPOJO;
import com.aggarwalankur.indoor_positioning.customviews.Panel;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment {

    private String mapPath ="";
    private int mode = IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING;

    private Panel mPanel;

    View vwStatusSeperator;

    public MapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mapPath = getArguments().getString(IConstants.INTENT_EXTRAS.MAP_PATH);
        mode = getArguments().getInt(IConstants.INTENT_EXTRAS.MODE);

        mPanel = (Panel) v.findViewById(R.id.panel);

        vwStatusSeperator = v.findViewById(R.id.vwStatusSeperator);

        //This is a direct call, so it should be avoided as much as possible
        mPanel.loadNewMap(mapPath, mode);

        mPanel.repaint();

        v.post(new Runnable() {
            @Override
            public void run() {
                int []iStatusBarLocarr = new int[2];
                vwStatusSeperator.getLocationOnScreen(iStatusBarLocarr);
                mPanel.setYOffSet(iStatusBarLocarr[1]);
            }
        });

        TrainingDataPOJO trainingData = TrainingDataManager.getInstance().getData();

        mPanel.mapHeightMetres = trainingData.mapHeight;
        mPanel.mapWidthMetres = trainingData.mapWidth;

        mPanel.setAnchorList((ArrayList<AnchorPOJO>) trainingData.anchorList.clone());
        mPanel.setWifiDataPointList(TrainingDataManager.getInstance().getData().getDataLocationPoints());

        return v;
    }

    public Panel getPanelData(){
        return mPanel;
    }

}
