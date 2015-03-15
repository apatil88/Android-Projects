package com.amrutpatil.friends;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Amrut on 3/8/15.
 * This class displays pop up messages for warnings or confirmation from the user.
 */
public class FriendsDialog extends DialogFragment {
    private static final String LOG_TAG = FriendsDialog.class.getSimpleName();
    private LayoutInflater mLayoutInflater;

    public static final String DIALOG_TYPE = "command";  //identifier to tell this class if the command is valid
    public static final String DELETE_RECORD = "deleteRecord";
    public static final String DELETE_DATABASE = "deleteDatabase";
    public static final String CONFIRM_EXIT = "confirmExit";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mLayoutInflater = getActivity().getLayoutInflater();
        final View view = mLayoutInflater.inflate(R.layout.friends_dialog, null);

        String command = getArguments().getString(DIALOG_TYPE);
        if (command.equals(DELETE_RECORD)) {
            final int _id = getArguments().getInt(FriendsContract.FriendsColumns.FRIENDS_ID);
            String name = getArguments().getString(FriendsContract.FriendsColumns.FRIENDS_NAME);

            //Create confirmation dialog to delete a record
            TextView popUpMessage = (TextView) view.findViewById(R.id.popup_message);
            popUpMessage.setText("Are you sure you want to delete " + name + " from your friends list?");

            builder.setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getActivity(), "Deleting Record", Toast.LENGTH_LONG).show();
                    //Invoke the Content Provider to delete the record
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    Uri uri = FriendsContract.Friends.buildFriendUri(String.valueOf(_id));  //Pass the record id as the parameter via the uri
                    contentResolver.delete(uri, null, null);
                    //Return to the Main Activity
                    Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   //If any other activity is open, close it and place the current activity at the top.
                    startActivity(intent);
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        } else if (command.equals(DELETE_DATABASE)) {
            //Create confirmation dialog to delete the database
            TextView popUpMessage = (TextView) view.findViewById(R.id.popup_message);
            popUpMessage.setText("Are you sure you wish to delete the entire database?");

            builder.setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Invoke the Content Provider to delete the record
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    Uri uri = FriendsContract.URI_TABLES;  //Pass the record id as the parameter via the uri
                    contentResolver.delete(uri, null, null);
                    //Return to the Main Activity
                    Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   //If any other activity is open, close it and place the current activity at the top.
                    startActivity(intent);
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        } else if (command.equals(CONFIRM_EXIT)) {
            TextView popUpMessage = (TextView) view.findViewById(R.id.popup_message);
            popUpMessage.setText("Are you sure you wish to exit without saving?");

            builder.setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().finish();
                }
            })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        } else {
            Log.d(LOG_TAG, "Invalid command passed as parameter");
        }
        return builder.create();
    }
}
