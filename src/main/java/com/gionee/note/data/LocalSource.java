package com.gionee.note.data;

import com.gionee.note.app.NoteAppImpl;

public class LocalSource extends NoteSource {
    public static final String LOCAL_ITEM_PATH = "/local/note/item";
    private static final int LOCAL_NOTE_ITEM = 2;
    private static final int LOCAL_NOTE_SET = 1;
    public static final String LOCAL_SET_PATH = "/local/note";
    private NoteAppImpl mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    public LocalSource(NoteAppImpl context) {
        super("local");
        this.mApplication = context;
        this.mMatcher.add(LOCAL_SET_PATH, 1);
        this.mMatcher.add("/local/note/item/*", 2);
    }

    public NoteObject createMediaObject(Path path) {
        NoteAppImpl app = this.mApplication;
        switch (this.mMatcher.match(path)) {
            case 1:
                return new LocalNoteSet(path, app);
            case 2:
                return new LocalNoteItem(path, this.mApplication, this.mMatcher.getIntVar(0));
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
