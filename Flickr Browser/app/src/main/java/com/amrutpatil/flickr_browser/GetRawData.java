package com.amrutpatil.flickr_browser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Amrut on 2/8/15.
 * This class downloads the unprocessed raw data from the URL.
 */

enum DownloadStatus {IDLE, PROCESSING, NOT_INTIALIZED, FAILED_OR_EMPTY, OK};

public class GetRawData {

    private String LOG_TAG = GetRawData.class.getSimpleName();
    private String mRawUrl;
    private String mData;
    private DownloadStatus mDownloadStatus;

    public GetRawData(String mRawUrl) {
        this.mRawUrl = mRawUrl;
        this.mDownloadStatus = DownloadStatus.IDLE;      //setting the download status when an object of this class is created
    }

    public void reset(){
        this.mDownloadStatus = DownloadStatus.IDLE;
        this.mRawUrl = null;
        this.mData = null;
    }

    public String getmData() {
        return mData;
    }

    public DownloadStatus getmDownloadStatus() {
        return mDownloadStatus;
    }

    public void setmRawUrl(String mRawUrl) {
        this.mRawUrl = mRawUrl;
    }

    //Starting the download process in background(another thread)
    public void execute(){
        this.mDownloadStatus = DownloadStatus.PROCESSING;
        DownloadRawData downloadRawData = new DownloadRawData();
        downloadRawData.execute(mRawUrl);
    }

    //Class to download the raw data and store in mData
    public class DownloadRawData extends AsyncTask<String, Void, String>{

        //process the data
        protected void onPostExecute(String webData){
           mData = webData;
           Log.v(LOG_TAG,"Data returned is : " + webData);
           if(mData == null){
               //if the URL was not passed
               if(mRawUrl == null){
                   mDownloadStatus = DownloadStatus.NOT_INTIALIZED;
               } else{   //if the URL was passed but it was not appropriate
                   mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
               }
           } else{
               //Success
               mDownloadStatus = DownloadStatus.OK;
           }
        }


        //Downloads the raw data
        protected String doInBackground(String...params){
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            if(params == null){
                return null;
            }

            try{
                URL url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(inputStream == null){
                    return null;
                }
                StringBuffer buffer = new StringBuffer();

                //storing data in buffer
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                //Reading buffer line by line
                while((line = reader.readLine())!=null){
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            }catch(IOException e){
                Log.e(LOG_TAG,"Error", e);
                return null;
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }

                if(reader != null){
                    try{
                        reader.close();
                    } catch (final IOException e){
                        Log.e(LOG_TAG,"Error closing stream", e);
                    }
                }
            }
        }

    }
}
