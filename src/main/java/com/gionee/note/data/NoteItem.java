package com.gionee.note.data;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import java.util.ArrayList;

public abstract class NoteItem extends NoteObject {
    public static final int INVALID_ID = -1;
    public static final int INVALID_REMINDER = 0;
    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int MEDIA_TYPE_NONE = -1;
    public static final int MEDIA_TYPE_VIDEO = 1;
    public static final int THUMBNAIL_TYPE_EDIT = 2;
    public static final int THUMBNAIL_TYPE_HOME = 1;
    public String content;
    public long dateCreatedInMs;
    public long dateModifiedInMs;
    public long dateReminderInMs;
    public int id;
    public ArrayList<Integer> label = new ArrayList();
    public String title;

    public abstract Bitmap requestImage(int i, Uri uri);

    protected abstract boolean updateFromCursor(Cursor cursor);

    public NoteItem(Path path, long version) {
        super(path, version);
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public ArrayList<Integer> getLabel() {
        return this.label;
    }

    public long getDateTimeCreated() {
        return this.dateCreatedInMs;
    }

    public long getDateTimeModified() {
        return this.dateModifiedInMs;
    }

    public long getDateTimeReminder() {
        return this.dateReminderInMs;
    }

    public static String convertToStringLabel(ArrayList<Integer> arrayLabel) {
        StringBuilder updateBuilder = new StringBuilder();
        int length = arrayLabel.size();
        for (int i = 0; i < length; i++) {
            updateBuilder.append(arrayLabel.get(i));
            if (i != length - 1) {
                updateBuilder.append(LocalNoteItem.LABEL_SEPARATOR);
            }
        }
        if (updateBuilder.length() == 0) {
            return null;
        }
        return updateBuilder.toString();
    }

    protected void updateContent(Cursor cursor) {
        if (updateFromCursor(cursor)) {
            this.mDataVersion = NoteObject.nextVersionNumber();
        }
    }
}
