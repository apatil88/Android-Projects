/**
 * 
 */
package com.apatil.notynotes;

import com.apatil.notynotes.data.NoteItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * @author A.Patil
 * This class receives the values that were passed from the MainActivity, stores them persistently in the note object and display the note text so that user can add, edit or remove the note.
 */
public class NoteEditorActivity extends Activity {
	
	private NoteItem note;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_editor);
		//transforms the launcher icon into a back button. Available in HoneyComb or later
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Get a reference to the intent from the first activity
		Intent intent = this.getIntent();
		
		note = new NoteItem();
		//Reconstruct the Editor object as the EditorActivity starts up
		
		note.setKey(intent.getStringExtra("key"));
		note.setText(intent.getStringExtra("text"));
		
		//Get a reference to EditText control defined in the layout to display the text
		EditText et = (EditText) findViewById(R.id.noteText);
		et.setText(note.getText());
		
		//Whether the note is added or edited, place the cursor at the end of the existing text value
		et.setSelection(note.getText().length());
	}
	
	private void saveAndFinish(){
		//Get the text from the Editor Activity
		EditText et = (EditText) findViewById(R.id.noteText);
		String noteText = et.getText().toString();
		
		//Send the data from the Editor Activity back to the MainActivity
		
		Intent intent = new Intent();
		intent.putExtra("key", note.getKey());
		intent.putExtra("text", noteText);    //Pass the text value that the user has updated
		setResult(RESULT_OK, intent); //Send a message back to the MainActivity
		finish();  //Go back to the activity that called this one
	}
	
	//Method to handle the event when the user touches the launcher icon to go back
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home){
			saveAndFinish();
		}
		return false;
	}
	
	//Method to handle the event when the user presses the devices back button to go back
	@Override
	public void onBackPressed() {
		saveAndFinish();
	}
}
