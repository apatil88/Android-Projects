package com.amrutpatil.makeanote;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Description: Class to create and edit notes. This class also sets reminders and storage location.
 */
public class NoteDetailActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = NoteDetailActivity.class.getCanonicalName();
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
            if (getIntent().getStringExtra(AppConstant.LIST) != null) {
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
    private void setValues(Note note) {
        try{
            getSupportActionBar().setTitle(AppConstant.REMINDERS);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        String title = note.getTitle();
        String description = note.getDescription();
        String date = note.getDate();
        String time = note.getTime();
        String image = note.getImagePath();
        if (note.getType().equals(AppConstant.LIST)) {
            mIsList = true;
        }
        mTitleEditText.setText(title);
        if (mIsList) {
            intializeComponents(LIST);
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            setUpList(description);
        } else {
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
                if (!mImagePath.equals(AppConstant.NO_IMAGE)) {
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

    private void updateStorageSelection(Bitmap bitmap, int storageSelectionResource, int selection) {
        if (bitmap != null) {
            mNoteImage.setImageBitmap(bitmap);
        }
        mStorageSelection.setBackgroundResource(storageSelectionResource);
        AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), selection);

    }

    private void setUpList(String description) {
        mDescription = description;
        if (!mIsNotificationMode) {
            mNoteCustomList.setUpForEditMode(description);
        } else {
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

    private void intializeComponents(int choice) {
        if (choice == LIST) {
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.VISIBLE);
            mIsList = true;
        } else if (choice == NORMAL) {
            CardView cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.GONE);
            mIsList = false;
        }

        mStorageSelection = (ImageView) findViewById(R.id.image_storage);
        if (AppSharedPreferences.getUploadPreference(getApplicationContext()) == AppConstant.GOOGLE_DRIVE_SELECTION) {
            mStorageSelection.setBackgroundResource(R.drawable.ic_google_drive);
        } else if (AppSharedPreferences.getUploadPreference(getApplicationContext()) == AppConstant.DROP_BOX_SELECTION) {
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
                            } else{
                                updateStorageSelection(null, R.drawable.ic_google_drive, AppConstant.GOOGLE_DRIVE_SELECTION);
                            }
                        }
                            else if (menuItem.getItemId() == R.id.action_dropbox) {
                                AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), AppConstant.DROP_BOX_SELECTION);
                                if (!AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext())) {
                                    startActivity(new Intent(NoteDetailActivity.this, DropboxPickerActivity.class));
                                    finish();
                                }
                                else {
                                    updateStorageSelection(null, R.drawable.ic_dropbox, AppConstant.DROP_BOX_SELECTION);
                                }
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

        LinearLayout layout = (LinearLayout) findViewById(R.id.add_check_list_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteCustomList.addNewCheckbox();
            }
        });
    }

    private Calendar getTargetTime() {
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();
        calSet.set(Calendar.MONTH, sMonth);
        calSet.set(Calendar.YEAR, sYear);
        calSet.set(Calendar.DAY_OF_MONTH, sDay);
        calSet.set(Calendar.HOUR, sHour);
        calSet.set(Calendar.MINUTE, sMinute);
        calSet.set(Calendar.SECOND, sSecond);
        calSet.set(Calendar.MILLISECOND, 0);

        if (calSet.compareTo(calNow) <= 0) {
            calSet.add(Calendar.DATE, 1);
        }
        return calSet;
    }

    private void setAlarm(Calendar targetCal, Note note) {
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(AppConstant.REMINDER, note.convertToString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), note.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }

    private void saveInDropbox() {
        AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                DropboxActions.storeAuth(session, getApplicationContext());
            } catch (IllegalStateException e) {
                showToast(AppConstant.AUTH_ERROR_DROPBOX + e.getLocalizedMessage());
            }

        }

        DropboxImageUploadAsync upload = new DropboxImageUploadAsync(this, mApi, mDropBoxFile,
                AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG);  //Provide a unique name to the file so that the user does not have to come up with one
        upload.execute();

        ContentValues values = createContentValues(AppConstant.NOTE_PREFIX + GDUT.time2Titl(null), AppConstant.DROP_BOX_SELECTION, true);
        createAlarm(values, insertNote(values));
    }

    private void saveInGoogleDrive() {
        GDUT.init(this);
        if (checkPlayServices() && checkUserAccount()) {
            GDActions.init(this, GDUT.AM.getActiveEmil());
            GDActions.connect(true);
        }

        if (mBundle != null) {
            sTmpFlNm = mBundle.getString(AppConstant.TMP_FILE_NAME);
        }
        final String resourceId = AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG;
        //Create the file on Google Drive
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    File tmpFile = new File(mImagePath);
                    GDActions.create(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                            resourceId, GDUT.MIME_JPEG, GDUT.file2Bytes(tmpFile));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //Add more error handling here
                }
            }
        }).start();

        ContentValues values = createContentValues(AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) +
                AppConstant.JPG, AppConstant.GOOGLE_DRIVE_SELECTION, true);
        createAlarm(values, insertNote(values));
    }

    private void saveInDevice() {
        ContentValues values = createContentValues(mImagePath, AppConstant.DEVICE_SELECTION, true);
        int id = insertNote(values);
        mId = id + "";
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_camera:
                mGoingToCameraOrGallery = true;
                callCamera();
                break;

            case R.id.action_gallery:
                mGoingToCameraOrGallery = true;
                callGallery();
                break;

            case android.R.id.home:
                if (!mIsNotificationMode) {
                    saveNote();
                } else {
                    if (!sTimeTextView.getText().toString().equals(AppConstant.NO_TIME)) {
                        actAsReminder();
                    } else {
                        actAsNote();
                    }
                    moveToArchive(mIsList);
                    mType = ARCHIVES;
                    mTitle = AppConstant.ARCHIVES;
                    startActivity(new Intent(NoteDetailActivity.this, ArchivesActivity.class));
                    finish();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void removeFromReminder(Note reminder) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(NotesContract.BASE_CONTENT_URI + "/notes/" + reminder.getId());
        contentResolver.delete(uri, null, null);

    }

    private void moveToArchive(boolean isList) {
        String type;
        ContentValues values = new ContentValues();
        TextView title = (TextView) findViewById(R.id.make_note_title);
        TextView description = (TextView) findViewById(R.id.make_note_detail);
        TextView dateTime = (TextView) findViewById(R.id.time_textview_make_note);

        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_TITLE, title.getText().toString());
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME, dateTime.getText().toString());
        if (isList) {
            type = AppConstant.LIST;
            values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION, mDescription);
        } else {
            type = AppConstant.NORMAL;
            values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION, description.getText().toString());
        }
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE, type);
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY, AppConstant.REMINDERS);

        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(ArchivesContract.BASE_CONTENT_URI + "/archives");
        contentResolver.insert(uri, values);

    }

    private void callGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TAKE_GALLERY_CODE);
    }

    protected void saveNote() {
        if (mIsEditing) {
            switch (AppSharedPreferences.getUploadPreference(getApplicationContext())) {
                case AppConstant.DROP_BOX_SELECTION:
                    if (!mImagePath.equals(AppConstant.NO_IMAGE)) {
                        editForSaveInDropbox();
                    } else {
                        editForSaveInDevice();
                    }
                    break;
                case AppConstant.GOOGLE_DRIVE_SELECTION:
                    if (!mImagePath.equals(AppConstant.NO_IMAGE) && mIsImageSet) {
                        editForSaveInGoogleDrive();
                    } else {
                        editForSaveInDevice();
                    }
                    break;

                case AppConstant.DEVICE_SELECTION:
                case AppConstant.NONE_SELECTION:
                    editForSaveInDevice();
                    break;
            }
        } else if (mTitleEditText.getText().toString().length() > 0 && !mGoingToCameraOrGallery) {
            switch (AppSharedPreferences.getUploadPreference(getApplicationContext())) {
                case AppConstant.DROP_BOX_SELECTION:
                    if (!mImagePath.equals(AppConstant.NO_IMAGE)) {
                        saveInDropbox();
                    } else {
                        saveInDevice();
                    }
                    break;

                case AppConstant.GOOGLE_DRIVE_SELECTION:
                    if (!mImagePath.equals(AppConstant.NO_IMAGE)) {
                        saveInGoogleDrive();
                    } else {
                        saveInDevice();
                    }
                    break;

                case AppConstant.DEVICE_SELECTION:
                case AppConstant.NONE_SELECTION:
                    saveInDevice();
                    break;
            }
        }
        startActivity(new Intent(NoteDetailActivity.this, NotesActivity.class));
        finish();
    }

    private void editForSaveInDropbox() {
        if (AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext())) {
            AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
            mApi = new DropboxAPI<AndroidAuthSession>(session);
            session = mApi.getSession();

            if (session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();
                    DropboxActions.storeAuth(session, getApplicationContext());
                } catch (IllegalStateException e) {
                    showToast(AppConstant.AUTH_ERROR_DROPBOX + e.getLocalizedMessage());
                }
            }
        }
        ContentValues values = createContentValues("", AppConstant.DROP_BOX_SELECTION, false);
        if (mIsImageSet) {
            String filename = AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG;
            values.put(NotesContract.NotesColumns.NOTES_IMAGE, filename);
            mDropBoxFile = new File(getApplicationContext().getCacheDir(), filename);
            try {
                mDropBoxFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Bitmap newImage = ((BitmapDrawable) mNoteImage.getDrawable()).getBitmap();
            newImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(mDropBoxFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            DropboxImageUploadAsync uploadAsync = new DropboxImageUploadAsync(this, mApi, mDropBoxFile, filename);
            uploadAsync.execute();
        }
        updateNote(values);
        createAlarm(values, (int) System.currentTimeMillis());
    }

    private void editForSaveInGoogleDrive() {
        GDUT.init(this);
        final String resourceId = AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG;
        if (checkPlayServices() && checkUserAccount()) {
            GDActions.init(this, GDUT.AM.getActiveEmil());
            GDActions.connect(true);
        }

        if (mBundle != null) {
            sTmpFlNm = mBundle.getString(AppConstant.TMP_FILE_NAME);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if (mIsImageSet) {
                        File tmpFile = null;
                        try {
                            tmpFile = new File(mImagePath);
                            GDActions.create(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                                    resourceId, GDUT.MIME_JPEG, GDUT.file2Bytes(tmpFile));
                        } finally {
                           /*if(tmpFile != null){
                               tmpFile.delete();
                           }*/
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        ContentValues values = createContentValues(resourceId, AppConstant.GOOGLE_DRIVE_SELECTION, false);
        updateNote(values);
        createAlarm(values, (int) System.currentTimeMillis());

    }

    private void editForSaveInDevice() {
        ContentValues values = createContentValues(mImagePath, AppConstant.DEVICE_SELECTION, false);
        updateNote(values);
        createAlarm(values, Integer.parseInt(mId));
    }

    public boolean hasPermissionInManifest(Context context, String permissionName) {
        final String packageName = context.getPackageName();
        try {
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            final String[] declaredPermisisons = packageInfo.requestedPermissions;
            if (declaredPermisisons != null && declaredPermisisons.length > 0) {
                for (String p : declaredPermisisons) {
                    if (p.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void callCamera() {
        if(hasPermissionInManifest(getApplicationContext(), Manifest.permission.CAMERA)){
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        } else if (Build.VERSION_CODES.M >= 23){
            Toast.makeText(NoteDetailActivity.this, "Please check permissions at Settings->Apps and try again", Toast.LENGTH_LONG).show();
        } else{
            Log.v(TAG, "Permission Denied: Opening Camera");
            Toast.makeText(NoteDetailActivity.this, "Please check permissions at Settings->Apps and try again", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = null;
        switch (requestCode) {
            case AppConstant.REQ_ACCPICK:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (GDUT.AM.setEmil(email) == GDUT.AM.CHANGED) {
                        GDActions.init(this, GDUT.AM.getActiveEmil());
                        GDActions.connect(true);
                    }
                } else if (GDUT.AM.getActiveEmil() == null) {
                    GDUT.AM.removeActiveAccnt();
                    finish();
                }
                break;

            case AppConstant.REQ_AUTH:
            case AppConstant.REQ_RECOVER:
                sIsInAuth = false;
                if (resultCode == Activity.RESULT_OK) {
                    GDActions.connect(true);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    GDUT.AM.removeActiveAccnt();
                    finish();
                }
                break;

            case AppConstant.REQ_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    final String titl = GDUT.time2Titl(null);
                    if (titl != null && sTmpFlNm != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File tmpFile = null;
                                GDActions.createTreeGDAA(GDUT.MYROOT, titl, GDUT.file2Bytes(tmpFile));
                            }
                        }).start();
                    }
                }
                break;
        }

        //If we are able to take a photo
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            mGoingToCameraOrGallery = false;
            photo = (Bitmap) data.getExtras().get("data");
            mNoteImage.setImageBitmap(photo);

            Uri tempUri = getImageUri(getApplicationContext(), photo);
            File finalFile = new File(getRealPathFromURI(tempUri));
            mImagePath = finalFile.toString();
            mIsImageSet = true;
        } else if (requestCode == TAKE_GALLERY_CODE) {
            if (resultCode == RESULT_OK) {
                mGoingToCameraOrGallery = false;
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mImagePath = cursor.getString(columnIndex);
                cursor.close();

                File tempFile = new File(mImagePath);
                photo = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                mNoteImage.setVisibility(View.VISIBLE);
                mNoteImage.setImageBitmap(photo);
                mIsImageSet = true;
            } else {
                mIsImageSet = false;
            }
        }
        if (mIsImageSet) {
            mDropBoxFile = new File(mImagePath);
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!sIsInAuth) {
            connectionResult.hasResolution();
            try {
                sIsInAuth = true;
                connectionResult.startResolutionForResult(this, AppConstant.REQ_AUTH);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
                //Add other error handling here
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    private boolean checkUserAccount() {
        String email = GDUT.AM.getActiveEmil();
        Account account = GDUT.AM.getPrimaryAccnt(true);
        if (email == null) {
            if (account == null) {
                account = showAccountPicker();
                return false;
            } else {
                //Only one account registered
                GDUT.AM.setEmil(account.name);
            }
            return true;
        }

        account = GDUT.AM.getActiveAccnt();
        if (account == null) {
            account = showAccountPicker();
            return false;
        }
        return true;
    }

    private boolean checkPlayServices() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (status != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(status)) {
                errorDialog(status, AppConstant.REQ_RECOVER);
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private void errorDialog(int errorCode, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(AppConstant.DIALOG_ERROR, errorCode);
        args.putInt(AppConstant.REQUEST_CODE, requestCode);
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), AppConstant.DIALOG_ERROR);
    }

    private ContentValues createContentValues(String noteImage, int storageSelection, boolean isSave) {
        if (noteImage == null || noteImage.equals("")) {
            noteImage = AppConstant.NO_IMAGE;
        }
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesColumns.NOTES_TITLE, mTitleEditText.getText().toString());
        values.put(NotesContract.NotesColumns.NOTES_DATE, sDateTextView.getText().toString());
        values.put(NotesContract.NotesColumns.NOTES_TIME, sTimeTextView.getText().toString());

        if (mIsImageSet || isSave) {
            values.put(NotesContract.NotesColumns.NOTES_IMAGE, noteImage);
        }
        values.put(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION, storageSelection);
        String type = AppConstant.NORMAL;
        String description = mDescriptionEditText.getText().toString();
        if (mIsList) {
            description = mNoteCustomList.getLists();
            type = AppConstant.LIST;
        }
        values.put(NotesContract.NotesColumns.NOTES_TYPE, type);
        values.put(NotesContract.NotesColumns.NOTES_DESCRIPTION, description);
        return values;
    }

    private int insertNote(ContentValues values) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(NotesContract.BASE_CONTENT_URI + "/notes");
        Uri returned = contentResolver.insert(uri, values);
        String[] temp = returned.toString().split("/");
        return Integer.parseInt(temp[temp.length - 1]);
    }

    private void updateNote(ContentValues values) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(NotesContract.BASE_CONTENT_URI + "/notes");
        String selection = NotesContract.NotesColumns.NOTES_ID + " = " + mId;
        contentResolver.update(uri, values, selection, null);
    }

    private void createAlarm(ContentValues values, int id) {
        if (!sTimeTextView.getText().toString().equals(AppConstant.NO_TIME)) {
            Note note = new Note(
                    values.getAsString(NotesContract.NotesColumns.NOTES_TITLE),
                    values.getAsString(NotesContract.NotesColumns.NOTES_DESCRIPTION),
                    values.getAsString(NotesContract.NotesColumns.NOTES_DATE),
                    values.getAsString(NotesContract.NotesColumns.NOTES_TIME),
                    values.getAsString(NotesContract.NotesColumns.NOTES_TYPE),
                    id,
                    values.getAsInteger(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION));
            note.setImagePath(values.getAsString(NotesContract.NotesColumns.NOTES_IMAGE));
            setAlarm(getTargetTime(), note);
        }
    }

    private Account showAccountPicker() {
        Account account = GDUT.AM.getPrimaryAccnt(false);
        Intent intent = AccountPicker.newChooseAccountIntent(account, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null, null, null);

        startActivityForResult(intent, AppConstant.REQ_ACCPICK);
        return account;
    }

    public static class AppDatePickerDialog extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private int mYear, mMonth, mDay;
        private String tempMonth;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            tempMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
            return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (year == mYear) {
                if (monthOfYear == mMonth) {
                    if (dayOfMonth == mDay) {
                        sDateTextView.setText(AppConstant.TODAY);
                    } else {
                        sDateTextView.setText(dayOfMonth + " " + sMonth);
                    }
                } else {
                    sDateTextView.setText(dayOfMonth + " " + sMonth);
                }
            } else {
                sDateTextView.setText(dayOfMonth + " " + sMonth + " " + year);
            }
            sYear = year;
            sMonth = monthOfYear;
            sDay = dayOfMonth;

            AppTimePickerDialog timePickerDialog = new AppTimePickerDialog();
            timePickerDialog.show(getFragmentManager(), AppConstant.DATE_PICKER);
        }
    }

    public static class AppTimePickerDialog extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener{

        private int mHour, mMinute;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, mHour, mMinute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if(minute < 10){
                sTimeTextView.setText(hourOfDay + ":0" + minute);
            } else{
                sTimeTextView.setText(hourOfDay + ":" + minute);
            }
            sHour = hourOfDay;
            sMinute = minute;
            sSecond = 0;
        }
    }


}
