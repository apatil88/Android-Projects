package com.amrutpatil.makeanote;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * Description: Class to connect to Google Drive
 */
public abstract class BaseGoogleDriveActivity extends Activity
                implements GoogleApiClient.ConnectionCallbacks,
                            GoogleApiClient.OnConnectionFailedListener {

    protected static final int REQUEST_CODE_RESOLUTION = 1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onResume() {
        super.onResume();
        if(mGoogleApiClient==null){
            //Intialize the Google Api Client
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        if(mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //If errors could not be resolved
        if(!connectionResult.hasResolution()){
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        try{
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e){
            //Put error message here
        }
    }

    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }
}
