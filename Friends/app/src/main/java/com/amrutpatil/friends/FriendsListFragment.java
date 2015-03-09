package com.amrutpatil.friends;

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.util.List;

/**
 * Created by Amrut on 3/7/15.
 */

//LoaderManager loads the data from the Content Provider, which in turn, loads the data from Sqlite database
public class FriendsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Friend>>{

    private static final String LOG_TAG = FriendsListFragment.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private FriendsCustomAdapter mAdapter;
    private ContentResolver mContentResolver;
    private List<Friend> mFriends;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
        mContentResolver = getActivity().getContentResolver();
        mAdapter = new FriendsCustomAdapter(getActivity(), getChildFragmentManager());

        setEmptyText("No Friends");
        setListAdapter(mAdapter);
        setListShown(false);

        //Intialize the Loader Manager and ask the Loader Manager to return the callbacks to this class (FriendsListFragment)
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    //Create the Loader
    @Override
    public Loader<List<Friend>> onCreateLoader(int i, Bundle bundle) {

        mContentResolver = getActivity().getContentResolver();
        return new FriendsListLoader(getActivity(), FriendsContract.URI_TABLES, mContentResolver);
    }

    //Mechanism when load is finished
    @Override
    public void onLoadFinished(Loader<List<Friend>> listLoader, List<Friend> friends) {

        //set the Adapter
        mAdapter.setData(friends);

        mFriends = friends;
        if(isResumed()){
            setListShown(true);
        }else{
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Friend>> listLoader) {
        mAdapter.setData(null);
    }
}
