package com.jarvanmo.exoplayerview.media;

import java.util.List;

/**
 * Created by mo on 16-11-30.
 * this package is com.jarvanmo.exoplayerview.ui
 */

public class SimpleMediaSource implements ExoMediaSource {

    private String displayName;

    private String url = "";

    private List<Quality> qualities;

    public SimpleMediaSource(String url) {
        this.url = url;
    }

    @Override
    public String name() {
        return displayName;
    }

    @Override
    public List<Quality> qualities() {
        return qualities;
    }

    @Override
    public String extension() {
        return null;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String url() {
        return url;
    }

    public void setQualities(List<Quality> qualities) {
        this.qualities = qualities;
    }

}
