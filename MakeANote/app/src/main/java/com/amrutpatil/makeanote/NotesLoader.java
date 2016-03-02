package com.amrutpatil.makeanote;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amrut on 3/1/16.
 * Description: AsyncTask Loader class for getting back notes from the database via Content Provider.
 */
public class NotesLoader extends AsyncTaskLoader<List<Note>>{

    private List<Note> mNotes;
    private ContentResolver mContentResolver;
    private Cursor mCursor;  //navigate the records
    private int mType;  //reminder or a note

    public NotesLoader(Context context, int type, ContentResolver contentResolver) {
        super(context);
        mType = type;
        mContentResolver = contentResolver;
    }

    @Override
    public List<Note> loadInBackground() {

        List<Note> entries = new ArrayList<Note>();
        String [] projection = {
                BaseColumns._ID,
                NotesContract.NotesColumns.NOTES_TITLE,
                NotesContract.NotesColumns.NOTES_DESCRIPTION,
                NotesContract.NotesColumns.NOTES_DATE,
                NotesContract.NotesColumns.NOTES_TIME,
                NotesContract.NotesColumns.NOTES_TYPE,
                NotesContract.NotesColumns.NOTES_IMAGE,
                NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION
        };

        Uri uri = NotesContract.URI_TABLE;
        mCursor = mContentResolver.query(uri, projection, null, null, BaseColumns._ID + "DESC");

        if(mCursor != null){
            if(mCursor.moveToFirst()){
                do{
                    String date = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DATE));
                    String title = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TITLE));
                    String type = mCursor.getColumnName(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TYPE));
                    String description = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DESCRIPTION));
                    String time = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TIME));
                    String imagePath = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE));
                    int imageSelection = mCursor.getInt(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION));
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));

                    if(mType == BaseActivity.NOTES){
                        if(time.equals(AppConstant.NO_TIME)){
                            time = "";
                            Note note = new Note(title, description, date, time, type, _id, imageSelection);
                            //Check if an image is stored with the note
                            if(!imagePath.equals(AppConstant.NO_IMAGE)){
                                //If the image is stored locally on the device
                                if(imageSelection == AppConstant.DEVICE_SELECTION){
                                    note.setBitmap(imagePath);
                                } else{
                                    //Is a Google Drive or Dropbox image
                                    note.setBitmap(AppConstant.NO_IMAGE);
                                }
                            }
                            entries.add(note);
                        }
                    } else if (mType == BaseActivity.REMINDERS){
                        if(time.equals(AppConstant.NO_TIME)){
                            Note note = new Note(title, description, date, time, type, _id, imageSelection);
                            if(!imagePath.equals(AppConstant.NO_IMAGE)){
                                if(imageSelection == AppConstant.DEVICE_SELECTION){
                                    note.setBitmap(imagePath);
                                }else{
                                    //Is a Google Drive or Dropbox
                                }
                            }
                        }

                    } else{
                        throw new IllegalArgumentException("Invalid type :  " + mType);
                    }
                }while (mCursor.moveToNext());
            }
        }
        return entries;
    }
}
