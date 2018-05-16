package com.jarvanmo.exoplayerview.media;

import android.net.Uri;

import java.util.List;

/**
 * Created by mo on 18-1-11.
 * 剑气纵横三万里 一剑光寒十九洲
 */

public interface ExoMediaSource {

    interface Quality {
        CharSequence name();

        Uri uri();
    }

    Uri uri();

    String name();

    List<Quality> qualities();

    String extension();
}
