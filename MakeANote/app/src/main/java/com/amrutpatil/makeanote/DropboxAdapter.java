package com.amrutpatil.makeanote;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Description: Class to sync with Dropbox
 */
public class DropboxAdapter extends RecyclerView.Adapter<DropboxAdapter.RecyclerViewHolder> {


    private LayoutInflater mInflater;
    private List<String> data = Collections.emptyList();

    public DropboxAdapter(Context context, List<String> data) {
        mInflater = LayoutInflater.from(context);
        this.data = data;
    }

    //Inflate the view based on the layout
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.custom_dbx_adapter_layout, parent, false);
        return new RecyclerViewHolder(view);
    }

    //Set the text for the title
    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        //Grab the title and display it on screen. Updated automatically by the RecyclerView Adapter
        holder.title.setText(data.get(position));
    }

    public void setData(List<String> data){
        this.data = data;
    }

    //Grab the directory coming from Dropbox, store in the list and sort in alphabetical order
    public void add(String dirName){
        data.add(dirName);
        Collections.sort(data, String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        public RecyclerViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.dropbox_directory_name);
        }
    }
}
