package com.amrutpatil.spotifystreamerapp;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment_container containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    // Log Tag
    private static final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();
    // Saved instance key
    private static final String SAVED_KEY = "TRACK_KEY";
    // Saved instance list
    private static final String SAVED_LIST = "TRACK_LIST";
    // List of tracks
    private ArrayList<TrackInfo> tracksList;
    // Track adapter
    private TrackAdapter trackAdapter;

    private String artistId;
    private static final String ARTIST_ID = "id";

    public TopTracksActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_KEY, artistId);
        outState.putParcelableArrayList(SAVED_LIST, tracksList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get artist id
        artistId = getActivity().getIntent().getStringExtra(ARTIST_ID);
        return inflater.inflate(R.layout.fragment_top_tracks, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView tracksListView = (ListView) getActivity().findViewById(R.id.top_tracks_list);

        tracksList = new ArrayList<TrackInfo>();

        trackAdapter = new TrackAdapter(getActivity(), R.layout.top_tracks_list_item, R.id.row_track_tv_name, tracksList);
        tracksListView.setAdapter(trackAdapter);

        final ProgressBar loader = (ProgressBar) getActivity().findViewById(R.id.top_pb_loader);

        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVED_KEY)) {

            // Instantiate the Spotify API
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();

            // Set options
            Map options = new HashMap();
            // Set country code to US
            options.put("country", "US");
            spotifyService.getArtistTopTrack(artistId, options, new Callback<Tracks>() {
                @Override
                public void success(final Tracks tracks, Response response) {
                    Log.d(LOG_TAG, tracks.tracks.toString());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tracks.tracks.isEmpty()) {
                                loader.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                            } else {
                                for (Track topTrack : tracks.tracks) {
                                    String thumbnail = "";
                                    if (!topTrack.album.images.isEmpty()) {
                                        thumbnail = topTrack.album.images.get(topTrack.album.images.size() - 1).url;
                                    }
                                    TrackInfo trackEntity = new TrackInfo(topTrack.name, topTrack.album.name, thumbnail);
                                    Log.v(LOG_TAG, topTrack.name);
                                    tracksList.add(trackEntity);
                                }
                                loader.setVisibility(View.GONE);
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
        } else {
            tracksList = savedInstanceState.getParcelableArrayList(SAVED_LIST);
            trackAdapter.clear();
            trackAdapter.addAll(tracksList);
            loader.setVisibility(View.GONE);
        }
    }
}
