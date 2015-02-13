package com.amrutpatil.flickr_browser;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Amrut on 2/10/15.
 * This class connects the Photo class with the view using RecyclerView
 */
public class FlickrImageViewHolder extends RecyclerView.ViewHolder {

    protected ImageView thumbnail;
    protected TextView title;

    public FlickrImageViewHolder(View view) {
        super(view);
        this.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        this.title = (TextView) view.findViewById(R.id.title);
    }

}
