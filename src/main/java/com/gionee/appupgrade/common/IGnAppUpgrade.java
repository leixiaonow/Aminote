package com.gionee.appupgrade.common;

import android.app.Activity;
import android.content.Context;

public interface IGnAppUpgrade {

    public interface CallBack {
        void onDownLoading(int i, int i2, String str);

        void onError(int i, String str);

        void onOperationStateChange(int i, String str);
    }

    public static class Error {
        public static final int DICK_NOSPACE = 102;
        public static final int ERROR_EMMC_NOSPACE = 110;
        public static final int ERROR_LOCAL_FILE_NOT_FOUND = 105;
        public static final int ERROR_LOCAL_FILE_VERIFY_ERROR = 106;
        public static final int ERROR_LOW_MEMORY = 108;
        public static final int ERROR_PATCH_FILE_ERROR = 107;
        public static final int ERROR_UPGRADING = 104;
        public static final int ERROR_VERIFY_FILE_ERROR = 109;
        public static final int NET_CONNECT_ERROR = 100;
        public static final int NOTIFY_REMOTE_FILE_NOTFOUND = 103;
        public static final int NO_SDCARD = 101;
    }

    public static class Status {
        public static final int DOWNLOAD_COMPLETE = 3;
        public static final int HAS_NEW_VERSION = 1;
        public static final int HAS_NOT_NEW_VERSION = 2;
    }

    Runnable checkApkVersion(boolean z, boolean z2);

    Runnable downLoadApk();

    int getDownloadFileSize();

    boolean getIsPatchFile();

    String getNewVersionNum();

    String getReleaseNote();

    int getTotalFileSize();

    boolean haveNewVersion();

    void initial(CallBack callBack, Context context, String str);

    Runnable installApk(Activity activity, int i);

    boolean isForceMode();

    void setIncUpgradeEnabled(boolean z);
}
