package com.amrutpatil.makeanote;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Description: Class to retrieve the list of directories from Dropbox.
 */
public class DropboxDirectoryListenerAsync extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private DropboxAPI<?> mApi;
    private List<String> mDirectories = new ArrayList<>();
    private String mErrorMessage;
    private String mCurrentDirectory;
    private OnLoadFinished mListener;

    public DropboxDirectoryListenerAsync(Context context, DropboxAPI<?> api, String currentDirectory, OnLoadFinished listener) {
        mContext = context;
        mApi = api;
        mListener = listener;
        mCurrentDirectory = currentDirectory;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            mErrorMessage = null;
            DropboxAPI.Entry directoryEntry = mApi.metadata(mCurrentDirectory, 1000, null, true, null);
            //Check if we are processing directories
            if(!directoryEntry.isDir || directoryEntry.contents == null){
                mErrorMessage = "File or empty directory";
                return false;
            }

            for(DropboxAPI.Entry entry : directoryEntry.contents){
                if(entry.isDir){
                    mDirectories.add(entry.fileName());  //If it is a directory, add it to our List
                }
            }

        } catch (DropboxUnlinkedException e){  //If we do not connect to Dropbox
            //Error here
            mErrorMessage = "Authentication Dropbox error";
        } catch (DropboxPartialFileException e){  //If the file was not downloaded completely
            mErrorMessage = "Download cancelled";
        } catch (DropboxServerException e){  //If there is some problem at the server end
            mErrorMessage = "Network error, try again";
        } catch (DropboxParseException e) {
            mErrorMessage = "Dropbox Parse Exception, try again";
        } catch (DropboxException e){
            mErrorMessage = "Unknown Dropbox error, try again";
        }
        if(mErrorMessage != null){
            return false;
        }else{
            return true;  //If the execution of program reaches here, there were no problems connecting and authenticating.
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result){
            mListener.onLoadFinished(mDirectories);
        }else{
            showToast(mErrorMessage);
        }
    }

    private void showToast(String message){
        Toast error = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
        error.show();
    }

    public interface OnLoadFinished{
        void onLoadFinished(List<String> values);
    }
}
