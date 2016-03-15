package com.amrutpatil.makeanote;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.Calendar;

/**
 * Description: Class to create and edit notes. This class also sets reminders and storage location.
 */
public class NoteDetailActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static int sMonth, sYear, sHour, sDay, sMinute, sSecond;
    private DropboxAPI<AndroidAuthSession> mApi;
    private File mDropBoxFile;
    private String mCameraFileName;
    private NoteCustomList mNoteCustomList;

    // Constants
    public static final int NORMAL = 1;
    public static final int LIST = 2;
    public static final int CAMERA_REQUEST = 1888;
    public static final int TAKE_GALLERY_CODE = 1;

    private static TextView sDateTextView, sTimeTextView;
    private static boolean sIsInAuth;
    private static String sTmpFlNm;

    private EditText mTitleEditText, mDescriptionEditText;
    private ImageView mNoteImage;
    private String mImagePath = AppConstant.NO_IMAGE;
    private String mId;
    private boolean mGoingToCameraOrGallery = false, mIsEditing = false;
    private boolean mIsImageSet = false;
    private boolean mIsList = false;
    private Bundle mBundle;
    private ImageView mStorageSelection;
    private boolean mIsNotificationMode = false;
    private String mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBundle = savedInstanceState;
        setContentView(R.layout.activity_detail_note_layout);
        activateToolbarWithHomeEnabled();
        if (getIntent().getStringExtra(AppConstant.LIST_NOTES) != null) {
            intializeComponents(LIST);
        } else {
            intializeComponents(NORMAL);
        }

        setUpIfEditing();
        if (getIntent().getStringExtra(AppConstant.GO_TO_CAMERA) != null) {
            callCamera();
        }
    }

    private void setUpIfEditing() {
        if (getIntent().getStringExtra(AppConstant.ID) != null) {
            mId = getIntent().getStringExtra(AppConstant.ID);
            mIsEditing = true;
            if (getIntent().getStringExtra(AppConstant.LIST_NOTES) != null) {
                intializeComponents(LIST);
            }
            setValues(mId);
            mStorageSelection.setEnabled(false);

            //If the user has clicked a notification
            if (getIntent().getStringExtra(AppConstant.REMINDER) != null) {
                Note aNote = new Note(getIntent().getStringExtra(AppConstant.REMINDER));
                mId = aNote.getId() + "";
                mIsNotificationMode = true;
                setValues(aNote);
                removeFromReminder(aNote);
                mStorageSelection.setEnabled(false);

            }
        }
    }


    private void setValues(String id) {
        String[] projection = {BaseColumns._ID,
                NotesContract.NotesColumns.NOTES_TITLE,
                NotesContract.NotesColumns.NOTES_DESCRIPTION,
                NotesContract.NotesColumns.NOTES_DATE,
                NotesContract.NotesColumns.NOTES_IMAGE,
                NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION,
                NotesContract.NotesColumns.NOTES_TIME};
        // Query database - check parameters to return only partial records.
        Uri r = NotesContract.URI_TABLE;
        String selection = NotesContract.NotesColumns.NOTES_ID + " = " + id;
        Cursor cursor = getContentResolver().query(r, projection, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DESCRIPTION));
                    String time = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TIME));
                    String date = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DATE));
                    String image = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE));
                    int storageSelection = cursor.getInt(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION));
                    mTitleEditText.setText(title);
                    if (mIsList) {
                        CardView cardView = (CardView) findViewById(R.id.card_view);
                        cardView.setVisibility(View.GONE);
                        setUpList(description);
                    } else {
                        mDescriptionEditText.setText(description);
                    }
                    sTimeTextView.setText(time);
                    sDateTextView.setText(date);
                    mImagePath = image;

                    if (!image.equals(AppConstant.NO_IMAGE)) {
                        mNoteImage.setImageBitmap(NotesActivity.mSendingImage);
                    }

                    switch (storageSelection) {
                        case AppConstant.GOOGLE_DRIVE_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_google_drive, AppConstant.GOOGLE_DRIVE_SELECTION);
                            break;

                        case AppConstant.DEVICE_SELECTION:
                        case AppConstant.NONE_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_local, AppConstant.DEVICE_SELECTION);
                            break;

                        case AppConstant.DROP_BOX_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_dropbox, AppConstant.DROP_BOX_SELECTION);
                            break;
                    }
                } while (cursor.moveToNext());
            }
        }
    }

    //This method is called when the user taps the reminder notification to display the contents of the note.
    //Grab the data from the note object instead of the content provider.
    private void setValues(Note note){
        getSupportActionBar().setTitle(AppConstant.REMINDERS);
        String title = note.getTitle();
        String description = note.getDescription();
        String date = note.getDate();
        String time = note.getTime();
        String image = note.getImagePath();
        if(note.getType().equals(AppConstant.LIST)){
            mIsList = true;
        }
        mTitleEditText.setText(title);
        if(mIsList){
            intializeComponents(LIST);
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            setUpList(description);
        } else{
            mDescriptionEditText.setText(description);
        }
        sTimeTextView.setText(time);
        sDateTextView.setText(date);
        mImagePath = image;
        int storageLocation = note.getStorageSelection();

        switch (storageLocation) {
            case AppConstant.GOOGLE_DRIVE_SELECTION:
                updateStorageSelection(null, R.drawable.ic_google_drive, AppConstant.GOOGLE_DRIVE_SELECTION);
                break;

            case AppConstant.DEVICE_SELECTION:
            case AppConstant.NONE_SELECTION:
                if(!mImagePath.equals(AppConstant.NO_IMAGE)) {
                    updateStorageSelection(null, R.drawable.ic_local, AppConstant.DEVICE_SELECTION);
                }
                break;

            case AppConstant.DROP_BOX_SELECTION:
                updateStorageSelection(BitmapFactory.decodeFile(mImagePath), R.drawable.ic_dropbox, AppConstant.DROP_BOX_SELECTION);
                break;

            default:
                break;
        }
    }

    private void updateStorageSelection(Bitmap bitmap, int storageSelectionResource, int selection){
        if(bitmap != null){
            mNoteImage.setImageBitmap(bitmap);
        }
        mStorageSelection.setBackgroundResource(storageSelectionResource);
        AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), selection);

    }

    private void setUpList(String description){
        mDescription = description;
        if(!mIsNotificationMode){
            mNoteCustomList.setUpForEditMode(description);
        } else{
            LinearLayout newItemLayout = (LinearLayout) findViewById(R.id.add_check_list_layout);
            newItemLayout.setVisibility(View.GONE);
            mNoteCustomList.setUpForListNotification(description);
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.add_check_list_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteCustomList.addNewCheckbox();
            }
        });
    }

    private void intializeComponents(int choice){
        if(choice == LIST){
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.VISIBLE);
            mIsList = true;
        } else if (choice == NORMAL){
            CardView cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.GONE);
            mIsList = false;
        }

        mStorageSelection = (ImageView) findViewById(R.id.image_storage);
        if(AppSharedPreferences.getUploadPreference(getApplicationContext()) == AppConstant.GOOGLE_DRIVE_SELECTION ){
            mStorageSelection.setBackgroundResource(R.drawable.ic_google_drive);
        } else if(AppSharedPreferences.getUploadPreference(getApplicationContext()) == AppConstant.DROP_BOX_SELECTION){
            mStorageSelection.setBackgroundResource(R.drawable.ic_dropbox);
        } else {
            mStorageSelection.setBackgroundResource(R.drawable.ic_local);
        }
        mNoteCustomList = new NoteCustomList(this);
        mNoteCustomList.setUp();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.check_list_layout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(mNoteCustomList);

        mStorageSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(NoteDetailActivity.this, v);
                final MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.actions_image_selection, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_device) {
                            updateStorageSelection(null, R.drawable.ic_local, AppConstant.DEVICE_SELECTION);
                        } else if (menuItem.getItemId() == R.id.action_google_drive) {
                            if (!AppSharedPreferences.isGoogleDriveAuthenticated(getApplicationContext())) {
                                startActivity(new Intent(NoteDetailActivity.this, GoogleDriveSelectionActivity.class));
                                finish();
                            } else if (menuItem.getItemId() == R.id.action_dropbox) {
                                AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), AppConstant.DROP_BOX_SELECTION);
                                if (!AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext())) {
                                    startActivity(new Intent(NoteDetailActivity.this, DropboxPickerActivity.class));
                                    finish();
                                }
                            } else {
                                updateStorageSelection(null, R.drawable.ic_google_drive, AppConstant.GOOGLE_DRIVE_SELECTION);
                            }
                        } else {
                            updateStorageSelection(null, R.drawable.ic_dropbox, AppConstant.DROP_BOX_SELECTION);
                        }

                        if (mBundle != null) {
                            mCameraFileName = mBundle.getString("mCameraFileName");
                        }
                        AndroidAuthSession authSession = DropboxActions.buildSession(getApplicationContext());
                        mApi = new DropboxAPI<AndroidAuthSession>(authSession);

                        return false;
                    }
                });
            }
        });

        mTitleEditText = (EditText) findViewById(R.id.make_note_title);
        mNoteImage = (ImageView) findViewById(R.id.image_make_note);
        mDescriptionEditText = (EditText) findViewById(R.id.make_note_detail);
        sDateTextView = (TextView) findViewById(R.id.date_textview_make_note);
        sTimeTextView = (TextView) findViewById(R.id.time_textview_make_note);

        ImageView datePickerImageView = (ImageView) findViewById(R.id.date_picker_button);
        ImageView dateTimeDeleteImageView = (ImageView) findViewById(R.id.delete_make_note);

        dateTimeDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sDateTextView.setText("");
                sTimeTextView.setText(AppConstant.NO_TIME);

            }
        });

        datePickerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDatePickerDialog datePickerdialog = new AppDatePickerDialog();
                datePickerdialog.show(getSupportFragmentManager(), AppConstant.DATE_PICKER);
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.check_list_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteCustomList.addNewCheckbox();
            }
        });
    }

    private Calendar getTargetTime(){
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();
        calSet.set(Calendar.MONTH, sMonth);
        calSet.set(Calendar.YEAR, sYear);
        calSet.set(Calendar.DAY_OF_MONTH, sDay);
        calSet.set(Calendar.HOUR, sHour);
        calSet.set(Calendar.MINUTE, sMinute);
        calSet.set(Calendar.SECOND, sSecond);
        calSet.set(Calendar.MILLISECOND, 0);

        if(calSet.compareTo(calNow) <= 0){
            calSet.add(Calendar.DATE, 1);
        }
        return calSet;
    }

    private void setAlarm(Calendar targetCal, Note note){
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(AppConstant.REMINDER, note.convertToString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), note.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, targetCal.getTimeInMillis(), pendingIntent);
    }

    private void saveInDropbox(){
        AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        session = mApi.getSession();
        if(session.authenticationSuccessful()){
            try{
                session.finishAuthentication();
                DropboxActions.storeAuth(session, getApplicationContext());
            } catch (IllegalStateException e){
                showToast(AppConstant.AUTH_ERROR_DROPBOX + e.getLocalizedMessage());
            }

        }

        DropboxImageUploadAsync upload = new DropboxImageUploadAsync(this, mApi, mDropBoxFile,
                AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG);  //Provide a unique name to the file so that the user does not have to come up with one
        upload.execute();

        ContentValues values = createContentValues(AppConstant.NOTE_PREFIX + GDUT.time2Titl(null), AppConstant.DROP_BOX_SELECTION);
        createAlarm(values, insertNote(values));
    }

    private void saveInGoogleDrive(){
        GDUT.init(this);
        if(checkPlayServices() && checkUserAccount()){
            GDActions.init(this, GDUT.AM.getActiveEmil());
            GDActions.connect(true);
        }

        if(mBundle != null){
            sTmpFlNm = mBundle.getString(AppConstant.TMP_FILE_NAME);
        }
        final String resourceId = AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG;
        //Create the file on Google Drive
        new Thread(new Runnable() {
            @Override
            public void run() {
               try{
                   Thread.sleep(1000);
                   File tmpFile = new File(mImagePath);
                   GDActions.create(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                           resourceId, GDUT.MIME_JPEG, GDUT.file2Bytes(tmpFile));
               }catch (InterruptedException e){
                   e.printStackTrace();
                   //Add more error handling here
               }
            }
        }).start();

        ContentValues values = createContentValues(AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) +
                AppConstant.JPG, AppConstant.GOOGLE_DRIVE_SELECTION, true);
        createAlarm(values, insertNote(values));
    }

    private void saveInDevice(){
        ContentValues values = createContentValues(AppConstant.NOTE_PREFIX, AppConstant.DEVICE_SELECTION, true);
        int id = insertNote(values);
        mId  = id + "";
        createAlarm(values, id);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AppConstant.TMP_FILE_NAME, sTmpFlNm);
        outState.putString("mCameraFileName", mCameraFileName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(NoteDetailActivity.this, NotesActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_note, menu);
        return true;
    }
}
