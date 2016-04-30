package com.gionee.framework.storage;

import com.gionee.framework.log.Logger;

public final class StorageMgr implements Storage {
    private Storage mImplStorageManager;

    private static final class Holder {
        private static final StorageMgr INSTANCE = new StorageMgr();

        private Holder() {
        }
    }

    private StorageMgr() {
        this.mImplStorageManager = new NormalSdkStorageManager();
    }

    public static StorageMgr getInstance() {
        return Holder.INSTANCE;
    }

    public boolean isSupportTwoSdcard() {
        return this.mImplStorageManager.isSupportTwoSdcard();
    }

    public boolean hasTwoSdcard() {
        return this.mImplStorageManager.hasTwoSdcard();
    }

    public boolean isNoSdcardMemory() {
        return this.mImplStorageManager.isNoSdcardMemory();
    }

    public boolean isSdcardAvailable() {
        return this.mImplStorageManager.isSdcardAvailable();
    }

    public String getSdcardRootPath() {
        Logger.printLog(Storage.TAG, "sdcard path = " + this.mImplStorageManager.getSdcardRootPath());
        return this.mImplStorageManager.getSdcardRootPath();
    }

    public long getSdcardTotalSize() {
        return this.mImplStorageManager.getSdcardTotalSize();
    }

    public long getSdcardAvailableSize() {
        return this.mImplStorageManager.getSdcardAvailableSize();
    }

    public String getInternalAppFilesPath() {
        return this.mImplStorageManager.getInternalAppFilesPath();
    }

    public long getInternalAvailableSize() {
        return this.mImplStorageManager.getInternalAvailableSize();
    }

    public long getInternalTotalSize() {
        return this.mImplStorageManager.getInternalTotalSize();
    }

    public boolean isNoInternalMemory() {
        return this.mImplStorageManager.isNoInternalMemory();
    }

    public void setOnSdcardStatusListener(SdcardStatusListener listener) {
        this.mImplStorageManager.setOnSdcardStatusListener(listener);
    }

    public void setOffSdcardStatusListener(SdcardStatusListener listener) {
        this.mImplStorageManager.setOffSdcardStatusListener(listener);
    }

    public String getExternalFilesDir(String path) {
        return this.mImplStorageManager.getExternalFilesDir(path);
    }
}
