package com.amrutpatil.friends;

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Amrut on 3/8/15.
 */
public class FriendsCustomAdapter extends ArrayAdapter<Friend> {

    private LayoutInflater mLayoutInflater;   //Custom layout for adapters
    private static FragmentManager sFragmentManager;  //for dealing with fragments

    public FriendsCustomAdapter(Context context, FragmentManager fragmentManager){
        super(context, android.R.layout.simple_list_item_2);  //set the default layout using built-in Android layouts

        //Intialize the layout inflater
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sFragmentManager = fragmentManager;
    }

    //Method to get the data from the ArrayList of friends and display it on screen

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;

        //If no view is passed, inflate layout with our custom view
        if(convertView == null){
            view = mLayoutInflater.inflate(R.layout.custom_friend, parent, false);
        } else
            view = convertView;

        //Get the actual Friend record from the ArrayList
        final Friend friend = getItem(position);

        final int _id = friend.getID();
        final String name = friend.getName();
        final String email = friend.getEmail();
        final String phone = friend.getPhone();

        ((TextView) view.findViewById(R.id.friend_name)).setText(name);
        ((TextView) view.findViewById(R.id.friend_email)).setText(email);
        ((TextView) view.findViewById(R.id.friend_phone)).setText(phone);

        Button editButton = (Button) view.findViewById(R.id.edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Clicking on this button should take us to another activity
                Intent intent = new Intent(getContext(), EditActivity.class);

                //Pass the current values to EditActivity
                intent.putExtra(FriendsContract.FriendsColumns.FRIENDS_ID, String.valueOf(_id));
                intent.putExtra(FriendsContract.FriendsColumns.FRIENDS_NAME, name);
                intent.putExtra(FriendsContract.FriendsColumns.FRIENDS_EMAIL, email);
                intent.putExtra(FriendsContract.FriendsColumns.FRIENDS_PHONE, phone);
                getContext().startActivity(intent);
            }
        });

        Button deleteButton = (Button) view.findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsDialog friendDialog = new FriendsDialog();
                Bundle args = new Bundle();
                args.putString(FriendsDialog.DIALOG_TYPE, FriendsDialog.DELETE_RECORD);
                args.putInt(FriendsContract.FriendsColumns.FRIENDS_ID, _id);
                args.putString(FriendsContract.FriendsColumns.FRIENDS_NAME, name);
                friendDialog.setArguments(args);
                friendDialog.show(sFragmentManager, "delete-record");  //TAG to reference the fragment
            }
        });

        return view;
    }

    public void setData(List<Friend> friends){
        clear();
        if(friends != null){
            for(Friend friend : friends){
                add(friend);   //Adds the record to the Adapter
            }
        }
    }
}
