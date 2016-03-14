package com.aggarwalankur.indoor_positioning.fragments;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.customviews.Panel;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment {

    private String mapPath ="";

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

        mPanel = (Panel) v.findViewById(R.id.panel);

        vwStatusSeperator = v.findViewById(R.id.vwStatusSeperator);

        //This is a direct call, so it should be avoided as much as possible
        mPanel.loadNewMap(mapPath);

        mPanel.repaint();

        v.post(new Runnable() {
            @Override
            public void run() {
                int []iStatusBarLocarr = new int[2];
                vwStatusSeperator.getLocationOnScreen(iStatusBarLocarr);
                mPanel.setYOffSet(iStatusBarLocarr[1]);
            }
        });


        return v;
    }

    public Panel getPanelData(){
        return mPanel;
    }

}
