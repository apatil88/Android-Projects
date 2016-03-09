package com.amrutpatil.makeanote;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.common.api.GoogleApiClient;

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
            }
        }

    }
}
