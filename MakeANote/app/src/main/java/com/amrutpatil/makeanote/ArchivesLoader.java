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
 * Created by Amrut on 3/3/16.
 * Description: Class to get Archive notes from database via Content Provider
 */
public class ArchivesLoader extends AsyncTaskLoader<List<Archive>> {
    private List<Archive> mArchives;
    private ContentResolver mContentResolver;
    private Cursor mCursor;

    public ArchivesLoader(Context context, ContentResolver contentResolver) {
        super(context);
        mContentResolver = contentResolver;
    }

    @Override
    public List<Archive> loadInBackground() {
        List<Archive> entries = new ArrayList<>();
        String[] projection = {BaseColumns._ID,
                ArchivesContract.ArchivesColumns.ARCHIVES_TITLE,
                ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION,
                ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME,
                ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY,
                ArchivesContract.ArchivesColumns.ARCHIVES_TYPE
        };
        Uri uri = ArchivesContract.URI_TABLE;
        mCursor = mContentResolver.query(uri, projection, null, null, BaseColumns._ID + " DESC");
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String title = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_TITLE));
                    String description = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION));
                    String dateTime = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME));
                    String category = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY));
                    String type = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE));
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    Archive archive = new Archive(title, description, dateTime, category, type, _id);

                    entries.add(archive);

                } while (mCursor.moveToNext());
            }
        }
        return entries;
    }

    @Override
    public void deliverResult(List<Archive> archives) {
        if (isReset()) {
            if(archives != null) {
                releaseResources();
                return;
            }
        }
        List<Archive> oldArchives = mArchives;
        mArchives = archives;
        if(isStarted()) {
            super.deliverResult(archives);
        }
        if(oldArchives != null && oldArchives != archives) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if(mArchives != null) {
            deliverResult(mArchives);
        }
        if(takeContentChanged()) {
            forceLoad();
        } else if(mArchives == null) {
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
        if(mArchives != null) {
            releaseResources();
            mArchives = null;
        }
    }


    @Override
    public void onCanceled(List<Archive> archive) {
        super.onCanceled(archive);
        releaseResources();
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources() {
        mCursor.close();
    }


}
