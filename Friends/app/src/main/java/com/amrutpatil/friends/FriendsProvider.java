package com.amrutpatil.friends;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Amrut on 2/25/15.
 */
public class FriendsProvider extends ContentProvider {

    private FriendsDatabase mOpenHelper;

    private static final String TAG = FriendsProvider.class.getSimpleName();

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int FRIENDS = 100;  //Access all Friends
    private static final int FRIENDS_ID = 101;   //Access individual Friend record

    private static UriMatcher buildUriMatcher(){

        //intialize the UriMatcher
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        //List of valid Uri's in the ContentProvider
        final String authority = FriendsContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "friends", FRIENDS);
        matcher.addURI(authority, "friends/*", FRIENDS_ID);
        return  matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new FriendsDatabase(getContext());   //get access to the database
        return true;
    }

    private void deleteDatabase(){
        mOpenHelper.close();
        FriendsDatabase.deleteDatabase(getContext());
        mOpenHelper = new FriendsDatabase(getContext());
    }

    //Allows calling process to check if it is a valid Uri
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case FRIENDS:
                return FriendsContract.Friends.CONTENT_TYPE;
            case FRIENDS_ID:
                return FriendsContract.Friends.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }
    }


    //Method to retrieve records from the database
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //Get access to the database
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        //Check which uri was passed
        final int match = sUriMatcher.match(uri);

        //Set the tables in the database
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(FriendsDatabase.Tables.FRIENDS);

        switch (match){
            case FRIENDS:
                //do nothing
                break;
            case FRIENDS_ID:
                //if the id is passed in the uri
                String id = FriendsContract.Friends.getFriendId(uri);  //Extract Friend ID from the incoming uri
                sqLiteQueryBuilder.appendWhere(BaseColumns._ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }

        //Create a cursor and return it to the calling process
        Cursor cursor = sqLiteQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    //Method to insert a record into the database
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(TAG, "insert uri=( " + uri + ", values= " + values.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match){
            case FRIENDS:
                //create a record and return the id of the record
                long recordId = db.insertOrThrow(FriendsDatabase.Tables.FRIENDS, null, values);
                //return the uri of the record
                return FriendsContract.Friends.buildFriendUri(String.valueOf(recordId));

            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }

    }

    //Method to update a record in the database
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(TAG, "update uri=( " + uri + ", values= " + values.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        String selectionCriteria = selection;

        switch (match){
            case FRIENDS:
                //Nothing to do
                break;
            case FRIENDS_ID:
                String id = FriendsContract.Friends.getFriendId(uri);
                selectionCriteria = BaseColumns._ID + " = " + id
                        + (!TextUtils.isEmpty(selection) ? "AND ( " + selection + " ) " :"");
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);

        }

        //Return the number of records updated
        return db.update(FriendsDatabase.Tables.FRIENDS,values,selectionCriteria,selectionArgs);
    }

    //Method to delete database or a single record from the database

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "delete uri=( " + uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        //Delete the database if ID is not passed
        if(uri.equals(FriendsContract.URI_TABLES)){
            deleteDatabase();
            return 0;
        }
        switch (match){
            //Delete particular record based on the ID passed in the uri
            case FRIENDS_ID:
                String id = FriendsContract.Friends.getFriendId(uri);
                String selectionCriteria = BaseColumns._ID + " = " + id
                        + (!TextUtils.isEmpty(selection) ? "AND ( " + selection + " ) " :"");
                return db.delete(FriendsDatabase.Tables.FRIENDS, selectionCriteria, selectionArgs);  //return number of records deleted
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }
    }
}
