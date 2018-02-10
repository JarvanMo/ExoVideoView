package com.jarvanmo.exoplayerview.media;

/**
 * Created by mo on 18-2-7.
 *
 * @author mo
 */

public class SimpleQuality implements ExoMediaSource.Quality {

    private CharSequence name;
    private String url;

    public SimpleQuality(CharSequence name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public CharSequence name() {
        return name;
    }

    @Override
    public String url() {
        return url;
    }


}
