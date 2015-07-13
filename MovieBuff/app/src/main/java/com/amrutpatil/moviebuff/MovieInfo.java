package com.amrutpatil.moviebuff;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Amrut on 7/11/15.
 */
public class MovieInfo implements Parcelable {

    private final String LOG_TAG = MovieInfo.class.getSimpleName();
    private int movieId;
    private String movieTitle;
    private String movieThumbnail;
    private String moviePlotSynopsis;
    private int movieUserRating;
    private String movieReleaseDate;
    private String image2;

    public String getImage2() {
        return image2;
    }

    public MovieInfo(){

    }

    public MovieInfo(JSONObject movie) throws JSONException{
        this.movieId = movie.getInt("id");
        this.movieTitle = movie.getString("original_title");
        this.movieThumbnail = movie.getString("poster_path");
        this.moviePlotSynopsis = movie.getString("overview");
        this.movieUserRating = movie.getInt("vote_average");
        this.movieReleaseDate = movie.getString("release_date");
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieThumbnail() {
        return movieThumbnail;
    }

    public void setMovieThumbnail(String movieThumbnail) {
        this.movieThumbnail = movieThumbnail;
    }

    public String getMoviePlotSynopsis() {
        return moviePlotSynopsis;
    }

    public void setMoviePlotSynopsis(String moviePlotSynopsis) {
        this.moviePlotSynopsis = moviePlotSynopsis;
    }

    public int getMovieUserRating() {
        return movieUserRating;
    }

    public void setMovieUserRating(int movieUserRating) {
        this.movieUserRating = movieUserRating;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(String movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeString(movieTitle);
        dest.writeString(movieThumbnail);
        dest.writeString(moviePlotSynopsis);
        dest.writeInt(movieUserRating);
        dest.writeString(movieReleaseDate);
    }

    public static final Parcelable.Creator<MovieInfo> CREATOR = new Parcelable.Creator<MovieInfo>(){
        public MovieInfo createFromParcel(Parcel in) {
            return new MovieInfo(in);
        }

        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };

    private MovieInfo (Parcel in){
        movieId = in.readInt();
        movieTitle = in.readString();
        movieThumbnail = in.readString();
        moviePlotSynopsis = in.readString();
        movieUserRating = in.readInt();
        movieReleaseDate = in.readString();
    }
}
