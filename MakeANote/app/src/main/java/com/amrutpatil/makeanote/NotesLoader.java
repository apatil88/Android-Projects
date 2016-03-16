package com.amrutpatil.makeanote;

import android.support.v4.content.AsyncTaskLoader;
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
 * Creates the necessary objects that contains the data so that the data can be put on screen.
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
                                    note.setImagePath(imagePath);
                                }
                            } else{
                                note.setImagePath(AppConstant.NO_IMAGE);
                            }
                            entries.add(note);
                        }
                    } else if (mType == BaseActivity.REMINDERS){
                        if(!time.equals(AppConstant.NO_TIME)){
                            Note note = new Note(title, description, date, time, type, _id, imageSelection);
                            if(!imagePath.equals(AppConstant.NO_IMAGE)){
                                if(imageSelection == AppConstant.DEVICE_SELECTION){
                                    note.setBitmap(imagePath);
                                }else{
                                    //Is a Google Drive or Dropbox
                                    note.setImagePath(imagePath);
                                }
                            } else{
                                note.setImagePath(AppConstant.NO_IMAGE);
                            }
                            entries.add(note);
                        }
                    } else{
                        throw new IllegalArgumentException("Invalid type :  " + mType);
                    }
                }while (mCursor.moveToNext());
            }
        }
        return entries;
    }

    @Override
    public void deliverResult(List<Note> notes) {

        if(isReset()){
            if(notes != null){
                releaseResources();
                return;
            }
        }
        List<Note> oldNotes = mNotes;
        mNotes = notes;

        if(isStarted()){
            super.deliverResult(notes);
        }
        if(oldNotes != null && oldNotes != notes){
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if(mNotes != null){
            deliverResult(mNotes);
        }
        if(takeContentChanged()){
            forceLoad();
        } else if (mNotes == null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if(mNotes != null){
            releaseResources();
            mNotes = null;
        }
    }

    @Override
    public void onCanceled(List<Note> notes) {
        super.onCanceled(notes);
        releaseResources();
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources(){
        mCursor.close();
    }
}
