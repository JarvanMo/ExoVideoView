package com.jarvanmo.demo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.exoplayer2.util.Util;
import com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView;
import com.jarvanmo.exoplayerview.ui.ExoVideoView;
import com.jarvanmo.exoplayerview.ui.SimpleMediaSource;
import com.jarvanmo.exoplayerview.widget.SuperAspectRatioFrameLayout;

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

        videoView = (ExoVideoView) findViewById(R.id.videoView);
        modeFit = (Button) findViewById(R.id.mode_fit);
        modeNone = (Button) findViewById(R.id.mode_none);
        modeHeight = (Button) findViewById(R.id.mode_height);
        modeWidth = (Button) findViewById(R.id.mode_width);
        wrapper = findViewById(R.id.wrapper);



        videoView.setBackListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public void onClick(View view, boolean isPortrait) {
                if (isPortrait) {
                    finish();
                } else {
                    changeToPortrait();
                }
            }
        });

//
//        videoView.setOrientationListener(new ExoVideoView.OrientationListener() {
//            @Override
//            public void onChange(boolean isPortrait) {
//                Toast.makeText(getApplicationContext(),"--"+isPortrait,Toast.LENGTH_SHORT).show();
//                if (isPortrait) {
//                    changeToPortrait();
//                } else {
//                    changeToLandscape();
//                }
//            }
//        });

        videoView.setFullScreenListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public void onClick(View view, boolean isPortrait) {
                if (!isPortrait) {
                    changeToPortrait();
                } else {
                    changeToLandscape();
                }
            }
        });


        videoView.setPortrait(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        SimpleMediaSource mediaSource = new SimpleMediaSource("http://video19.ifeng.com/video07/2013/11/11/281708-102-007-1138.mp4");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_example_v2/master.m3u8");
//        SimpleMediaSource mediaSource = new SimpleMediaSource("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource(" https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8");
//       SimpleMediaSource mediaSource = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_fmp4_example/master.m3u8");


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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        videoView.setPortrait(true);
        wrapper.setVisibility(View.VISIBLE);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//            }
//        },1000);

    }


    private void changeToLandscape() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        videoView.setPortrait(false);
        wrapper.setVisibility(View.GONE);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//            }
//        },1000);
    }



    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            videoView.resume();
//            videoView.initSelfPlayer(simpleMediaSource);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23)) {
            videoView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
//            videoView.releaseSelfPlayer();
            videoView.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //you should release the player created by ExoPlayerView
        videoView.releaseSelfPlayer();
    }
}
