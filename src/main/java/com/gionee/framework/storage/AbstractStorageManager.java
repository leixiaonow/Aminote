package com.gionee.framework.storage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.StatFs;
import com.gionee.framework.component.ApplicationContextHolder;
import com.gionee.framework.log.Logger;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class AbstractStorageManager implements Storage, ApplicationContextHolder {
    private static final CopyOnWriteArrayList<SdcardStatusListener> ALL_LISTENERS = new CopyOnWriteArrayList();
    static final boolean DEBUG = true;
    protected boolean mSdcardAvailble = Environment.getExternalStorageState().equals("mounted");
    private BroadcastReceiver mSdcardStateChangeListener = new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent intent) {
            String action = intent.getAction();
            Logger.printLog(Storage.TAG, "action = " + action);
            Logger.printLog(Storage.TAG, "path = " + intent.getData().getPath());
            AbstractStorageManager.this.onSdcardStatusChange();
            if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
                if (AbstractStorageManager.this.mSdcardStatus.equals(SdcardStatus.DISABLED)) {
                    AbstractStorageManager.this.mSdcardAvailble = AbstractStorageManager.DEBUG;
                    AbstractStorageManager.this.setupStatus();
                    AbstractStorageManager.this.performCallbackTraversal(AbstractStorageManager.this.mSdcardAvailble);
                }
            } else if (AbstractStorageManager.this.mSdcardStatus.equals(SdcardStatus.ENABLED)) {
                AbstractStorageManager.this.mSdcardAvailble = false;
                AbstractStorageManager.this.setupStatus();
                AbstractStorageManager.this.performCallbackTraversal(AbstractStorageManager.this.mSdcardAvailble);
            }
        }
    };
    private SdcardStatus mSdcardStatus;

    private enum SdcardStatus {
        ENABLED,
        DISABLED
    }

    abstract void onSdcardStatusChange();

    AbstractStorageManager() {
        registerSdcardReceiver();
        setupStatus();
    }

    private void registerSdcardReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.MEDIA_REMOVED");
        filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        filter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        filter.addAction("android.intent.action.MEDIA_EJECT");
        filter.addDataScheme("file");
        CONTEXT.registerReceiver(this.mSdcardStateChangeListener, filter);
    }

    private void setupStatus() {
        this.mSdcardStatus = this.mSdcardAvailble ? SdcardStatus.ENABLED : SdcardStatus.DISABLED;
    }

    private void performCallbackTraversal(boolean enabled) {
        Iterator i$ = ALL_LISTENERS.iterator();
        while (i$.hasNext()) {
            SdcardStatusListener callback = (SdcardStatusListener) i$.next();
            if (enabled) {
                callback.onEnabled();
            } else {
                callback.onDisabled();
            }
        }
    }

    private long getInternalMemorySizeHelper(boolean isTotal) {
        long blocks;
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = (long) stat.getBlockSize();
        Logger.printLog(Storage.TAG, "Internal storage directory is " + path + ",isTotal is " + isTotal);
        if (isTotal) {
            blocks = (long) stat.getBlockCount();
        } else {
            blocks = (long) stat.getAvailableBlocks();
        }
        long totalSize = blockSize * blocks;
        Logger.printLog(Storage.TAG, "blockSize is " + blockSize + ",blocks is " + blocks + ",totalSize is  " + totalSize);
        return totalSize;
    }

    public String getInternalAppFilesPath() {
        return CONTEXT.getFilesDir().getAbsolutePath();
    }

    public long getInternalAvailableSize() {
        long availableSize = getInternalMemorySizeHelper(false);
        Logger.printLog(Storage.TAG, "Internal total available size is  " + ((int) availableSize) + " KB");
        return availableSize;
    }

    public long getInternalTotalSize() {
        long totalSize = getInternalMemorySizeHelper(DEBUG);
        Logger.printLog(Storage.TAG, "Internal total size is  " + ((int) totalSize) + " KB");
        return totalSize;
    }

    public boolean isNoInternalMemory() {
        return getInternalAvailableSize() <= 0 ? DEBUG : false;
    }

    public void setOnSdcardStatusListener(SdcardStatusListener listener) {
        ALL_LISTENERS.add(listener);
    }

    public void setOffSdcardStatusListener(SdcardStatusListener listener) {
        ALL_LISTENERS.remove(listener);
    }

    public String getExternalFilesDir(String path) {
        File file = CONTEXT.getExternalFilesDir(path);
        if (file != null) {
            return file.getPath();
        }
        return getSdcardRootPath() + "/Android/data/com.gionee.amiweather/files/" + path;
    }
}
