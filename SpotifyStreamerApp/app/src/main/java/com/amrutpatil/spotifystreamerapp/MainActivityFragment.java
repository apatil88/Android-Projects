package com.amrutpatil.spotifystreamerapp;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment_container containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String SAVED_INSTANCE_KEY = "ARTIST_KEY";
    private static final String SAVED_INSTANCE_LIST = "ARTIST_LIST";
    private ArrayList<ArtistInfo> artistsList;
    private ArtistAdapter artistAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        EditText searchBox = (EditText) getActivity().findViewById(R.id.main_artist_search);
        outState.putString(SAVED_INSTANCE_KEY, searchBox.getEditableText().toString());
        outState.putParcelableArrayList(SAVED_INSTANCE_LIST, artistsList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ProgressBar loader = (ProgressBar) rootView.findViewById(R.id.main_pb_loader);

        //Search box to search artist
        EditText searchView = (EditText) rootView.findViewById(R.id.main_artist_search);
        // Artists list view
        final ListView artistsListView = (ListView) rootView.findViewById(R.id.main_artists_list);

        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVED_INSTANCE_KEY)) {
            artistsList = new ArrayList<ArtistInfo>();
        } else {
            searchView.setText(savedInstanceState.getString(SAVED_INSTANCE_KEY));
            artistsList = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_LIST);
        }

        artistAdapter = new ArtistAdapter(getActivity(), R.layout.artist_list_item, R.id.artistName, artistsList);
        artistsListView.setAdapter(artistAdapter);

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (isDeviceOffline()) {
                        Toast.makeText(getActivity(), getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    loader.setVisibility(View.VISIBLE);
                    // Get search text
                    String searchTerm = v.getEditableText().toString();

                    // Instantiate the Spotify API
                    SpotifyApi spotifyApi = new SpotifyApi();
                    SpotifyService spotifyService = spotifyApi.getService();

                    // Search for the artist
                    spotifyService.searchArtists(searchTerm, new Callback<ArtistsPager>() {
                        @Override
                        public void success(final ArtistsPager artistsPager, Response response) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    artistsList.clear();
                                    artistAdapter.clear();
                                    loader.setVisibility(View.GONE);   // Hide progress bar

                                    // If no match is found in the Spotify API
                                    if (artistsPager.artists.total == 0) {
                                        Toast.makeText(getActivity(), getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                                    } else {
                                        for (Artist a : artistsPager.artists.items) {
                                            String thumbnail = "";
                                            if (!a.images.isEmpty()) {
                                                thumbnail = a.images.get(a.images.size() - 1).url;
                                            }
                                            ArtistInfo artistInfo = new ArtistInfo(a.id, a.name, thumbnail);
                                            artistsList.add(artistInfo);
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void failure(final RetrofitError error) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loader.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    return true;
                }
                return false;
            }
        });
        return rootView;
    }

    //Connectivity Manager: http://developer.android.com/reference/android/net/ConnectivityManager.html
    private boolean isDeviceOffline() {
        boolean isDeviceOffline = true;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;

            // Validate first connection
            networkInfo = connectivityManager.getNetworkInfo(0);
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                isDeviceOffline = false;
            }

            // Validate second connection
            networkInfo = connectivityManager.getNetworkInfo(1);
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                isDeviceOffline = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return isDeviceOffline;
    }
}
