package com.aggarwalankur.indoor_positioning.core.listeners;

import android.graphics.PointF;

/**
 * Created by Ankur on 01/03/2016.
 */
public interface PositionListener {
    void onPositionChanged(PointF point);
}
