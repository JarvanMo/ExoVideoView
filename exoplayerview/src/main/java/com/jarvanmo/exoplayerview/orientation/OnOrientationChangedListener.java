package com.jarvanmo.exoplayerview.orientation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mo on 18-2-5.
 * 剑气纵横三万里 一剑光寒十九洲
 */

public interface OnOrientationChangedListener {
    int SENSOR_UNKNOWN = -1;
    int SENSOR_PORTRAIT = SENSOR_UNKNOWN + 1;
    int SENSOR_LANDSCAPE = SENSOR_PORTRAIT + 1;

    @IntDef({SENSOR_UNKNOWN, SENSOR_PORTRAIT, SENSOR_LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    @interface SensorOrientationType {

    }


    void onChanged(@SensorOrientationType int orientation);
}
