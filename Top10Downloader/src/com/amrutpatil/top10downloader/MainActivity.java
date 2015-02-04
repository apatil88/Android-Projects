package com.amrutpatil.top10downloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {

	Button btnParse;
	ListView listApps;
	String xmlData;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnParse = (Button) findViewById(R.id.btnParse);
		listApps = (ListView) findViewById(R.id.listApps);
		
		btnParse.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ParseApplications parse = new ParseApplications(xmlData);
				boolean operationStatus = parse.process();
				if(operationStatus){
					ArrayList<Application> allApps = parse.getApplications();  //Get all Application objects
					
					//Take entries in allApps and make an entry in ListView
					//Use a different layout for ListView (in our case list_item.xml is the different layout)
					ArrayAdapter<Application> adapter = new ArrayAdapter<Application>(MainActivity.this,R.layout.list_item, allApps);
					listApps.setVisibility(listApps.VISIBLE);
					listApps.setAdapter(adapter);
				}
				
			}
		});
		//Invoke the execution of the process(method) which downloads XML data
		//Top 10 Apps on iTunes Store : http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml
		//Top 10 songs on iTunes Store :http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml
		new DownloadData().execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml");
		//Also, add INTERNET permission access in AndroidManifest.xml
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
	
	//Class to download RSS feed. Should happen asynchronously
	//AsyncTask Params:
	//1st Param String: URL
	//2nd Param Void indicates progress of download
	//3rd Param String indicates what is returned by the calling process, in our case, contents of the XML file
	private class DownloadData extends AsyncTask<String, Void, String>{

		String myXmlData;     //Variable to store contents of XML file
		@Override
		protected String doInBackground(String... urls) {
			try{
				myXmlData = downloadXML(urls[0]);
			} catch(IOException e){
				return "Unable to download XML file";
			}
			return null;
		}
		
		protected void onPostExecute(String result){
			Log.d("onPostExecute", myXmlData);
			xmlData = myXmlData;   //indicates XML file has been downloaded successfully. Store contents of myXmlData into xmlData.
			
		}
		
		private String downloadXML(String theUrl) throws IOException{
			
			int BUFFER_SIZE = 2000;    //Number of characters to be parsed at a time
			InputStream is=null;
			
			String xmlContents = "";  //String to store raw XML data
			try{
				URL url = new URL(theUrl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();  //open a connection to the website
				conn.setReadTimeout(10000); //max time to wait for inputstream to read till it gives up
				conn.setConnectTimeout(15000); //max time to wait while connecting
				conn.setDoInput(true);
				conn.setRequestMethod("GET");
				int response = conn.getResponseCode();
				Log.d("DownloadXML", "The response returned is:" + response);
				is = conn.getInputStream();
				
				InputStreamReader isr = new InputStreamReader(is);   //Reader for the inputstream
				//Read character-by-character
				int charRead;
				char[] inputBuffer = new char[BUFFER_SIZE];   
				
				try{
					while((charRead = isr.read(inputBuffer)) > 0){
						String readString = String.copyValueOf(inputBuffer, 0, charRead);
						xmlContents += readString;
						inputBuffer = new char[BUFFER_SIZE]; //clear the buffer once the contents of the buffer have been read
					}
					return xmlContents;
					
				}catch (IOException e){
					e.printStackTrace();
					return null;
				}
			} finally{
				if(is!=null){
					is.close();
				}
			}
		}	
	}
}
