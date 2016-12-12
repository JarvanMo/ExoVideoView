package com.jarvanmo.exoplayerview.ui;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mo on 16-11-30.
 * this package is com.jarvanmo.exoplayerview.ui
 */

public class SimpleMediaSource implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeString(this.url);
    }

    protected SimpleMediaSource(Parcel in) {
        this.displayName = in.readString();
        this.url = in.readString();
    }

    public static final Parcelable.Creator<SimpleMediaSource> CREATOR = new Parcelable.Creator<SimpleMediaSource>() {
        @Override
        public SimpleMediaSource createFromParcel(Parcel source) {
            return new SimpleMediaSource(source);
        }

        @Override
        public SimpleMediaSource[] newArray(int size) {
            return new SimpleMediaSource[size];
        }
    };
}
