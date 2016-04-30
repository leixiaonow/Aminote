package com.gionee.feedback.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.gionee.feedback.db.vo.Token;
import com.gionee.feedback.utils.Log;

public class TokenProvider implements ITokenProvider<Token> {
    private static final String TAG = "TokenProvider";
    private DatabaseHelper mDBHelper;

    TokenProvider(Context context) {
        this.mDBHelper = DatabaseHelper.getInstance(context);
    }

    public Token getToken() {
        Cursor cursor = query();
        try {
            if (cursor.moveToFirst()) {
                Token token = new Token();
                token.setId(cursor.getLong(cursor.getColumnIndexOrThrow("_id")));
                token.setToken(cursor.getString(cursor.getColumnIndexOrThrow(TokenImpl.TOKEN)));
                return token;
            }
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void update(Token token) {
        Cursor cursor = query();
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    SQLiteDatabase sdb = this.mDBHelper.getWritableDatabase();
                    sdb.beginTransaction();
                    sdb.update(TokenImpl.TOKEN, buildContentValues(token), "_id = ?", new String[]{String.valueOf(token.getId())});
                    sdb.setTransactionSuccessful();
                    sdb.endTransaction();
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sdb.setTransactionSuccessful();
                sdb.endTransaction();
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        insert(token);
        if (cursor != null) {
            cursor.close();
        }
    }

    private long insert(Token token) {
        long index = -1;
        SQLiteDatabase sdb = this.mDBHelper.getWritableDatabase();
        sdb.beginTransaction();
        try {
            index = sdb.insert(TokenImpl.TOKEN, null, buildContentValues(token));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sdb.setTransactionSuccessful();
            sdb.endTransaction();
        }
        return index;
    }

    private Cursor query() {
        SQLiteDatabase sdb = this.mDBHelper.getReadableDatabase();
        Cursor cursor = null;
        sdb.beginTransaction();
        try {
            cursor = sdb.query(TokenImpl.TOKEN, new String[]{"_id", TokenImpl.TOKEN}, null, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 1) {
                Object valueOf;
                String str = TAG;
                StringBuilder append = new StringBuilder().append("cursor = ");
                if (cursor != null) {
                    valueOf = Integer.valueOf(cursor.getCount());
                } else {
                    valueOf = null;
                }
                Log.d(str, append.append(valueOf).toString());
                return cursor;
            }
            Log.e(TAG, "cursor count > 1");
            return null;
        } finally {
            sdb.setTransactionSuccessful();
            sdb.endTransaction();
        }
    }

    private ContentValues buildContentValues(Token token) {
        ContentValues v = new ContentValues();
        v.put(TokenImpl.TOKEN, token.getToken());
        return v;
    }
}
