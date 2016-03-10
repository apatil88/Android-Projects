package com.amrutpatil.makeanote;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<Note>>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private List<Note> mNotes;
    private RecyclerView mRecyclerView;
    private NotesAdapter mNotesAdapter;
    private ContentResolver mContentResolver;
    private static Boolean mIsInAuth;
    private static Bitmap mSendingImage = null;
    private boolean mIsImageNotFound = false;

    private DropboxAPI<AndroidAuthSession> mDropboxAPI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        activateToolbar();
        setUpForDropbox();
        setUpNavigationDrawer();
        setUpForRecyclerView();
        setUpActions();
    }

    private void setUpForDropbox(){
        //create a session
        AndroidAuthSession  session = DropboxActions.buildsession(getApplicationContext());
        mDropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
    }

    private void setUpForRecyclerView(){
        mContentResolver = getContentResolver();
        mNotesAdapter = new NotesAdapter(new ArrayList<Note>(), NotesActivity.this);
        int LOADER_ID = 1;
        getSupportLoaderManager().initLoader(LOADER_ID, null, NotesActivity.this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_home);
        GridLayoutManager linearlayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        linearlayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearlayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                edit(view);
            }

            @Override
            public void OnItemLongClick(View view, int position) {
                android.support.v7.widget.PopupMenu popupMenu = new android.support.v7.widget.PopupMenu(NotesActivity.this, view);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu_action_notes, popupMenu.getMenu());
                popupMenu.show();
                final View v = view;
                final int pos = position;
                popupMenu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.action_delete){
                            moveToTrash();
                            delete(v, pos);
                        } else if (item.getItemId() == R.id.action_archive){
                            moveToArchive(v, pos);
                        } else if (item.getItemId() == R.id.action_edit){
                            edit(v);
                        }
                        return false;
                    }
                });
            }
        }));
    }

    @Override
    public Loader<List<Note>> onCreateLoader(int id, Bundle args) {
        mContentResolver = getContentResolver();
        return new NotesLoader(NotesActivity.this, BaseActivity.mType, mContentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
        this.mNotes = data;
        //Retrieve the image from local storage/Google Drive/Dropbox in a separate thread
        Thread[] threads = new Thread[mNotes.size()];
        int threadCounter = 0;

        for(final Note aNote : mNotes){
            //If the note is coming from Google Drive
            if(AppConstant.GOOGLE_DRIVE_SELECTION == aNote.getStorageSelection()){
                GDUT.init(this);
                //Check if Google Drive is accessible and if the user account has been logged in successfully
                if(checkPlayServices() && checkUserAccount()){
                    GDActions.init(this, GDUT.AM.getActiveEmil());
                    GDActions.connect(true);
                }

                threads[threadCounter] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        do{
                            //Get the image file
                            ArrayList<GDActions.GF> gfs = GDActions.search(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                                    aNote.getImagePath(), GDUT.MIME_JPEG);

                            if(gfs.size() > 0){
                                //Retrieve the file, convert it into Bitmap to display on the screen
                                byte[] imageBytes = GDActions.read(gfs.get(0).id, 0);

                                //Process the entire byte array and convert it into an image
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                aNote.setBitmap(bitmap);
                                mIsImageNotFound = false;
                                mNotesAdapter.setData(mNotes);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Tell the Adapter that a graphic image has been obtained
                                        mNotesAdapter.notifyImageObtained();
                                    }
                                });
                            } else{
                                aNote.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_loading));
                                mIsImageNotFound = true;
                                try{
                                    Thread.sleep(500);
                                } catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                            }
                        }while(mIsImageNotFound);
                    }
                });
            } else if (AppConstant.DROP_BOX_SELECTION == aNote.getStorageSelection()){
                threads[threadCounter] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        do{
                            Drawable drawable = getImageFromDropbox(mDropboxAPI,
                                    AppSharedPreferences.getDropBoxUploadPath(getApplicationContext()),
                                    aNote.getImagePath());
                            if(drawable != null) {
                                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                                aNote.setBitmap(bitmap);
                            }
                            if(!mIsImageNotFound){
                                mNotesAdapter.setData(mNotes);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mNotesAdapter.notifyImageObtained();
                                    }
                                });
                            }
                            try{
                                Thread.sleep(500);
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }

                        }while(mIsImageNotFound);
                    }
                });
                threads[threadCounter].start();
                threadCounter++;
            } else {
                aNote.setHasNoImage(true);
            }
        }
        mNotesAdapter.setData(mNotes);  //Adapter has the latest copy of note
        changeNoItemTag();
    }

    //Method checks if the path to file is valid
    //Method looks for the file we are looking for, grabs it and returns it to the calling process
    private Drawable getImageFromDropbox(DropboxAPI<?> mApi, String mPath, String filename) {
        FileOutputStream fos;
        Drawable drawable;
        //Check to see if we have cached the file on the local device. If we found the file on the device, retrieve it
        String cachePath = getApplicationContext().getCacheDir().getAbsolutePath() + "/" + filename;
        File cacheFile = new File(cachePath);
        if (cacheFile.exists()) {
            mIsImageNotFound = false;
            return Drawable.createFromPath(cachePath);
        } else { //If we did not find the file, go to Dropbox to retrieve it
            try {
                DropboxAPI.Entry dirEnt = mApi.metadata(mPath, 1000, null, true, null);
                //If the path is not a directory path or if it is null
                if (!dirEnt.isDir || dirEnt.contents == null) {
                    mIsImageNotFound = true;
                }
                ArrayList<DropboxAPI.Entry> thumbs = new ArrayList<DropboxAPI.Entry>();
                for (DropboxAPI.Entry ent : dirEnt.contents) {
                    if (ent.thumbExists) {
                        if (ent.fileName().startsWith(filename)) {
                            thumbs.add(ent);
                        }
                    }
                }
                if (thumbs.size() == 0) {
                    mIsImageNotFound = true;
                } else {
                    //Grab the image from Dropbox
                    DropboxAPI.Entry ent = thumbs.get(0);
                    String path = ent.path;
                    try {
                        fos = new FileOutputStream(cachePath);

                    } catch (FileNotFoundException e) {
                        return getResources().getDrawable(R.drawable.ic_image_deleted);
                    }
                    mApi.getThumbnail(path, fos, DropboxAPI.ThumbSize.BESTFIT_960x640,
                            DropboxAPI.ThumbFormat.JPEG, null);
                    drawable = Drawable.createFromPath(cachePath);
                    mIsImageNotFound = false;
                    return drawable;
                }
            } catch (DropboxException e) {
                e.printStackTrace();
                mIsImageNotFound = true;
            }

            drawable = getResources().getDrawable(R.drawable.ic_loading);
            return drawable;
        }
    }

    //Method to show/hide the RecyclerView depending on item availability
    private void changeNoItemTag(){
        TextView mItemTextView = (TextView) findViewById(R.id.no_item_textview);
        //if there are items to show
        if(mNotesAdapter.getItemCount() != 0){
            mItemTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }else{
            mItemTextView.setText(AppConstant.EMPTY);
            mItemTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

}
