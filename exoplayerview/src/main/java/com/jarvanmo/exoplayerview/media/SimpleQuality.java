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
    private String quality;

    public SimpleQuality(CharSequence name, Uri uri) {
        this.name = name;
        this.uri = uri;
    }

    @Override
    public CharSequence getDisplayName() {
        return name;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @Override
    public void setDisplayName(CharSequence name) {
        this.name = name;
    }

    @Override
    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Override
    public String getQuality() {
        return quality;
    }


}
