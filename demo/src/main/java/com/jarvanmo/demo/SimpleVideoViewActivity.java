package com.jarvanmo.demo;

import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.jarvanmo.exoplayerview.extension.MultiQualitySelectorAdapter;
import com.jarvanmo.exoplayerview.media.ExoMediaSource;
import com.jarvanmo.exoplayerview.media.SimpleMediaSource;
import com.jarvanmo.exoplayerview.media.SimpleQuality;
import com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView;
import com.jarvanmo.exoplayerview.ui.ExoVideoView;

import java.util.ArrayList;
import java.util.List;

import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_LANDSCAPE;
import static com.jarvanmo.exoplayerview.orientation.OnOrientationChangedListener.SENSOR_PORTRAIT;

public class SimpleVideoViewActivity extends AppCompatActivity {

    private ExoVideoView videoView;
    private View wrapper;
    private final String[] modes = new String[]{"RESIZE_MODE_FIT", "RESIZE_MODE_FIXED_WIDTH"
            , "RESIZE_MODE_FIXED_HEIGHT", "RESIZE_MODE_FILL", "RESIZE_MODE_ZOOM"};
    private Spinner modeSpinner;
    private ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_video_view);
        wrapper = findViewById(R.id.wrapper);

        initSpinner();
        initControllerMode();
        initVideoView();
        initCustomViews();
    }

    private void initVideoView() {
        videoView = findViewById(R.id.videoView);
        videoView.setPortrait(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        videoView.setBackListener((view, isPortrait) -> {
            if (isPortrait) {
                finish();
            }
            return false;
        });

        videoView.setOrientationListener(orientation -> {
            if (orientation == SENSOR_PORTRAIT) {
                changeToPortrait();
            } else if (orientation == SENSOR_LANDSCAPE) {
                changeToLandscape();
            }
        });

//        videoView.setGestureEnabled(false);
//
//
        SimpleMediaSource mediaSource = new SimpleMediaSource("http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4");
//        mediaSource.setDisplayName("Apple HLS");

//        SimpleMediaSource mediaSource = new SimpleMediaSource("file:///storage/emulated/0/Download/喜欢你.mp4");
        mediaSource.setDisplayName("Apple HLS");

        //demo only,not real multi quality, urls are the same actually
        List<ExoMediaSource.Quality> qualities = new ArrayList<>();
        ExoMediaSource.Quality quality;
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.YELLOW);
        SpannableString spannableString = new SpannableString("1080p");
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        quality = new SimpleQuality(spannableString, mediaSource.uri());
        qualities.add(quality);

        spannableString = new SpannableString("720p");
        colorSpan = new ForegroundColorSpan(Color.LTGRAY);
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        quality = new SimpleQuality(spannableString, mediaSource.uri());
        qualities.add(quality);

        mediaSource.setQualities(qualities);
//        videoView.changeWidgetVisibility(R.id.exo_player_controller_back,View.INVISIBLE);
        videoView.setMultiQualitySelectorNavigator(new MultiQualitySelectorAdapter.MultiQualitySelectorNavigator() {
            @Override
            public boolean onQualitySelected(ExoMediaSource.Quality quality) {
                quality.setUri(Uri.parse("https://media.w3.org/2010/05/sintel/trailer.mp4"));
                return false;
            }
        });
        videoView.play(mediaSource, false);

    }

    private void initSpinner() {
        modeSpinner = findViewById(R.id.spinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                videoView.setResizeMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        adapter.addAll(modes);
        modeSpinner.setAdapter(adapter);
    }

    private void initControllerMode() {
        CheckBox all = findViewById(R.id.all);
        CheckBox top = findViewById(R.id.top);
        CheckBox topLandscape = findViewById(R.id.topLandscape);
        CheckBox bottom = findViewById(R.id.bottom);
        CheckBox bottomLandscape = findViewById(R.id.bottomLandscape);
        CheckBox none = findViewById(R.id.none);
        findViewById(R.id.applyControllerMode).setOnClickListener(v -> {
            int mode = ExoVideoPlaybackControlView.CONTROLLER_MODE_NONE;
            if (all.isChecked()) {
                mode |= ExoVideoPlaybackControlView.CONTROLLER_MODE_ALL;
            }
            if (top.isChecked()) {
                mode |= ExoVideoPlaybackControlView.CONTROLLER_MODE_TOP;
            }

            if (topLandscape.isChecked()) {
                mode |= ExoVideoPlaybackControlView.CONTROLLER_MODE_TOP_LANDSCAPE;
            }
            if (bottom.isChecked()) {
                mode |= ExoVideoPlaybackControlView.CONTROLLER_MODE_BOTTOM;
            }
            if (bottomLandscape.isChecked()) {
                mode |= ExoVideoPlaybackControlView.CONTROLLER_MODE_BOTTOM_LANDSCAPE;
            }
            if (none.isChecked()) {
                mode |= ExoVideoPlaybackControlView.CONTROLLER_MODE_NONE;
            }

            videoView.setControllerDisplayMode(mode);
            Toast.makeText(SimpleVideoViewActivity.this, "change controller display mode", Toast.LENGTH_SHORT).show();
        });


    }

    private void initCustomViews() {
        findViewById(R.id.addToTop).setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.cutom_view_top, null, false);
            videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_TOP, view);
        });

        findViewById(R.id.addToTopLandscape).setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.cutom_view_top_landscape, null, false);
            videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_TOP_LANDSCAPE, view);
        });


        findViewById(R.id.addToBottomLandscape).setOnClickListener(v -> {
            View view = getLayoutInflater().inflate(R.layout.cutom_view_bottom_landscape, null, false);
            videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_BOTTOM_LANDSCAPE, view);
        });
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

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return videoView.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
}
