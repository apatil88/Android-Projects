package com.amrutpatil.makeanote;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Description: Class to upload an image to Dropbox.
 */
public class DropboxImageUploadAsync extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> mApi;
    private String mPath;
    private File mFile;
    private String mFileName;

    public DropboxImageUploadAsync(Context context, DropboxAPI<?> api, File file, String fileName) {
        mApi = api;
        mFile = file;
        mFileName = fileName;
        this.mPath = AppSharedPreferences.getDropBoxUploadPath(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String mErrorMessage;
        try{
            FileInputStream fis = new FileInputStream(mFile);
            String path = mPath + "/" + mFileName;
            DropboxAPI.UploadRequest request = mApi.putFileOverwriteRequest(path, fis, mFileName.length(),
                    new ProgressListener() {

                        //Get an update about where we have progressed every 500 milliseconds
                        @Override
                        public long progressInterval() {
                            return 500;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress(bytes);
                        }
                    });

            if(request != null){
                request.upload();
                return true;
            }

        }catch (DropboxUnlinkedException e){  //If we do not connect to Dropbox
            mErrorMessage = "Authentication Dropbox error";
        } catch (DropboxPartialFileException e){  //If the file was not uploaded completely
            mErrorMessage = "Upload cancelled";
        } catch (DropboxServerException e){  //If there is some problem at the server end
            mErrorMessage = "Network error, try again";
        } catch (DropboxFileSizeException e){
            mErrorMessage = "File too large to be uploaded";
        } catch (DropboxException e){
            mErrorMessage = "Unknown Dropbox error, try again";
        } catch (FileNotFoundException e){
            mErrorMessage = "File Not Found Exception";
        }
        return false;
    }
}
