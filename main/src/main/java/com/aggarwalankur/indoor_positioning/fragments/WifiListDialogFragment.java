package com.aggarwalankur.indoor_positioning.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.core.listeners.SelectedAnchorListener;
import com.aggarwalankur.indoor_positioning.core.wifi.WifiScanResult;

import java.util.ArrayList;

/**
 * Created by Ankur on 13-Mar-16.
 */
public class WifiListDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener{

    String[] listitems = { "item01", "item02", "item03", "item04", "item05", "item06", "item07", "item08", "item09", "item10", "item11", "item12" };

    private ListView mylist;

    private String[] ssid;
    private String[] bssid;

    private SelectedAnchorListener selectedAnchorListener;

    public void setSelectedAnchorListener(SelectedAnchorListener listener){
        selectedAnchorListener = listener;
    }

    public synchronized void setWiFiList(final ArrayList<WifiScanResult> wifiResults){
        int size = wifiResults.size();

        listitems = new String[size];
        ssid = new String[size];
        bssid = new String[size];

        for(int temp =0; temp< size; temp++){
            ssid[temp] = wifiResults.get(temp).ssid;
            bssid[temp] = wifiResults.get(temp).bssid;

            listitems[temp] = ssid[temp] + " ("+bssid[temp] + ")";
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.wifi_dialog_fragment, null, false);
        mylist = (ListView) view.findViewById(R.id.wifi_list);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, listitems);

        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        dismiss();
        Toast.makeText(getActivity(), listitems[position], Toast.LENGTH_SHORT)
                .show();

        selectedAnchorListener.onAnchorSelected(bssid[position], IConstants.ANCHOR_TYPE.WIFI);
    }
}
