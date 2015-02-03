package com.amrutpatil.buttonclickcount;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button ourButton;
	TextView theTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ourButton = (Button) findViewById(R.id.button1);  //Get Reference to the button
		ourButton.setOnClickListener(new OurOnClickListener(this));  //Handle event when button is clicked
		 
		theTextView = (TextView) findViewById(R.id.textView1); //Get Reference to the view
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
			Toast toastMessage = Toast.makeText(this, "The settings menu item was tapped and the button text is "+ theTextView.getText(), Toast.LENGTH_LONG);
			//theTextView.setText("zero");
			toastMessage.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
