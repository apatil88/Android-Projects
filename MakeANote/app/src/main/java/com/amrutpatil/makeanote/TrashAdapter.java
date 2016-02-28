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
 * Created by Amrut on 2/28/16.
 */
public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.NoteHolder> {
    private LayoutInflater mInflater;
    private List<Trash> mData = Collections.emptyList();

    public TrashAdapter(Context context, List<Trash> data) {
        mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public TrashAdapter.NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.custom_trash_archive_adapter_layout, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteHolder holder, int position) {
        holder.id.setText(mData.get(position).getId()+"");
        holder.title.setText(mData.get(position).getTitle());
        holder.description.setText(mData.get(position).getDescription());
        holder.date.setText(mData.get(position).getDateTime());
    }

    public void setData(List<Trash> data) {
        this.mData = data;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void delete(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public class NoteHolder extends RecyclerView.ViewHolder {

        TextView title, description, date,id;

        public NoteHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.id_note_custom_home);
            title = (TextView) itemView.findViewById(R.id.title_note_custom_home);
            description = (TextView) itemView.findViewById(R.id.description_note_custom_home);
            date = (TextView) itemView.findViewById(R.id.date_time_note_custom_home);
        }
    }
}
