package com.gionee.note.provider;

import android.net.Uri;
import android.provider.BaseColumns;
import java.util.HashMap;

public class NoteContract {
    public static final String AUTHORITY = NoteProvider.AUTHORITY;
    private static final String AUTH_SCHEMA = ("content://" + AUTHORITY);
    private static final String SCHEME = "content://";

    public static final class NoteContent implements BaseColumns {
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_DATE_CREATED = "date_created";
        public static final String COLUMN_DATE_MODIFIED = "date_modified";
        public static final String COLUMN_LABEL = "label";
        public static final String COLUMN_REMINDER = "reminder";
        public static final String COLUMN_RESERVE_EIGHT = "reserve8";
        public static final String COLUMN_RESERVE_FIVE = "reserve5";
        public static final String COLUMN_RESERVE_FOUR = "reserve4";
        public static final String COLUMN_RESERVE_NINE = "reserve9";
        public static final String COLUMN_RESERVE_ONE = "reserve1";
        public static final String COLUMN_RESERVE_SERVEN = "reserve7";
        public static final String COLUMN_RESERVE_SIX = "reserve6";
        public static final String COLUMN_RESERVE_TEN = "reserve10";
        public static final String COLUMN_RESERVE_THREE = "reserve3";
        public static final String COLUMN_RESERVE_TWO = "reserve2";
        public static final String COLUMN_TITLE = "title";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.provider.note_item";
        public static final Uri CONTENT_URI = Uri.parse(NoteContract.AUTH_SCHEMA + PATH_NOTE_ITEMS);
        public static final String CREATE_TABLE_SQL = "CREATE TABLE note_item (_id INTEGER PRIMARY KEY, title TEXT, content TEXT, label TEXT, reserve1 TEXT, reserve2 TEXT, reserve3 TEXT, reserve4 TEXT, reserve5 TEXT, reserve6 TEXT, reserve7 TEXT, reserve8 TEXT, reserve9 TEXT, reserve10 TEXT, date_created INTEGER, date_modified INTEGER, reminder INTEGER);";
        public static final String PATH_NOTE_ITEM = "/note_items/";
        public static final String PATH_NOTE_ITEMS = "/note_items";
        public static final String TABLE_NAME = "note_item";
        public static final HashMap<String, String> sProjectionMap = new HashMap();

        static {
            NoteContract.doMap(sProjectionMap, "_id");
            NoteContract.doMap(sProjectionMap, COLUMN_TITLE);
            NoteContract.doMap(sProjectionMap, "content");
            NoteContract.doMap(sProjectionMap, COLUMN_LABEL);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_ONE);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_TWO);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_THREE);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_FOUR);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_FIVE);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_SIX);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_SERVEN);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_EIGHT);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_NINE);
            NoteContract.doMap(sProjectionMap, COLUMN_RESERVE_TEN);
            NoteContract.doMap(sProjectionMap, COLUMN_DATE_CREATED);
            NoteContract.doMap(sProjectionMap, COLUMN_DATE_MODIFIED);
            NoteContract.doMap(sProjectionMap, COLUMN_REMINDER);
        }
    }

    private static void doMap(HashMap<String, String> map, String column) {
        map.put(column, column);
    }
}
