package com.gionee.note.data;

import android.net.Uri;
import android.os.Handler;
import com.gionee.note.app.LabelManager.LabelHolder;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.common.Future;
import com.gionee.note.common.FutureListener;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.data.NoteSet.ItemConsumer;
import com.gionee.note.provider.NoteContract.NoteContent;
import java.util.ArrayList;
import java.util.Iterator;

public class LabelNoteSet extends NoteSet implements FutureListener<ArrayList<NoteItem>> {
    private static final String TAG = "LabelNoteSet";
    private NoteAppImpl mApp;
    private final NoteSet mBaseSet;
    private final Uri mBaseUri;
    private int mLabel;
    private ArrayList<NoteItem> mLabelNotes = new ArrayList();
    private ArrayList<LabelHolder> mLabels;
    private ArrayList<NoteItem> mLoadBuffer;
    private Future<ArrayList<NoteItem>> mLoadTask;
    private final Handler mMainHandler;
    private final ChangeNotifier mNotifier;

    private class LabelNoteLoader implements Job<ArrayList<NoteItem>> {
        private LabelNoteLoader() {
        }

        public ArrayList<NoteItem> run(JobContext jc) {
            final ArrayList<NoteItem> labelItems = new ArrayList();
            if (LabelNoteSet.this.containLabel(LabelNoteSet.this.mLabel)) {
                LabelNoteSet.this.mBaseSet.enumerateNoteItems(new ItemConsumer() {
                    public void consume(int index, NoteItem item) {
                        if (LabelNoteSet.this.containLabel(item.getLabel())) {
                            labelItems.add(item);
                        }
                    }
                }, jc);
            }
            return labelItems;
        }
    }

    public LabelNoteSet(Path path, NoteAppImpl application) {
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
        ArrayList<NoteItem> noteItems = this.mLabelNotes;
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
        return this.mLabelNotes.size();
    }

    public synchronized long reload() {
        if (this.mNotifier.isDirty()) {
            if (this.mLoadTask != null) {
                this.mLoadTask.cancel();
            }
            this.mLoadTask = this.mApp.getThreadPool().submit(new LabelNoteLoader(), this);
        }
        if (this.mLoadBuffer != null) {
            this.mLabelNotes = this.mLoadBuffer;
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
                    LabelNoteSet.this.notifyContentChanged();
                }
            });
        }
    }

    public synchronized void setLabel(int label) {
        this.mLabel = label;
        this.mLoadBuffer = null;
        this.mLabelNotes = new ArrayList();
        this.mNotifier.fakeChange();
    }

    private boolean containLabel(ArrayList<Integer> itemLabel) {
        return itemLabel.contains(Integer.valueOf(this.mLabel));
    }

    public void setLabels(ArrayList<LabelHolder> labels) {
        synchronized (this) {
            this.mLabels = labels;
            this.mDataVersion = NoteObject.nextVersionNumber();
        }
        notifyContentChanged();
    }

    private boolean containLabel(int label) {
        synchronized (this) {
            Iterator i$ = this.mLabels.iterator();
            while (i$.hasNext()) {
                if (((LabelHolder) i$.next()).mId == label) {
                    return true;
                }
            }
            return false;
        }
    }
}
