package com.gionee.appupgrade.common.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import com.amigoui.internal.util.HanziToPinyin.Token;
import com.gionee.feedback.config.NetConfig.NetType;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Utils {
    public static final String KEY_UPGRADE_CURRENT_CLIENT_VERSION = "upgrade_current_client_version";
    public static final String KEY_UPGRADE_DISPLAY_THIS_VERSION = "upgrade_DisplayThisVersion";
    public static final String KEY_UPGRADE_DISPLAY_VERSION = "upgrade_displayVersion";
    public static final String KEY_UPGRADE_DOWNLOAD_FILE_SIZE = "upgrade_downloadFileSize";
    public static final String KEY_UPGRADE_DOWNLOAD_URL = "upgrade_downloadURL";
    public static final String KEY_UPGRADE_FULL_PACKAGE_MD5 = "upgrade_full_package_md5";
    public static final String KEY_UPGRADE_FULL_PATCH_ID = "upgrade_patch_id";
    public static final String KEY_UPGRADE_HAVE_NEW_VERSION = "upgrade_have_version";
    public static final String KEY_UPGRADE_IS_PATCH_FILE = "upgrade_is_patch_file";
    public static final String KEY_UPGRADE_LAST_CHECK_NEW_VERSION_NUM = "upgrade_last_check_new_version_num";
    public static final String KEY_UPGRADE_LAST_CHECK_TIME = "upgrade_last_check_time";
    public static final String KEY_UPGRADE_LAST_FAILED_PATCH_MD5 = "upgrade_last_patch_md5";
    public static final String KEY_UPGRADE_MD5 = "upgrade_md5";
    public static final String KEY_UPGRADE_NEED_INSTALL_BACKGROUND = "upgrade_needInstall";
    public static final String KEY_UPGRADE_OLD_APK_MD5 = "upgrade_old_apk_md5";
    public static final String KEY_UPGRADE_RELEASE_NOTE = "upgrade_releaseNote";
    public static final String KEY_UPGRADE_STATE = "upgrade_state";
    public static final String KEY_UPGRADE_TOTAL_FILE_SIZE = "upgrade_total_file_size";
    public static final String KEY_UPGRADE_UPGRADE_MODE = "upgrade_upgradeMode";
    private static final String TAG = "Utils";
    public static final String UPGRADE_APP_KEY = "upgrade_app_key";
    public static final String UPGRADE_APP_PERFERENCES = "upgrade_app_perferehces";
    public static final String UPGRADE_APP_SPLITE = "<->";
    public static final String UPGRADE_PREFERENCES = "upgrade_preferences";
    public static final String UPGRADE_VERSION_INFO = "version_info";

    public static String getUrlStringByXML(String result) {
        int start = result.indexOf("<go href=\"") + "<go href=\"".length();
        return result.substring(start, result.indexOf("\"></go>", start));
    }

    public static String getVerName(Context context) {
        String verName = "";
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            LogUtils.loge(TAG, e.getMessage());
            return verName;
        }
    }

    public static String getPackageName(Context appcationContext) {
        String verName = "";
        PackageManager packageManager = appcationContext.getPackageManager();
        try {
            verName = packageManager.getPackageInfo(appcationContext.getPackageName(), 0).applicationInfo.loadLabel(packageManager).toString();
        } catch (NameNotFoundException e) {
            LogUtils.loge(TAG, e.getMessage());
        }
        return verName;
    }

    public static void deleteDownloadFile(Context context, String clientName, String version, boolean isPatchFile) {
        try {
            String path = getDownloadedFilePathInAllMountedStorage(context, clientName, version, isPatchFile);
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    LogUtils.logd(TAG, "deleteDownloadFile() delete " + path + Token.SEPARATOR + file.delete());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteDownloadFile(Context context, String clientName, String version) {
        deleteDownloadFile(context, clientName, version, true);
        deleteDownloadFile(context, clientName, version, false);
    }

    public static String getVersion(Context context) {
        LogUtils.log(TAG, LogUtils.getThreadName());
        String version = "";
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return version;
        }
    }

    public static boolean verifyFile(String downloadFileName, long fileSize, Context context, String version) {
        boolean isCorrect = false;
        File file = new File(downloadFileName);
        if (file.exists() && file.isFile() && file.canRead() && file.length() == fileSize) {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                try {
                    PackageInfo info = packageManager.getPackageArchiveInfo(downloadFileName, 1);
                    if (info != null) {
                        String packageName = info.packageName;
                        String clientName = downloadFileName;
                        clientName = clientName.substring(clientName.lastIndexOf("/") + 1);
                        clientName = clientName.substring(0, clientName.indexOf("_"));
                        String clientVersion = info.versionName;
                        if (clientVersion.startsWith("v") || clientVersion.startsWith("V")) {
                            clientVersion = clientVersion.substring(1);
                        }
                        if (packageName != null && packageName.equals(clientName) && version.contains(clientVersion)) {
                            isCorrect = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        LogUtils.log(TAG, LogUtils.getThreadName() + "downloadFileName =  " + downloadFileName + " result = " + isCorrect);
        return isCorrect;
    }

    public static String getDownloadFilePath(Context context, String clientName, String version, boolean isPatch) {
        List<String> mountedStorages = getAllMountedStorageVolumesPath(context);
        if (mountedStorages.size() > 0) {
            for (String mountedPoint : mountedStorages) {
                try {
                    String filePath = getFilePathInAppointedStorage(mountedPoint, clientName, version, isPatch);
                    if (new File(filePath).exists()) {
                        return filePath;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        LogUtils.log(TAG, "getDownloadFilePath(): not exists");
        return null;
    }

    public static String getMainActivityOfPackage(Context clientContext) {
        PackageManager pm = clientContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        for (ResolveInfo info : pm.queryIntentActivities(intent, 1)) {
            if (info.activityInfo.packageName.equals(clientContext.getPackageName())) {
                return info.activityInfo.name;
            }
        }
        return null;
    }

    public static String getTopActivityPackageName(Context context) {
        String s = ((RunningTaskInfo) ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1).get(0)).topActivity.getPackageName();
        LogUtils.logd(TAG, "getTopActivityPackageName() PackageName is " + s);
        return s;
    }

    public static boolean isTestMode() {
        File file = new File(Config.APP_UPGRADE_TEST_FLAGE_FILE_NAME_ALL);
        File file2 = new File(Config.APP_UPGRADE_TEST_FLAGE_FILE_NAME);
        if (file.exists() || file2.exists()) {
            return true;
        }
        return false;
    }

    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }

    public static String getUaString(String imei) {
        try {
            Class productConfigurationClass = Class.forName("com.amigo.utils.ProductConfiguration");
            return (String) productConfigurationClass.getMethod("getUAString", new Class[]{String.class}).invoke(productConfigurationClass, new Object[]{imei});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
        String brand = SystemProperties.get("ro.product.brand", "GiONEE");
        String model = SystemProperties.get("ro.product.model", "Phone");
        String extModel = SystemProperties.get("ro.gn.extmodel", "Phone");
        String romVer = SystemProperties.get("ro.gn.gnromvernumber", "GiONEE ROM4.0.1");
        String Ver = romVer.substring(romVer.indexOf("M") == -1 ? 0 : romVer.indexOf("M") + 1);
        String language = Locale.getDefault().getLanguage();
        String uaString = "Mozilla/5.0 (Linux; U; Android " + VERSION.RELEASE + "; " + language + "-" + Locale.getDefault().getCountry().toLowerCase() + ";" + brand + "-" + model + "/" + extModel + " Build/IMM76D) AppleWebKit534.30(KHTML,like Gecko)Version/4.0 Mobile Safari/534.30 Id/" + GNDecodeUtils.get(imei) + " RV/" + Ver;
        LogUtils.logd("uaString", "uaString=" + uaString);
        return uaString;
    }

    public static String getDecodeImei(String imei) {
        try {
            Class gNDecodeUtilsClass = Class.forName("com.amigo.utils.DecodeUtils");
            return (String) gNDecodeUtilsClass.getMethod("get", new Class[]{String.class}).invoke(gNDecodeUtilsClass, new Object[]{imei});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
        return GNDecodeUtils.get(imei);
    }

    public static String getClientApkPath(Context clientContext) {
        if (clientContext == null) {
            return null;
        }
        String path = clientContext.getPackageResourcePath();
        if (path == null || path.length() == 0) {
            LogUtils.loge(TAG, "getClientApkPath() ClientApkPath is null ");
            return null;
        }
        LogUtils.logd(TAG, "getClientApkPath() ClientApkPath = " + path);
        if (new File(path).exists()) {
            return path;
        }
        LogUtils.loge(TAG, "getClientApkPath() file not exists ");
        return null;
    }

    public static boolean isIncUpgradeSupport(Context clientContext) {
        if (clientContext == null) {
            return false;
        }
        String clientPackagePath = getClientApkPath(clientContext);
        if (clientPackagePath == null || clientPackagePath.length() == 0) {
            return false;
        }
        try {
            if (new File(clientPackagePath.substring(0, clientPackagePath.lastIndexOf(".apk")).concat(".odex")).exists()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verifyFileByMd5(String filePath, String md5) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        try {
            if (!md5.equals(getFileMd5(file))) {
                return false;
            }
            LogUtils.logd(TAG, "verifyFileByMd5(" + filePath + ", " + "md5" + ")  sucessful");
            return true;
        } catch (Exception e) {
            LogUtils.loge(TAG, "verifyFileByMd5() e = " + e.toString());
            return false;
        }
    }

    public static String getFileMd5(File file) {
        Exception cause;
        Throwable th;
        FileInputStream fileInputStream = null;
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] bytes = new byte[8192];
            FileInputStream in = new FileInputStream(file);
            while (true) {
                try {
                    int byteCount = in.read(bytes);
                    if (byteCount <= 0) {
                        break;
                    }
                    digester.update(bytes, 0, byteCount);
                } catch (Exception e) {
                    cause = e;
                    fileInputStream = in;
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = in;
                }
            }
            byte[] digest = digester.digest();
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e2) {
                    LogUtils.loge(TAG, "getFileMd5() e = " + e2.toString());
                }
                return digest != null ? null : byteArrayToHexString(digest);
            }
            fileInputStream = in;
            if (digest != null) {
            }
        } catch (Exception e3) {
            cause = e3;
            try {
                throw new RuntimeException("Unable to compute MD5 of \"" + file + "\"", cause);
            } catch (Throwable th3) {
                th = th3;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e22) {
                        LogUtils.loge(TAG, "getFileMd5() e = " + e22.toString());
                    }
                }
                throw th;
            }
        }
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Integer.toHexString((b >> 4) & 15));
            result.append(Integer.toHexString(b & 15));
        }
        return result.toString();
    }

    public static String getAesUUID(String password) {
        String str = null;
        if (!(password == null || password.length() == 0)) {
            try {
                SecretKeySpec key = new SecretKeySpec(Base64.decode(password, 0), "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(1, key);
                String uuid = UUID.randomUUID().toString();
                LogUtils.loge(TAG, "getAesUUID() uuid = " + uuid);
                str = Base64.encodeToString(cipher.doFinal(uuid.getBytes("UTF-8")), 8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    public static String getEmmcPathWhenSDcardInsert(Context context) {
        String[] mountPoints = getStorageVolumesPath(context);
        if (mountPoints.length > 1) {
            return mountPoints[1];
        }
        return null;
    }

    public static String getFirstMountedStoragePath(Context context) {
        try {
            String[] mountPoints = getStorageVolumesPath(context);
            int mountPointsNum = mountPoints.length;
            LogUtils.loge(TAG, "getFirstMountedStoragePath() mountPointsNuml =  " + mountPointsNum);
            if (mountPoints != null) {
                int i = 0;
                while (i < 2 && i < mountPointsNum) {
                    if (getStorageVolumeState(context, mountPoints[i]).equals("mounted")) {
                        return mountPoints[i];
                    }
                    i++;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean hasMultiMountedStorage(Context context) {
        try {
            String[] mountPoints = getStorageVolumesPath(context);
            int mountPointsNum = mountPoints.length;
            if (mountPoints == null || mountPointsNum <= 1) {
                return false;
            }
            for (int i = 0; i < 2; i++) {
                if (!getStorageVolumeState(context, mountPoints[i]).equals("mounted")) {
                    return false;
                }
            }
            return true;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static long getAppointedStorageAvailableSpace(String mountPoint) {
        try {
            StatFs sf = new StatFs(mountPoint);
            long length = ((long) sf.getBlockSize()) * ((long) sf.getAvailableBlocks());
            LogUtils.logd(TAG, "getAppointedStorageAvailableSpace() length = " + length);
            return length;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            e2.printStackTrace();
        }
        return 0;
    }

    public static String getStorageVolumeState(Context context, String mountPoint) {
        if (context == null || mountPoint == null) {
            throw new NullPointerException();
        }
        StorageManager storageManager = (StorageManager) context.getSystemService("storage");
        if (storageManager != null && !Config.IS_LOW_ANDROID_SDK_LEVEL) {
            return storageManager.getVolumeState(mountPoint);
        }
        LogUtils.loge(TAG, "getVolumeState() state is null ");
        return Environment.getExternalStorageState();
    }

    public static String[] getStorageVolumesPath(Context context) {
        String[] paths = null;
        StorageManager storageManager = (StorageManager) context.getSystemService("storage");
        if (!(storageManager == null || Config.IS_LOW_ANDROID_SDK_LEVEL)) {
            paths = storageManager.getVolumePaths();
        }
        if (paths == null) {
            paths = new String[1];
        }
        paths[0] = getExternalStoragePath();
        return paths;
    }

    public static List<String> getAllMountedStorageVolumesPath(Context context) {
        List<String> mountedPaths = new ArrayList(2);
        String[] paths = getStorageVolumesPath(context);
        if (paths != null) {
            int length = paths.length;
            int i = 0;
            while (i < length && i < 2) {
                try {
                    if ("mounted".equals(getStorageVolumeState(context, paths[i]))) {
                        mountedPaths.add(paths[i]);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }
        return mountedPaths;
    }

    public static String getFilePathInAppointedStorage(String mountPoint, String clientName, String version, boolean isPatch) {
        if (mountPoint == null) {
            LogUtils.loge(TAG, "getFilePathInAppointedMountPoint() mountPoint is null ");
            return null;
        }
        String path = mountPoint + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + clientName;
        if (!(version == null || version.length() == 0)) {
            path = path.concat("_" + version);
        }
        if (isPatch) {
            path = path.concat(".patch");
        } else {
            path = path.concat(".apk");
        }
        LogUtils.logd(TAG, "getFilePathInAppointedMountPoint() path = " + path);
        return path;
    }

    public static String getDownloadedFilePathInAllMountedStorage(Context context, String clientName, String version, boolean isPatch) {
        List<String> mountedStorages = getAllMountedStorageVolumesPath(context);
        if (mountedStorages.size() > 0) {
            for (String mountedPoint : mountedStorages) {
                String filePath = getFilePathInAppointedStorage(mountedPoint, clientName, version, isPatch);
                if (new File(filePath).exists()) {
                    return filePath;
                }
            }
        }
        return null;
    }

    public static String getStoragePathOfDownloadFile(String downloadFilePath) {
        try {
            String storagePath = downloadFilePath.substring(0, downloadFilePath.indexOf(File.separator + Environment.DIRECTORY_DOWNLOADS));
            LogUtils.logd(TAG, "getStoragePathOfDownloadFile() storagePath = " + storagePath);
            return storagePath;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e2) {
            e2.printStackTrace();
        }
        return null;
    }

    public static String getExternalStoragePath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static boolean isMobileNetwork(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            LogUtils.logd(TAG, "isMobileNetwork  getType: " + activeNetworkInfo.getType() + " activeNetworkInfo.getState(): " + activeNetworkInfo.getState());
            if (activeNetworkInfo.getType() == 0 && activeNetworkInfo.getState() == State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    private static void appene2GToUrl(StringBuffer url, boolean isWapNetwork) {
        url.append(NetType.NET_TYPE_2G);
        if (isWapNetwork) {
            url.append("&wap");
        }
    }
}
