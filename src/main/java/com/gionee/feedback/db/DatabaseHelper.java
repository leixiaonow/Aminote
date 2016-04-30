package com.gionee.feedback.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.amigoui.internal.util.HanziToPinyin.Token;
import com.gionee.feedback.utils.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_BASE_VERSION = 100;
    private static final String DB_NAME = "feedback.db";
    private static final int DB_VERSION = 101;
    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper sDatabaseHelper;

    private DatabaseHelper() {
        super(null, DB_NAME, null, 101);
    }

    protected static DatabaseHelper getInstance(Context context) {
        if (sDatabaseHelper == null) {
            sDatabaseHelper = new DatabaseHelper(context.getApplicationContext());
        }
        return sDatabaseHelper;
    }

    protected DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 101);
    }

    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, 101);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 100) {
            oldVersion = 100;
        }
        for (int version = oldVersion; version <= newVersion; version++) {
            upgradeTo(db, version);
        }
    }

    private void upgradeTo(SQLiteDatabase db, int version) {
        Log.d(TAG, "upgradeTo db version = " + version);
        switch (version) {
            case 100:
                createFeedbackDB(db);
                return;
            case 101:
                createTokenTable(db);
                createMessageSaveTable(db);
                addColumn(db, "message", MessageImpl.ATTACHS, "TEXT");
                return;
            default:
                updateDB(db);
                return;
        }
    }

    private void updateDB(SQLiteDatabase db) {
    }

    private void createFeedbackDB(SQLiteDatabase db) {
        createMessageTable(db);
        createReplyTable(db);
        createAppDataTable(db);
    }

    private void createAppDataTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS appdata (_id INTEGER PRIMARY KEY, imei TEXT,app_key TEXT)");
        addIndex(db, "appdata", AppDataImpl.APP_KEY);
        addIndex(db, "appdata", AppDataImpl.IMEI);
    }

    private void createReplyTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS reply (_id INTEGER PRIMARY KEY, content_id LONG,is_read TEXT,reply_content TEXT,reply_id LONG,reply_person TEXT,reply_time LONG)");
        addIndex(db, "reply", ReplyImpl.IS_READ);
        addIndex(db, "reply", ReplyImpl.REPLY_CONTENT);
        addIndex(db, "reply", ReplyImpl.REPLY_ID);
        addIndex(db, "reply", ReplyImpl.REPLY_PERSON);
        addIndex(db, "reply", ReplyImpl.REPLY_TIME);
    }

    private void createMessageTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS message (_id INTEGER PRIMARY KEY, content_id LONG,content TEXT,send_time TEXT,user_contact TEXT)");
        addIndex(db, "message", "content_id");
        addIndex(db, "message", "content");
        addIndex(db, "message", MessageImpl.SEND_TIME);
        addIndex(db, "message", MessageImpl.USER_CONTACT);
    }

    private void createMessageSaveTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS draft_save (_id INTEGER PRIMARY KEY, content TEXT, user_content TEXT, attach TEXT)");
    }

    private void createTokenTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS token (_id INTEGER PRIMARY KEY, token TEXT)");
    }

    private void addColumn(SQLiteDatabase db, String dbTable, String columnName, String columnDefinition) {
        db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName + Token.SEPARATOR + columnDefinition);
    }

    private void addIndex(SQLiteDatabase db, String dbTable, String columnName) {
        db.execSQL("CREATE INDEX " + columnName + "_index" + " ON " + dbTable + "(" + columnName + ");");
    }
}
