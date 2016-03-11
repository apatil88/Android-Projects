package com.amrutpatil.makeanote;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ArchivesActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<Archive>> {

    private static final int LOADER_ID = 1;
    private RecyclerView mRecyclerView;
    private ArchivesAdapter mArchivesAdapter;
    private ContentResolver mContentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_layout);
        mToolBar = activateToolbar();
        setUpNavigationDrawer();
        setUpRecyclerView();
        removeActions();
    }

    private void setUpRecyclerView() {
        mContentResolver = getContentResolver();
        getSupportLoaderManager().initLoader(LOADER_ID, null, ArchivesActivity.this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_home);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mArchivesAdapter = new ArchivesAdapter(ArchivesActivity.this, new ArrayList<Archive>());
        mRecyclerView.setAdapter(mArchivesAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void OnItemClick(final View view, final int position) {
                PopupMenu popup = new PopupMenu(ArchivesActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions_archive_delete, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuitem) {
                        if (menuitem.getItemId() == R.id.action_delete_archive) {
                            moveToDeleted();
                            delete(view, position);
                        }
                        return false;
                    }
                });
            }

            @Override
            public void OnItemLongClick(View view, int position) {

            }

        }));
    }

    private void moveToDeleted() {
        ContentValues values = new ContentValues();
        TextView title = (TextView) findViewById(R.id.title_note_custom_home);
        TextView description = (TextView) findViewById(R.id.description_note_custom_home);
        TextView dateTime = (TextView) findViewById(R.id.date_time_note_custom_home);

        values.put(TrashContract.TrashColumns.TRASH_TITLE, title.getText().toString());
        values.put(TrashContract.TrashColumns.TRASH_DESCRIPTION, description.getText().toString());
        values.put(TrashContract.TrashColumns.TRASH_DATE_TIME, dateTime.getText().toString());
        ContentResolver cr = getContentResolver();
        Uri uri = TrashContract.URI_TABLE;
        cr.insert(uri, values);
    }

    @Override
    public Loader<List<Archive>> onCreateLoader(int id, Bundle args) {
        mContentResolver = getContentResolver();
        return new ArchivesLoader(ArchivesActivity.this, mContentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<Archive>> loader, List<Archive> data) {
        mArchivesAdapter.setData(data);
        changeNoItemTag();
    }

    @Override
    public void onLoaderReset(Loader<List<Archive>> loader) {
        mArchivesAdapter.setData(null);
    }

    //Method to display records, if any
    private void changeNoItemTag() {
        if (mArchivesAdapter.getItemCount() != 0) {
            TextView noItemTV = (TextView) findViewById(R.id.no_item_textview);
            noItemTV.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            TextView noItemTV = (TextView) findViewById(R.id.no_item_textview);
            noItemTV.setText(AppConstant.NO_ARCHIVES);
            noItemTV.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    //Method to delete the note from the database and remove the note from the adapter
    private void delete(View view, int position) {
        String _ID = ((TextView) view.findViewById(R.id.id_note_custom_home)).getText().toString();
        Uri uri = ArchivesContract.Archives.buildArchiveUri(_ID);
        mContentResolver.delete(uri, null, null);
        mArchivesAdapter.delete(position);
        changeNoItemTag();
    }
}
