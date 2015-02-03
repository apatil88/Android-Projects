package com.amrutpatil.youtubeplayer;

/**
 * @author Amrut
 * Class to play Youtube Videos
 */

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

//Add INTERNET Permissions in AndroidManifest.xml

public class YoutubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

	
	public static final String GOOGLE_API_KEY = "AIzaSyCK1wqp7bAeqNWx2M-mAGECRm1tGvHgWDI"; //Get from console.developers.google.com
	
	public static final String YOUTUBE_VIDEO_ID ="3TtVsy98ces"; //Get from Youtube url
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.youtube);
		//Associate youtube player view
		YouTubePlayerView youtube = (YouTubePlayerView) findViewById(R.id.youtube_player);  
		
		youtube.initialize(GOOGLE_API_KEY, this);  //Pass API key to access functionality
	}
	@Override
	public void onInitializationFailure(Provider provider,
			YouTubeInitializationResult result) {
		Toast.makeText(this, "Cannot intialize", Toast.LENGTH_LONG).show();
		
		
	}

	@Override
	public void onInitializationSuccess(Provider provider, YouTubePlayer player,
			boolean wasRestored) {
		player.setPlayerStateChangeListener(playerStateChangeListener);
		player.setPlaybackEventListener(playbackEventListener);
		
		if(!wasRestored){
			player.cueVideo(YOUTUBE_VIDEO_ID);
		}
		
	}
	
	private PlaybackEventListener playbackEventListener = new PlaybackEventListener(){

		@Override
		public void onBuffering(boolean arg0) {

			
		}

		@Override
		public void onPaused() {
			
		}

		@Override
		public void onPlaying() {
			
		}

		@Override
		public void onSeekTo(int arg0) {
			
			
		}

		@Override
		public void onStopped() {
			
			
		}
		
	};
	
	
	
	private PlayerStateChangeListener playerStateChangeListener = new PlayerStateChangeListener(){

		@Override
		public void onAdStarted() {
	
			
		}

		@Override
		public void onError(ErrorReason arg0) {
	
			
		}

		@Override
		public void onLoaded(String arg0) {
		
			
		}

		@Override
		public void onLoading() {
	
		}

		@Override
		public void onVideoEnded() {
		
			
		}

		@Override
		public void onVideoStarted() {
	
			
		}
		
	};
	

}
