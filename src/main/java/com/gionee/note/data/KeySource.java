package com.gionee.note.data;

import com.gionee.note.app.NoteAppImpl;

public class KeySource extends NoteSource {
    private static final int KEY_NOTE_SET = 1;
    public static final String KEY_SET_PATH = "/key/note";
    private NoteAppImpl mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    public KeySource(NoteAppImpl context) {
        super("key");
        this.mApplication = context;
        this.mMatcher.add(KEY_SET_PATH, 1);
    }

    public NoteObject createMediaObject(Path path) {
        NoteAppImpl app = this.mApplication;
        switch (this.mMatcher.match(path)) {
            case 1:
                return new KeyNoteSet(path, app);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
