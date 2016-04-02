package com.amrutpatil.makeanote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by Amrut on 4/1/16.
 */
public class MakeANoteAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = MakeANoteAppWidgetProvider.class.getCanonicalName();
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, NoteDetailActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.makeanote_appwidget);

            views.setOnClickFillInIntent(R.id.action_camera_widget, new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
            views.setOnClickPendingIntent(R.id.action_note_widget, pendingIntent);
            views.setOnClickPendingIntent(R.id.action_list_note_widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
