package com.amrutpatil.makeanote;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Amrut on 2/27/16.
 * Description: Helper class to manage user preferences and authentication state with Google Drive and Dropbox
 */
public class AppSharedPreferences {
    //this method will set the state of user knowing about the existence of the Navigation Drawer
    public static void setUserLearned(Context context, String prefName, String prefValue) {
        android.content.SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(prefName, prefValue);
        editor.apply();
    }

    //this method will get the state of user knowing about the existence of the Navigation Drawer
    public static String hasUserLearned(Context context, String prefName, String defaultValue) {
        android.content.SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(prefName, defaultValue);
    }

    //this method will get the upload preference for the image; whether google drive, dropbox or local
    public static int getUploadPreference(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getInt(AppConstant.IMAGE_SELECTION_STORAGE, AppConstant.NONE_SELECTION);
    }

    //this method will set the upload preference for the image; whether google drive, dropbox or local
    public static void setPersonalNotesPreference(Context context, int value) {
        SharedPreferences preferences = context.getSharedPreferences(
                AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(AppConstant.IMAGE_SELECTION_STORAGE, value);
        editor.apply();
    }

    //this method will store the ResourceID of the google drive directory to which we will save the image
    public static void storeGoogleDriveResourceId(Context context, String value) {
        SharedPreferences preferences = context.getSharedPreferences(
                AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AppConstant.GOOGLE_DRIVE_ID, value);
        editor.apply();
    }

    //this method will get the ResourceID of the google drive directory to which we will save the image
    public static String getGoogleDriveResourceId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConstant.GOOGLE_DRIVE_ID, "");
    }

    //this method will store the name of the google drive directory to which we will save the image
    public static void storeGoogleDriveUploadFileName(Context context, String value) {
        SharedPreferences preferences = context.getSharedPreferences(
                AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AppConstant.GOOGLE_DRIVE_UPLOAD_DIR, value);
        editor.apply();
    }

    //this will get the path of the google drive directory to which we will save the image
    public static String getGoogleDriveUploadPath(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConstant.GOOGLE_DRIVE_UPLOAD_DIR, "");
    }

    //this will store the image upload path in dropbox
    public static void storeDropBoxUploadPath(Context context, String value) {
        SharedPreferences preferences = context.getSharedPreferences(
                AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AppConstant.DROP_BOX_UPLOAD_PATH, value);
        editor.apply();
    }

    //this will get the image upload path in dropbox
    public static String getDropBoxUploadPath(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AppConstant.DROP_BOX_UPLOAD_PATH, "");
    }

    //this will check the authenticated state of dropbox
    public static boolean isDropBoxAuthenticated(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AppConstant.DROP_BOX_AUTH_BOOL, false);
    }


    //this will set the authentication state of dropbox
    public static void isDropBoxAuthenticated(Context context, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(
                AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AppConstant.DROP_BOX_AUTH_BOOL, value);
        editor.apply();
    }

    //this will get the authenticated sate of google drive
    public static boolean isGoogleDriveAuthenticated(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AppConstant.GOOGLE_DRIVE_AUTH_BOOL, false);
    }

    //this will set the authenticated state of google drive
    public static void isGoogleDriveAuthenticated(Context context, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(
                AppConstant.MAKE_A_NOTE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(AppConstant.GOOGLE_DRIVE_AUTH_BOOL, value);
        editor.apply();
    }

}
