package com.amrutpatil.friends;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amrut on 3/5/15.
 * This class ensures cursor operations are done in the background and do not stop the present UI thread from executing.
 * Retrieving the records from the database happens in the background.
 */
public class FriendsSearchListLoader extends AsyncTaskLoader<List<Friend>> {

    private static final String LOG_TAG = FriendsSearchListLoader.class.getSimpleName();

    private List<Friend> mFriends;
    private ContentResolver mContentResolver;  //To access the Content Provider
    private Cursor mCursor;  //To access the data
    private String mFilterText;

    public FriendsSearchListLoader(Context context, Uri uri, ContentResolver contentResolver, String filterText){
        super(context);
        mContentResolver = contentResolver;
        mFilterText = filterText;
    }

    @Override
    public List<Friend> loadInBackground() {
        //Columns to work with
        String[] projection = {BaseColumns._ID,
                FriendsContract.FriendsColumns.FRIENDS_NAME,
                FriendsContract.FriendsColumns.FRIENDS_PHONE,
                FriendsContract.FriendsColumns.FRIENDS_EMAIL};

        List<Friend> entries = new ArrayList<Friend>();

        //Matching search text entered
        String selection = FriendsContract.FriendsColumns.FRIENDS_NAME + " LIKE '" + mFilterText + "%'";

        mCursor = mContentResolver.query(FriendsContract.URI_TABLES, projection, selection, null, null);
        if(mCursor != null){
            if(mCursor.moveToFirst()){
                do{
                    //Retrieve records from the database by the Content Provider
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    String name = mCursor.getString(mCursor.getColumnIndex(FriendsContract.FriendsColumns.FRIENDS_NAME));
                    String phone = mCursor.getString(mCursor.getColumnIndex(FriendsContract.FriendsColumns.FRIENDS_PHONE));
                    String email = mCursor.getString(mCursor.getColumnIndex(FriendsContract.FriendsColumns.FRIENDS_EMAIL));
                    Friend friend = new Friend(_id, name, phone, email);
                    entries.add(friend);
                }while (mCursor.moveToNext());
            }
        }
        return entries;
    }

    //Deliver data to the calling process
    @Override
    public void deliverResult(List<Friend> friends) {
        if(isReset()){
            if(friends != null){
                //While we were processing, we got another request
                mCursor.close();  //let the other process finish and continue what is was doing
            }
        }

        List<Friend> oldFriendList = mFriends;
        if(mFriends == null || mFriends.size() == 0){
            Log.d(LOG_TAG, "+++++++ No Data Returned +++++++");
        }

        //Deliver the data back to the calling process
        mFriends = friends;
        if(isStarted()){   //Check if data loading has started
            super.deliverResult(friends);
        }

        //If old friend list is different than the one passed
        if(oldFriendList != null || oldFriendList != friends){
            mCursor.close();
        }
    }

    @Override
    protected void onStartLoading() {
        if(mFriends != null){
            deliverResult(mFriends);
        }

        //if the data is changed when the loader was stopped
        if(takeContentChanged() || mFriends == null){
            forceLoad();  //reload data from the Content Provider
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if(mCursor != null){
            mCursor.close();
        }

        mFriends = null;
    }

    @Override
    public void onCanceled(List<Friend> friends) {
        super.onCanceled(friends);
        if(mCursor != null){
            mCursor.close();
        }
    }

    @Override
    public void forceLoad() {
       super.forceLoad();
    }
}
