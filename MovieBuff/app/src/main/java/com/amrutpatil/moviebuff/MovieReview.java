package com.amrutpatil.moviebuff;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Amrut on 7/12/15.
 */
public class MovieReview {

    private String id;
    private String author;
    private String content;

    public MovieReview(){

    }

    public MovieReview(JSONObject trailer) throws JSONException {
        this.id = trailer.getString("id");
        this.author = trailer.getString("author");
        this.content = trailer.getString("content");
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

}
