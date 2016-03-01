package com.amrutpatil.makeanote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Amrut on 2/29/16.
 * Description: Class to create, update, delete contents in Notes, Archives and Trash Tables.
 */
public class AppDatabase extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "makeanote.db";
    private static final int DATABASE_VERSION = 1;

    interface Tables {
        String NOTES = "notes";
        String ARCHIVES ="archives";
        String TRASH = "trash";
    }

    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + Tables.NOTES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NotesContract.NotesColumns.NOTES_TITLE + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTES_DESCRIPTION + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTES_TIME + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTES_TYPE + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION + " INTEGER NOT NULL,"
                + NotesContract.NotesColumns.NOTES_IMAGE + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTES_DATE + " TEXT NOT NULL)");


        db.execSQL("CREATE TABLE " + Tables.ARCHIVES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_TITLE + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_TYPE + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME+ " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + Tables.TRASH + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TrashContract.TrashColumns.TRASH_TITLE + " TEXT NOT NULL,"
                + TrashContract.TrashColumns.TRASH_DESCRIPTION + " TEXT NOT NULL,"
                + TrashContract.TrashColumns.TRASH_DATE_TIME+ " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        if(version == 1) {
            version = 2;
        }

        if(version != DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.NOTES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ARCHIVES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.TRASH);
            onCreate(db);
        }
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }

    public void emptyTrash(){
        getWritableDatabase().delete(Tables.TRASH, null,null);
    }

}
