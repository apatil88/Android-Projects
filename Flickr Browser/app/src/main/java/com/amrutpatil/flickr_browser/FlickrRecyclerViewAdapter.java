package com.amrutpatil.flickr_browser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Amrut on 2/10/15.
 *
 * Adapter stores and manipulates the data. The View constantly interacts with the Adapter to update itself.
 */
public class FlickrRecyclerViewAdapter extends RecyclerView.Adapter<FlickrImageViewHolder> {

    private List<Photo> mPhotosList;
    private Context mContext;
    private final String LOG_TAG = FlickrRecyclerViewAdapter.class.getSimpleName();

    public FlickrRecyclerViewAdapter(Context context, List<Photo> photosList) {
        mContext = context;
        this.mPhotosList = photosList;
    }

    @Override
    public FlickrImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Inflate the Layout i.e. create the object, get the data from the object, and put that in ImageView Holder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse,null);
        FlickrImageViewHolder flickrImageViewHolder = new FlickrImageViewHolder(view);
        return flickrImageViewHolder;

    }

    @Override
    public int getItemCount() {
        return (null != mPhotosList ? mPhotosList.size() : 0);  //returns the size of the mPhotosList if the mPhotosList is not empty
    }

    @Override
    public void onBindViewHolder(FlickrImageViewHolder flickrImageViewHolder, int position) {

        //position gives the index of the photo element and title on the View that is being drawn. We use this index to get that particular photo from the Photos ArrayList
        Photo photoItem = mPhotosList.get(position);

        Log.d(LOG_TAG,"Processing: "+photoItem.getmTitle() + "-->" + Integer.toString(position));

        //Draw the thumbnail
        Picasso.with(mContext).load(photoItem.getmImage())    //load the photo
                .error(R.drawable.placeholder)               //if there is an error, it will display the placeholder.png image
                .placeholder(R.drawable.placeholder)         //set the placeholder image
                .into(flickrImageViewHolder.thumbnail);      //Adapter places the thumbnail in FlickrImageViewHolder

        flickrImageViewHolder.title.setText(photoItem.getmTitle());  //Set the photo title
    }
}
