package com.gionee.note.app.inputbackup;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.gionee.note.common.NoteUtils;
import java.io.File;

class ImportDBHelp {
    static final String COLUMN_CONTENT = "content";
    static final String COLUMN_ID = "_id";
    static final String COLUMN_LABEL = "label";
    static final String COLUMN_TITLE = "title";
    private static final String DB_NAME = "temp_save.db";
    private static final String TABLE_NAME = "input";
    private static final String TAG = "ImportDBHelp";
    private SQLiteDatabase mDb;

    public ImportDBHelp() {
        File dirFile = ImportBackUp.sTempSaveFile;
        if (!(dirFile.exists() || dirFile.mkdirs())) {
            Log.i(TAG, "ImportDBHelp construct make dirs failure!!!!");
        }
        this.mDb = SQLiteDatabase.openOrCreateDatabase(new File(ImportBackUp.sTempSaveFile, DB_NAME), null);
        this.mDb.execSQL("CREATE TABLE IF NOT EXISTS input (_id INTEGER PRIMARY KEY,title TEXT,content TEXT,label TEXT)");
    }

    public long insert(ContentValues values) {
        return this.mDb.insert(TABLE_NAME, null, values);
    }

    public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return this.mDb.query(TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public int delete(String whereClause, String[] whereArgs) {
        return this.mDb.delete(TABLE_NAME, whereClause, whereArgs);
    }

    public void close() {
        NoteUtils.closeSilently(this.mDb);
    }
}
