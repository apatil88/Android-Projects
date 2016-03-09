package com.amrutpatil.makeanote;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
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

}
