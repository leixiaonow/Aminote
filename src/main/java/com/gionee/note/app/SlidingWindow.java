package com.gionee.note.app;

import android.app.Activity;
import android.net.Uri;
import com.gionee.note.app.DataLoader.DataListener;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.PlatformUtil;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.data.NoteItem;
import com.gionee.note.data.NoteParser;
import com.gionee.note.data.NoteSet;
import com.gionee.note.data.Path;

public class SlidingWindow implements DataListener {
    private static final int CACHE_SIZE = 36;
    private static final String TAG = "SlidingWindow";
    private int mActiveEnd = 0;
    private int mActiveStart = 0;
    private int mContentEnd = 0;
    private int mContentStart = 0;
    private int mCount;
    private final NoteEntry[] mData;
    private final DataLoader mDataLoader;
    private boolean mIsActive = false;
    private Listener mListener;
    private Object mLock = new Object();
    private NoteParser mNoteParser;

    public interface Listener {
        void onContentChanged();

        void onCountChanged(int i);
    }

    public static class NoteEntry {
        public String content = null;
        public int id;
        public NoteItem item;
        public int mediaType = -1;
        public Uri originUri = null;
        public Path path;
        public long reminder = 0;
        public Uri thumbnailUri = null;
        public String time;
        public long timeMillis;
        public String title = null;
    }

    public SlidingWindow(Activity activity, NoteSet set, LoadingListener loadingListener) {
        this.mDataLoader = new DataLoader(activity, set);
        this.mDataLoader.setLoadingListener(loadingListener);
        this.mDataLoader.setDataListener(this);
        this.mData = new NoteEntry[36];
        this.mNoteParser = new NoteParser();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public NoteEntry get(int itemIndex) {
        NoteEntry noteEntry;
        synchronized (this.mLock) {
            noteEntry = this.mData[itemIndex % this.mData.length];
        }
        return noteEntry;
    }

    private boolean isActiveSlot(int itemIndex) {
        return itemIndex >= this.mActiveStart && itemIndex < this.mActiveEnd;
    }

    private boolean isContentSlot(int itemIndex) {
        return itemIndex >= this.mContentStart && itemIndex < this.mContentEnd;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            int n;
            int i;
            if (contentStart >= this.mContentEnd || this.mContentStart >= contentEnd) {
                n = this.mContentEnd;
                for (i = this.mContentStart; i < n; i++) {
                    freeSlotContent(i);
                }
                this.mDataLoader.setActiveWindow(contentStart, contentEnd);
                for (i = contentStart; i < contentEnd; i++) {
                    prepareSlotContent(i);
                }
            } else {
                for (i = this.mContentStart; i < contentStart; i++) {
                    freeSlotContent(i);
                }
                n = this.mContentEnd;
                for (i = contentEnd; i < n; i++) {
                    freeSlotContent(i);
                }
                this.mDataLoader.setActiveWindow(contentStart, contentEnd);
                n = this.mContentStart;
                for (i = contentStart; i < n; i++) {
                    prepareSlotContent(i);
                }
                for (i = this.mContentEnd; i < contentEnd; i++) {
                    prepareSlotContent(i);
                }
            }
            this.mContentStart = contentStart;
            this.mContentEnd = contentEnd;
        }
    }

    public void setActiveWindow(int start, int end) {
        if (start > end || end - start > this.mData.length || end > this.mCount) {
            NoteUtils.fail("%s, %s, %s, %s", Integer.valueOf(start), Integer.valueOf(end), Integer.valueOf(this.mData.length), Integer.valueOf(this.mCount));
        }
        NoteEntry[] data = this.mData;
        this.mActiveStart = start;
        this.mActiveEnd = end;
        int contentStart = start;
        int contentEnd = end;
        if (this.mIsActive) {
            contentStart = NoteUtils.clamp(((start + end) / 2) - (data.length / 2), 0, Math.max(0, this.mCount - data.length));
            contentEnd = Math.min(data.length + contentStart, this.mCount);
        }
        setContentWindow(contentStart, contentEnd);
    }

    private void freeSlotContent(int itemIndex) {
        synchronized (this.mLock) {
            NoteEntry[] data = this.mData;
            data[itemIndex % data.length] = null;
        }
    }

    private void prepareSlotContent(final int itemIndex) {
        NoteAppImpl.getContext().getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                NoteEntry entry = new NoteEntry();
                NoteItem item = SlidingWindow.this.mDataLoader.get(itemIndex);
                entry.item = item;
                entry.path = item == null ? null : item.getPath();
                SlidingWindow.this.mNoteParser.parseNote(entry, item);
                synchronized (SlidingWindow.this.mLock) {
                    SlidingWindow.this.mData[itemIndex % SlidingWindow.this.mData.length] = entry;
                }
                if (SlidingWindow.this.mListener != null && SlidingWindow.this.isActiveSlot(itemIndex)) {
                    SlidingWindow.this.mListener.onContentChanged();
                }
                return null;
            }
        });
    }

    public void resume() {
        this.mIsActive = true;
        this.mDataLoader.resume();
        if (PlatformUtil.isGioneeDevice()) {
            setActiveWindow(this.mActiveStart, this.mActiveEnd);
        }
    }

    public void pause() {
        this.mIsActive = false;
        this.mDataLoader.pause();
        if (PlatformUtil.isGioneeDevice()) {
            setActiveWindow(this.mActiveStart, this.mActiveEnd);
        }
    }

    public void destroy() {
        this.mDataLoader.setLoadingListener(null);
    }

    public void onCountChanged(int count) {
        if (this.mCount != count) {
            this.mCount = count;
            if (this.mListener != null) {
                this.mListener.onCountChanged(this.mCount);
            }
            if (this.mContentEnd > this.mCount) {
                this.mContentEnd = this.mCount;
            }
            if (this.mActiveEnd > this.mCount) {
                this.mActiveEnd = this.mCount;
            }
        }
    }

    public void onContentChanged(int itemIndex) {
        if (itemIndex >= this.mContentStart && itemIndex < this.mContentEnd) {
            freeSlotContent(itemIndex);
            prepareSlotContent(itemIndex);
            if (this.mListener != null && isActiveSlot(itemIndex)) {
                this.mListener.onContentChanged();
            }
        }
    }
}
