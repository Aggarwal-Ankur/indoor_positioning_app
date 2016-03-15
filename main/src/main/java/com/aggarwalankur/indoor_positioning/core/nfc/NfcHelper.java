package com.aggarwalankur.indoor_positioning.core.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Ankur on 15-Mar-16.
 */
public class NfcHelper {

    private static NfcHelper mInstance;

    private static final String TAG = "NfcHelper";

    private NfcHelper(){

    }

    public synchronized static NfcHelper getInstance(){
        if(mInstance == null){
            mInstance = new NfcHelper();
        }

        return mInstance;
    }

    public String getNfcTag(Intent intent){
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] id = tag.getId();

        String idValue = "";

        for(int i =0; i<id.length; i++){
            if(i>0){
                idValue = idValue + ":" + Integer.toHexString(id[i] & 0xFF);
            }else{
                idValue = Integer.toHexString(id[i] & 0xFF);
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

        return idValue;
    }
}
