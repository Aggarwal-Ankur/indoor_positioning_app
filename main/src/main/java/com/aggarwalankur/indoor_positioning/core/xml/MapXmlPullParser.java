package com.aggarwalankur.indoor_positioning.core.xml;

import android.util.Log;

import com.aggarwalankur.indoor_positioning.core.trainingdata.AnchorPOJO;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataPOJO;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Ankur on 14-Mar-16.
 */
public class MapXmlPullParser {
    private static final String LOG_TAG = "MapXmlPullParser";


    public static final String TAG_ROOT = "mapData";

    public static final String ATR_PATH = "path";
    public static final String ATR_HEIGHT = "height";
    public static final String ATR_WIDTH = "width";
    public static final String ATR_BEARING = "bearing";
    public static final String ATR_STRIDELENGTH = "strideLength";


    public static final String ELEMENT_ANCHORLIST = "anchorList";
    public static final String ELEMENT_ANCHOR = "anchor";


    public static final String ATR_XCORD = "xCord";
    public static final String ATR_YCORD = "yCord";
    public static final String ATR_ID = "id";
    public static final String ATR_TYPE = "type";


    private TrainingDataPOJO trainingData;


    public synchronized TrainingDataPOJO parseXml(File file) throws XmlPullParserException, IOException {
        //Build the requisites
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xpp = factory.newPullParser();

        //Set input source
        FileInputStream fis = new FileInputStream(file);
        xpp.setInput(new InputStreamReader(fis));


        //Now parse using pull mechanism
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_DOCUMENT) {
                Log.d(LOG_TAG, "Start document");
                trainingData = new TrainingDataPOJO();
            } else if(eventType == XmlPullParser.START_TAG) {

                //Root element and its attributes
                if(xpp.getName().equalsIgnoreCase(TAG_ROOT)){
                    trainingData.mapPath = xpp.getAttributeValue(null, ATR_PATH);

                    trainingData.mapHeight = Integer.parseInt(xpp.getAttributeValue(null, ATR_HEIGHT));
                    trainingData.mapWidth = Integer.parseInt(xpp.getAttributeValue(null, ATR_WIDTH));
                    trainingData.mapBearing = Integer.parseInt(xpp.getAttributeValue(null, ATR_BEARING));
                    trainingData.strideLength = Integer.parseInt(xpp.getAttributeValue(null, ATR_STRIDELENGTH));
                    trainingData.stepLength = (float)trainingData.strideLength/2;
                }else if(xpp.getName().equalsIgnoreCase(ELEMENT_ANCHORLIST)){
                    trainingData.anchorList = new ArrayList<>();
                }else if(xpp.getName().equalsIgnoreCase(ELEMENT_ANCHOR)){
                    AnchorPOJO currentAnchor = new AnchorPOJO();

                    currentAnchor.x = Float.parseFloat(xpp.getAttributeValue(null, ATR_XCORD));
                    currentAnchor.y = Float.parseFloat(xpp.getAttributeValue(null, ATR_YCORD));
                    currentAnchor.id = xpp.getAttributeValue(null, ATR_ID);
                    currentAnchor.type = Integer.parseInt(xpp.getAttributeValue(null, ATR_TYPE));

                    trainingData.anchorList.add(currentAnchor);
                }

            }

            //Iterate through the xml
            eventType = xpp.next();
        }//Endof while


        Log.d(LOG_TAG, "End document");

        return trainingData;
    }



}
