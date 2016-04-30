package com.gionee.framework.storage;

interface Storage {
    public static final String TAG = "StorageMgr";

    String getExternalFilesDir(String str);

    String getInternalAppFilesPath();

    long getInternalAvailableSize();

    long getInternalTotalSize();

    long getSdcardAvailableSize();

    String getSdcardRootPath();

    long getSdcardTotalSize();

    boolean hasTwoSdcard();

    boolean isNoInternalMemory();

    boolean isNoSdcardMemory();

    boolean isSdcardAvailable();

    boolean isSupportTwoSdcard();

    void setOffSdcardStatusListener(SdcardStatusListener sdcardStatusListener);

    void setOnSdcardStatusListener(SdcardStatusListener sdcardStatusListener);
}
