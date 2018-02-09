package com.jarvanmo.demo;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.jarvanmo.exoplayerview.media.SimpleMediaSource;
import com.jarvanmo.exoplayerview.ui.ExoVideoView;

import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_LANDSCAPE;
import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_PORTRAIT;

public class SimpleVideoViewActivity extends AppCompatActivity {

    private ExoVideoView videoView;
    private View wrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_video_view);
        videoView = findViewById(R.id.videoView);
        Button modeFit = findViewById(R.id.mode_fit);
        Button modeNone = findViewById(R.id.mode_none);
        Button modeHeight = findViewById(R.id.mode_height);
        Button modeWidth = findViewById(R.id.mode_width);
        Button modeZoom = findViewById(R.id.mode_zoom);
        wrapper = findViewById(R.id.wrapper);

        videoView.setBackListener((view, isPortrait) -> {
            if (isPortrait) {
                finish();
            }
            return false;
        });
//
//
        videoView.setOrientationListener(orientation -> {
            if (orientation == SENSOR_PORTRAIT) {
                changeToPortrait();
            } else if (orientation == SENSOR_LANDSCAPE) {
                changeToLandscape();
            }
        });

//

        SimpleMediaSource mediaSource  = new SimpleMediaSource(" http://playready.directtaps.net/smoothstreaming/SSWSS720H264PR/SuperSpeedway_720.ism");

        mediaSource.setDisplayName("Super speed");

        videoView.play(mediaSource,false);


        modeFit.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT));
        modeWidth.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH));
        modeHeight.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT));
        modeNone.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL));
        modeZoom.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM));
    }

    private void changeToPortrait() {

        // WindowManager operation is not necessary
        WindowManager.LayoutParams attr = getWindow().getAttributes();
//        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        window.setAttributes(attr);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);



        wrapper.setVisibility(View.VISIBLE);
    }


    private void changeToLandscape() {

        // WindowManager operation is not necessary

        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = getWindow();
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        wrapper.setVisibility(View.GONE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT > 23) {
            videoView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Build.VERSION.SDK_INT <= 23)) {
            videoView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT <= 23) {
            videoView.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > 23) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.releasePlayer();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            return videoView.onKeyDown(keyCode,event);
        }
        return super.onKeyDown(keyCode, event);
    }
}
