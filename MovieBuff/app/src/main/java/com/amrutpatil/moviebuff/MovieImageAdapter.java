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
 * Created by Amrut on 7/11/15.
 */
public class MovieImageAdapter extends BaseAdapter{

    private final String LOG_TAG = MovieImageAdapter.class.getSimpleName();

    private final Context mContext;
    private final LayoutInflater mInflater;

    private final MovieInfo mLock = new MovieInfo();

    private List<MovieInfo> mObjects;

    public MovieImageAdapter(Context context, List<MovieInfo> objects){
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = objects;
    }

    public Context getContext(){
        return mContext;
    }

    public void add(MovieInfo object){
        synchronized (mLock){
            mObjects.add(object);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mLock){
            mObjects.clear();
        }
        notifyDataSetChanged();
    }

    public void setData(List<MovieInfo> data){
        clear();;
        for (MovieInfo movieInfo : data){
            add(movieInfo);
        }
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public MovieInfo getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.grid_item_movie, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        final MovieInfo movieInfo = getItem(position);

        viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.titleView.setText(movieInfo.getMovieTitle());

        String imageUri;

        if(!movieInfo.getMovieThumbnail().isEmpty()){
            imageUri = "http://image.tmdb.org/t/p/w185" + movieInfo.getMovieThumbnail();
        }else {
            imageUri = Uri.parse("android.resource://com.amrutpatil.moviebuff" +"/"+ R.drawable.film).toString();
        }

        Picasso.with(mContext).load(imageUri)
                .error(R.drawable.film)
                .placeholder(R.drawable.film)
                .into(viewHolder.imageView);

        return convertView;
    }

    public class ViewHolder{
        public final ImageView imageView;
        public final TextView titleView;

        public ViewHolder(View view){
            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
            titleView = (TextView) view.findViewById(R.id.grid_item_title);
        }
    }
}
