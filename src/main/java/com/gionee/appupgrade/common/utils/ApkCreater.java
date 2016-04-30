package com.gionee.appupgrade.common.utils;

public class ApkCreater {
    public static final int ERROR_CODE_ERROR_PATHCH_FILE = 4;
    public static final int ERROR_CODE_MALLOC_MEMORY_FAILED = 5;
    public static final int ERROR_CODE_NEW_APK_IO_ERROR = 2;
    public static final int ERROR_CODE_OLD_APK_IO_ERROR = 1;
    public static final int ERROR_CODE_PATCH_FILE_IO_ERROR = 3;
    private static boolean sHasIncSofile;

    public static native int applyPatch(String str, String str2, String str3);

    static {
        sHasIncSofile = true;
        try {
            System.loadLibrary("_gn_appincupgrade");
        } catch (Throwable th) {
            sHasIncSofile = false;
        }
    }

    public static boolean hasIncSoFile() {
        return sHasIncSofile;
    }
}
