package com.aggarwalankur.indoor_positioning.customviews;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


public class TouchHandler implements OnTouchListener
{
	private static final String TAG = "PanelTouchHandler";

	// These matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	
	private int mode = NONE;

	private PointF start, mid;
	private float oldDist = 0f;

	/** The view to be displayed */
	private Panel mPanel;

	public TouchHandler(Panel panel) {
		mPanel = panel;
		start = new PointF();
		mid = new PointF();

	}

	public boolean onTouch(View v, MotionEvent event)
	{
		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			Log.d(TAG, "mode=DRAG");
			mode = DRAG;
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;

			mPanel.setCrosshair(event.getX(), event.getY());
			
			Log.d(TAG, "mode=NONE");
			    
			/*if(mPanel.uiHandler != null)
            {
                GPSData gpsData = mPanel.getGpsData(mPanel.currentLocation);
                
                if(gpsData != null)
                {
                	if(IndoorPreference.entryMode.equalsIgnoreCase("create"))
                	{
                    mPanel.uiHandler.sendMessage(mPanel.uiHandler
                            .obtainMessage(WayPointActivity.PANEL_TOUCH_EVENT,
                                    (int)event.getX(), (int)event.getY(), gpsData));
                	}
                	else if(IndoorPreference.entryMode.equalsIgnoreCase("Gps") )
                	{
                    mPanel.uiHandler.sendMessage(mPanel.uiHandler
                            .obtainMessage(IndoorGeoReference.PANEL_TOUCH_EVENT,
                                    (int)event.getX(), (int)event.getY()));
                	}
                }
            }*/
			
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) 
			{
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d(TAG, "mode=ZOOM");
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) 
			{
				matrix.set(savedMatrix);
				Log.i(TAG, " event.getX() - start.x" + (event.getX() - start.x));
				
				if ((Math.abs((event.getX() - start.x)) > 20)
						|| Math.abs((event.getY() - start.y)) > 20)
				{
					matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
				}
					
			} 
			else if (mode == ZOOM) 
			{
				float newDist = spacing(event);
				Log.d(TAG, "newDist=" + newDist);
				
				if (newDist > 10f) 
				{
					matrix.set(savedMatrix);
					float scaleDiff = newDist / oldDist;
					matrix.postScale(scaleDiff, scaleDiff, mid.x, mid.y);
					float[] f = new float[9];
					matrix.getValues(f);

				}
			}
			break;
			
		default:
				
			break;
		}

		// Perform the transformation
		mPanel.setMatrix(matrix);
		mPanel.repaint();

		return true; 
	}

	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	

}