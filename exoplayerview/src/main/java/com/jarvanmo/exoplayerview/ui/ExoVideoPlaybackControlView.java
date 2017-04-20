package com.jarvanmo.exoplayerview.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.jarvanmo.exoplayerview.R;
import com.jarvanmo.exoplayerview.util.Permissions;
import com.jarvanmo.exoplayerview.widget.BatteryLevelView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by mo on 16-11-7.
 *
 * @author mo
 */

public class ExoVideoPlaybackControlView extends FrameLayout {

    /**
     * Listener to be notified about changes of the visibility of the UI control.
     */
    public interface VisibilityListener {
        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either {@link View#VISIBLE} or {@link View#GONE}.
         */
        void onVisibilityChange(int visibility);
    }


    public interface ExoClickListener {

        /***
         * called when buttons clicked in controller
         * @param view The view clicked
         * @param isPortrait the controller is portrait  or not
         * @return will interrupt operation in controller if return true
         * **/
        boolean onClick(View view, boolean isPortrait);

    }


    public interface OrientationListener {
        void onOrientationChange(@SensorOrientationType int orientation);
    }

    public static final int SENSOR_UNKNOWN = -1;
    public static final int SENSOR_PORTRAIT = SENSOR_UNKNOWN + 1;
    public static final int SENSOR_LANDSCAPE = SENSOR_PORTRAIT + 1;

    @IntDef({SENSOR_UNKNOWN, SENSOR_PORTRAIT, SENSOR_LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SensorOrientationType {

    }


    public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    public static final int DEFAULT_REWIND_MS = 5000;
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;

    private static final int PROGRESS_BAR_MAX = 1000;
    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;


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


    private final Timeline.Window currentWindow;
    private final ComponentListener componentListener;



    private final StringBuilder formatBuilder;
    private final Formatter formatter;


    private TextView displayName;
    private TextView localTime;
    private BatteryLevelView battery;
    private FrameLayout centerContentWrapper;
    private ProgressBar loadingProgressBar;
    private TextView centerInfo;
    private LinearLayout controllerWrapper;
    private ImageButton play;
    private TextView currentTime;
    private SeekBar videoProgress;
    private TextView endTime;
    private ImageButton fullScreen;
    private LinearLayout controllerWrapperLandscape;
    private SeekBar videoProgressLandscape;
    private ImageButton playLandscape;
    private ImageButton nextLandscape;
    private TextView timeLandscape;
    private FrameLayout customView;
    private ImageButton fullScreenLandscape;


    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private final Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    private int oldScreenOrientation = -1;
    private OrientationEventListener screenOrientationEventListener;
    private OrientationListener orientationListener;

    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_TICK)) {
                updateTime();
            }
        }
    };



    private boolean isAttachedToWindow;
    private boolean dragging;
    private int rewindMs;
    private int fastForwardMs;
    private int showTimeoutMs;
    private long hideAtMs;
    private boolean portrait = true;


    private long lastPlayerPosition = -1;


    private ExoPlayer player;
    private VisibilityListener visibilityListener;


    private ExoClickListener fullScreenListener;
    private ExoClickListener backListener;


    public ExoVideoPlaybackControlView(Context context) {
        this(context, null);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        rewindMs = DEFAULT_REWIND_MS;
        fastForwardMs = DEFAULT_FAST_FORWARD_MS;
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;


        if (attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExoVideoPlaybackControlView, 0, 0);

            try {
                rewindMs = typedArray.getInt(R.styleable.ExoVideoPlaybackControlView_rewindIncrement, rewindMs);
                fastForwardMs = typedArray.getInt(R.styleable.ExoVideoPlaybackControlView_fastForwardIncrement, fastForwardMs);
                showTimeoutMs = typedArray.getInt(R.styleable.ExoVideoPlaybackControlView_showTimeout, showTimeoutMs);
            } finally {
                typedArray.recycle();
            }
        }

        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        currentWindow = new Timeline.Window();
        componentListener = new ComponentListener();

        LayoutInflater.from(context).inflate(R.layout.exo_playback_control_view, this);

        findViews();
        initViews();
        initOrientationListener();
        showPortraitOrLandscape();
        updateTime();
    }


    private void findViews() {
        displayName = (TextView) findViewById(R.id.displayName);
        localTime =  (TextView) findViewById(R.id.localTime);
        battery = (BatteryLevelView) findViewById(R.id.battery);
        centerContentWrapper = (FrameLayout) findViewById(R.id.centerContentWrapper);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        centerInfo = (TextView) findViewById(R.id.centerInfo);
        controllerWrapper = (LinearLayout) findViewById(R.id.controllerWrapper);
        play = (ImageButton) findViewById(R.id.play);
        currentTime = (TextView) findViewById(R.id.currentTime);
        videoProgress = (SeekBar) findViewById(R.id.videoProgress);
        endTime = (TextView) findViewById(R.id.endTime);
        fullScreen = (ImageButton) findViewById(R.id.fullScreen);
        controllerWrapperLandscape = (LinearLayout) findViewById(R.id.controllerWrapperLandscape);
        videoProgressLandscape = (SeekBar) findViewById(R.id.videoProgressLandscape);
        playLandscape = (ImageButton) findViewById(R.id.playLandscape);
        nextLandscape = (ImageButton) findViewById(R.id.nextLandscape);
        timeLandscape = (TextView) findViewById(R.id.timeLandscape);
        customView = (FrameLayout) findViewById(R.id.customView);
        fullScreenLandscape = (ImageButton) findViewById(R.id.fullScreenLandscape);
    }

    public void setTopWrapperTextSize(float textSize) {
        if (textSize != Float.MIN_VALUE) {
            displayName.setTextSize(textSize);
            localTime.setTextSize(textSize);
        }
    }


    private void initViews() {
        displayName.setOnClickListener(componentListener);
        centerContentWrapper.setOnClickListener(componentListener);
        centerContentWrapper.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return dispatchCenterWrapperTouchEvent(event);
            }
        });

        //portrait
        videoProgress.setMax(PROGRESS_BAR_MAX);
        videoProgress.setOnSeekBarChangeListener(componentListener);
        play.setOnClickListener(componentListener);
        fullScreen.setOnClickListener(componentListener);

        //landscape
        videoProgressLandscape.setMax(PROGRESS_BAR_MAX);
        videoProgressLandscape.setOnSeekBarChangeListener(componentListener);
        playLandscape.setOnClickListener(componentListener);
        fullScreenLandscape.setOnClickListener(componentListener);
        nextLandscape.setOnClickListener(componentListener);
    }


    private void initOrientationListener() {
        screenOrientationEventListener = new OrientationEventListener(getContext()) {
            @Override
            public void onOrientationChanged(int orientation) {

                if(orientationListener == null || !isScreenOpenRotate()){
                    return;
                }


                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    changeOrientation(SENSOR_UNKNOWN);
                    return;  //手机平放时，检测不到有效的角度
                }
//只检测是否有四个角度的改变
                if (orientation > 350 || orientation < 10) { //0度
                    orientation = 0;
                } else if (orientation > 80 && orientation < 100) { //90度
                    orientation = 90;
                } else if (orientation > 170 && orientation < 190) { //180度
                    orientation = 180;
                } else if (orientation > 260 && orientation < 280) { //270度
                    orientation = 270;
                } else {
                    return;
                }

                if (oldScreenOrientation == orientation) {
                    changeOrientation(SENSOR_UNKNOWN);
                    return;
                }


                oldScreenOrientation = orientation;

                if (orientation == 0 || orientation == 180) {
                    changeOrientation(SENSOR_PORTRAIT);
                } else {
                    changeOrientation(SENSOR_LANDSCAPE);
                }
            }
        };

        screenOrientationEventListener.enable();
    }

    private void initVol() {

            /* Services and miscellaneous */
        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
        }
        this.player = player;
        if (player != null) {
            player.addListener(componentListener);
        }
        updateAll();
    }


    public void setFullScreenListener(ExoClickListener fullScreenListener) {
        this.fullScreenListener = fullScreenListener;
    }

    public void setPortraitBackListener(ExoClickListener backListener) {
        this.backListener = backListener;
    }


    public void setOrientationListener(OrientationListener orientationListener){
        this.orientationListener = orientationListener;
    }

    public void setDisplayName(String displayName) {
        this.displayName.setText(displayName);
    }

    public boolean isPortrait() {
        return portrait;
    }

    public void addViewToControllerWhenLandscape(View view) {
        customView.addView(view);
    }

    /**
     * Sets the {@link VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setVisibilityListener(VisibilityListener listener) {
        this.visibilityListener = listener;
    }

    public void setPortrait(boolean portrait) {
        this.portrait = portrait;
        showPortraitOrLandscape();
    }

    public void toggleControllerOrientation() {
        if (orientationListener == null) {
            setPortrait(!portrait);
        }else {
            changeOrientation(portrait ? SENSOR_LANDSCAPE:SENSOR_PORTRAIT);
        }

    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds.
     */
    public void setRewindIncrementMs(int rewindMs) {
        this.rewindMs = rewindMs;
        updateNavigation();
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        this.fastForwardMs = fastForwardMs;
        updateNavigation();
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input.
     *
     * @return The duration in milliseconds. A non-positive value indicates that the controls will
     * remain visible indefinitely.
     */
    public int getShowTimeoutMs() {
        return showTimeoutMs;
    }


    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input.
     *
     * @param showTimeoutMs The duration in milliseconds. A non-positive value will cause the controls
     *                      to remain visible indefinitely.
     */
    public void setShowTimeoutMs(int showTimeoutMs) {
        this.showTimeoutMs = showTimeoutMs;
    }

    /**
     * Shows the playback controls. If {@link #getShowTimeoutMs()} is positive then the controls will
     * be automatically hidden after this duration of time has elapsed without user input.
     */
    public void show() {
        if (!isVisible()) {
            setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }

            updateAll();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }


    public void showUtilHideCalled() {
        if (!isVisible()) {
            setVisibility(VISIBLE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            updateAll();
        }
    }


    public void hide() {
        if (isVisible()) {
            setVisibility(GONE);
            if (visibilityListener != null) {
                visibilityListener.onVisibilityChange(getVisibility());
            }
            removeCallbacks(updateProgressAction);
            removeCallbacks(hideAction);
            hideAtMs = C.TIME_UNSET;
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    private void hideAfterTimeout() {
        removeCallbacks(hideAction);
        if (showTimeoutMs > 0) {
            hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
            if (isAttachedToWindow) {
                postDelayed(hideAction, showTimeoutMs);
            }
        } else {
            hideAtMs = C.TIME_UNSET;
        }
    }


    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        boolean playing = player != null && player.getPlayWhenReady();
        String contentDescription = getResources().getString(
                playing ? R.string.exo_controls_pause_description : R.string.exo_controls_play_description);

        ImageButton playButton;
        if (portrait) {
            playButton = play;
        } else {
            playButton = playLandscape;
        }
        playButton.setContentDescription(contentDescription);
        playButton.setImageResource(playing ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);

    }


    private void updateNavigation() {

        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        Timeline currentTimeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
        boolean isSeekable = false;
        boolean enableNext = false;
        if (haveNonEmptyTimeline) {
            int currentWindowIndex = player.getCurrentWindowIndex();
            currentTimeline.getWindow(currentWindowIndex, currentWindow);
            isSeekable = currentWindow.isSeekable;
//            enablePrevious = currentWindowIndex > 0 || isSeekable || !currentWindow.isDynamic;
            enableNext = (currentWindowIndex < currentTimeline.getWindowCount() - 1)
                    || currentWindow.isDynamic;
        }
        if (videoProgress != null) {
            videoProgress.setEnabled(isSeekable);
        }
        if (videoProgressLandscape != null) {
            videoProgressLandscape.setEnabled(isSeekable);
        }


        setButtonEnabled(enableNext, nextLandscape);

    }


    private boolean canSeek() {

        Timeline currentTimeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
        boolean isSeekable = false;
        if (haveNonEmptyTimeline) {
            int currentWindowIndex = player.getCurrentWindowIndex();
            currentTimeline.getWindow(currentWindowIndex, currentWindow);
            isSeekable = currentWindow.isSeekable;
//            enablePrevious = currentWindowIndex > 0 || isSeekable || !currentWindow.isDynamic;
        }

        return isSeekable;
    }

    private void updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }


        long duration = player == null ? 0 : player.getDuration();
        long position = player == null ? 0 : player.getCurrentPosition();
        lastPlayerPosition = position;
        endTime.setText(stringForTime(duration));


        if (!dragging) {
            currentTime.setText(stringForTime(position));
            timeLandscape.setText(stringForTime(position) + "/" + stringForTime(duration));

            if (canSeek()) {
                videoProgress.setProgress(progressBarValue(position));
                videoProgressLandscape.setProgress(progressBarValue(position));
            } else {
                videoProgress.setProgress(PROGRESS_BAR_MAX);
                videoProgressLandscape.setProgress(PROGRESS_BAR_MAX);
            }


        }


        long bufferedPosition = player == null ? 0 : player.getBufferedPosition();
        videoProgress.setSecondaryProgress(progressBarValue(bufferedPosition));
        videoProgressLandscape.setSecondaryProgress(progressBarValue(bufferedPosition));
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction);

        // Schedule an update if necessary.
        int playbackState = player == null ? ExoPlayer.STATE_IDLE : player.getPlaybackState();
        if (playbackState != ExoPlayer.STATE_IDLE && playbackState != ExoPlayer.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == ExoPlayer.STATE_READY) {
                delayMs = 1000 - (position % 1000);
                if (delayMs < 200) {
                    delayMs += 1000;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }

    }


    private void updateTime() {
        final Calendar calendar = Calendar.getInstance();

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
//        int amOrPm = calendar.get(Calendar.AM_PM);
        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());

//        Resources res = getResources();
        String timeResult = "";
//        hourOfDay = is24HourFormat ? hourOfDay : (hourOfDay > 12 ? hourOfDay - 12: hourOfDay);
        if (hourOfDay >= 10) {
            timeResult += Integer.toString(hourOfDay);
        } else {
            timeResult += "0" + hourOfDay;
        }

        timeResult += ":";

        if (minute >= 10) {
            timeResult += Integer.toString(minute);
        } else {
            timeResult += "0" + minute;
        }


//
//        if (!is24HourFormat) {
//            String str = amOrPm == Calendar.AM ? res.getString(R.string.time_am) : res.getString(R.string.time_pm);
//            timeResult = timeResult + " " + str;
//        }
        localTime.setText(timeResult);
    }

    private void setButtonEnabled(boolean enabled, View view) {
        view.setEnabled(enabled);
        view.setVisibility(enabled ? VISIBLE : INVISIBLE);
    }

    private boolean dispatchCenterWrapperTouchEvent(MotionEvent event) {


        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

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
//                        hideCenterInfo();
//                            hideOverlay(true);
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

    private void setAudioVolume(int vol, boolean up) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);
        }

        mTouchAction = TOUCH_VOLUME;
        vol = vol * 100 / mAudioMax;
        int drawableId;
        if (newVol == 0) {
            drawableId = R.drawable.ic_volume_mute_white_36dp;
        } else if (up) {
            drawableId = R.drawable.ic_volume_up_white_36dp;
        } else {
            drawableId = R.drawable.ic_volume_down_white_36dp;
        }
        setVolumeOrBrightnessInfo(getContext().getString(R.string.volume_changing, vol), drawableId);
//        showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
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
        if (!(getContext() instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) getContext();


        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);

        int brightnessInt = (int) brightness;

        setVolumeOrBrightnessInfo(getContext().getString(R.string.brightness_changing, brightnessInt), whichBrightnessImageToUse(brightnessInt));
    }

    @DrawableRes
    private int whichBrightnessImageToUse(int brightnessInt) {
        if (brightnessInt <= 15) {
            return R.drawable.ic_brightness_1_white_36dp;
        } else if (brightnessInt <= 30 && brightnessInt > 15) {
            return R.drawable.ic_brightness_2_white_36dp;
        } else if (brightnessInt <= 45 && brightnessInt > 30) {
            return R.drawable.ic_brightness_3_white_36dp;
        } else if (brightnessInt <= 60 && brightnessInt > 45) {
            return R.drawable.ic_brightness_4_white_36dp;
        } else if (brightnessInt <= 75 && brightnessInt > 60) {
            return R.drawable.ic_brightness_5_white_36dp;
        } else if (brightnessInt <= 90 && brightnessInt > 75) {
            return R.drawable.ic_brightness_6_white_36dp;
        } else {
            return R.drawable.ic_brightness_7_white_36dp;
        }

    }

    private void setWindowBrightness(float brightness) {
        if (!(getContext() instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) getContext();


        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        activity.getWindow().setAttributes(lp);
    }


    private void initBrightnessTouch() {

        if (!(getContext() instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) getContext();

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
        if (seek && length > 0) {
            seek(time + jump);
        }

        if (length > 0) {
            //Show the jump's size
            setFastForwardOrRewind(time + jump, jump > 0 ? R.drawable.ic_fast_forward_white_36dp : R.drawable.ic_fast_rewind_white_36dp);
        }
    }


    private synchronized void changeOrientation(@SensorOrientationType int orientation){
        if (orientationListener == null) {
            return;
        }

        orientationListener.onOrientationChange(orientation);
        Context context = getContext();
        Activity activity ;
        if(! (context instanceof Activity)){
            return;
        }
        activity = (Activity) context;
        switch (orientation) {
            case SENSOR_PORTRAIT:
                setPortrait(true);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                break;
            case SENSOR_LANDSCAPE:
                setPortrait(false);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case SENSOR_UNKNOWN:
            default:
                break;
        }

    }



    private void seek(long position) {
        if (player != null) {
            player.seekTo(position);
        }

    }

    private void showPortraitOrLandscape() {
        if (portrait) {
            controllerWrapper.setVisibility(View.VISIBLE);
            controllerWrapperLandscape.setVisibility(View.GONE);
            battery.setVisibility(GONE);
            localTime.setVisibility(GONE);
        } else {
            controllerWrapper.setVisibility(View.GONE);
            controllerWrapperLandscape.setVisibility(View.VISIBLE);
            battery.setVisibility(VISIBLE);
            localTime.setVisibility(VISIBLE);
        }

    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        } else {
            loadingProgressBar.setVisibility(GONE);
        }
    }

    private void setFastForwardOrRewind(long changingTime, @DrawableRes int drawableId) {
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(generateFastForwardOrRewindTxt(changingTime));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }

    private void setVolumeOrBrightnessInfo(String txt, @DrawableRes int drawableId) {
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(txt);
        centerInfo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }


    private void hideCenterInfo() {
        centerInfo.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        centerInfo.setVisibility(GONE);

    }


    @TargetApi(11)
    private void setViewAlphaV11(View view, float alpha) {
        view.setAlpha(alpha);
    }

    private int progressBarValue(long position) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET || duration == 0 ? 0 : (int) ((position * PROGRESS_BAR_MAX) / duration);
    }


    private long positionValue(int progress) {
        long duration = player == null ? C.TIME_UNSET : player.getDuration();
        return duration == C.TIME_UNSET ? 0 : ((duration * progress) / PROGRESS_BAR_MAX);
    }

    private String stringForTime(long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }


    private CharSequence generateFastForwardOrRewindTxt(long changingTime) {

        long duration = player == null ? 0 : player.getDuration();
        String result = stringForTime(changingTime) + " / " + stringForTime(duration);

        int index = result.indexOf("/");

        SpannableString spannableString = new SpannableString(result);


        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        spannableString.setSpan(new ForegroundColorSpan(color), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private void previous() {
        Timeline currentTimeline = player.getCurrentTimeline();
        if (currentTimeline == null) {
            return;
        }
        int currentWindowIndex = player.getCurrentWindowIndex();
        currentTimeline.getWindow(currentWindowIndex, currentWindow);
        if (currentWindowIndex > 0 && (player.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || (currentWindow.isDynamic && !currentWindow.isSeekable))) {
            player.seekToDefaultPosition(currentWindowIndex - 1);
        } else {
            player.seekTo(0);
        }
    }


    private void next() {
        Timeline currentTimeline = player.getCurrentTimeline();
        if (currentTimeline == null) {
            return;
        }
        int currentWindowIndex = player.getCurrentWindowIndex();
        if (currentWindowIndex < currentTimeline.getWindowCount() - 1) {
            player.seekToDefaultPosition(currentWindowIndex + 1);
        } else if (currentTimeline.getWindow(currentWindowIndex, currentWindow, false).isDynamic) {
            player.seekToDefaultPosition();
        }
    }

    private void rewind() {
        if (rewindMs <= 0) {
            return;
        }
        player.seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
    }

    private void fastForward() {
        if (fastForwardMs <= 0) {
            return;
        }
        player.seekTo(Math.min(player.getCurrentPosition() + fastForwardMs, player.getDuration()));
    }


    private void registerBroadcast() {

        IntentFilter timeFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        getContext().registerReceiver(timeReceiver, timeFilter);

    }


    private void unregisterBroadcast() {
        getContext().unregisterReceiver(timeReceiver);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        isAttachedToWindow = true;

        registerBroadcast();

        initVol();

        if (hideAtMs != C.TIME_UNSET) {
            long delayMs = hideAtMs - SystemClock.uptimeMillis();
            if (delayMs <= 0) {
                hide();
            } else {
                postDelayed(hideAction, delayMs);
            }
        }
        updateAll();
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;

        mAudioManager = null;

        screenOrientationEventListener.disable();

        unregisterBroadcast();
        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (player == null || event.getAction() != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event);
        }

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                fastForward();
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                rewind();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                player.setPlayWhenReady(!player.getPlayWhenReady());
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                player.setPlayWhenReady(true);
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                player.setPlayWhenReady(false);
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                next();
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                previous();
                break;
            default:
                return false;
        }
        show();
        return true;
    }


    private boolean isScreenOpenRotate() {

        int gravity = 0;
        try {

            gravity = Settings.System.getInt(getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);

        } catch (Settings.SettingNotFoundException e) {

            e.printStackTrace();

        }
        return 1 == gravity;

    }

        private final class ComponentListener implements ExoPlayer.EventListener,
            SeekBar.OnSeekBarChangeListener, OnClickListener {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            removeCallbacks(hideAction);
            dragging = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
//                timeCurrent.setText(stringForTime(positionValue(progress)));
                videoProgress.setProgress(progress);
                videoProgressLandscape.setProgress(progress);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            dragging = false;
            player.seekTo(positionValue(seekBar.getProgress()));
            hideAfterTimeout();
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            updateProgress();
            if (playbackState == ExoPlayer.STATE_IDLE || playbackState == ExoPlayer.STATE_BUFFERING) {
                showUtilHideCalled();
                showLoading(true);
            } else if (playbackState == ExoPlayer.STATE_READY && player.getPlayWhenReady() || playbackState == ExoPlayer.STATE_ENDED) {
                showLoading(false);
                hide();
            }
        }

        @Override
        public void onPositionDiscontinuity() {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

//            showLoading(isLoading);

            if (isLoading && lastPlayerPosition == player.getCurrentPosition()) {
                showUtilHideCalled();
            } else if (isVisible()) {
                hide();
            }
            // Do nothing.
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Do nothing.
        }

        @Override
        public void onClick(View view) {
//            Timeline currentTimeline = player.getCurrentTimeline();

            if (nextLandscape == view) {
                next();
            } else if (play == view || playLandscape == view) {
                player.setPlayWhenReady(!player.getPlayWhenReady());
            } else if (displayName == view && backListener != null) {
                if(!backListener.onClick(view, portrait)){
                    if(!portrait){
                        changeOrientation(SENSOR_PORTRAIT);
                    }
                }

            } else if(fullScreen == view){
                changeOrientation(SENSOR_LANDSCAPE);
            }else if(fullScreenLandscape == view){
                changeOrientation(SENSOR_PORTRAIT);
            }

//            else if (previousButton == view) {
//                previous();
//            } else if (fastForwardButton == view) {
//                fastForward();
//            } else if (rewindButton == view && currentTimeline != null) {
//                rewind();
//            } else if (playButton == view) {
//                player.setPlayWhenReady(!player.getPlayWhenReady());
//            }
            hideAfterTimeout();
        }

    }


}
