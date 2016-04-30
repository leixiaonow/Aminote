package com.gionee.framework.storage;

import android.os.Environment;
import android.os.StatFs;
import com.gionee.framework.log.Logger;

class NormalSdkStorageManager extends AbstractStorageManager {
    private static final long THRESHOLD = 100;

    NormalSdkStorageManager() {
    }

    private static long getSdcardAvailableSpace(boolean isTotal) {
        Logger.printLog(Storage.TAG, "getSdcardAvailableSpace");
        if ("mounted".equals(Environment.getExternalStorageState())) {
            try {
                long blocks;
                StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
                if (isTotal) {
                    blocks = (long) sf.getBlockCount();
                } else {
                    blocks = (long) sf.getAvailableBlocks();
                }
                long length = ((long) sf.getBlockSize()) * blocks;
                Logger.printLog(Storage.TAG, "getSdcardAvailableSpace length = " + length);
                return length;
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public boolean isSupportTwoSdcard() {
        return false;
    }

    public boolean hasTwoSdcard() {
        return false;
    }

    public boolean isNoSdcardMemory() {
        return getSdcardAvailableSpace(false) <= THRESHOLD;
    }

    public boolean isSdcardAvailable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public String getSdcardRootPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public long getSdcardTotalSize() {
        return getSdcardAvailableSpace(true);
    }

    public long getSdcardAvailableSize() {
        return getSdcardAvailableSpace(false);
    }

    void onSdcardStatusChange() {
    }
}
