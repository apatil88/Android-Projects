package com.amrutpatil.moviebuff;

/**
 * Created by Amrut on 7/12/15.
 */
public class Utility {
    public static String buildImageUrl(int width, String fileName) {
        return "http://image.tmdb.org/t/p/w" + Integer.toString(width) + fileName;
    }
}
