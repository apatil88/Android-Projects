package com.amrutpatil.buttonclickcount;

import android.view.View;
import android.view.View.OnClickListener;

public class OurOnClickListener implements OnClickListener {
	
	MainActivity caller;
	private int count;
	
	public OurOnClickListener(MainActivity activity){
		this.caller = activity;
		this.count = 0;
	}

	@Override
	public void onClick(View v) {
		count = count + 1;
		/*if(count == 1){
			caller.theTextView.setText("This button was clicked " + count + " time");
		} else{
			caller.theTextView.setText("This button was clicked " + count + " times.");
		}*/
		String outputString = "This button itself has been clicked " + count + " time" ;
		if(count != 1){
			outputString += "s";
		}
		caller.theTextView.setText(outputString);
	}

}
