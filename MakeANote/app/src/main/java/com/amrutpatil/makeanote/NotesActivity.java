package com.amrutpatil.makeanote;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<List<Note>>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private List<Note> mNotes;
    private RecyclerView mRecyclerView;
    private NotesAdapter mNotesAdapter;
    private ContentResolver mContentResolver;
    private static Boolean mIsInAuth;
    public static Bitmap mSendingImage = null;
    private boolean mIsImageNotFound = false;

    private DropboxAPI<AndroidAuthSession> mDropboxAPI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        activateToolbar();
        setUpForDropbox();
        setUpNavigationDrawer();
        setUpForRecyclerView();
        setUpActions();
    }

    private void setUpForDropbox(){
        //create a session
        AndroidAuthSession  session = DropboxActions.buildsession(getApplicationContext());
        mDropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
    }

    private void setUpForRecyclerView(){
        mContentResolver = getContentResolver();
        mNotesAdapter = new NotesAdapter(new ArrayList<Note>(), NotesActivity.this);
        int LOADER_ID = 1;
        getSupportLoaderManager().initLoader(LOADER_ID, null, NotesActivity.this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_home);
        GridLayoutManager linearlayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        linearlayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearlayoutManager);
        mRecyclerView.setAdapter(mNotesAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                edit(view);
            }

            @Override
            public void OnItemLongClick(View view, int position) {
                android.support.v7.widget.PopupMenu popupMenu = new android.support.v7.widget.PopupMenu(NotesActivity.this, view);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu_action_notes, popupMenu.getMenu());
                popupMenu.show();
                final View v = view;
                final int pos = position;
                popupMenu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.action_delete){
                            moveToTrash();
                            delete(v, pos);
                        } else if (item.getItemId() == R.id.action_archive){
                            moveToArchive(v, pos);
                        } else if (item.getItemId() == R.id.action_edit){
                            edit(v);
                        }
                        return false;
                    }
                });
            }
        }));
    }

    @Override
    public Loader<List<Note>> onCreateLoader(int id, Bundle args) {
        mContentResolver = getContentResolver();
        return new NotesLoader(NotesActivity.this, BaseActivity.mType, mContentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<Note>> loader, List<Note> data) {
        this.mNotes = data;
        //Retrieve the image from local storage/Google Drive/Dropbox in a separate thread
        Thread[] threads = new Thread[mNotes.size()];
        int threadCounter = 0;

        for(final Note aNote : mNotes){
            //If the note is coming from Google Drive
            if(AppConstant.GOOGLE_DRIVE_SELECTION == aNote.getStorageSelection()){
                GDUT.init(this);
                //Check if Google Drive is accessible and if the user account has been logged in successfully
                if(checkPlayServices() && checkUserAccount()){
                    GDActions.init(this, GDUT.AM.getActiveEmil());
                    GDActions.connect(true);
                }

                threads[threadCounter] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        do{
                            //Get the image file
                            ArrayList<GDActions.GF> gfs = GDActions.search(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                                    aNote.getImagePath(), GDUT.MIME_JPEG);

                            if(gfs.size() > 0){
                                //Retrieve the file, convert it into Bitmap to display on the screen
                                byte[] imageBytes = GDActions.read(gfs.get(0).id, 0);

                                //Process the entire byte array and convert it into an image
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                aNote.setBitmap(bitmap);
                                mIsImageNotFound = false;
                                mNotesAdapter.setData(mNotes);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Tell the Adapter that a graphic image has been obtained
                                        mNotesAdapter.notifyImageObtained();
                                    }
                                });
                            } else{
                                aNote.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_loading));
                                mIsImageNotFound = true;
                                try{
                                    Thread.sleep(500);
                                } catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                            }
                        }while(mIsImageNotFound);
                    }
                });
            } else if (AppConstant.DROP_BOX_SELECTION == aNote.getStorageSelection()){
                threads[threadCounter] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        do{
                            Drawable drawable = getImageFromDropbox(mDropboxAPI,
                                    AppSharedPreferences.getDropBoxUploadPath(getApplicationContext()),
                                    aNote.getImagePath());
                            if(drawable != null) {
                                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                                aNote.setBitmap(bitmap);
                            }
                            if(!mIsImageNotFound){
                                mNotesAdapter.setData(mNotes);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mNotesAdapter.notifyImageObtained();
                                    }
                                });
                            }
                            try{
                                Thread.sleep(500);
                            } catch (InterruptedException e){
                                e.printStackTrace();
                            }

                        }while(mIsImageNotFound);
                    }
                });
                threads[threadCounter].start();
                threadCounter++;
            } else {
                aNote.setHasNoImage(true);
            }
        }
        mNotesAdapter.setData(mNotes);  //Adapter has the latest copy of note
        changeNoItemTag();
    }

    //Method checks if the path to file is valid
    //Method looks for the file we are looking for, grabs it and returns it to the calling process
    private Drawable getImageFromDropbox(DropboxAPI<?> mApi, String mPath, String filename) {
        FileOutputStream fos;
        Drawable drawable;
        //Check to see if we have cached the file on the local device. If we found the file on the device, retrieve it
        String cachePath = getApplicationContext().getCacheDir().getAbsolutePath() + "/" + filename;
        File cacheFile = new File(cachePath);
        if (cacheFile.exists()) {
            mIsImageNotFound = false;
            return Drawable.createFromPath(cachePath);
        } else { //If we did not find the file, go to Dropbox to retrieve it
            try {
                DropboxAPI.Entry dirEnt = mApi.metadata(mPath, 1000, null, true, null);
                //If the path is not a directory path or if it is null
                if (!dirEnt.isDir || dirEnt.contents == null) {
                    mIsImageNotFound = true;
                }
                ArrayList<DropboxAPI.Entry> thumbs = new ArrayList<DropboxAPI.Entry>();
                for (DropboxAPI.Entry ent : dirEnt.contents) {
                    if (ent.thumbExists) {
                        if (ent.fileName().startsWith(filename)) {
                            thumbs.add(ent);
                        }
                    }
                }
                if (thumbs.size() == 0) {
                    mIsImageNotFound = true;
                } else {
                    //Grab the image from Dropbox
                    DropboxAPI.Entry ent = thumbs.get(0);
                    String path = ent.path;
                    try {
                        fos = new FileOutputStream(cachePath);

                    } catch (FileNotFoundException e) {
                        return getResources().getDrawable(R.drawable.ic_image_deleted);
                    }
                    mApi.getThumbnail(path, fos, DropboxAPI.ThumbSize.BESTFIT_960x640,
                            DropboxAPI.ThumbFormat.JPEG, null);
                    drawable = Drawable.createFromPath(cachePath);
                    mIsImageNotFound = false;
                    return drawable;
                }
            } catch (DropboxException e) {
                e.printStackTrace();
                mIsImageNotFound = true;
            }

            drawable = getResources().getDrawable(R.drawable.ic_loading);
            return drawable;
        }
    }

    //Method to show/hide the RecyclerView depending on item availability
    private void changeNoItemTag(){
        TextView mItemTextView = (TextView) findViewById(R.id.no_item_textview);
        //if there are items to show
        if(mNotesAdapter.getItemCount() != 0){
            mItemTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }else{
            mItemTextView.setText(AppConstant.EMPTY);
            mItemTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Note>> loader) {
        mNotesAdapter.setData(null);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(!mIsInAuth){
            try{
                mIsInAuth = true;
                if(connectionResult.hasResolution()){
                    connectionResult.startResolutionForResult(this, AppConstant.REQ_AUTH);
                }
            } catch (IntentSender.SendIntentException e){
                e.printStackTrace();
                finish();
            }
        } else{
            finish();
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    //Method to check for valid primary Google account on the device
    private boolean checkUserAccount(){
        String email = GDUT.AM.getActiveEmil();
        Account account = GDUT.AM.getPrimaryAccnt(true);
        if(email == null){
            if(account == null){
                //If no account is found, pop up an account picker
                account = GDUT.AM.getPrimaryAccnt(false);
                String[] allowableAccountType = {GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
                Intent accountIntent = AccountPicker.newChooseAccountIntent(account, null, allowableAccountType, true,
                        null, null, null, null);
                startActivityForResult(accountIntent, AppConstant.REQ_ACCPICK);
                return false;
            } else{
                GDUT.AM.setEmil(account.name);
            }
            return true;
        }
        account = GDUT.AM.getActiveAccnt();
        if(account == null) {
            String[] allowableAccountType = {GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
            Intent accountIntent = AccountPicker.newChooseAccountIntent(account, null, allowableAccountType, true,
                    null, null, null, null);
            startActivityForResult(accountIntent, AppConstant.REQ_ACCPICK);
        }
        return true;
    }

    //Method to check if Google Play Services is online
    private boolean checkPlayServices(){
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        //int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if( status != ConnectionResult.SUCCESS){
            /*if(GooglePlayServicesUtil.isUserRecoverableError(status)){
                errorDialog(status, AppConstant.REQ_RECOVER);
            }*/
            if(GoogleApiAvailability.getInstance().isUserResolvableError(status)){
                errorDialog(status, AppConstant.REQ_RECOVER);
            }
            else{
                finish();
            }
            return false;
        }
        return true;
    }

    //Method to display error if connection to Google Play Services fails
    private void errorDialog(int errorCode, int requestCode){
        Bundle args = new Bundle();
        args.putInt(AppConstant.DIALOG_ERROR, errorCode);
        args.putInt(AppConstant.REQUEST_CODE, requestCode);
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        errorDialogFragment.setArguments(args);
        errorDialogFragment.show(getFragmentManager(), AppConstant.DIALOG_ERROR);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void moveToTrash(){
        ContentValues contentValues = new ContentValues();
        TextView title = (TextView) findViewById(R.id.title_note_custom_home);
        TextView description = (TextView) findViewById(R.id.description_note_custom_home);
        TextView dateTime = (TextView) findViewById(R.id.date_time_note_custom_home);

        contentValues.put(TrashContract.TrashColumns.TRASH_TITLE, title.getText().toString());
        contentValues.put(TrashContract.TrashColumns.TRASH_DESCRIPTION, description.getText().toString());
        contentValues.put(TrashContract.TrashColumns.TRASH_DATE_TIME, dateTime.getText().toString());

        ContentResolver cr = this.getContentResolver();
        Uri uri = TrashContract.URI_TABLE;
        cr.insert(uri, contentValues);
    }

    private void moveToArchive(View view, int position){
        ContentValues contentValues = new ContentValues();
        TextView title = (TextView) findViewById(R.id.title_note_custom_home);
        TextView description = (TextView) findViewById(R.id.description_note_custom_home);
        TextView dateTime = (TextView) findViewById(R.id.date_time_note_custom_home);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.home_list);
        int isList = linearLayout.getVisibility();
        String listDescription = "";
        if(isList == View.VISIBLE){
            //If a note contains list of items
            NoteCustomList noteCustomList = (NoteCustomList) linearLayout.getChildAt(0);
            for(int i = 0 ; i < noteCustomList.getChildCount(); i++){
                LinearLayout first = (LinearLayout) linearLayout.getChildAt(i);
                CheckBox checkBox = (CheckBox) first.getChildAt(0);
                TextView textView = (TextView) first.getChildAt(1);
                listDescription = description + textView.toString() + checkBox.isChecked() + "%";
            }
            contentValues.put(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE, AppConstant.LIST);
        } else{
            listDescription = description.getText().toString();
            contentValues.put(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE, AppConstant.NORMAL);
        }

        contentValues.put(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION, listDescription);
        contentValues.put(ArchivesContract.ArchivesColumns.ARCHIVES_TITLE, title.getText().toString());
        contentValues.put(ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME, dateTime.getText().toString());
        contentValues.put(ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY, mTitle);

        ContentResolver cr = this.getContentResolver();
        Uri uri = ArchivesContract.URI_TABLE;
        cr.insert(uri, contentValues);
        delete(view, position);
    }

    private void delete(View view, int position){
        ContentResolver cr = this.getContentResolver();
        //Get the ID of the note that is to be deleted
        String _ID = ((TextView) view.findViewById(R.id.id_note_custom_home)).getText().toString();
        Uri uri = NotesContract.Notes.buildNoteUri(_ID);
        cr.delete(uri, null, null);
        mNotesAdapter.delete(position);
        changeNoItemTag();

    }

    //Method to edit a note
    private void edit(View view){
        Intent intent = new Intent(NotesActivity.this, NoteDetailActivity.class);
        String id = ((TextView) view.findViewById(R.id.id_note_custom_home)).getText().toString();
        intent.putExtra(AppConstant.ID, id);

        //If it is a custom note list
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.home_list);
        int isList = linearLayout.getVisibility();
        if(isList == View.VISIBLE){
            intent.putExtra(AppConstant.LIST, AppConstant.TRUE);
        }

        ImageView tempImageView = (ImageView) view.findViewById(R.id.image_note_custom_home);
        if(tempImageView.getDrawable() != null){
            mSendingImage = ((BitmapDrawable) tempImageView.getDrawable()).getBitmap();
        }
        startActivity(intent);
    }

    //Method to handle Account picker errors
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstant.REQ_ACCPICK: {
                //If we picked a valid email account
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (GDUT.AM.setEmil(email) == GDUT.AM.CHANGED) {
                        GDActions.init(this, GDUT.AM.getActiveEmil());
                        GDActions.connect(true);
                    }
                } else if (GDUT.AM.getActiveEmil() == null) { // if we do not have a valid email from the account picker
                    GDUT.AM.removeActiveAccnt();
                    finish();
                }
                break;
            }

            case AppConstant.REQ_AUTH:

            case AppConstant.REQ_RECOVER:{
                mIsInAuth = false;
                if(resultCode == Activity.RESULT_OK){
                    GDActions.connect(true);
                } else if(resultCode == RESULT_CANCELED){
                    GDUT.AM.removeActiveAccnt();
                    finish();
                }
            }
            break;
        }
    }
}
