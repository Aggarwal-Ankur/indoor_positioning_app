package com.aggarwalankur.indoor_positioning.customviews;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.aggarwalankur.indoor_positioning.R;
import com.aggarwalankur.indoor_positioning.common.IConstants;
import com.aggarwalankur.indoor_positioning.core.trainingdata.AnchorPOJO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by Ankur on 12-Mar-16.
 */
public class Panel extends View{

    /** Background Image and parameters */
    public Bitmap bgImage = null;
    public int indoorMapWidth = 800, indoorMapHeight = 400;

    public int actualMapHeight = 800, actualMapWidth = 400;

    public int mapHeightMetres = 100, mapWidthMetres = 100;

    private static final String TAG = "Panel";

    //TempImgPath is used to allow async loading
    private String tempImgPath = "/sdcard/";
    private int mode;

    private Paint paint = new Paint();
    private static Matrix matrix;
    private Handler mHandler;
    private Context mContext;

    /** crosshair image and parameters */
    private Bitmap crosshair = BitmapFactory.decodeResource(this.getResources(), R.drawable.crosshair);
    private float crosshairWidth = crosshair.getWidth();
    private float crosshairHeight = crosshair.getHeight();

    private Bitmap nAnchor = BitmapFactory.decodeResource(this.getResources(), R.drawable.n_anchor);
    private Bitmap wAnchor = BitmapFactory.decodeResource(this.getResources(), R.drawable.w_anchor);
    private Bitmap bAnchor = BitmapFactory.decodeResource(this.getResources(), R.drawable.b_anchor);


    private ArrayList<AnchorPOJO> anchorList = new ArrayList<>();

    private ArrayList<PointF> wifiDataLocations = new ArrayList<>();
    private ArrayList<PointF> iAmHereLocations = new ArrayList<>();

    private PointF crosshairCoords = new PointF();

    private PointF currentLocation;

    /** Floats used in the transformation matrix */
    private float[] values = new float[9];

    /** backup image */
    private Bitmap backupMap = null;
    public DisplayMetrics metrics;

    private static  int Y_OFFSET = 0;

    public Panel(Context context) {
        super(context);
        mContext = context;
        mHandler = new Handler();
        this.setOnTouchListener(new TouchHandler(this));
    }

    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHandler = new Handler();
        this.setOnTouchListener(new TouchHandler(this));
    }

    /**
     * Schedule a view content repaint.
     */
    public void repaint() {
        mHandler.post(new Runnable() {
            public void run() {
                System.gc();
                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        myDraw(canvas);
    }

    private void myDraw(Canvas canvas){
        canvas.save();
        if(matrix == null){
            matrix = new Matrix();
        }
        canvas.setMatrix(matrix);
        canvas.drawColor(Color.TRANSPARENT);

        // Do not draw any bitmap if map is null
        if (bgImage != null){
            canvas.drawBitmap(bgImage, 0, 0, paint);
        }
        else
        {
            if(backupMap == null)
            {
                backupMap = BitmapFactory.decodeResource(getResources(),R.drawable.file);
            }
            canvas.drawBitmap(backupMap, 0, 0, paint);
            indoorMapWidth = backupMap.getWidth();
            indoorMapHeight = backupMap.getHeight();

        }

        //Draw the anchors. These have to be always drawn
        AnchorListLoop : for(AnchorPOJO currentAnchor : anchorList){
            Bitmap anchorBitmap;
            switch (currentAnchor.type){
                case IConstants.ANCHOR_TYPE.NFC:
                    anchorBitmap = nAnchor;
                    break;
                case IConstants.ANCHOR_TYPE.WIFI:
                    anchorBitmap = wAnchor;
                    break;
                case IConstants.ANCHOR_TYPE.BLE:
                    anchorBitmap = bAnchor;
                    break;
                default :
                    continue AnchorListLoop;
            }

            float anchorX = (currentAnchor.x/mapWidthMetres) * indoorMapWidth;
            float anchorY = (currentAnchor.y/mapHeightMetres) * indoorMapHeight;

            canvas.drawBitmap(anchorBitmap, anchorX - anchorBitmap.getWidth()/ 2,
                    indoorMapHeight - anchorY - anchorBitmap.getHeight()/ 2, null);

        }

        if(mode == IConstants.MAP_ACTIVITY_MODES.MODE_TRAIN_WIFI){
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#FFFF4444"));
            paint.setStrokeWidth(4);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            for(PointF currentPoint : wifiDataLocations){
                float locX = (currentPoint.x/mapWidthMetres) * indoorMapWidth;
                float locY = (currentPoint.y/mapHeightMetres) * indoorMapHeight;
                locY = indoorMapHeight - locY;

                canvas.drawCircle(locX, locY, 4, paint);
            }
        }else if(mode == IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING){
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#FF0099CC"));
            paint.setStrokeWidth(4);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            for(PointF currentPoint : iAmHereLocations){
                float locX = (currentPoint.x/mapWidthMetres) * indoorMapWidth;
                float locY = (currentPoint.y/mapHeightMetres) * indoorMapHeight;
                locY = indoorMapHeight - locY;

                canvas.drawCircle(locX, locY, 4, paint);
            }
        }

        canvas.restore();
        if (mode != IConstants.MAP_ACTIVITY_MODES.INDOOR_POSITIONING){
            canvas.drawBitmap(crosshair, crosshairCoords.x - crosshairWidth/ 2, crosshairCoords.y - crosshairHeight/ 2, null);
        }


    }



    /** Utility function to allow loading a new bg map */
    public void loadNewMap(String path, int mode) {
        if(bgImage != null)
        {
            bgImage.recycle();
            bgImage = null;
        }
        System.gc();
        tempImgPath = path;
        this.mode = mode;


        this.post(new Runnable() {

            public void run() {
                try {

                    bgImage = decodeFile(tempImgPath);
                    if(bgImage!=null){
                        indoorMapWidth = bgImage.getWidth();
                        indoorMapHeight = bgImage.getHeight();
                        Panel.this.repaint();
                    }
                    else{

                        //Toast.makeText(this.getAppContext(), "Image is corrupted. Please load some other image", Toast.LENGTH_SHORT).show();
                    }
                } catch (OutOfMemoryError e) {
                    Log.e(TAG, "OutOfMemoryError");
                }
            }
        });
    }


    //decodes image and scales it to reduce memory consumption
    public Bitmap decodeFile(String f){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            actualMapHeight = o.outHeight;
            actualMapWidth = o.outWidth;
            Log.d(TAG, "actualMapHeight= "+o.outHeight +": actualMapHeight= "+o.outWidth);

            int REQUIRED_SIZE = 700;

            //Find the correct scale value. It should be the power of 2.
            int scale=1;

            while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
            {
                scale*=2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;

            return BitmapFactory.decodeFile(f, o2);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** setter for the matrix */
    public void setMatrix(Matrix matrix) {
        Panel.matrix = matrix;
    }

    /** Helper function to set crosshair on touch */
    public void setCrosshair(float x, float y) {
        matrix.getValues(values);
        metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay()
                .getMetrics(metrics);



        crosshairCoords.x = x;
        crosshairCoords.y = y;
        /*
         * values[0] : scaling factor values[2] : x-offset values[5]: y-offset
         */

        if (x < values[2]) {
            crosshairCoords.x = values[2];
        } else if (x > (values[2] + values[0] * bgImage.getWidth())) {
            crosshairCoords.x = values[2] + values[0] * bgImage.getWidth();
        }


        if (y < values[5] - Y_OFFSET) {
            crosshairCoords.y = values[5] - Y_OFFSET;
        } else if (y > (values[5] - Y_OFFSET + values[0] * bgImage.getHeight())) {
            crosshairCoords.y = values[5] - Y_OFFSET + values[0]
                    * bgImage.getHeight();
        }


        currentLocation = new PointF();
        currentLocation.x = (crosshairCoords.x - values[2]) / values[0];
        currentLocation.y = (crosshairCoords.y - values[5] + Y_OFFSET)
                / values[0];

        currentLocation.y = indoorMapHeight - currentLocation.y;

        currentLocation.x = (currentLocation.x /indoorMapWidth) * mapWidthMetres;
        currentLocation.y = (currentLocation.y /indoorMapHeight) * mapHeightMetres;

    }

    public PointF getCurrentLoc(){
        return currentLocation;
    }

    public void setYOffSet(int offset){
        Y_OFFSET = offset;
        matrix = new Matrix();
        matrix.postTranslate(0, Y_OFFSET);
    }

    public void setAnchorList(ArrayList<AnchorPOJO> anchorList) {
        this.anchorList = anchorList;
    }

    public void setWifiDataPointList(ArrayList<PointF> dataLocationPoints){
        if(dataLocationPoints == null){
            this.wifiDataLocations = new ArrayList<>();
        }else {
            this.wifiDataLocations = dataLocationPoints;
        }
    }


    public void addIamHereDataPoint(PointF iAmHerePoint){
        if(iAmHereLocations == null){
            this.iAmHereLocations = new ArrayList<>();
        }

        iAmHereLocations.add(iAmHerePoint);
    }

    public void resetIamHereLocations(){
        iAmHereLocations.clear();
    }

}
