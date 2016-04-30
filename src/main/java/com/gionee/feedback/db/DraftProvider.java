package com.gionee.feedback.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gionee.feedback.logic.vo.DraftInfo;
import com.gionee.feedback.utils.Log;

public class DraftProvider implements IDraftProvider<DraftInfo> {
    private static final String TAG = "DraftProvider";
    private DatabaseHelper mDBHelper;

    DraftProvider(Context context) {
        this.mDBHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(DraftInfo info) {
        long index = -1;
        SQLiteDatabase sdb = this.mDBHelper.getWritableDatabase();
        sdb.beginTransaction();
        try {
            index = sdb.insert("draft_save", null, buildContentValues(info));
        } catch (Exception e) {
            Log.e(TAG, "insert " + e.toString());
        } finally {
            sdb.setTransactionSuccessful();
            sdb.endTransaction();
        }
        return index;
    }

    public void delete(DraftInfo info) {
        SQLiteDatabase sdb = this.mDBHelper.getWritableDatabase();
        sdb.beginTransaction();
        try {
            sdb.delete("draft_save", "_id = ?", new String[]{String.valueOf(info.getId())});
        } catch (Exception e) {
            Log.e(TAG, "delete " + e.toString());
        } finally {
            sdb.setTransactionSuccessful();
            sdb.endTransaction();
        }
    }

    public void update(DraftInfo info) {
        SQLiteDatabase sdb = this.mDBHelper.getWritableDatabase();
        sdb.beginTransaction();
        try {
            sdb.update("draft_save", buildContentValues(info), "_id = ?", new String[]{String.valueOf(info.getId())});
        } catch (Exception e) {
            Log.e(TAG, "update " + e.toString());
        } finally {
            sdb.setTransactionSuccessful();
            sdb.endTransaction();
        }
    }

    public DraftInfo queryHead() {
        SQLiteDatabase sdb = this.mDBHelper.getReadableDatabase();
        Cursor cursor = null;
        sdb.beginTransaction();
        try {
            cursor = sdb.query("draft_save", new String[]{"_id", "content", DraftImpl.USER_CONTENT, DraftImpl.ATTACH}, null, null, null, null, null);
            Log.d(TAG, "cursor = " + (cursor != null ? cursor.getCount() : 0));
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                sdb.setTransactionSuccessful();
                sdb.endTransaction();
                return null;
            }
            DraftInfo info = new DraftInfo();
            info.setId(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
            info.setContentText(cursor.getString(cursor.getColumnIndexOrThrow("content")));
            info.setContactText(cursor.getString(cursor.getColumnIndexOrThrow(DraftImpl.USER_CONTENT)));
            String string = cursor.getString(cursor.getColumnIndexOrThrow(DraftImpl.ATTACH));
            Log.d(TAG, "string = " + string);
            info.setAttachTexts(string);
            return info;
        } catch (Exception e) {
            Log.e(TAG, "queryHead " + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            sdb.setTransactionSuccessful();
            sdb.endTransaction();
        }
    }

    private ContentValues buildContentValues(DraftInfo info) {
        ContentValues v = new ContentValues();
        v.put("content", info.getContentText());
        v.put(DraftImpl.USER_CONTENT, info.getContactText());
        v.put(DraftImpl.ATTACH, info.getAttachTexts());
        return v;
    }
}
