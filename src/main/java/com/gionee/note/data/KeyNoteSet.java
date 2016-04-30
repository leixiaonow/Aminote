package com.gionee.note.data;

import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.common.Future;
import com.gionee.note.common.FutureListener;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.data.NoteSet.ItemConsumer;
import com.gionee.note.provider.NoteContract.NoteContent;
import java.util.ArrayList;

public class KeyNoteSet extends NoteSet implements FutureListener<ArrayList<NoteItem>> {
    private static final String TAG = "KeyNoteSet";
    private NoteAppImpl mApp;
    private final NoteSet mBaseSet;
    private final Uri mBaseUri;
    private String mKey;
    private ArrayList<NoteItem> mKeyNotes = new ArrayList();
    private ArrayList<NoteItem> mLoadBuffer;
    private Future<ArrayList<NoteItem>> mLoadTask;
    private final Handler mMainHandler;
    private final ChangeNotifier mNotifier;

    private class KeyNoteLoader implements Job<ArrayList<NoteItem>> {
        private KeyNoteLoader() {
        }

        public ArrayList<NoteItem> run(JobContext jc) {
            final ArrayList<NoteItem> keyItems = new ArrayList();
            if (NoteUtils.checkExternalStoragePermission()) {
                KeyNoteSet.this.mBaseSet.enumerateNoteItems(new ItemConsumer() {
                    public void consume(int index, NoteItem item) {
                        if (KeyNoteSet.this.containKey(item)) {
                            keyItems.add(item);
                        }
                    }
                }, jc);
            }
            return keyItems;
        }
    }

    public KeyNoteSet(Path path, NoteAppImpl application) {
        super(path, NoteObject.nextVersionNumber());
        this.mApp = application;
        this.mBaseUri = NoteContent.CONTENT_URI;
        this.mBaseSet = application.getDataManager().getMediaSet(LocalSource.LOCAL_SET_PATH);
        this.mMainHandler = new Handler(application.getMainLooper());
        this.mNotifier = new ChangeNotifier(this, this.mBaseUri, application);
    }

    public Uri getContentUri() {
        return this.mBaseUri;
    }

    public ArrayList<NoteItem> getNoteItem(int start, int count) {
        ArrayList<NoteItem> list = new ArrayList();
        ArrayList<NoteItem> noteItems = this.mKeyNotes;
        int end = start + count;
        int size = noteItems.size();
        if (size != 0 && start < size) {
            if (end > size) {
                end = size;
            }
            list.addAll(noteItems.subList(start, end));
        }
        return list;
    }

    public int getNoteItemCount() {
        return this.mKeyNotes.size();
    }

    public synchronized long reload() {
        if (this.mNotifier.isDirty()) {
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mLoadTask = this.mApp.getThreadPool().submit(new KeyNoteLoader(), this);
        }
        if (this.mLoadBuffer != null) {
            this.mKeyNotes = this.mLoadBuffer;
            this.mLoadBuffer = null;
            this.mDataVersion = NoteObject.nextVersionNumber();
        }
        return this.mDataVersion;
    }

    public synchronized void onFutureDone(Future<ArrayList<NoteItem>> future) {
        if (this.mLoadTask == future) {
            this.mLoadBuffer = (ArrayList) future.get();
            if (this.mLoadBuffer == null) {
                this.mLoadBuffer = new ArrayList();
            }
            this.mMainHandler.post(new Runnable() {
                public void run() {
                    KeyNoteSet.this.notifyContentChanged();
                }
            });
        }
    }

    public synchronized void setKey(String key) {
        this.mKey = key;
        this.mLoadBuffer = null;
        this.mKeyNotes = new ArrayList();
        this.mNotifier.fakeChange();
    }

    private boolean containKey(NoteItem item) {
        String key = this.mKey;
        if (key == null || TextUtils.isEmpty(key)) {
            return false;
        }
        String title = item.getTitle();
        if (title == null || !title.contains(key)) {
            return DataConvert.getContent(item.getContent()).contains(key);
        }
        return true;
    }
}
