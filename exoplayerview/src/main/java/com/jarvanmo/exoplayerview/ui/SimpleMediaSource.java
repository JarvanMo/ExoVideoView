package com.jarvanmo.exoplayerview.ui;

/**
 * Created by mo on 16-11-30.
 * this package is com.jarvanmo.exoplayerview.ui
 */

public class SimpleMediaSource {

    private String displayName;

    private String url = "";

    public SimpleMediaSource(String url){
        this.url = url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

}
