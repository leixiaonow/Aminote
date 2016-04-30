package com.gionee.note.data;

import android.net.Uri;
import com.gionee.note.app.NoteAppImpl;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChangeNotifier {
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);
    private NoteSet mMediaSet;

    public ChangeNotifier(NoteSet set, Uri uri, NoteAppImpl application) {
        this.mMediaSet = set;
        application.getDataManager().registerChangeNotifier(uri, this);
    }

    public boolean isDirty() {
        return this.mContentDirty.compareAndSet(true, false);
    }

    public void fakeChange() {
        onChange(false);
    }

    protected void onChange(boolean selfChange) {
        if (this.mContentDirty.compareAndSet(false, true)) {
            this.mMediaSet.notifyContentChanged();
        }
    }
}
