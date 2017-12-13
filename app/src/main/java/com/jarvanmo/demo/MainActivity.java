package com.jarvanmo.demo;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView;
import com.jarvanmo.exoplayerview.ui.ExoVideoView;
import com.jarvanmo.exoplayerview.ui.SimpleMediaSource;
import com.jarvanmo.exoplayerview.util.AndroidUtil;
import com.jarvanmo.exoplayerview.widget.SuperAspectRatioFrameLayout;

import static com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView.SENSOR_LANDSCAPE;
import static com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView.SENSOR_PORTRAIT;

public class MainActivity extends AppCompatActivity {


    private ExoVideoView videoView;
    private Button modeFit;
    private Button modeNone;
    private Button modeHeight;
    private Button modeWidth;
    private View wrapper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        modeFit = findViewById(R.id.mode_fit);
        modeNone = findViewById(R.id.mode_none);
        modeHeight = findViewById(R.id.mode_height);
        modeWidth = findViewById(R.id.mode_width);
        wrapper = findViewById(R.id.wrapper);



        videoView.setBackListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public boolean onClick(View view, boolean isPortrait) {
                if(isPortrait){
                    finish();
                }
              return false;
            }
        });


        videoView.setOrientationListener(new ExoVideoPlaybackControlView.OrientationListener() {
            @Override
            public void onOrientationChange(@ExoVideoPlaybackControlView.SensorOrientationType int orientation) {
                if(orientation == SENSOR_PORTRAIT){
                    changeToPortrait();
                }else if(orientation == SENSOR_LANDSCAPE){
                    changeToLandscape();
                }
            }
        });

        videoView.setFullScreenListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public boolean onClick(View view, boolean isPortrait) {
                return false;
            }
        });



       final SimpleMediaSource mediaSource = new SimpleMediaSource("http://video19.ifeng.com/video07/2013/11/11/281708-102-007-1138.mp4");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_example_v2/master.m3u8");
//        SimpleMediaSource mediaSource = new SimpleMediaSource("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource(" https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8");
//       SimpleMediaSource mediaSource = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_fmp4_example/master.m3u8");
//        SimpleMediaSource mediaSource = new SimpleMediaSource("http://pullhlsbb8f2e48.live.126.net/live/7de213ebb3dc4db2aa2f32f3da0b028d/playlist.m3u8");
//        SimpleMediaSource mediaSource = new SimpleMediaSource("http://hlive.shuidi.huajiao.com/live_jia_public/36030158032/index.m3u8");

        mediaSource.setDisplayName("VideoPlaying");

        videoView.play(mediaSource);
        modeFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
        });

        modeNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_NONE);
            }
        });

        modeHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
            }
        });

        modeWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            }
        });
//        videoView.play(mediaSource,1000 * 15); // play from a particular position(ms)...
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
        if ( Build.VERSION.SDK_INT > 23) {
            videoView.resume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (( Build.VERSION.SDK_INT <= 23)) {
            videoView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if ( Build.VERSION.SDK_INT <= 23) {
            videoView.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if ( Build.VERSION.SDK_INT > 23) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //you should release the player created by ExoPlayerView
        videoView.releaseSelfPlayer();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){

            if(videoView.isPortrait()){
               finish();
                return false;
            }else {
                videoView.toggleControllerOrientation();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
