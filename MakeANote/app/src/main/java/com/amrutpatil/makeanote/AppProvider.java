package com.amrutpatil.makeanote;

import android.content.ContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;


/**
 * Created by Amrut on 2/29/16.
 * Description: Content Provider class to save information in Notes, Archives and Trash in the database.
 */
public class AppProvider extends ContentProvider {

    protected AppDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int NOTES = 100;
    private static final int NOTES_ID = 101;

    private static final int ARCHIVES = 200;
    private static final int ARCHIVES_ID = 201;

    private static final int TRASH = 300;
    private static final int TRASH_ID = 301;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = NotesContract.CONTENT_AUTHORITY;  //incoming content provider Uri
        uriMatcher.addURI(authority, "notes", NOTES);
        uriMatcher.addURI(authority, "notes/*", NOTES_ID);

        authority = ArchivesContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, "archives", ARCHIVES);
        uriMatcher.addURI(authority, "archives/*", ARCHIVES_ID);

        authority = TrashContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, "trash", TRASH);
        uriMatcher.addURI(authority, "trash/*", TRASH_ID);

        return uriMatcher;
    }

    private void deleteDatabase(){
        mOpenHelper.close();
        AppDatabase.deleteDatabase(getContext());
        mOpenHelper = new AppDatabase(getContext());
    }

    //Intialize database when content provider is intialized
    @Override
    public boolean onCreate() {
        mOpenHelper = new AppDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case NOTES:
                return NotesContract.Notes.CONTENT_TYPE;   //all notes records

            case NOTES_ID:
                return NotesContract.Notes.CONTENT_ITEM_TYPE;  //single note record

            case ARCHIVES:
                return ArchivesContract.Archives.CONTENT_TYPE;

            case ARCHIVES_ID:
                return ArchivesContract.Archives.CONTENT_ITEM_TYPE;

            case TRASH:
                return TrashContract.Trash.CONTENT_TYPE;

            case TRASH_ID:
                return TrashContract.Trash.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);

        }
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match){
            case NOTES:
                queryBuilder.setTables(AppDatabase.Tables.NOTES);
                break;

            case NOTES_ID:
                queryBuilder.setTables(AppDatabase.Tables.NOTES);
                String note_id = NotesContract.Notes.getNoteId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + " = "+ note_id);
                break;

            case ARCHIVES:
                queryBuilder.setTables(AppDatabase.Tables.ARCHIVES);
                break;

            case ARCHIVES_ID:
                queryBuilder.setTables(AppDatabase.Tables.ARCHIVES);
                String archive_id = ArchivesContract.Archives.getArchiveId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + " = " + archive_id);
                break;

            case TRASH:
                queryBuilder.setTables(AppDatabase.Tables.TRASH);
                break;

            case TRASH_ID:
                queryBuilder.setTables(AppDatabase.Tables.TRASH);
                String trash_id = TrashContract.Trash.getTrashId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + " = " + trash_id);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);

        }
        return queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
    }
}
