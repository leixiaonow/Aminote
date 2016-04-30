package com.gionee.note.data;

import com.gionee.framework.log.Logger;
import com.gionee.note.data.NoteSet.ItemConsumer;
import java.util.ArrayList;

public abstract class NoteSource {
    private static final String TAG = "NoteSource";
    private String mPrefix;

    public static class PathId {
        public int id;
        public Path path;

        public PathId(Path path, int id) {
            this.path = path;
            this.id = id;
        }
    }

    public abstract NoteObject createMediaObject(Path path);

    protected NoteSource(String prefix) {
        this.mPrefix = prefix;
    }

    public String getPrefix() {
        return this.mPrefix;
    }

    public void mapMediaItems(ArrayList<PathId> list, ItemConsumer consumer) {
        int n = list.size();
        for (int i = 0; i < n; i++) {
            PathId pid = (PathId) list.get(i);
            synchronized (DataManager.LOCK) {
                NoteObject obj = pid.path.getObject();
                if (obj == null) {
                    try {
                        obj = createMediaObject(pid.path);
                    } catch (Throwable th) {
                        Logger.printLog(TAG, "cannot create media object: " + pid.path + ",,th,," + th);
                    }
                }
            }
            if (obj != null) {
                consumer.consume(pid.id, (NoteItem) obj);
            }
        }
    }
}
