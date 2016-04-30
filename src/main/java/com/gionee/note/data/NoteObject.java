package com.gionee.note.data;

import android.net.Uri;

public class NoteObject {
    public static final long INVALID_DATA_VERSION = -1;
    private static long sVersionSerial = 0;
    protected long mDataVersion;
    protected final Path mPath;

    public NoteObject(Path path, long version) {
        path.setObject(this);
        this.mPath = path;
        this.mDataVersion = version;
    }

    public static synchronized long nextVersionNumber() {
        long j;
        synchronized (NoteObject.class) {
            j = sVersionSerial + 1;
            sVersionSerial = j;
        }
        return j;
    }

    public Path getPath() {
        return this.mPath;
    }

    public void delete() throws Exception {
        throw new UnsupportedOperationException();
    }

    public void rotate(int degrees) {
        throw new UnsupportedOperationException();
    }

    public Uri getContentUri() {
        throw new UnsupportedOperationException();
    }

    public Uri getPlayUri() {
        throw new UnsupportedOperationException();
    }

    public synchronized long getDataVersion() {
        return this.mDataVersion;
    }
}
