package com.gionee.note.data;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.NoteAppImpl;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;

public class DataManager {
    public static final Object LOCK = new Object();
    private static final String TAG = "DataManager";
    private NoteAppImpl mApplication;
    private final Handler mDefaultMainHandler;
    private HashMap<Uri, NotifyBroker> mNotifierMap = new HashMap();
    private HashMap<String, NoteSource> mSourceMap = new LinkedHashMap();

    private static class NotifyBroker extends ContentObserver {
        private boolean mBlock;
        private boolean mContentChanged;
        private WeakHashMap<ChangeNotifier, Object> mNotifiers = new WeakHashMap();

        public NotifyBroker(Handler handler) {
            super(handler);
        }

        public synchronized void registerNotifier(ChangeNotifier notifier) {
            this.mNotifiers.put(notifier, null);
        }

        public void block(boolean block) {
            if (block) {
                this.mBlock = true;
                this.mContentChanged = false;
                return;
            }
            this.mBlock = false;
            if (this.mContentChanged) {
                this.mContentChanged = false;
                onChange(false);
            }
        }

        public synchronized void onChange(boolean selfChange) {
            if (this.mBlock) {
                this.mContentChanged = true;
            } else {
                for (ChangeNotifier notifier : this.mNotifiers.keySet()) {
                    notifier.onChange(selfChange);
                }
            }
        }
    }

    public DataManager(NoteAppImpl application) {
        this.mApplication = application;
        this.mDefaultMainHandler = new Handler(application.getMainLooper());
    }

    public synchronized void initializeSourceMap() {
        if (this.mSourceMap.isEmpty()) {
            addSource(new LocalSource(this.mApplication));
            addSource(new KeySource(this.mApplication));
            addSource(new LabelSource(this.mApplication));
        }
    }

    void addSource(NoteSource source) {
        if (source != null) {
            this.mSourceMap.put(source.getPrefix(), source);
        }
    }

    public NoteObject peekNoteObject(Path path) {
        return path.getObject();
    }

    public NoteObject getMediaObject(Path path) {
        synchronized (LOCK) {
            NoteObject obj = path.getObject();
            if (obj != null) {
                return obj;
            }
            NoteSource source = (NoteSource) this.mSourceMap.get(path.getPrefix());
            if (source == null) {
                Logger.printLog(TAG, "cannot find note source for path: " + path);
                return null;
            }
            try {
                NoteObject object = source.createMediaObject(path);
                if (object == null) {
                    Log.w(TAG, "cannot create note object: " + path);
                }
                return object;
            } catch (Throwable t) {
                Logger.printLog(TAG, "exception in creating note object: " + path + ",,,t,,," + t);
                return null;
            }
        }
    }

    public NoteObject getMediaObject(String s) {
        return getMediaObject(Path.fromString(s));
    }

    public NoteSet getMediaSet(Path path) {
        return (NoteSet) getMediaObject(path);
    }

    public NoteSet getMediaSet(String s) {
        return (NoteSet) getMediaObject(s);
    }

    public NoteSet[] getMediaSetsFromString(String segment) {
        String[] seq = Path.splitSequence(segment);
        int n = seq.length;
        NoteSet[] sets = new NoteSet[n];
        for (int i = 0; i < n; i++) {
            sets[i] = getMediaSet(seq[i]);
        }
        return sets;
    }

    public void delete(Path path) throws Exception {
        getMediaObject(path).delete();
    }

    public Uri getContentUri(Path path) {
        return getMediaObject(path).getContentUri();
    }

    public void registerChangeNotifier(Uri uri, ChangeNotifier notifier) {
        synchronized (this.mNotifierMap) {
            NotifyBroker broker = (NotifyBroker) this.mNotifierMap.get(uri);
            if (broker == null) {
                NotifyBroker broker2 = new NotifyBroker(this.mDefaultMainHandler);
                try {
                    this.mApplication.getContentResolver().registerContentObserver(uri, true, broker2);
                    this.mNotifierMap.put(uri, broker2);
                    broker = broker2;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    broker = broker2;
                    throw th2;
                }
            }
            try {
                broker.registerNotifier(notifier);
            } catch (Throwable th3) {
                th2 = th3;
                throw th2;
            }
        }
    }

    public void blockChangeNotifier(boolean block) {
        synchronized (this.mNotifierMap) {
            for (NotifyBroker broker : this.mNotifierMap.values()) {
                broker.block(block);
            }
        }
    }
}
