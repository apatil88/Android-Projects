package com.amrutpatil.spotifystreamerapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Amrut on 6/30/15.
 */
/**
 * Track Entity.
 */
public class TrackInfo implements Parcelable {
    private String name;
    private String album;
    private String thumbnail;

    public TrackInfo(String name, String album, String thumbnail) {
        this.name = name;
        this.album = album;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.album);

        dest.writeString(this.thumbnail);
    }

    private TrackInfo(Parcel in) {
        this.name = in.readString();
        this.album = in.readString();

        this.thumbnail = in.readString();
    }

    public static final Parcelable.Creator<TrackInfo> CREATOR = new Parcelable.Creator<TrackInfo>() {
        public TrackInfo createFromParcel(Parcel in) {
            return new TrackInfo(in);
        }

        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };
}