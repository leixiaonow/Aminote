package com.gionee.appupgrade.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.gionee.appupgrade.common.utils.LogUtils;
import com.gionee.appupgrade.common.utils.Utils;

public class NewVersion {
    private static final String TAG = "NewVersion";
    private String mClientName;
    private Editor mEditor;
    private SharedPreferences mSharedPreferences;

    public class VersionType {
        public static final String FORCED_VERSION = "1";
        public static final String NORMAL_VERSION = "0";
    }

    public NewVersion(Context context, String clientName) {
        if (context == null || clientName == null) {
            throw new NullPointerException();
        }
        this.mSharedPreferences = context.getSharedPreferences("upgrade_preferences_" + clientName + "_newversion", 0);
        this.mEditor = this.mSharedPreferences.edit();
        this.mClientName = clientName;
    }

    public String getStrUrl() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_DOWNLOAD_URL, "");
    }

    public void setStrUrl(String strUrl) {
        this.mEditor.putString(Utils.KEY_UPGRADE_DOWNLOAD_URL, strUrl).commit();
    }

    public String getReleaseNote() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_RELEASE_NOTE, "");
    }

    public void setReleaseNote(String releaseNote) {
        this.mEditor.putString(Utils.KEY_UPGRADE_RELEASE_NOTE, releaseNote).commit();
    }

    public String getDisplayVersion() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_DISPLAY_VERSION, "");
    }

    public void setDisplayVersion(String displayVersion) {
        this.mEditor.putString(Utils.KEY_UPGRADE_DISPLAY_VERSION, displayVersion).commit();
    }

    public String getFileSize() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_DOWNLOAD_FILE_SIZE, VersionType.NORMAL_VERSION);
    }

    public void setFileSize(String fileSize) {
        this.mEditor.putString(Utils.KEY_UPGRADE_DOWNLOAD_FILE_SIZE, fileSize).commit();
    }

    public String getUpgradeMode() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_UPGRADE_MODE, VersionType.NORMAL_VERSION);
    }

    public void setUpgradeMode(String upgradeMode) {
        if (VersionType.NORMAL_VERSION.equals(upgradeMode) || VersionType.FORCED_VERSION.equals(upgradeMode)) {
            this.mEditor.putString(Utils.KEY_UPGRADE_UPGRADE_MODE, upgradeMode).commit();
        } else {
            LogUtils.loge(TAG, this.mClientName + " setUpgradeMode() upgradeMode = " + upgradeMode);
        }
    }

    public String getTotalFileSize() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_TOTAL_FILE_SIZE, VersionType.NORMAL_VERSION);
    }

    public void setTotalFileSize(String totalSize) {
        this.mEditor.putString(Utils.KEY_UPGRADE_TOTAL_FILE_SIZE, totalSize).commit();
    }

    public boolean getIsPatchFile() {
        return this.mSharedPreferences.getBoolean(Utils.KEY_UPGRADE_IS_PATCH_FILE, false);
    }

    public void setIsPatchFile(boolean isPatchFile) {
        this.mEditor.putBoolean(Utils.KEY_UPGRADE_IS_PATCH_FILE, isPatchFile).commit();
    }

    public String getMd5() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_MD5, "");
    }

    public void setMd5(String md5) {
        this.mEditor.putString(Utils.KEY_UPGRADE_MD5, md5).commit();
    }

    public String getFullPackageMd5() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_FULL_PACKAGE_MD5, "");
    }

    public void setFullPackageMd5(String fullPackageMd5) {
        this.mEditor.putString(Utils.KEY_UPGRADE_FULL_PACKAGE_MD5, fullPackageMd5).commit();
    }

    public String getPatchId() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_FULL_PATCH_ID, "");
    }

    public void setPatchId(String patchId) {
        this.mEditor.putString(Utils.KEY_UPGRADE_FULL_PATCH_ID, patchId).commit();
    }

    public void initial() {
        this.mEditor.clear().commit();
    }

    public String getStoragedClientCurrentVersion() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_CURRENT_CLIENT_VERSION, "");
    }

    public void setStoragedClientCurrentVersion(String version) {
        this.mEditor.putString(Utils.KEY_UPGRADE_CURRENT_CLIENT_VERSION, version).commit();
    }

    public String getOldApkMd5() {
        return this.mSharedPreferences.getString(Utils.KEY_UPGRADE_OLD_APK_MD5, "");
    }

    public void setOldApkMd5(String md5) {
        this.mEditor.putString(Utils.KEY_UPGRADE_OLD_APK_MD5, md5).commit();
    }
}
