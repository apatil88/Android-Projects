package com.amrutpatil.makeanote;

import android.net.Uri;
import android.provider.BaseColumns;


/**
 * Created by Amrut on 2/29/16.
 */
public class NotesContract {
    interface NotesColumns{
        String NOTE_ID = "_ID";
        String NOTE_TITLE = "notes_title";
        String NOTE_DESCRIPTION = "notes_description";
        String NOTE_DATE = "notes_date";
        String NOTE_TIME = "notes_time";
        String NOTE_IMAGE = "notes_image";
        String NOTE_TYPE = "notes_type";
        String NOTE_IMAGE_STORAGE_SELECTION = "notes_image_storage_selection";
    }

    public static final String CONTENT_AUTHORITY = "com.amrutpatil.makeanote.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_NOTES = "notes";
    public static final Uri URI_TABLE = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_NOTES).build();

    public static class Notes implements NotesColumns, BaseColumns{
        //Access the content provider for all the notes or a single note
        public static final String CONTENT_TYPE= "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + ".notes";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + ".notes";

        //Method which enables content provider to return an individual note
        public static Uri buildNote(String noteId){
          return URI_TABLE.buildUpon().appendEncodedPath(noteId).build();
        }

        //Method to extract the note id
        public static String getNoteId(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }
}
