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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Amrut on 3/8/15.
 * This class displays pop up messages for warnings or confirmation from the user.
 */
public class FriendsDialog extends DialogFragment {
    private LayoutInflater mLayoutInflater;

    public static final String DIALOG_TYPE = "command";  //identifier to tell this class if the command is valid
    public static final String DELETE_RECORD = "deleteRecord";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mLayoutInflater = getActivity().getLayoutInflater();
        final View view = mLayoutInflater.inflate(R.layout.friends_dialog, null);

        String command = getArguments().getString(DIALOG_TYPE);
        if(command.equals(DELETE_RECORD)){
            final int _id = getArguments().getInt(FriendsContract.FriendsColumns.FRIENDS_ID);
            String name = getArguments().getString(FriendsContract.FriendsColumns.FRIENDS_NAME);

            //Create confirmation dialog to delete a record
            TextView popUpMessage = (TextView) view.findViewById(R.id.popup_message);
            popUpMessage.setText("Are you sure you want to delete " + name + " from your friend list?");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Invoke the Content Provider to delete the record
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    Uri uri = FriendsContract.Friends.buildFriendUri(String.valueOf(_id));  //Pass the record id as the parameter via the uri
                    contentResolver.delete(uri, null, null);
                    //Return to the Main Activity
                    Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   //If any other activity is open, close it and place the current activity at the top.
                    startActivity(intent);
                }
            });
        }
        return builder.create();
    }
}
