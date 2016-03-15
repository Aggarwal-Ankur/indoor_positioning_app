package com.aggarwalankur.indoor_positioning.core.trainingdata;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by Ankur on 14-Mar-16.
 */
public class TrainingDataPOJO {

    public String mapPath = "";
    public int mapHeight = -1;
    public int mapWidth = -1;
    public int mapBearing = -1;
    public int strideLength = -1;
    public float stepLength = -1;


    public ArrayList<AnchorPOJO> anchorList = new ArrayList<>();

    public void resetAllData(){
         mapPath = "";
          mapHeight = 1;
          mapWidth = 1;
          mapBearing = 1;
          strideLength = 1;
          stepLength = 1;


         anchorList = new ArrayList<>();
    }

    public void addAnchor(PointF point, String id, int type){
        AnchorPOJO anchor = new AnchorPOJO();

        anchor.x = point.x;
        anchor.y = point.y;
        anchor.id = id;
        anchor.type = type;

        boolean alreadyExists = false;

        //No duplicates
        for( AnchorPOJO currentAnchor : anchorList){
            if(currentAnchor.id.equalsIgnoreCase(anchor.id)
                    && currentAnchor.type == anchor.type){
                alreadyExists = true;
                currentAnchor.x = point.x;
                currentAnchor.y = point.y;
                break;
            }
        }

        if(!alreadyExists) {
            anchorList.add(anchor);
        }
    }
}
