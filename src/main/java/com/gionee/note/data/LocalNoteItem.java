package com.gionee.note.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import com.gionee.note.app.Config.NoteCard;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.reminder.ReminderManager;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.ThumbnailDecodeProcess;
import com.gionee.note.common.ThumbnailDecodeProcess.ThumbnailDecodeMode;
import com.gionee.note.common.UpdateHelper;
import com.gionee.note.provider.NoteContract.NoteContent;
import java.io.File;

public class LocalNoteItem extends NoteItem {
    public static final int INDEX_CONTENT = 2;
    public static final int INDEX_DATE_CREATED = 4;
    public static final int INDEX_DATE_MODIFIED = 5;
    public static final int INDEX_ID = 0;
    public static final int INDEX_LABEL = 3;
    public static final int INDEX_REMINDER = 6;
    public static final int INDEX_TITLE = 1;
    public static final Path ITEM_PATH = Path.fromString(LocalSource.LOCAL_ITEM_PATH);
    public static final String LABEL_SEPARATOR = ",";
    private static final String[] NOTE_PROJECTION = new String[]{"_id", NoteContent.COLUMN_TITLE, "content", NoteContent.COLUMN_LABEL, NoteContent.COLUMN_DATE_CREATED, NoteContent.COLUMN_DATE_MODIFIED, NoteContent.COLUMN_REMINDER};
    private Context mContext;
    private final ContentResolver mResolver;

    public LocalNoteItem(Path path, NoteAppImpl application, Cursor cursor) {
        super(path, NoteObject.nextVersionNumber());
        this.mContext = application;
        this.mResolver = application.getContentResolver();
        loadFromCursor(cursor);
    }

    public LocalNoteItem(Path path, NoteAppImpl application, int id) {
        super(path, NoteObject.nextVersionNumber());
        this.mContext = application;
        this.mResolver = application.getContentResolver();
        Cursor cursor = LocalNoteSet.getItemCursor(this.mResolver, NoteContent.CONTENT_URI, NOTE_PROJECTION, id);
        if (cursor == null) {
            throw new RuntimeException("cannot get cursor for: " + path);
        }
        try {
            if (cursor.moveToNext()) {
                loadFromCursor(cursor);
                return;
            }
            throw new RuntimeException("cannot find data for: " + path);
        } finally {
            NoteUtils.closeSilently(cursor);
        }
    }

    private void loadFromCursor(Cursor cursor) {
        this.id = cursor.getInt(0);
        this.title = cursor.getString(1);
        this.content = cursor.getString(2);
        convertLabel(cursor.getString(3));
        this.dateCreatedInMs = cursor.getLong(4);
        this.dateModifiedInMs = cursor.getLong(5);
        this.dateReminderInMs = cursor.getLong(6);
    }

    private void convertLabel(String labels) {
        this.label.clear();
        if (labels != null) {
            for (String temp : labels.split(LABEL_SEPARATOR)) {
                this.label.add(Integer.valueOf(Integer.parseInt(temp)));
            }
        }
    }

    protected boolean updateFromCursor(Cursor cursor) {
        UpdateHelper uh = new UpdateHelper();
        this.id = uh.update(this.id, cursor.getInt(0));
        this.title = (String) uh.update(this.title, cursor.getString(1));
        this.content = (String) uh.update(this.content, cursor.getString(2));
        convertLabel(uh.update(this.label, cursor.getString(3)));
        this.dateCreatedInMs = uh.update(this.dateCreatedInMs, cursor.getLong(4));
        this.dateModifiedInMs = uh.update(this.dateModifiedInMs, cursor.getLong(5));
        this.dateReminderInMs = uh.update(this.dateReminderInMs, cursor.getLong(6));
        return uh.isUpdated();
    }

    public Bitmap requestImage(int mediaType, Uri uri) {
        if ("file".equals(uri.getScheme()) && !new File(uri.getPath()).exists()) {
            return null;
        }
        NoteCard config = NoteCard.get(this.mContext);
        return new ThumbnailDecodeProcess(this.mContext, uri, config.mImageWidth, config.mImageHeight, ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT).getThumbnail();
    }

    public void delete() throws Exception {
        this.mResolver.delete(NoteContent.CONTENT_URI, "_id=?", new String[]{String.valueOf(this.id)});
        cancelReminder();
        NoteUtils.deleteOriginMediaFile(this.content);
    }

    private void cancelReminder() {
        if (this.dateReminderInMs != 0) {
            ReminderManager.cancelReminder(this.mContext, (long) this.id);
        }
    }

    public Uri getContentUri() {
        return NoteContent.CONTENT_URI.buildUpon().appendPath(String.valueOf(this.id)).build();
    }
}
