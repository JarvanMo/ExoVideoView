package com.jarvanmo.exoplayerview.gesture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView;
import com.jarvanmo.exoplayerview.util.Permissions;

import static android.content.Context.AUDIO_SERVICE;
import static com.jarvanmo.exoplayerview.gesture.OnVideoGestureChangeListener.VOLUME_CHANGED_INCREMENT;
import static com.jarvanmo.exoplayerview.gesture.OnVideoGestureChangeListener.VOLUME_CHANGED_MUTE;
import static com.jarvanmo.exoplayerview.gesture.OnVideoGestureChangeListener.VOLUME_CHANGED_REDUCTION;

/**
 * Created by mo on 18-2-2.
 * 剑气纵横三万里 一剑光寒十九洲
 */

public class VideoGesture implements View.OnTouchListener {

    private final Context context;
    private final OnVideoGestureChangeListener onVideoGestureChangeListener;
    private final ExoVideoPlaybackControlView.PlayerAccessor playerAccessor;

    private final Timeline.Window window;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;

    //touch
    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange;
    private float mInitTouchY;
    private float touchX = -1f;
    private float touchY = -1f;


    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private float mVol;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;

    private boolean enabled = true;

    public VideoGesture(Context context, OnVideoGestureChangeListener onVideoGestureChangeListener, @NonNull ExoVideoPlaybackControlView.PlayerAccessor playerAccessor) {
        this.context = context;
        this.onVideoGestureChangeListener = onVideoGestureChangeListener;
        mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        this.playerAccessor = playerAccessor;
        window = new Timeline.Window();

        initVol();
    }


    private void initVol() {

            /* Services and miscellaneous */
        mAudioManager = (AudioManager) context.getApplicationContext().getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return dispatchCenterWrapperTouchEvent(event);

    }

    private boolean dispatchCenterWrapperTouchEvent(MotionEvent event) {


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics screen = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(screen);

        if (mSurfaceYDisplayRange == 0) {
            mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
        }

        float x_changed, y_changed;
        if (touchX != -1f && touchY != -1f) {
            y_changed = event.getRawY() - touchY;
            x_changed = event.getRawX() - touchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }

//        Log.e("tag","x_c=" + x_changed + "screen_x =" + screen.xdpi +" screen_rawx" + event.getRawX());
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = (((event.getRawX() - touchX) / screen.xdpi) * 2.54f);//2.54f


        float delta_y = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / screen.xdpi + 0.5f) * 2f);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mTouchAction = TOUCH_NONE;
                touchX = event.getRawX();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchY = mInitTouchY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                if (mTouchAction != TOUCH_SEEK && coef > 2) {
                    if (Math.abs(y_changed / mSurfaceYDisplayRange) < 0.05) {
                        return false;
                    }

                    touchX = event.getRawX();
                    touchY = event.getRawY();


                    if ((int) touchX > (4 * screen.widthPixels / 7)) {
                        doVolumeTouch(y_changed);

                    }
                    // Brightness (Up or Down - Left side)
                    if ((int) touchX < (3 * screen.widthPixels / 7)) {
                        doBrightnessTouch(y_changed);
                    }

                } else {
                    doSeekTouch(Math.round(delta_y), xgesturesize, false);
                }

                break;

            case MotionEvent.ACTION_UP:
                if (mTouchAction == TOUCH_SEEK) {
                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
                }

                if (mTouchAction != TOUCH_NONE) {
                    hideCenterInfo();
                }

                touchX = -1f;
                touchY = -1f;
                break;
            default:
                break;
        }


        return mTouchAction != TOUCH_NONE;
    }

    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME) {
            return;
        }

        int oldVol = (int) mVol;
        mTouchAction = TOUCH_VOLUME;
        float delta = -((y_changed / mSurfaceYDisplayRange) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol, vol > oldVol);
        }
    }

    private void setAudioVolume(int vol, boolean isUp) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);
        }

        mTouchAction = TOUCH_VOLUME;
        vol = vol * 100 / mAudioMax;
        int type;
        if (newVol == 0) {
            type = VOLUME_CHANGED_MUTE;
        } else if (isUp) {
            type = VOLUME_CHANGED_INCREMENT;
        } else {
            type = VOLUME_CHANGED_REDUCTION;
        }

        onVolumeChanged(vol, type);
    }

    private void onVolumeChanged(int range, @OnVideoGestureChangeListener.VolumeChangeType int type) {
        if (onVideoGestureChangeListener != null) {
            onVideoGestureChangeListener.onVolumeChanged(range, type);
        }
    }


    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS) {
            return;
        }

        mTouchAction = TOUCH_BRIGHTNESS;
        if (mIsFirstBrightnessGesture) {
            initBrightnessTouch();
        }

        mTouchAction = TOUCH_BRIGHTNESS;
//
        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -y_changed / mSurfaceYDisplayRange;
        changeBrightness(delta);
    }

    private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        if (!(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;


        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);

        lp.screenBrightness = brightness;
        // Set Brightness
        activity.getWindow().setAttributes(lp);

        brightness = Math.round(brightness * 100);

        onBrightnessChanged((int) brightness);

    }

    private void onBrightnessChanged(int brightnessPercent) {
        if (onVideoGestureChangeListener != null) {
            onVideoGestureChangeListener.onBrightnessChanged(brightnessPercent);
        }
    }

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        if (coef == 0) {
            coef = 1;
        }


        // No seek action if coef > 0.5 and gesturesize < 1cm

        if (Math.abs(gesturesize) < 1 || !canSeek()) {
            return;
        }


        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK) {
            return;
        }


        mTouchAction = TOUCH_SEEK;
        Player player = playerAccessor.attachPlayer();
        long length = player.getDuration();
        long time = player.getCurrentPosition();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length)) {
            jump = (int) (length - time);
        }


        if ((jump < 0) && ((time + jump) < 0)) {
            jump = (int) -time;
        }

        //Jump !
//        if (seek && length > 0) {
//            jump(time + jump);
//        }

        if (length > 0) {
            //Show the jump's size
            seekAndShowJump(seek, time + jump, jump > 0);
        }
    }

    private void seekAndShowJump(boolean seek, long jumpSize, boolean isFastForward) {
        if (onVideoGestureChangeListener != null) {
            onVideoGestureChangeListener.onShowSeekSize(jumpSize, isFastForward);
        }
    }

    private void hideCenterInfo() {
        if (onVideoGestureChangeListener != null) {
            onVideoGestureChangeListener.onNoGesture();
        }
    }


    private void initBrightnessTouch() {
        if (!(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;

        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
        // Initialize the layoutParams screen brightness
        try {
            if (Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                if (!Permissions.canWriteSettings(activity)) {
                    return;
                }
                Settings.System.putInt(activity.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                restoreAutoBrightness = android.provider.Settings.System.getInt(activity.getContentResolver(),
//                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else if (brightnesstemp == 0.6f) {
                brightnesstemp = android.provider.Settings.System.getInt(activity.getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        lp.screenBrightness = brightnesstemp;
        activity.getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

    private boolean canSeek() {

        Player player = playerAccessor.attachPlayer();

        Timeline timeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        boolean isSeekable = false;
        if (haveNonEmptyTimeline) {
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
            isSeekable = window.isSeekable;
        }

        return isSeekable;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }
}
