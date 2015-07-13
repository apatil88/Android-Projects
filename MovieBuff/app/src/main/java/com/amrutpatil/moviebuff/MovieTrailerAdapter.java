package com.amrutpatil.moviebuff;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Amrut on 7/12/15.
 */
public class MovieTrailerAdapter extends BaseAdapter {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final MovieTrailer mLock = new MovieTrailer();

    private List<MovieTrailer> mObjects;

    public MovieTrailerAdapter(Context context, List<MovieTrailer> objects) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = objects;
    }

    public Context getContext() {
        return mContext;
    }

    public void add(MovieTrailer object) {
        synchronized (mLock) {
            mObjects.add(object);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock) {
            mObjects.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public MovieTrailer getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            view = mInflater.inflate(R.layout.item_movie_trailer, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        final MovieTrailer trailer = getItem(position);

        viewHolder = (ViewHolder) view.getTag();

        viewHolder.nameView.setText(trailer.getName());

        String youtube_thumbnail_url;

        if(!trailer.getSite().isEmpty()){
            youtube_thumbnail_url = "http://img.youtube.com/vi/" + trailer.getKey() + "/0.jpg";
        } else {
            youtube_thumbnail_url = Uri.parse("android.resource://com.amrutpatil.moviebuff" + "/" + R.drawable.photo).toString();
        }

        Picasso.with(mContext).load(youtube_thumbnail_url)
                .error(R.drawable.photo)
                .placeholder(R.drawable.photo)
                .into(viewHolder.imageView);

        return view;
    }

    public static class ViewHolder {
        public final ImageView imageView;
        public final TextView nameView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.trailer_image);
            nameView = (TextView) view.findViewById(R.id.trailer_name);
        }
    }

}
