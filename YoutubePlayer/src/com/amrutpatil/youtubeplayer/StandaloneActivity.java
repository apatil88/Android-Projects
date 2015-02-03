package com.amrutpatil.youtubeplayer;

/**
 * @author Amrut
 * Class to play Youtube Videos
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.youtube.player.YouTubeStandalonePlayer;


public class StandaloneActivity extends Activity implements View.OnClickListener{

	
	public static final String GOOGLE_API_KEY = "AIzaSyCK1wqp7bAeqNWx2M-mAGECRm1tGvHgWDI"; //Get from console.developers.google.com
	
	public static final String YOUTUBE_VIDEO_ID ="3TtVsy98ces"; //Get from Youtube url
	
	public static final String YOUTUBE_PLAYLIST_ID="PL47ABB7D71762D488"; //Get from Youtube url
	
	private Button btnPlay;
	private Button btnPlaylist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.standalone);
		
		//Associate Button views
		btnPlay = (Button) findViewById(R.id.btnStart);
		btnPlaylist = (Button) findViewById(R.id.btnPlaylist);
		
		btnPlay.setOnClickListener(this);
		btnPlaylist.setOnClickListener(this);
	}
	
	public void onClick(View v){
		Intent intent = null;
		if(v == btnPlay){
			//Play a single video
			intent = YouTubeStandalonePlayer.createVideoIntent(this, GOOGLE_API_KEY, YOUTUBE_VIDEO_ID);
		}else if (v == btnPlaylist){
			//Play playlist
			intent = YouTubeStandalonePlayer.createPlaylistIntent(this, GOOGLE_API_KEY, YOUTUBE_PLAYLIST_ID);
		}
		
		if(intent != null){
			//startActivityForResult(intent, 1);  // requestCode = 1 indicates we want this to work. We will get an Activity result which we can process to know what happened.
												  // requestCode = 0 indicates we would not get an Activity result
			startActivity(intent); 
		}
	}
	

}
