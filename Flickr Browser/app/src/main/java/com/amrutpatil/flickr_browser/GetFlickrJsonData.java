package com.amrutpatil.flickr_browser;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amrut on 2/8/15.
 */
public class GetFlickrJsonData extends GetRawData {

    private String LOG_TAG = GetFlickrJsonData.class.getSimpleName();
    private List<Photo> mPhotos;
    private Uri mDestinationUri;

    //if matchAll = 1, tagMode = ALL
    //if matchAll = 0, tagMode = ANY
    //tagMode - Control whether items must have ALL the tags (tagmode=all), or ANY (tagmode=any) of the tags. Default is ALL.

    public GetFlickrJsonData(String searchCriteria, boolean matchAll) {
        super(null);  //GetRawData is expecting a URL, however, we just have tags and tagmode
        createAndUpdateUri(searchCriteria, matchAll);
        mPhotos = new ArrayList<Photo>();
    }

    public void execute(){
        super.setmRawUrl(mDestinationUri.toString());
        DownloadJsonData downloadJsonData = new DownloadJsonData();
        Log.v(LOG_TAG, "Built URI = " + mDestinationUri.toString());
        downloadJsonData.execute(mDestinationUri.toString());

    }
    //This method puts together a URL which can be used by GetRawData to download the raw data.
    public boolean createAndUpdateUri(String searchCriteria, boolean matchAll){

        final String FLICKR_API_BASE_URL = "https://api.flickr.com/services/feeds/photos_public.gne";
        final String TAGS_PARAM = "tags";
        final String TAGMODE_PARAM = "tagmode";
        final String FORMAT_PARAM = "format";
        final String NO_JSON_CALLBACK = "nojsoncallback";

        //construct the URL
        mDestinationUri = Uri.parse(FLICKR_API_BASE_URL).buildUpon()
                .appendQueryParameter(TAGS_PARAM,searchCriteria)
                .appendQueryParameter(TAGMODE_PARAM,matchAll ? "ALL":"ANY")
                .appendQueryParameter(FORMAT_PARAM, "json")
                .appendQueryParameter(NO_JSON_CALLBACK,"1")
                .build();

        return mDestinationUri!=null;    //return true if URL was constructed

    }

    public List<Photo> getMPhotos() {
        return mPhotos;
    }

    public void processResults(){

        if(getmDownloadStatus() != DownloadStatus.OK){
            Log.e(LOG_TAG, "Error downloading file");
            return;
        }

        //Photo information to be extracted from the JSON data
        final String FLICKR_ITEMS = "items";
        final String FLICKR_TITLE = "title";
        final String FLICKR_MEDIA = "media";
        final String FLICKR_PHOTO_URL = "m";
        final String FLICKR_LINK = "link";
        final String FLICKR_AUTHOR = "author";
        final String FLICKR_AUTHOR_ID = "author_id";
        final String FLICKR_TAGS = "tags";

        try{
            JSONObject jsonObject = new JSONObject(getmData());  //Storing the raw data coming from GetRawData class into a JSON object

            JSONArray itemsArray = jsonObject.getJSONArray(FLICKR_ITEMS);   //Getting items array from JSON data as items array contains list of photos

            //Iterating through the list of photos
            for(int i = 0; i < itemsArray.length(); i++){

                //Extracting the actual data for each photo from the JSON data

                JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                String title = jsonPhoto.getString(FLICKR_TITLE);
                String author = jsonPhoto.getString(FLICKR_AUTHOR);
                String author_id = jsonPhoto.getString(FLICKR_AUTHOR_ID);
                String link = jsonPhoto.getString(FLICKR_LINK);
                String tags = jsonPhoto.getString(FLICKR_TAGS);

                //Extracting the photoURL from the media since media is a JSON object by itself
                JSONObject jsonMedia = jsonPhoto.getJSONObject(FLICKR_MEDIA);
                String photoUrl = jsonMedia.getString(FLICKR_PHOTO_URL);

                //Create a photo object with the extracted photo information
                Photo photoObject = new Photo(title,author,author_id,link,tags, photoUrl);

                //add the photo to the List of photos
                this.mPhotos.add(photoObject);
            }

            for(Photo singlePhoto : mPhotos){
                Log.v(LOG_TAG, singlePhoto.toString());
            }

        }catch(JSONException jsone){
            jsone.printStackTrace();
            Log.e(LOG_TAG,"Error downloading Json data");
        }
    }

    public class DownloadJsonData extends DownloadRawData{

        protected void onPostExecute(String webData){
            super.onPostExecute(webData);
            processResults();  //Method to process downloaded raw Data as JSON data
        }

        protected String doInBackground(String ... params){
            //We now know the location of the data
            //Prevent crashing
            String[] par = { mDestinationUri.toString() };
            return super.doInBackground(par);
        }

    }
}
