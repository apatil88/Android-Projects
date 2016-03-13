package com.amrutpatil.makeanote;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

/**
 * Description: Class to create a directory on Dropbox.
 */
public class DropboxDirectoryCreatorAsyncTask extends AsyncTask<Void, Long, Boolean>{
    private DropboxAPI mApi;
    private Context mContext;
    private String mPath;
    private OnDirectoryCreateFinished mListener;
    private String mName;
    private String mMessage;

    public DropboxDirectoryCreatorAsyncTask(DropboxAPI api, Context context, String path, OnDirectoryCreateFinished listener, String name) {
        mApi = api;
        mContext = context;
        mPath = path;
        mListener = listener;
        mName = name;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            //Create the folder on Dropbox
            mApi.createFolder(mPath);
            mMessage = AppConstant.FOLDER_CREATED;
        } catch (DropboxException e){
            mMessage = AppConstant.FOLDER_CREATE_ERROR;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result){
            //If we created the folder successfully
            mListener.onDirectoryCreateFinished(mName);
            Toast.makeText(mContext.getApplicationContext(), mMessage, Toast.LENGTH_LONG).show();
        }
    }

    public interface OnDirectoryCreateFinished{
        void onDirectoryCreateFinished(String dirName);
    }
}
