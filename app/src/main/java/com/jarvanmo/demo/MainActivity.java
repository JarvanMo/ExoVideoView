package com.jarvanmo.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.android.exoplayer2.util.Util;
import com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView;
import com.jarvanmo.exoplayerview.ui.ExoVideoView;
import com.jarvanmo.exoplayerview.ui.SimpleMediaSource;
import com.jarvanmo.exoplayerview.widget.SuperAspectRatioFrameLayout;

public class MainActivity extends AppCompatActivity {


    private ExoVideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = (ExoVideoView) findViewById(R.id.videoView);

        videoView.setBackListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public void onClick(View view, boolean isPortrait) {
                if (isPortrait) {
                    finish();
                } else {
                    videoView.changeOrientation();
                }
            }
        });

//        videoView.addViewToControllerWhenLandscape(view);


        videoView.setFullScreenListener(new ExoVideoPlaybackControlView.ExoClickListener() {
            @Override
            public void onClick(View view, boolean isPortrait) {
                videoView.changeOrientation();
            }
        });




        videoView.setPortrait(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        SimpleMediaSource mediaSource = new SimpleMediaSource("http://video19.ifeng.com/video07/2013/11/11/281708-102-007-1138.mp4");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_example_v2/master.m3u8");
//        SimpleMediaSource mediaSource = new SimpleMediaSource("http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8");
//        SimpleMediaSource mediaSource  = new SimpleMediaSource(" https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear0/prog_index.m3u8");
//       SimpleMediaSource mediaSource = new SimpleMediaSource("https://tungsten.aaplimg.com/VOD/bipbop_adv_fmp4_example/master.m3u8");


        mediaSource.setDisplayName("LuYu YouYue");
        videoView.play(mediaSource);
        findViewById(R.id.mode_fit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
        });

        findViewById(R.id.mode_none).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_NONE);
            }
        });
        findViewById(R.id.mode_height).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
            }
        });

        findViewById(R.id.mode_width).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.setResizeMode(SuperAspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH );
            }
        });
//        videoView.play(mediaSource,1000 * 15); // play from a particular position(ms)...
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        videoView.changeOrientation();
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
