package com.amrutpatil.top10downloader;

import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

/**
 * @author Amrut
 * Class to parse raw XML data
 */
public class ParseApplications {
	
	private String data;    //variable to store xml data
	private ArrayList<Application> applications;  //array of Application class objects. Each object contains information about name, artist and release date

	public ParseApplications(String xmlData){
		data = xmlData;    //coming from MainActivity class
		applications = new ArrayList<Application>();  //intialize the ArrayList
	}
	
	//MainActivity will call this method to get the final list of applications.
	public ArrayList<Application> getApplications() {
		return applications;
	}
	
	public boolean process(){
		boolean operationStatus = true;  //track parsing operation status
		Application currentRecord = null;  //Application class object which stores the required values
		boolean inEntry = false;  //check whether we are in <entry>..</entry> block in raw XML data
		String textValue="";   //to store value of name, artist and release date
		
		try{
			//parsing XML raw data
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			
			xpp.setInput(new StringReader(this.data));  //converts the data into a format suitable for parsing
			int eventType = xpp.getEventType();
			
			//Keep parsing till the end of document
			while(eventType != XmlPullParser.END_DOCUMENT){
				String tagName = xpp.getName();   //returns all tags from XML e.g. name, artist, reader, etc.
				if (eventType == XmlPullParser.START_TAG){
					if(tagName.equalsIgnoreCase("entry")){   //if we encounter <entry> tag
						inEntry = true;
						currentRecord = new Application();
					}	
				} else if (eventType == XmlPullParser.TEXT){
					textValue = xpp.getText();    //store the text (which is not a tag) into textValue
				} else if (eventType == XmlPullParser.END_TAG){
					if(inEntry){
						// Reached </entry> tag
						if(tagName.equalsIgnoreCase("entry")){
							applications.add(currentRecord);   //add the Application object to the ArrayList of application objects
							inEntry = false;
						}
						if(tagName.equalsIgnoreCase("name")){
							currentRecord.setName(textValue);
						}else if(tagName.equalsIgnoreCase("artist")){
							currentRecord.setArtist(textValue);
						} else if(tagName.equalsIgnoreCase("releaseDate")){
							currentRecord.setReleaseDate(textValue);
						}
					}
				}
				eventType = xpp.next();    //move on to the next tag
			}
		}catch(Exception e){
			e.printStackTrace();
			operationStatus = false;  //parse operation failed
		}
		
		for(Application app: applications){
			Log.d("LOG", "*******************");
			Log.d("LOG", app.getName());
			Log.d("LOG", app.getArtist());
			Log.d("LOG", app.getReleaseDate());
		}
		return operationStatus;
	}

	
}
