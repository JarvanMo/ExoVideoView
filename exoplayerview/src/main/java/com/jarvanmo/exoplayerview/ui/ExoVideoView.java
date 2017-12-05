package com.jarvanmo.exoplayerview.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.jarvanmo.exoplayerview.R;
import com.jarvanmo.exoplayerview.widget.SuperAspectRatioFrameLayout;

import java.util.HashMap;
import java.util.List;

import static android.content.Context.AUDIO_SERVICE;
import static com.google.android.exoplayer2.ExoPlayer.STATE_ENDED;
import static com.google.android.exoplayer2.ExoPlayer.STATE_READY;

/**
 * Created by mo on 16-11-7.
 *
 * @author mo
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class ExoVideoView extends FrameLayout {


    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private final View surfaceView;

    private final ExoVideoPlaybackControlView controller;
    private final ComponentListener componentListener;
    private final SuperAspectRatioFrameLayout layout;

    //    private final View surfaceView;
    private final View shutterView;
    private final SubtitleView subtitleLayout;
    private final ImageView frameCover;

    private SimpleExoPlayer player;
    private boolean useController = true;

    private int controllerShowTimeoutMs;


    private EventLogger eventLogger;
    private Handler mainHandler;
    private MappingTrackSelector trackSelector;
    private DataSource.Factory mediaDataSourceFactory;
    private Timeline.Window window;


//    private boolean isTimelineStatic;

    private int playerWindow;
    private long playerPosition;

    private boolean isPauseFromUser = false;


    private final AudioManager audioManager;
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
                resume();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // audioManager.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
                audioManager.abandonAudioFocus(afChangeListener);
                // Stop playback
                stop();

            }
        }
    };

    public ExoVideoView(Context context) {
        this(context, null);
    }

    public ExoVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("WrongConstant")
    public ExoVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        boolean useTextureView = false;
        @SuperAspectRatioFrameLayout.ResizeMode
        int resizeMode = SuperAspectRatioFrameLayout.RESIZE_MODE_FIT;

        boolean portrait = true;

        int rewindMs = PlaybackControlView.DEFAULT_REWIND_MS;
        int fastForwardMs = ExoVideoPlaybackControlView.DEFAULT_FAST_FORWARD_MS;
        int controllerShowTimeoutMs = ExoVideoPlaybackControlView.DEFAULT_SHOW_TIMEOUT_MS;

        float textSize = Float.MIN_VALUE;

        if (attrs != null) {
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.ExoVideoView, 0, 0);

            try {
                useController = typedArray.getBoolean(R.styleable.ExoVideoView_useController, useController);
                useTextureView = typedArray.getBoolean(R.styleable.ExoVideoView_useTextureView, false);
                resizeMode = typedArray.getInt(R.styleable.ExoVideoView_resizeMode, SuperAspectRatioFrameLayout.RESIZE_MODE_FIT);
                rewindMs = typedArray.getInt(R.styleable.ExoVideoView_rewindIncrement, rewindMs);
                fastForwardMs = typedArray.getInt(R.styleable.ExoVideoView_fastForwardIncrement, fastForwardMs);
                controllerShowTimeoutMs = typedArray.getInt(R.styleable.ExoVideoView_showTimeout, controllerShowTimeoutMs);
                portrait = typedArray.getBoolean(R.styleable.ExoVideoView_isPortrait, true);
                textSize = typedArray.getDimension(R.styleable.ExoVideoView_topWrapperTextSize, Float.MIN_VALUE);
            } finally {
                typedArray.recycle();
            }
        }



        LayoutInflater.from(getContext()).inflate(R.layout.exo_video_view, this);

        componentListener = new ComponentListener();

        layout =  findViewById(R.id.videoFrame);
        layout.setResizeMode(resizeMode);

        frameCover =  findViewById(R.id.frameCover);

        shutterView = findViewById(R.id.shutter);

        subtitleLayout =  findViewById(R.id.subtitles);
        subtitleLayout.setUserDefaultStyle();
        subtitleLayout.setUserDefaultTextSize();


        controller =  findViewById(R.id.control);
        controller.setTopWrapperTextSize(textSize);
        controller.setPortrait(portrait);
        controller.hide();
        controller.setRewindIncrementMs(rewindMs);
        controller.setFastForwardIncrementMs(fastForwardMs);
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;

        View view = useTextureView ? new TextureView(context) : new SurfaceView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        surfaceView = view;
        layout.addView(surfaceView, 0);

        audioManager =  (AudioManager) context.getApplicationContext().getSystemService(AUDIO_SERVICE);

//        this.controllerShowTimeoutMs = controllerShowTimeoutMs;


    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        audioManager.abandonAudioFocus(afChangeListener);
    }

    /**
     * Returns the player currently set on this view, or null if no player is set.
     */
    public SimpleExoPlayer getPlayer() {
        return player;
    }


    /**
     * Set the {@link SimpleExoPlayer} to use. The {@link SimpleExoPlayer#setTextOutput} and
     * {@link SimpleExoPlayer#setVideoListener} method of the player will be called and previous
     * assignments are overridden.
     *
     * @param player The {@link SimpleExoPlayer} to use.
     */
    public void setPlayer(SimpleExoPlayer player) {
        setPlayer(player, false);
    }

    private void setPlayer(SimpleExoPlayer player, boolean isFromSelf) {
        if (this.player == player && !isFromSelf) {
            return;
        }
        if (this.player != null) {
            this.player.setTextOutput(null);
            this.player.setVideoListener(null);
            this.player.removeListener(componentListener);
            this.player.setVideoSurface(null);
        }

        this.player = player;
        if (useController) {
            controller.setPlayer(player);
        }
        if (player != null) {
            if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            } else if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            }
            player.setVideoListener(componentListener);
            player.addListener(componentListener);
            player.setTextOutput(componentListener);
            maybeShowController(false);
        } else {
            shutterView.setVisibility(VISIBLE);
            controller.hide();
        }
    }


    /**
     * Sets the resize mode which can be of value {@link SuperAspectRatioFrameLayout#RESIZE_MODE_FIT},
     * {@link SuperAspectRatioFrameLayout#RESIZE_MODE_FIXED_HEIGHT} ,
     * {@link SuperAspectRatioFrameLayout#RESIZE_MODE_FIXED_WIDTH} or
     * {@link SuperAspectRatioFrameLayout#RESIZE_MODE_NONE}
     *
     * @param resizeMode The resize mode.
     */
    public void setResizeMode(@SuperAspectRatioFrameLayout.ResizeMode int resizeMode) {
        layout.setResizeMode(resizeMode);
    }

    /**
     * Returns whether the playback controls are enabled.
     */
    public boolean getUseController() {
        return useController;
    }

    /**
     * Sets whether playback controls are enabled. If set to {@code false} the playback controls are
     * never visible and are disconnected from the player.
     *
     * @param useController Whether playback controls should be enabled.
     */
    public void setUseController(boolean useController) {
        if (this.useController == useController) {
            return;
        }
        this.useController = useController;
        if (useController) {
            controller.setPlayer(player);
        } else {
            controller.hide();
            controller.setPlayer(null);
        }
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     * visible indefinitely.
     */
    public int getControllerShowTimeoutMs() {
        return controllerShowTimeoutMs;
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause
     *                                the controller to remain visible indefinitely.
     */
    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        this.controllerShowTimeoutMs = controllerShowTimeoutMs;
    }

    /**
     * Set the {@link ExoVideoPlaybackControlView.VisibilityListener}.
     *
     * @param listener The listener to be notified about visibility changes.
     */
    public void setControllerVisibilityListener(ExoVideoPlaybackControlView.VisibilityListener listener) {
        if (controller != null) {
            controller.setVisibilityListener(listener);
        }

    }

    /**
     * Sets the rewind increment in milliseconds.
     *
     * @param rewindMs The rewind increment in milliseconds.
     */
    public void setRewindIncrementMs(int rewindMs) {
        controller.setRewindIncrementMs(rewindMs);
    }

    /**
     * Sets the fast forward increment in milliseconds.
     *
     * @param fastForwardMs The fast forward increment in milliseconds.
     */
    public void setFastForwardIncrementMs(int fastForwardMs) {
        controller.setFastForwardIncrementMs(fastForwardMs);
    }

    public void setDisplayName(String displayName) {
        if (controller != null) {
            controller.setDisplayName(displayName);
        }
    }


    public boolean isPortrait() {
        return controller != null && controller.isPortrait();
    }

    public void setPortrait(boolean portrait) {
        if (controller != null) {
            controller.setPortrait(portrait);
        }

    }

    public void setOrientationListener(ExoVideoPlaybackControlView.OrientationListener orientationListener) {
        if (orientationListener != null && controller != null) {
            controller.setOrientationListener(orientationListener);
        }
    }

    public void setFullScreenListener(@NonNull ExoVideoPlaybackControlView.ExoClickListener fullScreenListener) {
        if (controller != null) {
            controller.setFullScreenListener(fullScreenListener);
        }
    }

    public void setBackListener(@NonNull ExoVideoPlaybackControlView.ExoClickListener backListener) {
        if (controller != null) {
            controller.setPortraitBackListener(backListener);
        }
    }


    public void toggleControllerOrientation() {
        if (controller != null) {
            controller.toggleControllerOrientation();
        }
    }

    public void addViewToControllerWhenLandscape(View view) {
        if (controller != null) {
            controller.addViewToControllerWhenLandscape(view);
        }
    }


    /**
     * Get the view onto which video is rendered. This is either a {@link SurfaceView} (default)
     * or a {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
     *
     * @return either a {@link SurfaceView} or a {@link TextureView}.
     */
    public View getVideoSurfaceView() {
        return surfaceView;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!useController || player == null || ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (controller.isVisible()) {
            controller.hide();
        } else {
            maybeShowController(true);
        }
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (!useController || player == null) {
            return false;
        }
        maybeShowController(true);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return useController ? controller.dispatchKeyEvent(event) : super.dispatchKeyEvent(event);
    }

    private void maybeShowController(boolean isForced) {
        if (!useController || player == null) {
            return;
        }
        int playbackState = player.getPlaybackState();
        boolean showIndefinitely = playbackState == Player.STATE_IDLE
                || playbackState == Player.STATE_ENDED || !player.getPlayWhenReady();
        boolean wasShowingIndefinitely = controller.isVisible() && controller.getShowTimeoutMs() <= 0;
        controller.setShowTimeoutMs(showIndefinitely ? 0 : controllerShowTimeoutMs);
        if (isForced || showIndefinitely || wasShowingIndefinitely) {
            controller.show();
        }
    }


    private boolean requestAudioFocus() {
        // Request audio focus for playback
        int result = audioManager.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void initSelfPlayer() {

        if (mainHandler == null) {
            mainHandler = new Handler();
        }

        if (mediaDataSourceFactory == null) {
            mediaDataSourceFactory = buildDataSourceFactory(true);
        }

        if (window == null) {
            window = new Timeline.Window();
        }

        if (trackSelector == null) {
            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        }


        eventLogger = new EventLogger(trackSelector);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(getContext(),
                null, DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

//            TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
//        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, new DefaultLoadControl(),
//                null, EXTENSION_RENDERER_MODE_OFF);
        player.addListener(eventLogger);
        player.setAudioDebugListener(eventLogger);
        player.setVideoDebugListener(eventLogger);
        player.setMetadataOutput(eventLogger);
        setPlayer(player, true);


    }


    public void releaseSelfPlayer() {

        if (player != null) {
            playerWindow = player.getCurrentWindowIndex();
            playerPosition = C.TIME_UNSET;
            player.release();
            player = null;
            trackSelector = null;
            eventLogger = null;
        }


    }


    public void play(SimpleMediaSource source, long where) {
        play(source,true,where);
//        player.prepare(mediaSource,false,false);
    }


    public void play(SimpleMediaSource source) {
        play(source, true ,C.TIME_UNSET);
    }

    public void play(SimpleMediaSource source,boolean playWhenReady){
        play(source,playWhenReady,C.TIME_UNSET);
    }

    public void play(SimpleMediaSource source,boolean playWhenReady,long where){
        if (player == null) {
            initSelfPlayer();
        }

        setDisplayName(source.getDisplayName());

        getFrameCover(source.getUrl());

        MediaSource mediaSource = buildMediaSource(Uri.parse(source.getUrl()), null);
        player.setPlayWhenReady(requestAudioFocus() && playWhenReady);

        player.prepare(mediaSource);
        if (where == C.TIME_UNSET) {
            player.seekToDefaultPosition();
        } else {
            player.seekTo(where);
        }

    }
    public void pause() {
        if (player == null) {
            return;
        }

        if (player.getPlayWhenReady()) {

            Timeline currentTimeline = player.getCurrentTimeline();
            boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
            if (haveNonEmptyTimeline && currentTimeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition = player.getCurrentPosition();
            }

            player.setPlayWhenReady(false);

            isPauseFromUser = false;

        } else {
            isPauseFromUser = true;
        }
    }

    public void resume() {
        if (player == null) {
            return;
        }

        if (!player.getPlayWhenReady() && !isPauseFromUser) {
            player.seekTo(playerPosition - 500 < 0 ? 0 : playerPosition - 500);
            player.setPlayWhenReady(true);
//            removeCallbacks(resumeAction);
//            postDelayed(resumeAction,1500);
        }

    }

    public void stop() {
        if (player != null) {
            player.stop();
        }
    }

    private void getFrameCover(String dataSource) {
        frameCover.setVisibility(VISIBLE);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(dataSource, new HashMap<String, String>());
            Bitmap bitmap = retriever.getFrameAtTime();
            frameCover.setImageBitmap(bitmap);
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, eventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }


    DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(getContext().getApplicationContext(), bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(getContext().getApplicationContext(), "ExoVideoView"), bandwidthMeter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private final class ComponentListener implements SimpleExoPlayer.VideoListener,
            TextRenderer.Output, ExoPlayer.EventListener {

        // TextRenderer.Output implementation

        @Override
        public void onCues(List<Cue> cues) {
            subtitleLayout.onCues(cues);
        }

        // SimpleExoPlayer.VideoListener implementation

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            layout.setAspectRatio(height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
        }

        @Override
        public void onRenderedFirstFrame() {
            shutterView.setVisibility(GONE);
        }


        // ExoPlayer.EventListener implementation

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // Do nothing.
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playWhenReady && STATE_READY == playbackState) {
                frameCover.setVisibility(GONE);
            }

            if (playbackState == STATE_ENDED) {
                playerPosition = C.TIME_UNSET;
            }

            maybeShowController(false);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            // Do nothing.
        }

        @Override
        public void onPositionDiscontinuity() {
            // Do nothing.
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
//            isTimelineStatic = !timeline.isEmpty()
//                    && !timeline.getWindow(timeline.getWindowCount() - 1, window).isDynamic;
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

    }
}
