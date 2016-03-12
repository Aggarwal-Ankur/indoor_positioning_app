package com.aggarwalankur.indoor_positioning.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aggarwalankur.indoor_positioning.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Ankur on 12-Mar-16.
 */
public class Panel extends View{

    /** Background Image and parameters */
    public Bitmap bgImage = null;
    public int indoorMapWidth = 800, indoorMapHeight = 400;

    public int actualMapHeight = 800, actualMapWidth = 400;

    private static final String TAG = "Panel";

    //TempImgPath is used to allow async loading
    private String tempImgPath = "/sdcard/";

    private Paint paint = new Paint();
    private Matrix matrix;

    /** backup image */
    private Bitmap backupMap = null;

    /** crosshair image and parameters */
    private Bitmap crosshair = BitmapFactory.decodeResource(
            this.getResources(), R.drawable.crosshair);

    public Panel(Context context) {
        super(context);
    }

    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Panel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        canvas.drawColor(Color.WHITE);

        // Do not draw any bitmap if map is null
        if (bgImage != null){
            canvas.drawBitmap(bgImage, 0, 0, paint);
        }
        else
        {
            if(backupMap == null)
            {
                backupMap = BitmapFactory.decodeResource(getResources(),R.drawable.indoor_backup_img);
            }
            canvas.drawBitmap(backupMap, 0, 0, paint);
            indoorMapWidth = backupMap.getWidth();
            indoorMapHeight = backupMap.getHeight();

        }
    }

    /** Utility function to allow loading a new bg map */
    public void loadNewMap(String path) {
        if(bgImage != null)
        {
            bgImage.recycle();
            bgImage = null;
        }
        System.gc();
        tempImgPath = path;


        this.post(new Runnable() {

            public void run() {
                try {

                    bgImage = decodeFile(tempImgPath);
                    if(bgImage!=null){
                        indoorMapWidth = bgImage.getWidth();
                        indoorMapHeight = bgImage.getHeight();
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
}
