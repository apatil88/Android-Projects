package com.amrutpatil.makeanote;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Amrut on 4/1/16.
 */
public class MakeANoteAppWidgetConfigure extends Activity {

    static final String TAG = MakeANoteAppWidgetConfigure.class.getCanonicalName();
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    ImageView mAppWidgetCamera;
    ImageView mAppWidgetNote;
    ImageView mAppWidgetListNote;

    public MakeANoteAppWidgetConfigure() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);
        // Set the view layout resource to use.
        setContentView(R.layout.makeanote_appwidget);

        //Find the ImageView
        mAppWidgetCamera = (ImageView)findViewById(R.id.action_camera_widget);
        mAppWidgetNote = (ImageView) findViewById(R.id.action_note_widget);
        mAppWidgetListNote = (ImageView) findViewById(R.id.action_list_note_widget);

        // Bind the action.
        findViewById(R.id.action_camera_widget).setOnClickListener(mOnClickCameraListener);
        findViewById(R.id.action_note_widget).setOnClickListener(mOnClickNoteListener);
        findViewById(R.id.action_list_note_widget).setOnClickListener(mOnClickListNoteListener);
        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }
    /*View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MakeANoteAppWidgetConfigure.this;
            // When the button is clicked, save the string in our prefs and return that they
            // clicked OK.
            String titlePrefix = mAppWidgetPrefix.getText().toString();
            saveTitlePref(context, mAppWidgetId, titlePrefix);
            // Push widget update to surface with newly set prefix
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            MakeANoteAppWidgetProvider.updateAppWidget(context, appWidgetManager,
                    mAppWidgetId, titlePrefix);
            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };*/

    View.OnClickListener mOnClickCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG, "Camera in App Widget configure invoked");
            final Context context = MakeANoteAppWidgetConfigure.this;
            //When camera icon is clicked, launch camera
            NoteDetailActivity callCamera = new NoteDetailActivity();
            callCamera.callCamera();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            //MakeANoteAppWidgetProvider.onUpdate(context, appWidgetManager, mAppWidgetId);
            //Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    View.OnClickListener mOnClickNoteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG, "Note in App Widget configure invoked");
            final Context context = MakeANoteAppWidgetConfigure.this;
            //When camera icon is clicked, launch camera
            Intent intent = new Intent(MakeANoteAppWidgetConfigure.this, NoteDetailActivity.class);
            startActivity(intent);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            //MakeANoteAppWidgetProvider.onUpdate(context, appWidgetManager, mAppWidgetId);
            //Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    View.OnClickListener mOnClickListNoteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.v(TAG, "List Note in App Widget configure invoked");
            final Context context = MakeANoteAppWidgetConfigure.this;
            //When camera icon is clicked, launch camera
            Intent intent = new Intent(MakeANoteAppWidgetConfigure.this, NoteDetailActivity.class);
            startActivity(intent);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            //MakeANoteAppWidgetProvider.onUpdate(context, appWidgetManager, mAppWidgetId);
            //Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

}
