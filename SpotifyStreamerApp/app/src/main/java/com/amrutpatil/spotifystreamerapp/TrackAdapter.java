package com.amrutpatil.spotifystreamerapp;

/**
 * Created by Amrut on 6/30/15.
 */

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter to display a top track in the list view.
 */
public class TrackAdapter extends ArrayAdapter<TrackInfo> {
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private Context context;

    public TrackAdapter(Context context, int resource, int textViewResourceId, List<TrackInfo> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);
        TrackInfo track = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.top_tracks_list_item, parent, false);
        }

        TextView trackName = (TextView) convertView.findViewById(R.id.row_track_tv_name);
        trackName.setText(track.getName());

        TextView trackAlbum = (TextView) convertView.findViewById(R.id.row_track_tv_album);
        trackAlbum.setText(track.getAlbum());

        ImageView albumThumbnail = (ImageView) convertView.findViewById(R.id.row_album_iv_thumbnail);

        String thumbnailUri;

        if (!track.getThumbnail().isEmpty()) {
            thumbnailUri = track.getThumbnail();
        } else {
            thumbnailUri = Uri.parse("android.resource://me.habel.spotifystreamer/drawable/logo").toString();
        }

        Picasso.with(context).load(thumbnailUri)        //load the thumbnail
                .error(R.drawable.spotify_1)            //if no thumnail is present, display the spotify_1.png image as a placeholder
                .placeholder(R.drawable.spotify_1)      //set the placeholder image
                .into(albumThumbnail);

        return convertView;
    }
}