package com.amrutpatil.spotifystreamerapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Amrut on 6/29/15.
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

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getThumbNailUrl() {
        return thumbNailUrl;
    }

    public void setThumbNailUrl(String thumbNailUrl) {
        this.thumbNailUrl = thumbNailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

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

    public static final Parcelable.Creator<ArtistInfo> CREATOR = new Parcelable.Creator<ArtistInfo>() {
        public ArtistInfo createFromParcel(Parcel in) {
            return new ArtistInfo(in);
        }

        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };
}
