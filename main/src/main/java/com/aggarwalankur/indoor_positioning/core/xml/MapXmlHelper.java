package com.aggarwalankur.indoor_positioning.core.xml;

import android.content.Context;
import android.util.Log;

import com.aggarwalankur.indoor_positioning.core.trainingdata.AnchorPOJO;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataManager;
import com.aggarwalankur.indoor_positioning.core.trainingdata.TrainingDataPOJO;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Ankur on 13-Mar-16.
 *
 * This must be singleton
 */
public class MapXmlHelper {

    private static final String TAG = "MapXmlHelper";

    private static MapXmlHelper mInstance;

    private String mXMLPath = "";
    private String mXMLFile = "training_data.xml";
    private Context mContext;

    private MapXmlHelper(Context context){
        mContext = context.getApplicationContext();
        mXMLPath = mContext.getExternalFilesDir("XML").toString() + "/" + mXMLFile;
    }

    public synchronized static MapXmlHelper getInstance(Context context){
        if(mInstance == null){
            mInstance = new MapXmlHelper(context);
        }

        return mInstance;
    }


    public void writeXML(TrainingDataPOJO trainingData){
        try {

            //Build the document factory first
            DocumentBuilderFactory dbFactory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder =
                    dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            //Add the elements in DOM

            // root element
            Element rootElement = doc.createElement(MapXmlPullParser.TAG_ROOT);

            // setting attributes to root
            Attr attrMapPath = doc.createAttribute(MapXmlPullParser.ATR_PATH);
            attrMapPath.setValue(trainingData.mapPath);
            rootElement.setAttributeNode(attrMapPath);

            Attr attrMapHeight = doc.createAttribute(MapXmlPullParser.ATR_HEIGHT);
            attrMapHeight.setValue(Integer.toString(trainingData.mapHeight));
            rootElement.setAttributeNode(attrMapHeight);

            Attr attrMapWidth = doc.createAttribute(MapXmlPullParser.ATR_WIDTH);
            attrMapWidth.setValue(Integer.toString(trainingData.mapWidth));
            rootElement.setAttributeNode(attrMapWidth);

            Attr attrMapBearing = doc.createAttribute(MapXmlPullParser.ATR_BEARING);
            attrMapBearing.setValue(Integer.toString(trainingData.mapBearing));
            rootElement.setAttributeNode(attrMapBearing);

            Attr attrStrideLength = doc.createAttribute(MapXmlPullParser.ATR_STRIDELENGTH);
            attrStrideLength.setValue(Integer.toString(trainingData.strideLength));
            rootElement.setAttributeNode(attrStrideLength);

            doc.appendChild(rootElement);


            //Add the Anchor list

            Element anchorList = doc.createElement(MapXmlPullParser.ELEMENT_ANCHORLIST);

            for(AnchorPOJO currentAnchor : trainingData.anchorList){
                Element currentAnchorElement = doc.createElement(MapXmlPullParser.ELEMENT_ANCHOR);


                Attr attrXCord = doc.createAttribute(MapXmlPullParser.ATR_XCORD);
                attrXCord.setValue(Float.toString(currentAnchor.x));
                currentAnchorElement.setAttributeNode(attrXCord);

                Attr attrYCord = doc.createAttribute(MapXmlPullParser.ATR_YCORD);
                attrYCord.setValue(Float.toString(currentAnchor.y));
                currentAnchorElement.setAttributeNode(attrYCord);

                Attr attrId = doc.createAttribute(MapXmlPullParser.ATR_ID);
                attrId.setValue(currentAnchor.id);
                currentAnchorElement.setAttributeNode(attrId);

                Attr attrType = doc.createAttribute(MapXmlPullParser.ATR_TYPE);
                attrType.setValue(Integer.toString(currentAnchor.type));
                currentAnchorElement.setAttributeNode(attrType);

                anchorList.appendChild(currentAnchorElement);
            }


            rootElement.appendChild(anchorList);


            //Write the data to actual XML

            // write the content into xml file
            TransformerFactory transformerFactory =
                    TransformerFactory.newInstance();
            Transformer transformer =
                    transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(mXMLPath));
            transformer.transform(source, result);

            Log.d(TAG, "Map XML written ");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadXml(){
        InputStream inputStream = null;
        MapXmlPullParser parser = new MapXmlPullParser();

        //parse xml file
        try {
            File xmlFile = new File(mXMLPath);

            if (xmlFile.exists()) {
                Log.d(TAG, " Xml file exists in the SD card");
                inputStream = new FileInputStream(xmlFile);

                TrainingDataPOJO trainingData = parser.parseXml(xmlFile);
                TrainingDataManager.getInstance().setData(trainingData);

                inputStream.close();

                Log.d(TAG, " Xml file parsed");
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
