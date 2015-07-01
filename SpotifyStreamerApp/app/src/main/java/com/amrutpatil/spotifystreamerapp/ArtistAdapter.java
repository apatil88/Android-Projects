package com.amrutpatil.spotifystreamerapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Amrut on 6/29/15.
 */
public class ArtistAdapter extends ArrayAdapter<ArtistInfo> {
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String ARTIST_ID = "id";

    private Context context;

    public ArtistAdapter(Context context, int resource, int textViewResourceId, List<ArtistInfo> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);
        ArtistInfo artist = getItem(position);
        final String artistId = artist.getArtistId();

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.artist_list_item, parent, false);
        }

        TextView artistName = (TextView) convertView.findViewById(R.id.artistName);
        artistName.setText(artist.getArtistName());

        ImageView imgThumbnail = (ImageView) convertView.findViewById(R.id.artistThumbnail);

        String imgUri;

        if (!artist.getThumbNailUrl().isEmpty()) {
            imgUri = artist.getThumbNailUrl();
        } else {
            imgUri = Uri.parse("android.resource://com.amrutpatil.com/drawable/spotify_1").toString();
        }

        Picasso.with(context).load(imgUri)        //load the thumbnail
                .error(R.drawable.spotify_1)      //if no thumnail is present, display the spotify_1.png image as a placeholder
                .placeholder(R.drawable.spotify_1)  //set the placeholder image
                .into(imgThumbnail);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Clicked");
                if (getContext() instanceof FragmentActivity) {
                    Intent intent = new Intent(context, TopTracksActivity.class);
                    intent.putExtra(ARTIST_ID, artistId);
                    context.startActivity(intent);
                }
            }
        });

        return convertView;
    }
}
