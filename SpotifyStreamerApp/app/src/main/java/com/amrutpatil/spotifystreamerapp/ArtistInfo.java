package com.amrutpatil.spotifystreamerapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Amrut on 6/29/15.
 *
 * Parcelable Interface: http://developer.android.com/reference/android/os/Parcelable.html
 *
 * Parcelable Example and how is it different from Serialization: http://www.3pillarglobal.com/insights/parcelable-vs-java-serialization-in-android-app-development
 */
public class ArtistInfo implements Parcelable {
    private String artistId;
    private String artistName;
    private String thumbNailUrl;

    public ArtistInfo(String artistId, String artistName, String thumbNailUrl) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.thumbNailUrl = thumbNailUrl;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getThumbNailUrl() {
        return thumbNailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //Convert Java object to Parcelable
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.artistId);
        dest.writeString(this.artistName);
        dest.writeString(this.thumbNailUrl);
    }

    private ArtistInfo(Parcel in) {
        this.artistId = in.readString();
        this.artistName= in.readString();
        this.thumbNailUrl = in.readString();
    }

    //De-serialize the Java object
    public static final Parcelable.Creator<ArtistInfo> CREATOR = new Parcelable.Creator<ArtistInfo>() {
        public ArtistInfo createFromParcel(Parcel in) {
            return new ArtistInfo(in);
        }

        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };
}
