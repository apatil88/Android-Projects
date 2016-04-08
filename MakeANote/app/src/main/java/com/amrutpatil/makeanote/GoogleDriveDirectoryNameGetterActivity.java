package com.amrutpatil.makeanote;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;

/**
 * Description: Class to get the name of the Google Drive directory to which the image storage is set to.
 * Reference :https://developers.google.com/drive/android/folders#get_the_root_folder
 *            https://developers.google.com/drive/android/metadata#custom_properties
 */
public class GoogleDriveDirectoryNameGetterActivity extends BaseGoogleDriveActivity {

    private static final String TAG = GoogleDriveDirectoryNameGetterActivity.class.getCanonicalName();
    private static final int REQUEST_CODE = 101;

    private final ResultCallback<DriveResource.MetadataResult> mMetadataCallback =
            new ResultCallback<DriveResource.MetadataResult>() {
        @Override
        public void onResult(DriveResource.MetadataResult metadataResult) {
            if(!metadataResult.getStatus().isSuccess()){
                showMessage("Problem trying to fetch metadata");
                return;
            }

            Metadata metadata = metadataResult.getMetadata();
            AppSharedPreferences.storeGoogleDriveUploadFileName(getApplicationContext(), metadata.getTitle());
            startActivity(new Intent(GoogleDriveDirectoryNameGetterActivity.this, NotesActivity.class));
            finish();
        }
    };

    private final ResultCallback<DriveApi.DriveIdResult> mIdCallback =
            new ResultCallback<DriveApi.DriveIdResult>() {
        @Override
        public void onResult(DriveApi.DriveIdResult driveIdResult) {
            if(!driveIdResult.getStatus().isSuccess()){
                showMessage("Cannot find Drive ID. Are you authorised to use this file?");
                return;
            }

            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), driveIdResult.getDriveId());
            file.getDriveId().encodeToString();
            file.getMetadata(getGoogleApiClient()).setResultCallback(mMetadataCallback);
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Drive.DriveApi.fetchDriveId(getGoogleApiClient(), AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext())).setResultCallback(mIdCallback);
            AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), AppConstant.GOOGLE_DRIVE_SELECTION);
            AppSharedPreferences.isGoogleDriveAuthenticated(getApplicationContext(), true);
            showMessage("Image location set in Google Drive");
        } catch(IllegalStateException e){
            //this can happen when a newly created directory is selected and Google Drive has not synced with it yet
            showMessage("An error occured while selecting this folder. Sync issue? Please try again");
            startActivity(new Intent(GoogleDriveDirectoryNameGetterActivity.this, GoogleDriveSelectionActivity.class));
            finish();
        } catch (IllegalArgumentException e){
            showMessage("An error occured while selecting this folder. Sync issue? Please try again");
            startActivity(new Intent(GoogleDriveDirectoryNameGetterActivity.this, GoogleDriveSelectionActivity.class));
            finish();
        }
    }


    /*callback when there there's an error connecting the client to the service.*/
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed");
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            Log.i(TAG, "trying to resolve the Connection failed error...");
            result.startResolutionForResult(this, REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        switch (i) {
            case 1:
                Log.i(TAG, "Connection suspended - Cause: " + "Service disconnected");
                break;
            case 2:
                Log.i(TAG, "Connection suspended - Cause: " + "Connection lost");
                break;
            default:
                Log.i(TAG, "Connection suspended - Cause: " + "Unknown");
                break;
        }
    }
}
