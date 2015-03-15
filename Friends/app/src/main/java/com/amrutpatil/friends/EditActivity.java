package com.amrutpatil.friends;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Amrut on 3/14/15.
 */
public class EditActivity extends FragmentActivity{
    private static final String LOG_TAG = EditActivity.class.getSimpleName();

    private TextView mNameTextView, mEmailTextView, mPhoneTextView;
    private Button mButton;
    private ContentResolver mContentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mNameTextView = (TextView) findViewById(R.id.friendName);
        mEmailTextView = (TextView) findViewById(R.id.friendEmail);
        mPhoneTextView = (TextView) findViewById(R.id.friendPhone);

        mContentResolver = EditActivity.this.getContentResolver();

        //Retrieve the arguments pass to this activity by FriendsCustomAdapter
        Intent intent = getIntent();
        final String _id = intent.getStringExtra(FriendsContract.FriendsColumns.FRIENDS_ID);
        String name = intent.getStringExtra(FriendsContract.FriendsColumns.FRIENDS_NAME);
        String email = intent.getStringExtra(FriendsContract.FriendsColumns.FRIENDS_EMAIL);
        String phone = intent.getStringExtra(FriendsContract.FriendsColumns.FRIENDS_PHONE);

        //Display values coming out from database in TextView in this activity which we can edit later
        mNameTextView.setText(name);
        mEmailTextView.setText(email);
        mPhoneTextView.setText(phone);

        mButton = (Button) findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    ContentValues values = new ContentValues();  //Send the values to the ContentProvider via ContentResolver
                    values.put(FriendsContract.FriendsColumns.FRIENDS_NAME, mNameTextView.getText().toString());
                    values.put(FriendsContract.FriendsColumns.FRIENDS_EMAIL, mEmailTextView.getText().toString());
                    values.put(FriendsContract.FriendsColumns.FRIENDS_PHONE, mPhoneTextView.getText().toString());

                    Uri uri = FriendsContract.Friends.buildFriendUri(_id); //Pass id of the record which ContentProvider needs to update which gets send to the database
                    int recordsUpdated = mContentResolver.update(uri, values, null, null);
                    Log.d(LOG_TAG, "Number of records updated: " + recordsUpdated);  //Ideally should be 1 since we specified just one _id of the record in uri.
                    Intent intent = new Intent(EditActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
            }
        });
    }

    //If someone presses home, go back to the main activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return true;
    }
}
