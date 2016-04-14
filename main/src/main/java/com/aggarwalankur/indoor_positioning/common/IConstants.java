package com.aggarwalankur.indoor_positioning.common;

/**
 * Created by Ankur on 10/03/2016.
 */
public interface IConstants {
    interface FILE_PICKER_CONSTANTS{
        int PICK_FILE = 0;
        int SAVE_FILE = 1;
    }

    interface INTENT_EXTRAS{
        String MAP_PATH = "map_path";

        String MODE = "mode";
    }

    interface WIFI_CONSTANTS{
        int ITEM_LIMIT = 12;

        //ms
        int WIFI_DELAY = 500;
    }

    interface ANCHOR_TYPE{
        int UNDEFINED = 1000;

        int NFC = UNDEFINED + 1;
        int WIFI= UNDEFINED + 2;
        int BLE = UNDEFINED + 3;
    }

    interface MAP_ACTIVITY_MODES{
        int MODE_SET_ANCHORS = 2000;
        int MODE_TRAIN_WIFI = 2001;
        int INDOOR_POSITIONING = 2002;
    }

    interface WEBSERVICE_CONSTANTS{
        String URL = "http://192.168.244.51";

        String PORTNO = "8999";

        String WEBSERVICE_URI = "/indoor_position_webservice/Positioning";
    }
}
