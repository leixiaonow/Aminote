package com.gionee.note.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import java.util.HashMap;

public class LabelContract {
    public static final String AUTHORITY = NoteProvider.AUTHORITY;
    public static final String AUTH_SCHEMA = (SCHEME + AUTHORITY);
    public static final String SCHEME = "content://";

    public static final class LabelContent implements BaseColumns {
        public static final String COLUMN_LABEL_CONTENT = "content";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.provider.label_item";
        public static final Uri CONTENT_URI = Uri.parse(LabelContract.AUTH_SCHEMA + PATH_NOTE_ITEMS);
        public static final String CREATE_TABLE_SQL = "CREATE TABLE label_item (_id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT);";
        public static final String PATH_NOTE_ITEM = "/label_items/";
        public static final String PATH_NOTE_ITEMS = "/label_items";
        public static final String TABLE_NAME = "label_item";
        public static final HashMap<String, String> sProjectionMap = new HashMap();

        static {
            LabelContract.doMap(sProjectionMap, "_id");
            LabelContract.doMap(sProjectionMap, "content");
        }
    }

    private static void doMap(HashMap<String, String> map, String column) {
        map.put(column, column);
    }
}
