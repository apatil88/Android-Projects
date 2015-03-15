package com.amrutpatil.friends;

import android.support.v4.app.LoaderManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

/**
 * Created by Amrut on 3/14/15.
 */
public class SearchActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<List<Friend>> {

    private static final String LOG_TAG = SearchActivity.class.getSimpleName();
    private static int LOADER_ID = 2;
    private FriendsCustomAdapter mFriendsCustomAdapter;
    private ContentResolver mContentResolver;
    private List<Friend> friendsRetrieved;
    private EditText mSearchEditText;
    private Button mSearchFriendButton;
    private ListView listView;
    private String matchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        listView = (ListView) findViewById(R.id.searchResultsList);
        mSearchEditText = (EditText) findViewById(R.id.searchName);
        mSearchFriendButton = (Button) findViewById(R.id.searchButton);
        mContentResolver = getContentResolver();
        mFriendsCustomAdapter = new FriendsCustomAdapter(SearchActivity.this, getSupportFragmentManager());
        listView.setAdapter(mFriendsCustomAdapter);

        mSearchFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matchText = mSearchEditText.getText().toString();
                getSupportLoaderManager().initLoader(LOADER_ID++,null, SearchActivity.this); //everytime we invoke a new loader it should have a new id
            }
        });
    }

    @Override
    public Loader<List<Friend>> onCreateLoader(int i, Bundle bundle) {
        return new FriendsSearchListLoader(SearchActivity.this, FriendsContract.URI_TABLES, this.mContentResolver, matchText);
    }

    @Override
    public void onLoadFinished(Loader<List<Friend>> listLoader, List<Friend> friends) {
        mFriendsCustomAdapter.setData(friends);
        this.friendsRetrieved = friends;
    }

    @Override
    public void onLoaderReset(Loader<List<Friend>> listLoader) {
        mFriendsCustomAdapter.setData(null);
    }
}
