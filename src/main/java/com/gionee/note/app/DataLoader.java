package com.gionee.note.app;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import com.gionee.framework.log.Logger;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.data.ContentListener;
import com.gionee.note.data.NoteItem;
import com.gionee.note.data.NoteSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class DataLoader {
    private static final int DATA_CACHE_SIZE = 100;
    private static final int MAX_LOAD_COUNT = 64;
    private static final int MIN_LOAD_COUNT = 32;
    private static final int MSG_LOAD_FINISH = 2;
    private static final int MSG_LOAD_START = 1;
    private static final int MSG_RUN_OBJECT = 3;
    private static final String TAG = "DataLoader";
    private int mActiveEnd = 0;
    private int mActiveStart = 0;
    private int mContentEnd = 0;
    private MyContentListener mContentListener = new MyContentListener();
    private int mContentStart = 0;
    private int mCount = 0;
    private final NoteItem[] mData;
    private DataListener mDataListener;
    private long mFailedVersion = -1;
    private final long[] mItemVersion;
    private LoadingListener mLoadingListener;
    private final Handler mMainHandler;
    private final NoteSet mNoteSet;
    private ReloadTask mReloadTask;
    private final long[] mSetVersion;
    private long mSourceVersion = -1;

    public interface DataListener {
        void onContentChanged(int i);

        void onCountChanged(int i);
    }

    private class GetUpdateInfo implements Callable<UpdateInfo> {
        private final long mVersion;

        public GetUpdateInfo(long version) {
            this.mVersion = version;
        }

        public UpdateInfo call() throws Exception {
            if (DataLoader.this.mFailedVersion == this.mVersion) {
                return null;
            }
            UpdateInfo info = new UpdateInfo();
            long version = this.mVersion;
            info.version = DataLoader.this.mSourceVersion;
            info.size = DataLoader.this.mCount;
            long[] setVersion = DataLoader.this.mSetVersion;
            int n = DataLoader.this.mContentEnd;
            for (int i = DataLoader.this.mContentStart; i < n; i++) {
                if (setVersion[i % 100] != version) {
                    info.reloadStart = i;
                    info.reloadCount = Math.min(64, n - i);
                    return info;
                }
            }
            return DataLoader.this.mSourceVersion == this.mVersion ? null : info;
        }
    }

    private class ReloadTask extends Thread {
        private volatile boolean mActive;
        private volatile boolean mDirty;
        private boolean mIsLoading;

        private ReloadTask() {
            this.mActive = true;
            this.mDirty = true;
            this.mIsLoading = false;
        }

        private void updateLoading(boolean loading) {
            if (this.mIsLoading != loading) {
                this.mIsLoading = loading;
                DataLoader.this.mMainHandler.sendEmptyMessage(loading ? 1 : 2);
            }
        }

        public void run() {
            Process.setThreadPriority(10);
            boolean updateComplete = false;
            while (this.mActive) {
                synchronized (this) {
                    if (this.mActive && !this.mDirty && updateComplete) {
                        updateLoading(false);
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            Logger.printLog(DataLoader.TAG, "unexpected interrupt: " + this);
                        }
                    } else {
                        this.mDirty = false;
                        updateLoading(true);
                        long version = DataLoader.this.mNoteSet.reload();
                        UpdateInfo info = (UpdateInfo) DataLoader.this.executeAndWait(new GetUpdateInfo(version));
                        updateComplete = info == null;
                        if (!updateComplete) {
                            if (info.version != version) {
                                info.size = DataLoader.this.mNoteSet.getNoteItemCount();
                                if (info.size >= 0) {
                                    info.version = version;
                                }
                            }
                            if (info.reloadCount > 0) {
                                info.items = DataLoader.this.mNoteSet.getNoteItem(info.reloadStart, info.reloadCount);
                            }
                            DataLoader.this.executeAndWait(new UpdateContent(info));
                        }
                    }
                }
            }
            updateLoading(false);
        }

        public synchronized void notifyDirty() {
            this.mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            this.mActive = false;
            notifyAll();
        }
    }

    private class UpdateContent implements Callable<Void> {
        private UpdateInfo mUpdateInfo;

        public UpdateContent(UpdateInfo info) {
            this.mUpdateInfo = info;
        }

        public Void call() throws Exception {
            UpdateInfo info = this.mUpdateInfo;
            DataLoader.this.mSourceVersion = info.version;
            if (DataLoader.this.mCount != info.size) {
                DataLoader.this.mCount = info.size;
                if (DataLoader.this.mDataListener != null) {
                    DataLoader.this.mDataListener.onCountChanged(DataLoader.this.mCount);
                }
                if (DataLoader.this.mContentEnd > DataLoader.this.mCount) {
                    DataLoader.this.mContentEnd = DataLoader.this.mCount;
                }
                if (DataLoader.this.mActiveEnd > DataLoader.this.mCount) {
                    DataLoader.this.mActiveEnd = DataLoader.this.mCount;
                }
            }
            ArrayList<NoteItem> items = info.items;
            DataLoader.this.mFailedVersion = -1;
            if (items != null && !items.isEmpty()) {
                int start = Math.max(info.reloadStart, DataLoader.this.mContentStart);
                int end = Math.min(info.reloadStart + items.size(), DataLoader.this.mContentEnd);
                int i = start;
                while (i < end) {
                    int index = i % 100;
                    DataLoader.this.mSetVersion[index] = info.version;
                    NoteItem updateItem = (NoteItem) items.get(i - info.reloadStart);
                    long itemVersion = updateItem.getDataVersion();
                    if (DataLoader.this.mItemVersion[index] != itemVersion) {
                        DataLoader.this.mItemVersion[index] = itemVersion;
                        DataLoader.this.mData[index] = updateItem;
                        if (DataLoader.this.mDataListener != null && i >= DataLoader.this.mActiveStart && i < DataLoader.this.mActiveEnd) {
                            DataLoader.this.mDataListener.onContentChanged(i);
                        }
                    }
                    i++;
                }
            } else if (info.reloadCount > 0) {
                DataLoader.this.mFailedVersion = info.version;
                Logger.printLog(DataLoader.TAG, "loading failed: " + DataLoader.this.mFailedVersion);
            }
            return null;
        }
    }

    private static class UpdateInfo {
        public ArrayList<NoteItem> items;
        public int reloadCount;
        public int reloadStart;
        public int size;
        public long version;

        private UpdateInfo() {
        }
    }

    private class MyContentListener implements ContentListener {
        private MyContentListener() {
        }

        public void onContentDirty() {
            if (DataLoader.this.mReloadTask != null) {
                DataLoader.this.mReloadTask.notifyDirty();
            }
        }
    }

    public DataLoader(Activity activity, NoteSet noteSet) {
        this.mNoteSet = noteSet;
        this.mData = new NoteItem[100];
        this.mItemVersion = new long[100];
        this.mSetVersion = new long[100];
        Arrays.fill(this.mItemVersion, -1);
        Arrays.fill(this.mSetVersion, -1);
        this.mMainHandler = new Handler(activity.getMainLooper()) {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        if (DataLoader.this.mLoadingListener != null) {
                            DataLoader.this.mLoadingListener.onLoadingStarted();
                            return;
                        }
                        return;
                    case 2:
                        if (DataLoader.this.mLoadingListener != null) {
                            DataLoader.this.mLoadingListener.onLoadingFinished(DataLoader.this.mFailedVersion != -1);
                            return;
                        }
                        return;
                    case 3:
                        ((Runnable) message.obj).run();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void resume() {
        this.mNoteSet.addContentListener(this.mContentListener);
        this.mReloadTask = new ReloadTask();
        this.mReloadTask.start();
    }

    public void pause() {
        this.mReloadTask.terminate();
        this.mReloadTask = null;
        this.mNoteSet.removeContentListener(this.mContentListener);
    }

    public NoteItem get(int index) {
        if (isActive(index)) {
            return this.mData[index % this.mData.length];
        }
        return (NoteItem) this.mNoteSet.getNoteItem(index, 1).get(0);
    }

    public int getActiveStart() {
        return this.mActiveStart;
    }

    public boolean isActive(int index) {
        return index >= this.mActiveStart && index < this.mActiveEnd;
    }

    public int getCount() {
        return this.mCount;
    }

    private void clearSlot(int slotIndex) {
        this.mData[slotIndex] = null;
        this.mItemVersion[slotIndex] = -1;
        this.mSetVersion[slotIndex] = -1;
    }

    private void setContentWindow(int contentStart, int contentEnd) {
        if (contentStart != this.mContentStart || contentEnd != this.mContentEnd) {
            int end = this.mContentEnd;
            int start = this.mContentStart;
            synchronized (this) {
                this.mContentStart = contentStart;
                this.mContentEnd = contentEnd;
            }
            int n;
            int i;
            if (contentStart >= end || start >= contentEnd) {
                n = end;
                for (i = start; i < n; i++) {
                    clearSlot(i % 100);
                }
            } else {
                for (i = start; i < contentStart; i++) {
                    clearSlot(i % 100);
                }
                n = end;
                for (i = contentEnd; i < n; i++) {
                    clearSlot(i % 100);
                }
            }
            if (this.mReloadTask != null) {
                this.mReloadTask.notifyDirty();
            }
        }
    }

    public void setActiveWindow(int start, int end) {
        if (start != this.mActiveStart || end != this.mActiveEnd) {
            boolean z;
            if (start > end || end - start > this.mData.length || end > this.mCount) {
                z = false;
            } else {
                z = true;
            }
            NoteUtils.assertTrue(z);
            int length = this.mData.length;
            this.mActiveStart = start;
            this.mActiveEnd = end;
            if (start != end) {
                int contentStart = NoteUtils.clamp(((start + end) / 2) - (length / 2), 0, Math.max(0, this.mCount - length));
                int contentEnd = Math.min(contentStart + length, this.mCount);
                if (this.mContentStart > start || this.mContentEnd < end || Math.abs(contentStart - this.mContentStart) > 32) {
                    setContentWindow(contentStart, contentEnd);
                }
            }
        }
    }

    public void setDataListener(DataListener listener) {
        this.mDataListener = listener;
    }

    public void setLoadingListener(LoadingListener listener) {
        this.mLoadingListener = listener;
    }

    private <T> T executeAndWait(Callable<T> callable) {
        FutureTask<T> task = new FutureTask(callable);
        this.mMainHandler.sendMessage(this.mMainHandler.obtainMessage(3, task));
        try {
            return task.get();
        } catch (InterruptedException e) {
            return null;
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        }
    }
}
