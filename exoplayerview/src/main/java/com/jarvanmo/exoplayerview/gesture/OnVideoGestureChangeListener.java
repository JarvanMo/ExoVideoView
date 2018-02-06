package com.jarvanmo.exoplayerview.gesture;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mo on 18-2-2.
 * 剑气纵横三万里 一剑光寒十九洲
 */

public interface OnVideoGestureChangeListener {

    int VOLUME_CHANGED_REDUCTION = -1;
    int VOLUME_CHANGED_MUTE =VOLUME_CHANGED_REDUCTION+1;
    int VOLUME_CHANGED_INCREMENT =VOLUME_CHANGED_MUTE+1;


    @IntDef({VOLUME_CHANGED_REDUCTION, VOLUME_CHANGED_MUTE, VOLUME_CHANGED_INCREMENT})
    @Retention(RetentionPolicy.SOURCE)
    @interface VolumeChangeType {

    }


    void onVolumeChanged(int range,@VolumeChangeType int type);

    void onBrightnessChanged(int brightnessPercent);

    void onNoGesture();

    void onShowSeekSize(long seekSize, boolean fastForward);

}
