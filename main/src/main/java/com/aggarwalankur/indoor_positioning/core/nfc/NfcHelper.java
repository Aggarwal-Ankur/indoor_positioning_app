package com.aggarwalankur.indoor_positioning.core.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;

import com.aggarwalankur.indoor_positioning.core.listeners.NfcListener;

import java.util.ArrayList;

/**
 * Created by Ankur on 15-Mar-16.
 */
public class NfcHelper {

    private static NfcHelper mInstance;

    private static final String TAG = "NfcHelper";

    private ArrayList<NfcListener> mListenerList = new ArrayList<>();

    private NfcHelper(){

    }

    public synchronized static NfcHelper getInstance(){
        if(mInstance == null){
            mInstance = new NfcHelper();
        }

        return mInstance;
    }

    public void addListener(NfcListener listener){
        if(!mListenerList.contains(listener)){
            mListenerList.add(listener);
        }
    }

    public void removeListener(NfcListener listener){
        if(mListenerList.contains(listener)){
            mListenerList.remove(listener);
        }
    }

    public String getNfcTag(Intent intent){
        long timestamp = System.currentTimeMillis();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] id = tag.getId();

        String idValue = "";

        for(int i =0; i<id.length; i++){
            String tempVal = Integer.toHexString(id[i] & 0xFF);

            if(tempVal.length()==1){
                tempVal = "0"+tempVal;
            }
            if(i>0){
                idValue = idValue + ":" + tempVal;
            }else{
                idValue = tempVal;
            }
        }

        Log.d(TAG, "idValue = "+idValue);

        String[] techList = tag.getTechList();
        for (int i = 0; i < techList.length; i++) {

            if (techList[i].equals(MifareUltralight.class.getName())) {
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:



                        break;
                }
            }
        }


        //Also, send the tag event to listeners
        for(NfcListener currentListener : mListenerList){
            currentListener.onNfcTagScanned(idValue, timestamp);
        }

        return idValue;
    }
}
