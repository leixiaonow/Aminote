package com.gionee.note.data;

import com.gionee.note.common.ThreadPool.JobContext;
import java.util.ArrayList;
import java.util.WeakHashMap;

public abstract class NoteSet extends NoteObject {
    public static final int MEDIAITEM_BATCH_FETCH_COUNT = 50;
    private WeakHashMap<ContentListener, Object> mListeners = new WeakHashMap();

    public interface ItemConsumer {
        void consume(int i, NoteItem noteItem);
    }

    public abstract long reload();

    public NoteSet(Path path, long version) {
        super(path, version);
    }

    public int getNoteItemCount() {
        return 0;
    }

    public ArrayList<NoteItem> getNoteItem(int start, int count) {
        return new ArrayList();
    }

    public void enumerateNoteItems(ItemConsumer consumer, JobContext jc) {
        enumerateNoteItems(consumer, 0, jc);
    }

    protected void enumerateNoteItems(ItemConsumer consumer, int startIndex, JobContext jc) {
        int total = getNoteItemCount();
        int start = 0;
        while (start < total && !jc.isCancelled()) {
            int count = Math.min(50, total - start);
            ArrayList<NoteItem> items = getNoteItem(start, count);
            int i = 0;
            int n = items.size();
            while (i < n) {
                if (!jc.isCancelled()) {
                    consumer.consume((startIndex + start) + i, (NoteItem) items.get(i));
                    i++;
                } else {
                    return;
                }
            }
            start += count;
        }
    }

    public void addContentListener(ContentListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.put(listener, null);
        }
    }

    public void removeContentListener(ContentListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
        }
    }

    public void notifyContentChanged() {
        synchronized (this.mListeners) {
            for (ContentListener listener : this.mListeners.keySet()) {
                listener.onContentDirty();
            }
        }
    }
}
