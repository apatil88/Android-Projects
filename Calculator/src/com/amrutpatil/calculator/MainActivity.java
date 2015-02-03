package com.amrutpatil.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	EditText operand1;
	EditText operand2;
	Button btnAdd;
	Button btnSubtract;
	Button btnDivide;
	Button btnMultiply;
	Button btnClear;
	TextView result;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Get Reference to EditText view
		//Operand fields from the main screen
		operand1 = (EditText) findViewById(R.id.editOperand1);
		operand2 = (EditText) findViewById(R.id.editOperand2);
		
		//Get Reference to Button view
		//Associate Buttons
		btnAdd = (Button) findViewById(R.id.btnAdd);
		btnSubtract = (Button) findViewById(R.id.btnSubtract);
		btnDivide = (Button) findViewById(R.id.btnDivide);
		btnMultiply = (Button) findViewById(R.id.btnMultiply);
		btnClear = (Button) findViewById(R.id.btnClear);
		
		//Associate result textfield
		result = (TextView) findViewById(R.id.textResult);
		
		//Add functionality to process results
		//Inner class concept
		
		//Add calculator processing
		btnAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				float oper1 = Float.parseFloat(operand1.getText().toString());  //Get value from operand 1 which is in string format. Convert it into float
				float oper2 = Float.parseFloat(operand2.getText().toString());
				
				Float theResult = oper1 + oper2;
				result.setText(Float.toString(theResult));
			}
		});
		
		//Subtract calculator processing
		btnSubtract.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				float oper1 = Float.parseFloat(operand1.getText().toString());  //Get value from operand 1 which is in string format. Convert it into float
				float oper2 = Float.parseFloat(operand2.getText().toString());
				
				Float theResult = oper1 - oper2;
				result.setText(Float.toString(theResult));
			}
		});
		
		//Divide calculator processing
		btnDivide.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				float oper1 = Float.parseFloat(operand1.getText().toString());  //Get value from operand 1 which is in string format. Convert it into float
				float oper2 = Float.parseFloat(operand2.getText().toString());
				
				Float theResult = oper1 / oper2;
				result.setText(Float.toString(theResult));
			}
		});
		
		//Multiply calculator processing
		btnMultiply.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				float oper1 = Float.parseFloat(operand1.getText().toString());  //Get value from operand 1 which is in string format. Convert it into float
				float oper2 = Float.parseFloat(operand2.getText().toString());
				
				Float theResult = oper1 * oper2;
				result.setText(Float.toString(theResult));
			}
		});			
		
		//Add calculator processing
		btnClear.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				operand1.setText("");
				operand2.setText("");
				result.setText("0.0");
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
