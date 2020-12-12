package com.jarvanmo.exoplayerview.v3.orientation

import android.content.Context
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import android.view.OrientationEventListener
import androidx.annotation.IntDef
import com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener

class SensorOrientation(private val context: Context, private val onOrientationChangedListener: OnOrientationChangedListener) {

    private var oldScreenOrientation = OnOrientationChangedListener.SENSOR_UNKNOWN
    private val screenOrientationEventListener: OrientationEventListener


   init {
        screenOrientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                var newOrientation = orientation
                if (!isScreenOpenRotate()) {
                    return
                }
                if (orientation == ORIENTATION_UNKNOWN) {
                    onOrientationChangedListener.onChanged(OnOrientationChangedListener.SENSOR_UNKNOWN)
                    return  //手机平放时，检测不到有效的角度
                }
                //只检测是否有四个角度的改变
                newOrientation = if (orientation > 350 || orientation < 10) { //0度
                    0
                } else if (orientation in 81..99) { //90度
                    90
                } else if (orientation in 171..189) { //180度
                    180
                } else if (orientation in 261..279) { //270度
                    270
                } else {
                    return
                }
                if (oldScreenOrientation == newOrientation) {
                    onOrientationChangedListener.onChanged(OnOrientationChangedListener.SENSOR_UNKNOWN)
                    return
                }
                oldScreenOrientation = newOrientation
                if (orientation == 0 || orientation == 180) {
                    onOrientationChangedListener.onChanged(OnOrientationChangedListener.SENSOR_PORTRAIT)
                } else {
                    onOrientationChangedListener.onChanged(OnOrientationChangedListener.SENSOR_LANDSCAPE)
                }
            }
        }
    }

    private fun isScreenOpenRotate(): Boolean {
        var gravity = 0
        try {
            gravity = Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION)
        } catch (e: SettingNotFoundException) {
            Log.e(javaClass.simpleName, e.message + "")
        }
        return 1 == gravity
    }

    fun enable() {
        screenOrientationEventListener.enable()
    }

    fun disable() {
        screenOrientationEventListener.disable()
    }
}

interface OnOrientationChangedListener {
    @IntDef(SENSOR_UNKNOWN, SENSOR_PORTRAIT, SENSOR_LANDSCAPE)
    @kotlin.annotation.Retention
    annotation class SensorOrientationType

    fun onChanged(@SensorOrientationType orientation: Int)

    companion object {
        const val SENSOR_UNKNOWN = -1
        const val SENSOR_PORTRAIT = SENSOR_UNKNOWN + 1
        const val SENSOR_LANDSCAPE = SENSOR_PORTRAIT + 1
    }
}
