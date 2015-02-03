package com.amrutpatil.youtubeplayer;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	
	Button btnPlay;
	Button btnStandalone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Associate Button view
		btnPlay = (Button) findViewById(R.id.btnPlay);
		btnPlay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Invoke the YoutubeActivity using Intents
				Intent intent = new Intent(MainActivity.this, YoutubeActivity.class); //MainActivity invokes the YoutubeActivity
				startActivity(intent);	
			}
		});
		
		
		btnStandalone = (Button) findViewById(R.id.btnSubMenu);
		btnStandalone.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Invoke the StandaloneActivity using Intents
				Intent intent = new Intent(MainActivity.this, StandaloneActivity.class); //MainActivity invokes the StandaloneActivity
				startActivity(intent);	
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
