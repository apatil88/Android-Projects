package com.amrutpatil.makeanote;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Amrut on 2/29/16.
 */
public class ArchivesContract {
    interface ArchivesColumns {
        String ARCHIVES_TITLE = "archives_title";
        String ARCHIVES_DESCRIPTION = "archives_description";
        String ARCHIVES_DATE_TIME = "archives_date_time";
        String ARCHIVES_CATEGORY = "archives_category";
        String ARCHIVES_TYPE = "archives_type";
    }

    public static final String CONTENT_AUTHORITY = "com.amrutpatil.makeanote.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_ARCHIVES = "archives";
    public static final Uri URI_TABLE = BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_ARCHIVES).build();


    public static class Archives implements ArchivesColumns, BaseColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + ".archives";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + ".archives";

        public static Uri buildArchiveUri(String archiveId) {
            return URI_TABLE.buildUpon().appendEncodedPath(archiveId).build();
        }

        public static String getArchiveId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

}
