package com.amrutpatil.friends;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Amrut on 2/25/15.
 */
public class FriendsContract {

    //Define columns in the table
    interface FriendsColumns{
        String FRIENDS_ID = "_id";
        String FRIENDS_NAME = "friends_name";
        String FRIENDS_EMAIL = "friends_email";
        String FRIENDS_PHONE = "friends_phone";
    }

    public static final String CONTENT_AUTHORITY= "com.amrutpatil.friends.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_FRIENDS = "friends";
    public static final Uri URI_TABLES = Uri.parse(BASE_CONTENT_URI.toString() + "/" + PATH_FRIENDS);

    private static final String[] TOP_LEVEL_PATHS = {
            PATH_FRIENDS
    };

    public static class Friends implements FriendsContract.FriendsColumns, BaseColumns{

        //Builds the Content Uri to access the Friends component of the ContentProvider
        private static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_FRIENDS).build();

        //Return a MIME type in Android's vendor-specific MIME format
        //Returns all the records
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + ".friends";

        //For editing a single record
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + ".friends";

        //Build Uri to access particular record
        public static Uri buildFriendUri(String friendId){
            return CONTENT_URI.buildUpon().appendEncodedPath(friendId).build();
        }

        public static String getFriendId(Uri uri){
            return uri.getPathSegments().get(1);
        }

    }
}
