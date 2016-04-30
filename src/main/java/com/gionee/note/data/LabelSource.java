package com.gionee.note.data;

import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.provider.NoteContract.NoteContent;

public class LabelSource extends NoteSource {
    private static final int LABEL_NOTE_SET = 1;
    public static final String LABEL_SET_PATH = "/label/note";
    private NoteAppImpl mApplication;
    private PathMatcher mMatcher = new PathMatcher();

    public LabelSource(NoteAppImpl context) {
        super(NoteContent.COLUMN_LABEL);
        this.mApplication = context;
        this.mMatcher.add(LABEL_SET_PATH, 1);
    }

    public NoteObject createMediaObject(Path path) {
        NoteAppImpl app = this.mApplication;
        switch (this.mMatcher.match(path)) {
            case 1:
                return new LabelNoteSet(path, app);
            default:
                throw new RuntimeException("bad path: " + path);
        }
    }
}
