package com.jarvanmo.exoplayerview.media;

import android.net.Uri;

import java.util.List;

/**
 * Created by mo on 18-1-11.
 * 剑气纵横三万里 一剑光寒十九洲
 */

public interface ExoMediaSource {

    interface Quality {
        CharSequence getDisplayName();

        Uri getUri();

        void setUri(Uri uri);

        void setDisplayName(CharSequence displayName);

        void setQuality(String quality);

        String getQuality();

    }

    Uri uri();

    String name();

    List<Quality> qualities();

    String extension();
}
