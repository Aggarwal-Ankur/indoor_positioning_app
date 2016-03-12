package com.aggarwalankur.indoor_positioning.fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.customviews.Panel;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment {

    private String mapPath ="";

    private Panel mPanel;

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

        //This is a direct call, so it should be avoided as much as possible
        mPanel.loadNewMap(mapPath);


        return v;
    }
}
