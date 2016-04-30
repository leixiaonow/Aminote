package com.gionee.note.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.provider.NoteContract.NoteContent;
import java.util.ArrayList;

public class LocalNoteSet extends NoteSet {
    public static final String[] COUNT_PROJECTION = new String[]{"_id"};
    private static final int INVALID_COUNT = -1;
    public static final String[] NOTE_PROJECTION = new String[]{"_id", NoteContent.COLUMN_TITLE, "content", NoteContent.COLUMN_LABEL, NoteContent.COLUMN_DATE_CREATED, NoteContent.COLUMN_DATE_MODIFIED, NoteContent.COLUMN_REMINDER};
    private static final String TAG = "LocalNoteSet";
    private final NoteAppImpl mApplication;
    private final Uri mBaseUri;
    private int mCachedCount = -1;
    private final Path mItemPath;
    private final ChangeNotifier mNotifier;
    private final String mOrderClause;
    private final ContentResolver mResolver;

    public LocalNoteSet(Path path, NoteAppImpl application) {
        super(path, NoteObject.nextVersionNumber());
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        this.mOrderClause = "date_modified DESC";
        this.mBaseUri = NoteContent.CONTENT_URI;
        this.mItemPath = LocalNoteItem.ITEM_PATH;
        this.mNotifier = new ChangeNotifier(this, this.mBaseUri, application);
    }

    private static NoteItem loadOrUpdateItem(Path path, Cursor cursor, DataManager dataManager, NoteAppImpl app) {
        LocalNoteItem item;
        synchronized (DataManager.LOCK) {
            item = (LocalNoteItem) dataManager.peekNoteObject(path);
            if (item == null) {
                item = new LocalNoteItem(path, app, cursor);
            } else {
                item.updateContent(cursor);
            }
        }
        return item;
    }

    public static Cursor getItemCursor(ContentResolver resolver, Uri uri, String[] projection, int id) {
        return resolver.query(uri, projection, "_id=?", new String[]{String.valueOf(id)}, null);
    }

    public Uri getContentUri() {
        return this.mBaseUri;
    }

    public ArrayList<NoteItem> getNoteItem(int start, int count) {
        DataManager dataManager = this.mApplication.getDataManager();
        Uri uri = this.mBaseUri.buildUpon().appendQueryParameter("limit", start + LocalNoteItem.LABEL_SEPARATOR + count).build();
        ArrayList<NoteItem> list = new ArrayList();
        Cursor cursor = this.mResolver.query(uri, NOTE_PROJECTION, null, null, this.mOrderClause);
        if (cursor == null) {
            Logger.printLog(TAG, "query fail: " + uri);
        } else {
            while (cursor.moveToNext()) {
                try {
                    list.add(loadOrUpdateItem(this.mItemPath.getChild(cursor.getInt(0)), cursor, dataManager, this.mApplication));
                } finally {
                    NoteUtils.closeSilently(cursor);
                }
            }
        }
        return list;
    }

    public int getNoteItemCount() {
        if (!NoteUtils.checkExternalStoragePermission()) {
            return 0;
        }
        if (this.mCachedCount == -1) {
            Cursor cursor = this.mResolver.query(this.mBaseUri, COUNT_PROJECTION, null, null, null);
            if (cursor == null) {
                Logger.printLog(TAG, "query fail");
                return 0;
            }
            try {
                this.mCachedCount = cursor.getCount();
            } catch (Exception e) {
                Logger.printLog(TAG, "LocalNoteSet getNoteItemCount E = " + e);
            } finally {
                NoteUtils.closeSilently(cursor);
            }
        }
        return this.mCachedCount;
    }

    public long reload() {
        if (this.mNotifier.isDirty()) {
            this.mDataVersion = NoteObject.nextVersionNumber();
            this.mCachedCount = -1;
        }
        return this.mDataVersion;
    }
}
