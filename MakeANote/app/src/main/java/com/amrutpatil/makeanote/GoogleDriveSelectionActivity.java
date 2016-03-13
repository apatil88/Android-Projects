package com.amrutpatil.makeanote;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

/**
 * Description: Class to select directories from Google Drive
 */
public class GoogleDriveSelectionActivity extends BaseGoogleDriveActivity {
    private static final int REQUEST_CODE_OPENER = 1;
    private static DriveId mDriveId;

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{DriveFolder.MIME_TYPE})
                .setActivityTitle("Choose image storage")
                .build(getGoogleApiClient());

        try{
            startIntentSenderForResult(intentSender, REQUEST_CODE_RESOLUTION, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e){
            //Error processing
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_OPENER:
                if(data != null && resultCode == RESULT_OK){
                    //Check to see if the data that is come back is what we expected
                    mDriveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    AppSharedPreferences.storeGoogleDriveResourceId(getApplicationContext(), mDriveId.getResourceId());
                    BaseActivity.actAsNote();
                    startActivity(new Intent(GoogleDriveSelectionActivity.this, GoogleDriveDirectoryNameGetterActivity.class));
                    finish();
                    return;
                } else{
                    startActivity(new Intent(GoogleDriveSelectionActivity.this, GoogleDriveDirectoryNameGetterActivity.class));
                    return;
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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

    @Override
    public void onConnectionSuspended(int i) {

    }
}
