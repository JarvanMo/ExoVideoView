package com.jarvanmo.exoplayerview.media;

import android.net.Uri;

/**
 * Created by mo on 18-2-7.
 *
 * @author mo
 */

public class SimpleQuality implements ExoMediaSource.Quality {

    private CharSequence name;
    private Uri uri;

    public SimpleQuality(CharSequence name, Uri uri) {
        this.name = name;
        this.uri = uri;
    }

    @Override
    public CharSequence name() {
        return name;
    }

    @Override
    public Uri uri() {
        return uri;
    }


}
