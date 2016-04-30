package com.gionee.note.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.gionee.note.common.PlatformUtil;
import com.gionee.note.provider.LabelContract.LabelContent;
import com.gionee.note.provider.NoteContract.NoteContent;

public class NoteProvider extends ContentProvider {
    public static final String AUTHORITY;
    public static final String DATABASE_NAME = "note.db";
    private static final int DATABASE_VERSION = 2;
    private static final int LABEL_ITEMS = 2;
    private static final int NOTE_ITEMS = 1;
    public static final String TAG = "NoteProvider";
    private static final UriMatcher URI_MATCHER = new UriMatcher(-1);
    private DatabaseHelper mOpenHelper;

    static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, NoteProvider.DATABASE_NAME, null, 2);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(NoteContent.CREATE_TABLE_SQL);
            db.execSQL(LabelContent.CREATE_TABLE_SQL);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (PlatformUtil.isGioneeDevice() && newVersion == 2) {
                addColumn(db, NoteContent.COLUMN_RESERVE_ONE);
                addColumn(db, NoteContent.COLUMN_RESERVE_TWO);
                addColumn(db, NoteContent.COLUMN_RESERVE_THREE);
                addColumn(db, NoteContent.COLUMN_RESERVE_FOUR);
                addColumn(db, NoteContent.COLUMN_RESERVE_FIVE);
                addColumn(db, NoteContent.COLUMN_RESERVE_SIX);
                addColumn(db, NoteContent.COLUMN_RESERVE_SERVEN);
                addColumn(db, NoteContent.COLUMN_RESERVE_EIGHT);
                addColumn(db, NoteContent.COLUMN_RESERVE_NINE);
                addColumn(db, NoteContent.COLUMN_RESERVE_TEN);
            }
        }

        private void addColumn(SQLiteDatabase db, String columnName) {
            try {
                db.execSQL("ALTER TABLE note_item ADD COLUMN " + columnName + " TEXT;");
            } catch (Exception e) {
                Log.d(NoteProvider.TAG, "onUpgrade addColumn e : " + e);
            }
        }
    }

    static {
        if (PlatformUtil.isGioneeDevice()) {
            AUTHORITY = "com.gionee.note.provider.NoteProvider";
        } else {
            AUTHORITY = "com.gionee.aminote.provider.NoteProvider";
        }
        URI_MATCHER.addURI(AUTHORITY, "note_items", 1);
        URI_MATCHER.addURI(AUTHORITY, "label_items", 2);
    }

    public boolean onCreate() {
        this.mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy = null;
        switch (URI_MATCHER.match(uri)) {
            case 1:
                qb.setTables(NoteContent.TABLE_NAME);
                qb.setProjectionMap(NoteContent.sProjectionMap);
                break;
            case 2:
                qb.setTables(LabelContent.TABLE_NAME);
                qb.setProjectionMap(LabelContent.sProjectionMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (!TextUtils.isEmpty(sortOrder)) {
            orderBy = sortOrder;
        }
        Cursor cursor = qb.query(this.mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, orderBy, uri.getQueryParameter("limit"));
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case 1:
                return NoteContent.CONTENT_TYPE;
            case 2:
                return LabelContent.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        String tableName;
        Uri noteUri;
        int match = URI_MATCHER.match(uri);
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        switch (match) {
            case 1:
                tableName = NoteContent.TABLE_NAME;
                noteUri = NoteContent.CONTENT_URI;
                break;
            case 2:
                tableName = LabelContent.TABLE_NAME;
                noteUri = LabelContent.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        long rowId = this.mOpenHelper.getWritableDatabase().insert(tableName, null, values);
        if (rowId > 0) {
            noteUri = ContentUris.withAppendedId(noteUri, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case 1:
                count = db.delete(NoteContent.TABLE_NAME, selection, selectionArgs);
                break;
            case 2:
                count = db.delete(LabelContent.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case 1:
                count = db.update(NoteContent.TABLE_NAME, values, selection, selectionArgs);
                break;
            case 2:
                count = db.update(LabelContent.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
