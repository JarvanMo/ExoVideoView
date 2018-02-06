package com.jarvanmo.demo;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

public class MainActivity extends AppCompatActivity {


    private ExoVideoView videoView;
    private Button modeFit;
    private Button modeNone;
    private Button modeHeight;
    private Button modeWidth;
    private Button modeZoom;
    private View wrapper;
    private Button play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        modeFit =  findViewById(R.id.mode_fit);
        modeNone =  findViewById(R.id.mode_none);
        modeHeight = findViewById(R.id.mode_height);
        modeWidth = findViewById(R.id.mode_width);
        modeZoom = findViewById(R.id.mode_zoom);
        wrapper = findViewById(R.id.wrapper);
        play = findViewById(R.id.play);

        videoView.setBackListener((view, isPortrait) -> {
            if(isPortrait){
                finish();
            }
          return false;
        });
//
//
        videoView.setOrientationListener(orientation -> {
            if(orientation == SENSOR_PORTRAIT){
                changeToPortrait();
            }else if(orientation == SENSOR_LANDSCAPE){
                changeToLandscape();
            }
        });
//

//       final SimpleMediaSource mediaSource = new SimpleMediaSource("http://video19.ifeng.com/video07/2013/11/11/281708-102-007-1138.mp4");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_example_v2/master.m3u8");
//        final SimpleMediaSource mediaSource = new SimpleMediaSource("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource(" https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8");
//       SimpleMediaSource mediaSource = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_fmp4_example/master.m3u8");
//        SimpleMediaSource mediaSource = new SimpleMediaSource("http://pullhlsbb8f2e48.live.126.net/live/7de213ebb3dc4db2aa2f32f3da0b028d/playlist.m3u8");
        SimpleMediaSource mediaSource = new SimpleMediaSource("http://rotation.vod.zlive.cc/channel/1234.m3u8");

        mediaSource.setDisplayName("VideoPlaying");


        play.setOnClickListener(view ->{
            videoView.play(mediaSource);
            play.setVisibility(View.INVISIBLE);
        });


        modeFit.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT));
        modeWidth.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH));
        modeHeight.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT));
        modeNone.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL));
        modeZoom.setOnClickListener(v -> videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM));
    }

    private void changeToPortrait() {

        WindowManager.LayoutParams attr = getWindow().getAttributes();
        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Window window = getWindow();
        window.setAttributes(attr);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        wrapper.setVisibility(View.VISIBLE);
    }


    private void changeToLandscape() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
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
        //you should release the player created by ExoPlayerView
        videoView.releasePlayer();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            if(videoView.isPortrait()){
               finish();
                return false;
            }else {
                videoView.setPortrait(false);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
