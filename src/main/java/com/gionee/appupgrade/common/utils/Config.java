package com.gionee.appupgrade.common.utils;

import android.content.Context;
import android.os.Environment;
import android.os.SystemProperties;
import java.io.File;

public class Config {
    public static final String ACTION_CHECK_UPDATE_RESULT = "action_appupgrade_check_update_result";
    public static final int ANDROID_SDK_LEVEL = SystemProperties.getInt("ro.build.version.sdk", 3);
    public static final String APP_UPGRADE_PROD_FLAGE_FILE_NAME_ALL = "appupgrade1234567890prodall";
    public static final String APP_UPGRADE_TEST_FLAGE_FILE_NAME = "appupgrade1234567890test";
    public static final String APP_UPGRADE_TEST_FLAGE_FILE_NAME_ALL = "appupgrade1234567890testall";
    public static final int DEFAULT_CHECK_CYCLE = 43200000;
    public static final int DEFAULT_MIN_STORAGE_SIZE = 10485760;
    public static final String DEFAULT_VERSION = "1.0.0001";
    public static final int DOWNLOAD_TIMEOUT_CHECK_TIMES = 180;
    public static final boolean GEMINI_SUPPORT;
    public static final String GN_APP_SAVE_LOG_FLAGE_FILE_NAME = "appupgrade1234567890savelog";
    public static final String GN_APP_SAVE_LOG_FLAGE_FILE_NAME_OLD = "gnappupgrade1234567890test";
    public static final boolean IS_GIONEE_PHONE = SystemProperties.get("ro.product.brand", "").equalsIgnoreCase("GIONEE");
    public static final boolean IS_LOW_ANDROID_SDK_LEVEL;
    public static final boolean IS_MTK_PLATFORM;
    public static final String KEY = "JoyO9m8YdCVV1WGX2JAitw==";
    public static final int MAX_STORAGE_NUM = 2;
    public static final int NETWORK_CONNECT_TIMEOUT = 10000;
    public static final int NETWORK_SOCKET_TIMEOUT = 15000;
    public static final String NORMARL_HOST = "http://red.gionee.com";
    public static final String NORMARL_HOST_INC_FAILED = "http://update.gionee.com";
    private static final String TAG = "MSH.GnAppUpgradeConfig";
    public static final String TEST_HOST = "http://test1.gionee.com";
    private static final String URL_UPGRADE_PROD = "http://update.gionee.com/synth/open/checkUpgrade.do?";
    private static final String URL_UPGRADE_PROD_ALL = "http://update.gionee.com/synth/open/checkUpgrade.do?test=true&";
    private static final String URL_UPGRADE_TEST = "http://test1.gionee.com/synth/open/checkUpgrade.do?";
    private static final String URL_UPGRADE_TEST_ALL = "http://test1.gionee.com/synth/open/checkUpgrade.do?test=true&";

    static {
        boolean z;
        boolean z2 = true;
        if (ANDROID_SDK_LEVEL < 11) {
            z = true;
        } else {
            z = false;
        }
        IS_LOW_ANDROID_SDK_LEVEL = z;
        if ("true".equals(SystemProperties.get("ro.mediatek.gemini_support")) || "dsda".equals(SystemProperties.get("persist.multisim.config")) || "dsds".equals(SystemProperties.get("persist.multisim.config"))) {
            z = true;
        } else {
            z = false;
        }
        GEMINI_SUPPORT = z;
        if (SystemProperties.get("ro.mediatek.version.release") == null || SystemProperties.get("ro.mediatek.version.release").equals("")) {
            z2 = false;
        }
        IS_MTK_PLATFORM = z2;
    }

    public static void loadInitConfigs() {
        try {
            if (Environment.getExternalStorageState().equals("mounted")) {
                String sdCardDir = Utils.getExternalStoragePath() + "/";
                File saveLogFileOld = new File(sdCardDir + GN_APP_SAVE_LOG_FLAGE_FILE_NAME_OLD);
                File saveLogFile = new File(sdCardDir + GN_APP_SAVE_LOG_FLAGE_FILE_NAME);
                if (saveLogFileOld.exists() || saveLogFile.exists()) {
                    LogUtils.logd(TAG, "loadInitConfigs save log is true");
                    LogUtils.sIsSaveLog = true;
                    return;
                }
                LogUtils.logd(TAG, "loadInitConfigs save log is false !!!");
                LogUtils.sIsSaveLog = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getServerUri(Context context) {
        String defalutDNS = URL_UPGRADE_PROD;
        String firstMountedStoragePath = Utils.getFirstMountedStoragePath(context);
        if (firstMountedStoragePath != null) {
            try {
                String sdCardDir = firstMountedStoragePath + "/";
                File upgradeProdVersionAll = new File(sdCardDir + APP_UPGRADE_PROD_FLAGE_FILE_NAME_ALL);
                File upgradeTestVersion = new File(sdCardDir + APP_UPGRADE_TEST_FLAGE_FILE_NAME);
                File upgradeTestVersionAll = new File(sdCardDir + APP_UPGRADE_TEST_FLAGE_FILE_NAME_ALL);
                if (upgradeProdVersionAll.exists()) {
                    defalutDNS = URL_UPGRADE_PROD_ALL;
                } else if (upgradeTestVersion.exists()) {
                    defalutDNS = URL_UPGRADE_TEST;
                } else if (upgradeTestVersionAll.exists()) {
                    defalutDNS = URL_UPGRADE_TEST_ALL;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LogUtils.logd(TAG, "getServerUri() url = " + defalutDNS);
        return defalutDNS;
    }

    public static boolean isTestMode(Context context) {
        String firstMountedStoragePath = Utils.getFirstMountedStoragePath(context);
        if (firstMountedStoragePath == null) {
            return false;
        }
        File file = new File(firstMountedStoragePath + "/" + APP_UPGRADE_TEST_FLAGE_FILE_NAME);
        File file2 = new File(firstMountedStoragePath + "/" + APP_UPGRADE_TEST_FLAGE_FILE_NAME_ALL);
        if (file.exists() || file2.exists()) {
            return true;
        }
        return false;
    }
}
