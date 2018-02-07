package com.jarvanmo.exoplayerview.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.RepeatModeUtil;
import com.google.android.exoplayer2.util.Util;
import com.jarvanmo.exoplayerview.R;
import com.jarvanmo.exoplayerview.gesture.OnVideoGestureChangeListener;
import com.jarvanmo.exoplayerview.gesture.VideoGesture;
import com.jarvanmo.exoplayerview.media.ExoMediaSource;
import com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener;
import com.jarvanmo.exoplayerview.orientation.SensorOrientation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Locale;

import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_LANDSCAPE;
import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_PORTRAIT;
import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_UNKNOWN;

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
         * @param view  null when back pressed.
         * @param isPortrait the controller is portrait  or not
         * @return will interrupt operation in controller if return true
         * **/
        boolean onClick(@Nullable View view, boolean isPortrait);

    }


    public interface OrientationListener {
        void onOrientationChange(@OnOrientationChangedListener.SensorOrientationType int orientation);
    }


    /**
     * The default fast forward increment, in milliseconds.
     */
    public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    /**
     * The default rewind increment, in milliseconds.
     */
    public static final int DEFAULT_REWIND_MS = 5000;
    /**
     * The default show timeout, in milliseconds.
     */
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;
    /**
     * The default repeat toggle modes.
     */
    public static final @RepeatModeUtil.RepeatToggleModes
    int DEFAULT_REPEAT_TOGGLE_MODES =
            RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE;

    /**
     * The maximum number of windows that can be shown in a multi-window time bar.
     */
    public static final int MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR = 100;

    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;


    //        <!--<enum name="all" value="0b1111"/>-->
//    <!--<enum name="top" value="0b1000"/>-->
//    <!--<enum name="top_landscape" value="0b0100"/>-->
//    <!--<enum name="bottom" value="0b0010"/>-->
//    <!--<enum name="bottom_landscape" value="0b0001"/>-->
    public static final int CONTROLLER_MODE_ALL = 0b1111;
    public static final int CONTROLLER_MODE_TOP = 0b1000;
    public static final int CONTROLLER_MODE_TOP_LANDSCAPE = 0b0100;
    public static final int CONTROLLER_MODE_BOTTOM = 0b0010;
    public static final int CONTROLLER_MODE_BOTTOM_LANDSCAPE = 0b0001;

    @IntDef({CONTROLLER_MODE_ALL, CONTROLLER_MODE_TOP, CONTROLLER_MODE_TOP_LANDSCAPE, CONTROLLER_MODE_BOTTOM, CONTROLLER_MODE_BOTTOM_LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ControllerModeType {

    }


    private final ComponentListener componentListener;
    private final View previousButton;
    private final View nextButton;
    private final View playButton;
    private final View pauseButton;
    private final View fastForwardButton;
    private final View rewindButton;
    private final ImageView repeatToggleButton;
    private final View shuffleButton;
    private final TextView durationView;
    private final TextView positionView;
    private final TimeBar timeBar;
    private final StringBuilder formatBuilder;
    private final Formatter formatter;
    private final Timeline.Period period;
    private final Timeline.Window window;


    private final Drawable repeatOffButtonDrawable;
    private final Drawable repeatOneButtonDrawable;
    private final Drawable repeatAllButtonDrawable;
    private final String repeatOffButtonContentDescription;
    private final String repeatOneButtonContentDescription;
    private final String repeatAllButtonContentDescription;

    private Player player;
    private com.google.android.exoplayer2.ControlDispatcher controlDispatcher;
    private VisibilityListener visibilityListener;

    private boolean isAttachedToWindow;
    private boolean showMultiWindowTimeBar;
    private boolean multiWindowTimeBar;
    private boolean scrubbing;
    private int rewindMs;
    private int fastForwardMs;
    private int showTimeoutMs;
    private @RepeatModeUtil.RepeatToggleModes
    int repeatToggleModes;
    private boolean showShuffleButton;
    private long hideAtMs;
    private long[] adGroupTimesMs;
    private boolean[] playedAdGroups;
    private long[] extraAdGroupTimesMs;
    private boolean[] extraPlayedAdGroups;

    private final Runnable updateProgressAction = this::updateProgress;

    private final Runnable hideAction = this::hide;


    private final TimeBar timeBarLandscape;
    private final View playButtonLandScape;
    private final View pauseButtonLandScape;
    private final TextView durationViewLandscape;
    private final View enterFullscreen;
    private final View exitFullscreen;


    private final View exoPlayerControllerTop;
    private final View exoPlayerControllerTopLandscape;
    private final View exoPlayerControllerBottom;
    private final View exoPlayerControllerBottomLandscape;

    private final View centerInfoWrapper;
    private final TextView centerInfo;

    private final TextView exoPlayerVideoName;
    private final TextView exoPlayerVideoNameLandscape;

    private boolean portrait = true;


    private SensorOrientation sensorOrientation;

    private OrientationListener orientationListener;
    private ExoClickListener backListener;


    private boolean isHls;

    private int displayMode = CONTROLLER_MODE_ALL;


    public ExoVideoPlaybackControlView(Context context) {
        this(context, null);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, attrs);
    }

    public ExoVideoPlaybackControlView(Context context, AttributeSet attrs, int defStyleAttr,
                                       AttributeSet playbackAttrs) {
        super(context, attrs, defStyleAttr);


        int controllerLayoutId = R.layout.exo_video_playback_control_view;
        rewindMs = DEFAULT_REWIND_MS;
        fastForwardMs = DEFAULT_FAST_FORWARD_MS;
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;
        repeatToggleModes = DEFAULT_REPEAT_TOGGLE_MODES;
        showShuffleButton = false;
        if (playbackAttrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(playbackAttrs,
                    R.styleable.ExoVideoPlaybackControlView, 0, 0);
            try {
                rewindMs = a.getInt(R.styleable.ExoVideoPlaybackControlView_rewind_increment, rewindMs);
                fastForwardMs = a.getInt(R.styleable.ExoVideoPlaybackControlView_fastforward_increment,
                        fastForwardMs);
                showTimeoutMs = a.getInt(R.styleable.ExoVideoPlaybackControlView_show_timeout, showTimeoutMs);
                controllerLayoutId = a.getResourceId(R.styleable.ExoVideoPlaybackControlView_controller_layout_id,
                        controllerLayoutId);
                repeatToggleModes = getRepeatToggleModes(a, repeatToggleModes);
                showShuffleButton = a.getBoolean(R.styleable.ExoVideoPlaybackControlView_show_shuffle_button,
                        showShuffleButton);
                displayMode = a.getInt(R.styleable.ExoVideoPlaybackControlView_controller_display_mode, CONTROLLER_MODE_ALL);
            } finally {
                a.recycle();
            }
        }


        period = new Timeline.Period();
        window = new Timeline.Window();
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        adGroupTimesMs = new long[0];
        playedAdGroups = new boolean[0];
        extraAdGroupTimesMs = new long[0];
        extraPlayedAdGroups = new boolean[0];
        componentListener = new ComponentListener();
        controlDispatcher = new com.google.android.exoplayer2.DefaultControlDispatcher();

        LayoutInflater.from(context).inflate(controllerLayoutId, this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        durationView = findViewById(R.id.exo_player_duration);
        positionView = findViewById(R.id.exo_player_position);
        timeBar = findViewById(R.id.exo_player_progress);
        if (timeBar != null) {
            timeBar.addListener(componentListener);
        }


        playButton = findViewById(R.id.exo_player_play);
        if (playButton != null) {
            playButton.setOnClickListener(componentListener);
        }


        pauseButton = findViewById(R.id.exo_player_pause);
        if (pauseButton != null) {
            pauseButton.setOnClickListener(componentListener);
        }


        previousButton = findViewById(R.id.exo_prev);
        if (previousButton != null) {
            previousButton.setOnClickListener(componentListener);
        }
        nextButton = findViewById(R.id.exo_next);
        if (nextButton != null) {
            nextButton.setOnClickListener(componentListener);
        }
        rewindButton = findViewById(R.id.exo_rew);
        if (rewindButton != null) {
            rewindButton.setOnClickListener(componentListener);
        }
        fastForwardButton = findViewById(R.id.exo_ffwd);
        if (fastForwardButton != null) {
            fastForwardButton.setOnClickListener(componentListener);
        }
        repeatToggleButton = findViewById(R.id.exo_repeat_toggle);
        if (repeatToggleButton != null) {
            repeatToggleButton.setOnClickListener(componentListener);
        }
        shuffleButton = findViewById(R.id.exo_shuffle);
        if (shuffleButton != null) {
            shuffleButton.setOnClickListener(componentListener);
        }
        Resources resources = context.getResources();
        repeatOffButtonDrawable = resources.getDrawable(R.drawable.exo_controls_repeat_off);
        repeatOneButtonDrawable = resources.getDrawable(R.drawable.exo_controls_repeat_one);
        repeatAllButtonDrawable = resources.getDrawable(R.drawable.exo_controls_repeat_all);
        repeatOffButtonContentDescription = resources.getString(
                R.string.exo_controls_repeat_off_description);
        repeatOneButtonContentDescription = resources.getString(
                R.string.exo_controls_repeat_one_description);
        repeatAllButtonContentDescription = resources.getString(
                R.string.exo_controls_repeat_all_description);


        durationViewLandscape = findViewById(R.id.exo_player_position_duration_landscape);

        timeBarLandscape = findViewById(R.id.exo_player_progress_landscape);
        if (timeBarLandscape != null) {
            timeBarLandscape.addListener(componentListener);
        }

        playButtonLandScape = findViewById(R.id.exo_player_play_landscape);
        if (playButtonLandScape != null) {
            playButtonLandScape.setOnClickListener(componentListener);
        }

        pauseButtonLandScape = findViewById(R.id.exo_player_pause_landscape);
        if (pauseButtonLandScape != null) {
            pauseButtonLandScape.setOnClickListener(componentListener);
        }

        enterFullscreen = findViewById(R.id.exo_player_enter_fullscreen);
        if (enterFullscreen != null) {
            enterFullscreen.setOnClickListener(componentListener);
        }


        exitFullscreen = findViewById(R.id.exo_player_exit_fullscreen);
        if (exitFullscreen != null) {
            exitFullscreen.setOnClickListener(componentListener);
        }

        centerInfoWrapper = findViewById(R.id.exo_player_center_info_wrapper);
        centerInfo = findViewById(R.id.exo_player_center_text);


        exoPlayerControllerTop = findViewById(R.id.exo_player_controller_top);
        exoPlayerControllerTopLandscape = findViewById(R.id.exo_player_controller_top_landscape);
        exoPlayerControllerBottom = findViewById(R.id.exo_player_controller_bottom);
        exoPlayerControllerBottomLandscape = findViewById(R.id.exo_player_controller_bottom_landscape);


        exoPlayerVideoName = findViewById(R.id.exo_player_video_name);
        if (exoPlayerVideoName != null) {
            exoPlayerVideoName.setOnClickListener(componentListener);
        }

        exoPlayerVideoNameLandscape = findViewById(R.id.exo_player_video_name_landscape);
        if (exoPlayerVideoNameLandscape != null) {
            exoPlayerVideoNameLandscape.setOnClickListener(componentListener);
        }

        if (centerInfoWrapper != null) {
            setupVideoGesture();
        }

        sensorOrientation = new SensorOrientation(getContext(), this::changeOrientation);
        showControllerByDisplayMode();


    }


    private void setupVideoGesture() {
        OnVideoGestureChangeListener onVideoGestureChangeListener = new OnVideoGestureChangeListener() {

            @Override
            public void onVolumeChanged(int range, int type) {
                show();
                int drawableId;
                if (type == VOLUME_CHANGED_MUTE) {
                    drawableId = R.drawable.ic_volume_mute_white_36dp;
                } else if (type == VOLUME_CHANGED_INCREMENT) {
                    drawableId = R.drawable.ic_volume_up_white_36dp;
                } else {
                    drawableId = R.drawable.ic_volume_down_white_36dp;
                }

                setVolumeOrBrightnessInfo(getContext().getString(R.string.volume_changing, range), drawableId);
            }

            @Override
            public void onBrightnessChanged(int brightnessPercent) {
                show();
                String info = getContext().getString(R.string.brightness_changing, brightnessPercent);
                int drawable = whichBrightnessImageToUse(brightnessPercent);
                setVolumeOrBrightnessInfo(info, drawable);
            }

            @Override
            public void onNoGesture() {

                if (centerInfo == null) {
                    return;
                }
                centerInfo.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                centerInfo.setVisibility(GONE);
            }

            @Override
            public void onShowSeekSize(long seekSize, boolean fastForward) {
                if(isHls){
                    return;
                }

                show();
                seekTo(seekSize);
                if (centerInfo == null) {
                    return;
                }

                centerInfo.setVisibility(VISIBLE);
                centerInfo.setText(generateFastForwardOrRewindTxt(seekSize));
                int drawableId = fastForward ? R.drawable.ic_fast_forward_white_36dp : R.drawable.ic_fast_rewind_white_36dp;
                Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);
                centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            }
        };


        VideoGesture videoGesture = new VideoGesture(getContext(), onVideoGestureChangeListener, () -> player);
        centerInfoWrapper.setOnClickListener(componentListener);
        centerInfoWrapper.setOnTouchListener(videoGesture);

    }


    private CharSequence generateFastForwardOrRewindTxt(long changingTime) {

        long duration = player == null ? 0 : player.getDuration();
        String result = Util.getStringForTime(formatBuilder, formatter, changingTime);
        result = result + "/";
        result = result + Util.getStringForTime(formatBuilder, formatter, duration);

        int index = result.indexOf("/");

        SpannableString spannableString = new SpannableString(result);


        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        spannableString.setSpan(new ForegroundColorSpan(color), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), index, result.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
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

    private void setVolumeOrBrightnessInfo(String txt, @DrawableRes int drawableId) {
        if (centerInfo == null) {
            return;
        }
        centerInfo.setVisibility(VISIBLE);
        centerInfo.setText(txt);
        centerInfo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }


    @SuppressWarnings("ResourceType")
    private static @RepeatModeUtil.RepeatToggleModes
    int getRepeatToggleModes(TypedArray a,
                             @RepeatModeUtil.RepeatToggleModes int repeatToggleModes) {
        return a.getInt(R.styleable.ExoVideoPlaybackControlView_repeat_toggle_modes, repeatToggleModes);
    }

    /**
     * Returns the {@link Player} currently being controlled by this view, or null if no player is
     * set.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the {@link Player} to control.
     *
     * @param player The {@link Player} to control.
     */
    public void setPlayer(Player player) {
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

    /**
     * Sets whether the time bar should show all windows, as opposed to just the current one. If the
     * timeline has a period with unknown duration or more than
     * {@link #MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR} windows the time bar will fall back to showing a
     * single window.
     *
     * @param showMultiWindowTimeBar Whether the time bar should show all windows.
     */
    public void setShowMultiWindowTimeBar(boolean showMultiWindowTimeBar) {
        this.showMultiWindowTimeBar = showMultiWindowTimeBar;
        updateTimeBarMode();
    }

    /**
     * Sets the millisecond positions of extra ad markers relative to the start of the window (or
     * timeline, if in multi-window mode) and whether each extra ad has been played or not. The
     * markers are shown in addition to any ad markers for ads in the player's timeline.
     *
     * @param extraAdGroupTimesMs The millisecond timestamps of the extra ad markers to show, or
     *                            {@code null} to show no extra ad markers.
     * @param extraPlayedAdGroups Whether each ad has been played, or {@code null} to show no extra ad
     *                            markers.
     */
    public void setExtraAdGroupMarkers(@Nullable long[] extraAdGroupTimesMs,
                                       @Nullable boolean[] extraPlayedAdGroups) {
        if (extraAdGroupTimesMs == null) {
            this.extraAdGroupTimesMs = new long[0];
            this.extraPlayedAdGroups = new boolean[0];
        } else {
            Assertions.checkArgument(extraAdGroupTimesMs.length == extraPlayedAdGroups.length);
            this.extraAdGroupTimesMs = extraAdGroupTimesMs;
            this.extraPlayedAdGroups = extraPlayedAdGroups;
        }
        updateProgress();
    }

    /**
     * Sets the {@link VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setVisibilityListener(VisibilityListener listener) {
        this.visibilityListener = listener;
    }

    /**
     * Sets the {@link com.google.android.exoplayer2.ControlDispatcher}.
     *
     * @param controlDispatcher The {@link com.google.android.exoplayer2.ControlDispatcher}, or null
     *                          to use {@link com.google.android.exoplayer2.DefaultControlDispatcher}.
     */
    public void setControlDispatcher(
            @Nullable com.google.android.exoplayer2.ControlDispatcher controlDispatcher) {
        this.controlDispatcher = controlDispatcher == null
                ? new com.google.android.exoplayer2.DefaultControlDispatcher() : controlDispatcher;
    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds. A non-positive value will cause the
     *                 rewind button to be disabled.
     */
    public void setRewindIncrementMs(int rewindMs) {
        this.rewindMs = rewindMs;
        updateNavigation();
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds. A non-positive value will
     *                      cause the fast forward button to be disabled.
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
     * Returns which repeat toggle modes are enabled.
     *
     * @return The currently enabled {@link RepeatModeUtil.RepeatToggleModes}.
     */
    public @RepeatModeUtil.RepeatToggleModes
    int getRepeatToggleModes() {
        return repeatToggleModes;
    }

    /**
     * Sets which repeat toggle modes are enabled.
     *
     * @param repeatToggleModes A set of {@link RepeatModeUtil.RepeatToggleModes}.
     */
    public void setRepeatToggleModes(@RepeatModeUtil.RepeatToggleModes int repeatToggleModes) {
        this.repeatToggleModes = repeatToggleModes;
        if (player != null) {
            @Player.RepeatMode int currentMode = player.getRepeatMode();
            if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
                    && currentMode != Player.REPEAT_MODE_OFF) {
                controlDispatcher.dispatchSetRepeatMode(player, Player.REPEAT_MODE_OFF);
            } else if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE
                    && currentMode == Player.REPEAT_MODE_ALL) {
                controlDispatcher.dispatchSetRepeatMode(player, Player.REPEAT_MODE_ONE);
            } else if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
                    && currentMode == Player.REPEAT_MODE_ONE) {
                controlDispatcher.dispatchSetRepeatMode(player, Player.REPEAT_MODE_ALL);
            }
        }
    }

    /**
     * Returns whether the shuffle button is shown.
     */
    public boolean getShowShuffleButton() {
        return showShuffleButton;
    }

    /**
     * Sets whether the shuffle button is shown.
     *
     * @param showShuffleButton Whether the shuffle button is shown.
     */
    public void setShowShuffleButton(boolean showShuffleButton) {
        this.showShuffleButton = showShuffleButton;
        updateShuffleButton();
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
            requestPlayPauseFocus();
        }
        // Call hideAfterTimeout even if already visible to reset the timeout.
        hideAfterTimeout();
    }

    /**
     * Hides the controller.
     */
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
        updateRepeatModeButton();
        updateShuffleButton();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        boolean requestPlayPauseFocus = false;
        boolean playing = player != null && player.getPlayWhenReady();
        if (playButton != null) {
            requestPlayPauseFocus |= playing && playButton.isFocused();
            playButton.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (pauseButton != null) {
            requestPlayPauseFocus |= !playing && pauseButton.isFocused();
            pauseButton.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }


        if (playButtonLandScape != null) {
            requestPlayPauseFocus |= playing && playButtonLandScape.isFocused();
            playButtonLandScape.setVisibility(playing ? View.GONE : View.VISIBLE);
        }
        if (pauseButtonLandScape != null) {
            requestPlayPauseFocus |= !playing && pauseButtonLandScape.isFocused();
            pauseButtonLandScape.setVisibility(!playing ? View.GONE : View.VISIBLE);
        }


        if (requestPlayPauseFocus) {
            requestPlayPauseFocus();
        }
    }

    private void updateNavigation() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }
        Timeline timeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        boolean isSeekable = false;
        boolean enablePrevious = false;
        boolean enableNext = false;
        if (haveNonEmptyTimeline && !player.isPlayingAd()) {
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
            isSeekable = window.isSeekable;
            enablePrevious = isSeekable || !window.isDynamic
                    || player.getPreviousWindowIndex() != C.INDEX_UNSET;
            enableNext = window.isDynamic || player.getNextWindowIndex() != C.INDEX_UNSET;
        }
        setButtonEnabled(enablePrevious, previousButton);
        setButtonEnabled(enableNext, nextButton);
        setButtonEnabled(fastForwardMs > 0 && isSeekable, fastForwardButton);
        setButtonEnabled(rewindMs > 0 && isSeekable, rewindButton);
        if (timeBar != null) {
            timeBar.setEnabled(isSeekable && !isHls);
        }
        if (timeBarLandscape != null) {
            timeBarLandscape.setEnabled(isSeekable && !isHls);
        }
    }

    private void updateRepeatModeButton() {
        if (!isVisible() || !isAttachedToWindow || repeatToggleButton == null) {
            return;
        }
        if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE) {
            repeatToggleButton.setVisibility(View.GONE);
            return;
        }
        if (player == null) {
            setButtonEnabled(false, repeatToggleButton);
            return;
        }
        setButtonEnabled(true, repeatToggleButton);
        switch (player.getRepeatMode()) {
            case Player.REPEAT_MODE_OFF:
                repeatToggleButton.setImageDrawable(repeatOffButtonDrawable);
                repeatToggleButton.setContentDescription(repeatOffButtonContentDescription);
                break;
            case Player.REPEAT_MODE_ONE:
                repeatToggleButton.setImageDrawable(repeatOneButtonDrawable);
                repeatToggleButton.setContentDescription(repeatOneButtonContentDescription);
                break;
            case Player.REPEAT_MODE_ALL:
                repeatToggleButton.setImageDrawable(repeatAllButtonDrawable);
                repeatToggleButton.setContentDescription(repeatAllButtonContentDescription);
                break;
        }
        repeatToggleButton.setVisibility(View.VISIBLE);
    }

    private void updateShuffleButton() {
        if (!isVisible() || !isAttachedToWindow || shuffleButton == null) {
            return;
        }
        if (!showShuffleButton) {
            shuffleButton.setVisibility(View.GONE);
        } else if (player == null) {
            setButtonEnabled(false, shuffleButton);
        } else {
            shuffleButton.setAlpha(player.getShuffleModeEnabled() ? 1f : 0.3f);
            shuffleButton.setEnabled(true);
            shuffleButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateTimeBarMode() {
        if (player == null) {
            return;
        }
        multiWindowTimeBar = showMultiWindowTimeBar
                && canShowMultiWindowTimeBar(player.getCurrentTimeline(), window);
    }

    private void updateProgress() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }

        long position = 0;
        long bufferedPosition = 0;
        long duration = 0;
        if (player != null) {
            long currentWindowTimeBarOffsetUs = 0;
            long durationUs = 0;
            int adGroupCount = 0;
            Timeline timeline = player.getCurrentTimeline();
            if (!timeline.isEmpty()) {
                int currentWindowIndex = player.getCurrentWindowIndex();
                int firstWindowIndex = multiWindowTimeBar ? 0 : currentWindowIndex;
                int lastWindowIndex =
                        multiWindowTimeBar ? timeline.getWindowCount() - 1 : currentWindowIndex;
                for (int i = firstWindowIndex; i <= lastWindowIndex; i++) {
                    if (i == currentWindowIndex) {
                        currentWindowTimeBarOffsetUs = durationUs;
                    }
                    timeline.getWindow(i, window);
                    if (window.durationUs == C.TIME_UNSET) {
                        Assertions.checkState(!multiWindowTimeBar);
                        break;
                    }
                    for (int j = window.firstPeriodIndex; j <= window.lastPeriodIndex; j++) {
                        timeline.getPeriod(j, period);
                        int periodAdGroupCount = period.getAdGroupCount();
                        for (int adGroupIndex = 0; adGroupIndex < periodAdGroupCount; adGroupIndex++) {
                            long adGroupTimeInPeriodUs = period.getAdGroupTimeUs(adGroupIndex);
                            if (adGroupTimeInPeriodUs == C.TIME_END_OF_SOURCE) {
                                if (period.durationUs == C.TIME_UNSET) {
                                    // Don't show ad markers for postrolls in periods with unknown duration.
                                    continue;
                                }
                                adGroupTimeInPeriodUs = period.durationUs;
                            }
                            long adGroupTimeInWindowUs = adGroupTimeInPeriodUs + period.getPositionInWindowUs();
                            if (adGroupTimeInWindowUs >= 0 && adGroupTimeInWindowUs <= window.durationUs) {
                                if (adGroupCount == adGroupTimesMs.length) {
                                    int newLength = adGroupTimesMs.length == 0 ? 1 : adGroupTimesMs.length * 2;
                                    adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, newLength);
                                    playedAdGroups = Arrays.copyOf(playedAdGroups, newLength);
                                }
                                adGroupTimesMs[adGroupCount] = C.usToMs(durationUs + adGroupTimeInWindowUs);
                                playedAdGroups[adGroupCount] = period.hasPlayedAdGroup(adGroupIndex);
                                adGroupCount++;
                            }
                        }
                    }
                    durationUs += window.durationUs;
                }
            }
            duration = C.usToMs(durationUs);
            position = C.usToMs(currentWindowTimeBarOffsetUs);
            bufferedPosition = position;
            if (player.isPlayingAd()) {
                position += player.getContentPosition();
                bufferedPosition = position;
            } else {
                position += player.getCurrentPosition();
                bufferedPosition += player.getBufferedPosition();
            }
            if (timeBar != null) {
                int extraAdGroupCount = extraAdGroupTimesMs.length;
                int totalAdGroupCount = adGroupCount + extraAdGroupCount;
                if (totalAdGroupCount > adGroupTimesMs.length) {
                    adGroupTimesMs = Arrays.copyOf(adGroupTimesMs, totalAdGroupCount);
                    playedAdGroups = Arrays.copyOf(playedAdGroups, totalAdGroupCount);
                }
                System.arraycopy(extraAdGroupTimesMs, 0, adGroupTimesMs, adGroupCount, extraAdGroupCount);
                System.arraycopy(extraPlayedAdGroups, 0, playedAdGroups, adGroupCount, extraAdGroupCount);
                timeBar.setAdGroupTimesMs(adGroupTimesMs, playedAdGroups, totalAdGroupCount);
            }
        }
        if (durationView != null && !isHls) {
            durationView.setText(Util.getStringForTime(formatBuilder, formatter, duration));
        }

        if (durationViewLandscape != null && !isHls) {
            String positionStr = Util.getStringForTime(formatBuilder, formatter, position);
            String durationStr = Util.getStringForTime(formatBuilder, formatter, duration);
            durationViewLandscape.setText(positionStr.concat("/").concat(durationStr));
        }

        if (positionView != null && !scrubbing && !isHls) {
            positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
        }


        if (timeBar != null && !isHls) {
            timeBar.setPosition(position);
            timeBar.setBufferedPosition(bufferedPosition);
            timeBar.setDuration(duration);
        }

        if (timeBarLandscape != null && !isHls) {
            timeBarLandscape.setPosition(position);
            timeBarLandscape.setBufferedPosition(bufferedPosition);
            timeBarLandscape.setDuration(duration);
        }


        // Cancel any pending updates and schedule a new one if necessary.
        removeCallbacks(updateProgressAction);
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                float playbackSpeed = player.getPlaybackParameters().speed;
                if (playbackSpeed <= 0.1f) {
                    delayMs = 1000;
                } else if (playbackSpeed <= 5f) {
                    long mediaTimeUpdatePeriodMs = 1000 / Math.max(1, Math.round(1 / playbackSpeed));
                    long mediaTimeDelayMs = mediaTimeUpdatePeriodMs - (position % mediaTimeUpdatePeriodMs);
                    if (mediaTimeDelayMs < (mediaTimeUpdatePeriodMs / 5)) {
                        mediaTimeDelayMs += mediaTimeUpdatePeriodMs;
                    }
                    delayMs = playbackSpeed == 1 ? mediaTimeDelayMs
                            : (long) (mediaTimeDelayMs / playbackSpeed);
                } else {
                    delayMs = 200;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    private void requestPlayPauseFocus() {
        boolean playing = player != null && player.getPlayWhenReady();
        if (!playing && playButton != null) {
            playButton.requestFocus();
        } else if (playing && pauseButton != null) {
            pauseButton.requestFocus();

        }

        if (!playing && playButtonLandScape != null) {
            playButtonLandScape.requestFocus();
        } else if (playing && pauseButtonLandScape != null) {
            pauseButtonLandScape.requestFocus();

        }
    }

    private void setButtonEnabled(boolean enabled, View view) {
        if (view == null) {
            return;
        }
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.3f);
        view.setVisibility(VISIBLE);
    }

    private void previous() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = player.getCurrentWindowIndex();
        timeline.getWindow(windowIndex, window);
        int previousWindowIndex = player.getPreviousWindowIndex();
        if (previousWindowIndex != C.INDEX_UNSET
                && (player.getCurrentPosition() <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || (window.isDynamic && !window.isSeekable))) {
            seekTo(previousWindowIndex, C.TIME_UNSET);
        } else {
            seekTo(0);
        }
    }

    private void next() {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int windowIndex = player.getCurrentWindowIndex();
        int nextWindowIndex = player.getNextWindowIndex();
        if (nextWindowIndex != C.INDEX_UNSET) {
            seekTo(nextWindowIndex, C.TIME_UNSET);
        } else if (timeline.getWindow(windowIndex, window, false).isDynamic) {
            seekTo(windowIndex, C.TIME_UNSET);
        }
    }

    private void rewind() {
        if (rewindMs <= 0) {
            return;
        }
        seekTo(Math.max(player.getCurrentPosition() - rewindMs, 0));
    }

    private void fastForward() {
        if (fastForwardMs <= 0) {
            return;
        }
        long durationMs = player.getDuration();
        long seekPositionMs = player.getCurrentPosition() + fastForwardMs;
        if (durationMs != C.TIME_UNSET) {
            seekPositionMs = Math.min(seekPositionMs, durationMs);
        }
        seekTo(seekPositionMs);
    }

    private void seekTo(long positionMs) {
        seekTo(player.getCurrentWindowIndex(), positionMs);
    }

    private void seekTo(int windowIndex, long positionMs) {
        boolean dispatched = controlDispatcher.dispatchSeekTo(player, windowIndex, positionMs);
        if (!dispatched) {
            // The seek wasn't dispatched. If the progress bar was dragged by the user to perform the
            // seek then it'll now be in the wrong position. Trigger a progress update to snap it back.
            updateProgress();
        }
    }

    private void seekToTimeBarPosition(long positionMs) {
        int windowIndex;
        Timeline timeline = player.getCurrentTimeline();
        if (multiWindowTimeBar && !timeline.isEmpty()) {
            int windowCount = timeline.getWindowCount();
            windowIndex = 0;
            while (true) {
                long windowDurationMs = timeline.getWindow(windowIndex, window).getDurationMs();
                if (positionMs < windowDurationMs) {
                    break;
                } else if (windowIndex == windowCount - 1) {
                    // Seeking past the end of the last window should seek to the end of the timeline.
                    positionMs = windowDurationMs;
                    break;
                }
                positionMs -= windowDurationMs;
                windowIndex++;
            }
        } else {
            windowIndex = player.getCurrentWindowIndex();
        }
        seekTo(windowIndex, positionMs);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        sensorOrientation.enable();
        isAttachedToWindow = true;
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
        sensorOrientation.disable();
        isAttachedToWindow = false;
        removeCallbacks(updateProgressAction);
        removeCallbacks(hideAction);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (player == null || !isHandledMediaKey(keyCode)) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                fastForward();
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                rewind();
            } else if (event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, !player.getPlayWhenReady());
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        controlDispatcher.dispatchSetPlayWhenReady(player, true);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, false);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        next();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        previous();
                        break;
                    default:
                        break;
                }
            }
        }
        return true;
    }



    public void setBackListener(ExoClickListener backListener) {
        this.backListener = backListener;
    }


    public void setPortrait(boolean portrait) {
        this.portrait = portrait;
        showControllerByDisplayMode();
    }

    public boolean isPortrait() {
        return portrait;
    }

    private void toggleControllerOrientation() {
        if (orientationListener == null) {
            setPortrait(!portrait);
        } else {
            changeOrientation(portrait ? SENSOR_LANDSCAPE : SENSOR_PORTRAIT);
        }

    }


    public void setOrientationListener(OrientationListener orientationListener) {
        this.orientationListener = orientationListener;
    }


    public void setMediaSource(ExoMediaSource exoMediaSource) {
        if (exoPlayerVideoName != null) {
            exoPlayerVideoName.setText(exoMediaSource.getDisplayName());
        }

        if (exoPlayerVideoNameLandscape != null) {
            exoPlayerVideoNameLandscape.setText(exoMediaSource.getDisplayName());
        }
    }


    public void setControllerDisplayMode(int displayMode) {
        this.displayMode = displayMode;
        showControllerByDisplayMode();
    }

    private void showControllerByDisplayMode() {

        if (exoPlayerControllerTop != null) {
            boolean showByMode = (displayMode & CONTROLLER_MODE_TOP) == CONTROLLER_MODE_TOP;
            int visibility = showByMode && isPortrait() ? VISIBLE : INVISIBLE;
            exoPlayerControllerTop.setVisibility(visibility);
        }

        if (exoPlayerControllerTopLandscape != null) {
            boolean showByMode = (displayMode & CONTROLLER_MODE_TOP_LANDSCAPE) == CONTROLLER_MODE_TOP_LANDSCAPE;
            int visibility = showByMode && !portrait ? VISIBLE : INVISIBLE;
            exoPlayerControllerTopLandscape.setVisibility(visibility);
        }

        if (exoPlayerControllerBottom != null) {
            boolean showByMode = (displayMode & CONTROLLER_MODE_BOTTOM) == CONTROLLER_MODE_BOTTOM;
            int visibility = showByMode && portrait ? VISIBLE : INVISIBLE;
            exoPlayerControllerBottom.setVisibility(visibility);
        }

        if (exoPlayerControllerBottomLandscape != null) {
            boolean showByMode = (displayMode & CONTROLLER_MODE_BOTTOM_LANDSCAPE) == CONTROLLER_MODE_BOTTOM_LANDSCAPE;
            int visibility = showByMode && !portrait ? VISIBLE : INVISIBLE;
            exoPlayerControllerBottomLandscape.setVisibility(visibility);
        }


    }

    private synchronized void changeOrientation(@OnOrientationChangedListener.SensorOrientationType int orientation) {
        if (orientationListener == null) {
            return;
        }

        orientationListener.onOrientationChange(orientation);
        Context context = getContext();
        Activity activity;
        if (!(context instanceof Activity)) {
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
                showControllerByDisplayMode();
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                break;
            case SENSOR_UNKNOWN:
            default:
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (backListener != null) {
                if (!backListener.onClick(null, !portrait)) {
                    changeOrientation(portrait ? SENSOR_LANDSCAPE : SENSOR_PORTRAIT);
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("InlinedApi")
    private static boolean isHandledMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT
                || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    }

    /**
     * Returns whether the specified {@code timeline} can be shown on a multi-window time bar.
     *
     * @param timeline The {@link Timeline} to check.
     * @param window   A scratch {@link Timeline.Window} instance.
     * @return Whether the specified timeline can be shown on a multi-window time bar.
     */
    private static boolean canShowMultiWindowTimeBar(Timeline timeline, Timeline.Window window) {
        if (timeline.getWindowCount() > MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR) {
            return false;
        }
        int windowCount = timeline.getWindowCount();
        for (int i = 0; i < windowCount; i++) {
            if (timeline.getWindow(i, window).durationUs == C.TIME_UNSET) {
                return false;
            }
        }
        return true;
    }

    private final class ComponentListener extends Player.DefaultEventListener implements
            TimeBar.OnScrubListener, OnClickListener {

        @Override
        public void onScrubStart(TimeBar timeBar, long position) {
            removeCallbacks(hideAction);
            scrubbing = true;
        }

        @Override
        public void onScrubMove(TimeBar timeBar, long position) {
            if (positionView != null) {
                positionView.setText(Util.getStringForTime(formatBuilder, formatter, position));
            }
        }

        @Override
        public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
            scrubbing = false;
            if (!canceled && player != null) {
                seekToTimeBarPosition(position);
            }
            hideAfterTimeout();
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
            updateProgress();
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            updateRepeatModeButton();
            updateNavigation();
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            updateShuffleButton();
            updateNavigation();
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            updateNavigation();
            updateProgress();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            if (manifest instanceof HlsManifest) {
                HlsManifest hlsManifest = (HlsManifest) manifest;
                isHls = !hlsManifest.mediaPlaylist.hasEndTag && hlsManifest.mediaPlaylist.playlistType == HlsMediaPlaylist.PLAYLIST_TYPE_UNKNOWN;
            } else {
                isHls = false;
            }


            updateNavigation();
            updateTimeBarMode();
            updateProgress();
        }

        @Override
        public void onClick(View view) {
            if (player != null) {
                if (nextButton == view) {
                    next();
                } else if (previousButton == view) {
                    previous();
                } else if (fastForwardButton == view) {
                    fastForward();
                } else if (rewindButton == view) {
                    rewind();
                } else if (playButton == view || playButtonLandScape == view) {
                    controlDispatcher.dispatchSetPlayWhenReady(player, true);
                } else if (pauseButton == view || pauseButtonLandScape == view) {
                    controlDispatcher.dispatchSetPlayWhenReady(player, false);
                } else if (repeatToggleButton == view) {
                    controlDispatcher.dispatchSetRepeatMode(player, RepeatModeUtil.getNextRepeatMode(
                            player.getRepeatMode(), repeatToggleModes));
                } else if (shuffleButton == view) {
                    controlDispatcher.dispatchSetShuffleModeEnabled(player, !player.getShuffleModeEnabled());
                } else if (enterFullscreen == view) {
                    changeOrientation(SENSOR_LANDSCAPE);
                } else if (exitFullscreen == view) {
                    changeOrientation(SENSOR_PORTRAIT);
                } else if (exoPlayerVideoName == view) {
                    if (backListener != null) {
                        if (!backListener.onClick(view, portrait)) {
                            changeOrientation(SENSOR_LANDSCAPE);
                        }
                    }
                } else if (exoPlayerVideoNameLandscape == view) {
                    if (backListener != null) {
                        if (!backListener.onClick(view, portrait)) {
                            changeOrientation(SENSOR_PORTRAIT);
                        }
                    }
                }else if(centerInfoWrapper == view){
                    playOrPause();
                }
            }
            hideAfterTimeout();
        }

        long[] mHits =new long[2];
        private void playOrPause() {

            System.arraycopy(mHits,1,mHits,0,mHits.length-1);
            mHits[mHits.length-1]=SystemClock.uptimeMillis();

            if(500>(SystemClock.uptimeMillis()-mHits[0])){
              controlDispatcher.dispatchSetPlayWhenReady(player,!player.getPlayWhenReady());
            }

        }


    }


    /**
     * Created by mo on 18-2-5.
     * 剑气纵横三万里 一剑光寒十九洲
     */

    public static interface PlayerAccessor {

        Player attachPlayer();
    }
}
