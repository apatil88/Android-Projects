package com.amrutpatil.makeanote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Amrut on 2/27/16.
 * Description: Class that inflates views based on items selected in the Navigation Drawer.
 */
public class NavigationDrawerAdapter extends BaseAdapter {
    private List<NavigationDrawerItem> mDrawerItems;
    private LayoutInflater mLayoutInflater;

    public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> drawerItems) {
        super();
        mDrawerItems = drawerItems;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        //Not used in this application
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = mLayoutInflater.inflate(R.layout.custom_navigation_drawer, null);
        NavigationDrawerItem navigationDrawerItem = mDrawerItems.get(position);

        TextView textView = (TextView) convertView.findViewById(R.id.navigation_item_title);
        textView.setText(navigationDrawerItem.getTitle());

        ImageView icon = (ImageView) convertView.findViewById(R.id.navigation_item_icon);
        icon.setImageResource(navigationDrawerItem.getIconId());

        return convertView;

    }
}
