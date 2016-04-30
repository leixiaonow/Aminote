package com.gionee.appupgrade.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import com.amigoui.internal.util.HanziToPinyin.Token;
import com.gionee.appupgrade.common.IGnAppUpgrade.CallBack;
import com.gionee.appupgrade.common.IGnAppUpgrade.Error;
import com.gionee.appupgrade.common.NewVersion.VersionType;
import com.gionee.appupgrade.common.http.HttpManager;
import com.gionee.appupgrade.common.parsers.NewVersionParser;
import com.gionee.appupgrade.common.utils.ApkCreater;
import com.gionee.appupgrade.common.utils.Config;
import com.gionee.appupgrade.common.utils.LogUtils;
import com.gionee.appupgrade.common.utils.NetworkUtils;
import com.gionee.appupgrade.common.utils.Utils;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;

public class GnAppUpgradeImple implements IGnAppUpgrade {
    private static final int GET_AND_PARSE_UPGRADE_INFO_ERROR = 0;
    private static final int GET_AND_PARSE_UPGRADE_INFO_NO_NEW_VERSION = 0;
    private static final int GET_AND_PARSE_UPGRADE_INFO_SUCESSFUL = 1;
    private static final String TAG = "GnAppUpgradeImple";
    private boolean mActivityRunning = false;
    private Editor mAppEditor = null;
    private SharedPreferences mAppPreferences = null;
    private CallBack mCallBack;
    private String mClientName;
    private NewVersion mClientVersionInfo = null;
    private Context mContext;
    private int mDownloadLength = 0;
    private GnAppDownloadTask mDownloadTask = null;
    private Editor mEditor = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean flag = true;
            switch (msg.what) {
                case MSG.DOWNLOAD_COMPLETE /*10001*/:
                    GnAppUpgradeImple.this.setStatus(State.DOWNLOAD_COMPLETE);
                    GnAppUpgradeImple.this.mCallBack.onOperationStateChange(3, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.NOTIFY_FILE_ERROR /*10002*/:
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_VERIFY_FILE_ERROR, GnAppUpgradeImple.this.mClientName);
                    GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                    return;
                case MSG.NOTIFY_REMOTE_FILE_NOTFOUND /*10003*/:
                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), GnAppUpgradeImple.this.getIsPatchFile());
                    GnAppUpgradeImple.this.mCallBack.onError(MSG.NOTIFY_REMOTE_FILE_NOTFOUND, GnAppUpgradeImple.this.mClientName);
                    GnAppUpgradeImple.this.clearData(0, VerSionState.INITIAL);
                    return;
                case MSG.DOWNLOAD_PROGRESS /*10004*/:
                    int totalSize = GnAppUpgradeImple.this.getDownloadFileSize();
                    GnAppUpgradeImple.this.mDownloadLength = msg.arg1;
                    GnAppUpgradeImple.this.mCallBack.onDownLoading(totalSize, msg.arg1, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.NOTIFY_DICK_NOSPACE /*10005*/:
                    GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                    GnAppUpgradeImple.this.mCallBack.onError(102, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.UPDATE_DOWNLOAD_SERVER_CONNECTION_FAILED /*10006*/:
                case MSG.NOTIFY_DOWNLOAD_NETWORK_ERROR /*10007*/:
                    GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                    GnAppUpgradeImple.this.mCallBack.onError(100, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.NOTIFY_NO_SDCARD /*10008*/:
                    GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                    GnAppUpgradeImple.this.mCallBack.onError(101, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.START_DOWNLOAD /*10009*/:
                    GnAppUpgradeImple.this.mCallBack.onOperationStateChange(MSG.START_DOWNLOAD, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.INSTALL_COMPLETE /*10011*/:
                    GnAppUpgradeImple.this.mCallBack.onOperationStateChange(MSG.INSTALL_COMPLETE, GnAppUpgradeImple.this.mClientName);
                    GnAppUpgradeImple.this.installSuccessful();
                    return;
                case MSG.INSTALL_FAILED /*10012*/:
                    GnAppUpgradeImple.this.mCallBack.onError(MSG.INSTALL_FAILED, GnAppUpgradeImple.this.mClientName);
                    GnAppUpgradeImple.this.installFailed();
                    return;
                case MSG.HAS_NEW_VERSION /*10016*/:
                    GnAppUpgradeImple.this.mCallBack.onOperationStateChange(1, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.HAS_NOT_NEW_VERSION /*10017*/:
                    GnAppUpgradeImple.this.mCallBack.onOperationStateChange(2, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.NO_MORE_THAN_12_HOUR /*10018*/:
                    if (GnAppUpgradeImple.this.haveNewVersion()) {
                        GnAppUpgradeImple.this.mCallBack.onOperationStateChange(1, GnAppUpgradeImple.this.mClientName);
                        return;
                    } else {
                        GnAppUpgradeImple.this.mCallBack.onOperationStateChange(2, GnAppUpgradeImple.this.mClientName);
                        return;
                    }
                case MSG.CONTINUE_DOWNLOADING /*10019*/:
                    GnAppUpgradeImple.this.mCallBack.onOperationStateChange(MSG.CONTINUE_DOWNLOADING, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.SHOW_CHECKING_DIALOG /*10020*/:
                    Intent intent = new Intent(GnAppUpgradeImple.this.mClientName + "." + Config.ACTION_CHECK_UPDATE_RESULT);
                    intent.putExtra("result", true);
                    intent.addCategory("com.gionee.appupgrade");
                    if (!GnAppUpgradeImple.this.mActivityRunning) {
                        GnAppUpgradeImple.this.mContext.sendBroadcast(intent);
                        return;
                    }
                    return;
                case MSG.UPDATE_SERVER_CONNECTION_FAILED /*10023*/:
                    if (!GnAppUpgradeImple.this.mIsAuto) {
                        GnAppUpgradeImple.this.mCallBack.onError(100, GnAppUpgradeImple.this.mClientName);
                        return;
                    }
                    return;
                case MSG.PATCH_FILE_DOWNLOAD_COMPLETE /*10026*/:
                    if (!GnAppUpgradeImple.this.mIsPatching) {
                        GnAppUpgradeImple.this.setStatus(State.PATCH_FILE_DOWNLOAD_COMPLETE);
                        GnAppUpgradeImple.this.applyPatch();
                        return;
                    }
                    return;
                case MSG.DOWNLOAD_TASK_COMPLETED /*10027*/:
                    GnAppUpgradeImple.this.mDownloadTask.unregisterHandler();
                    GnAppUpgradeImple.this.mDownloadTask = null;
                    GnAppUpgradeImple.this.sendMessageToMyHandler(msg.arg1);
                    return;
                case MSG.SEND_DOWNLOAD_START_REQUEST /*10028*/:
                    if (msg.arg1 != 1) {
                        flag = false;
                    }
                    new Thread(new Runnable() {
                        public void run() {
                            HttpManager.sendDownloadStartRequest(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientVersionInfo.getStrUrl(), flag);
                        }
                    }).start();
                    return;
                case MSG.ERROR_UPGRADING /*10029*/:
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_UPGRADING, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.ERROR_LOCAL_FILE_NOT_FOUND /*10030*/:
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_LOCAL_FILE_NOT_FOUND, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.ERROR_LOCAL_FILE_VERIFY_ERROR /*10031*/:
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_LOCAL_FILE_VERIFY_ERROR, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.ERROR_PATCH_FILE_ERROR /*10032*/:
                    GnAppUpgradeImple.this.setLastFailedPatchMd5(GnAppUpgradeImple.this.mClientVersionInfo.getMd5());
                    GnAppUpgradeImple.this.mClientVersionInfo.initial();
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_PATCH_FILE_ERROR, GnAppUpgradeImple.this.mClientName);
                    new Thread(new Runnable() {
                        public void run() {
                            GnAppUpgradeImple.this.getAndParseFullPackageInfo();
                        }
                    }).start();
                    return;
                case MSG.ERROR_LOW_MEMORY /*10033*/:
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_LOW_MEMORY, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.ERROR_EMMC_NOSPACE /*10034*/:
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_EMMC_NOSPACE, GnAppUpgradeImple.this.mClientName);
                    return;
                case MSG.ERROR_OLD_APK_WRONG /*10035*/:
                    GnAppUpgradeImple.this.mClientVersionInfo.initial();
                    GnAppUpgradeImple.this.mCallBack.onError(Error.ERROR_PATCH_FILE_ERROR, GnAppUpgradeImple.this.mClientName);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIncUpgradeEnable = true;
    private boolean mInstalling = false;
    private boolean mIsAuto = false;
    private boolean mIsPatching = false;
    private Lock mLock = new ReentrantLock();
    private SharedPreferences mPreferences = null;
    private State mState = State.NONE;
    private StringBuffer mStringBuffer = null;

    public enum State {
        INITIAL(1),
        CHECKING(2),
        READY_TO_DOWNLOAD(3),
        DOWNLOADING(4),
        PATCH_FILE_DOWNLOAD_COMPLETE(5),
        DOWNLOAD_COMPLETE(6),
        INSTALLING(7),
        NONE(8);
        
        private int mValue;

        private State(int arg1) {
            this.mValue = arg1;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    public void initial(CallBack callBack, Context context, String clientName) {
        LogUtils.logd(TAG, "GnAppUpgradeImpleinitial clientName = " + clientName);
        if (State.NONE == this.mState) {
            if (callBack == null || context == null || clientName == null) {
                throw new NullPointerException();
            }
            this.mCallBack = callBack;
            this.mContext = context.getApplicationContext();
            this.mClientName = clientName;
            if (Thread.currentThread().getId() != this.mContext.getMainLooper().getThread().getId()) {
                throw new RuntimeException("not run in main thread");
            } else if (this.mCallBack == null || this.mContext == null || this.mClientName == null) {
                throw new NullPointerException();
            } else {
                this.mPreferences = this.mContext.getSharedPreferences("upgrade_preferences_" + clientName, 0);
                this.mEditor = this.mPreferences.edit();
                this.mAppPreferences = this.mContext.getSharedPreferences(Utils.UPGRADE_APP_PERFERENCES, 0);
                this.mAppEditor = this.mAppPreferences.edit();
                if (getStatus() == State.CHECKING) {
                    setStatus(State.INITIAL);
                } else {
                    this.mState = getStatus();
                }
                this.mClientVersionInfo = new NewVersion(this.mContext, this.mClientName);
                if (this.mState == State.INITIAL) {
                    this.mClientVersionInfo.initial();
                    if (getHaveVersion().equals(VerSionState.HASVERSION)) {
                        setHaveVersion(VerSionState.INITIAL);
                    }
                }
                LogUtils.logd(TAG, "state = " + this.mState + "  getHaverVersion = " + getHaveVersion() + "  forace = " + isForceMode() + " , hasIncSoFile = " + ApkCreater.hasIncSoFile());
            }
        }
    }

    public Runnable checkApkVersion(final boolean isAuto, final boolean showCheckingDialog) {
        return new Runnable() {
            public void run() {
                if (GnAppUpgradeImple.this.mLock.tryLock()) {
                    Lock access$1300;
                    GnAppUpgradeImple.this.mIsAuto = isAuto;
                    if (NetworkUtils.isNetworkAvailable(GnAppUpgradeImple.this.mContext)) {
                        String haveNewVersion = GnAppUpgradeImple.this.getHaveVersion();
                        LogUtils.logd(GnAppUpgradeImple.TAG, "startCheck() begin mState = " + GnAppUpgradeImple.this.mState + "\thaveNewVersion = -" + haveNewVersion + "- isNotify = " + GnAppUpgradeImple.this.isNotify() + "\tlastCheckTime = " + GnAppUpgradeImple.this.mPreferences.getLong(Utils.KEY_UPGRADE_LAST_CHECK_TIME, 0) + " isAuto = " + isAuto + " lastFailedPatchMd5 = " + GnAppUpgradeImple.this.getLastFailedPatchMd5() + " mActivityRunning = " + GnAppUpgradeImple.this.mActivityRunning);
                        if (GnAppUpgradeImple.this.mState == State.DOWNLOADING) {
                            LogUtils.logd(GnAppUpgradeImple.TAG, "startCheck() mClientVersionInfo.getStrUrl() = " + GnAppUpgradeImple.this.mClientVersionInfo.getStrUrl());
                            if (GnAppUpgradeImple.this.mDownloadTask == null || !GnAppUpgradeImple.this.mDownloadTask.mIsRunning) {
                                try {
                                    if (haveNewVersion.equals(VerSionState.HASVERSION) && !GnAppUpgradeImple.this.mClientVersionInfo.getStrUrl().equals("")) {
                                        GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.HAS_NEW_VERSION);
                                        access$1300 = GnAppUpgradeImple.this.mLock;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    LogUtils.loge(GnAppUpgradeImple.TAG, "startCheck() JSONException currentVersionInfo = " + currentVersionInfo);
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.UPDATE_SERVER_CONNECTION_FAILED);
                                } catch (Throwable th) {
                                    GnAppUpgradeImple.this.mLock.unlock();
                                }
                            } else {
                                if (!isAuto) {
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_UPGRADING);
                                }
                                access$1300 = GnAppUpgradeImple.this.mLock;
                            }
                        } else {
                            if (GnAppUpgradeImple.this.mState == State.DOWNLOAD_COMPLETE) {
                                String curClientVersionNum = Utils.getVersion(GnAppUpgradeImple.getClientContext(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName));
                                String lastCheckVerson = GnAppUpgradeImple.this.getClientVersion();
                                if (curClientVersionNum.startsWith("V") || curClientVersionNum.startsWith("v")) {
                                    curClientVersionNum = curClientVersionNum.substring(1);
                                }
                                if (lastCheckVerson.startsWith("V") || lastCheckVerson.startsWith("v")) {
                                    lastCheckVerson = lastCheckVerson.substring(1);
                                }
                                if (lastCheckVerson.equals(curClientVersionNum)) {
                                    GnAppUpgradeImple.this.clearData(0, VerSionState.INITIAL);
                                } else if (GnAppUpgradeImple.this.verifyFullPackage()) {
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.DOWNLOAD_COMPLETE);
                                    access$1300 = GnAppUpgradeImple.this.mLock;
                                } else {
                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion());
                                    GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                                }
                            } else {
                                if (GnAppUpgradeImple.this.mState != State.INSTALLING) {
                                    if (GnAppUpgradeImple.this.mState != State.PATCH_FILE_DOWNLOAD_COMPLETE) {
                                        if (GnAppUpgradeImple.this.mState == State.READY_TO_DOWNLOAD && GnAppUpgradeImple.this.mActivityRunning) {
                                            LogUtils.loge(GnAppUpgradeImple.TAG, "Activity is runnung, not check");
                                            access$1300 = GnAppUpgradeImple.this.mLock;
                                        }
                                    } else if (GnAppUpgradeImple.this.mIsPatching) {
                                        if (!isAuto) {
                                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_UPGRADING);
                                        }
                                        access$1300 = GnAppUpgradeImple.this.mLock;
                                    } else {
                                        String fileMd5 = GnAppUpgradeImple.this.mClientVersionInfo.getMd5();
                                        String patchFilePath = Utils.getDownloadFilePath(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), true);
                                        String fullPackagePath = Utils.getDownloadFilePath(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), false);
                                        if (fullPackagePath == null || !Utils.verifyFileByMd5(fullPackagePath, GnAppUpgradeImple.this.mClientVersionInfo.getFullPackageMd5())) {
                                            if (patchFilePath != null) {
                                                if (GnAppUpgradeImple.this.getLastFailedPatchMd5().equals(fileMd5)) {
                                                    GnAppUpgradeImple.this.sendIncUpgradeFailedInfo(GnAppUpgradeImple.this.mClientVersionInfo.getPatchId());
                                                } else {
                                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), false);
                                                    if (Utils.verifyFileByMd5(patchFilePath, fileMd5)) {
                                                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.PATCH_FILE_DOWNLOAD_COMPLETE);
                                                        access$1300 = GnAppUpgradeImple.this.mLock;
                                                    }
                                                }
                                            }
                                            Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion());
                                            GnAppUpgradeImple.this.clearData(0, VerSionState.INITIAL);
                                        } else {
                                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.DOWNLOAD_COMPLETE);
                                            access$1300 = GnAppUpgradeImple.this.mLock;
                                        }
                                    }
                                } else if (GnAppUpgradeImple.this.getInstalling()) {
                                    if (!isAuto) {
                                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_UPGRADING);
                                    }
                                    access$1300 = GnAppUpgradeImple.this.mLock;
                                } else if (GnAppUpgradeImple.this.verifyFullPackage()) {
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.DOWNLOAD_COMPLETE);
                                    access$1300 = GnAppUpgradeImple.this.mLock;
                                } else {
                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion());
                                    GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                                }
                            }
                        }
                        if (isAuto || !showCheckingDialog) {
                            if (GnAppUpgradeImple.this.mState == State.INITIAL) {
                                GnAppUpgradeImple.this.setStatus(State.CHECKING);
                            }
                        } else {
                            GnAppUpgradeImple.this.setStatus(State.CHECKING);
                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.SHOW_CHECKING_DIALOG);
                        }
                        String currentVersionInfo = GnAppUpgradeImple.this.getClientUpgradeInfo(false);
                        if (currentVersionInfo != null) {
                            LogUtils.logd(GnAppUpgradeImple.TAG, "startcheck->(currentVersionInfo != null) mState = " + GnAppUpgradeImple.this.mState + "\tcurrentVersionInfo = " + currentVersionInfo);
                            if (currentVersionInfo.length() != 0) {
                                NewVersionParser.parse(currentVersionInfo, GnAppUpgradeImple.getClientContext(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName), GnAppUpgradeImple.this.mClientName);
                                if (!GnAppUpgradeImple.this.mClientVersionInfo.getMd5().equals("") && GnAppUpgradeImple.this.mClientVersionInfo.getMd5().equals(GnAppUpgradeImple.this.getLastFailedPatchMd5())) {
                                    int result = GnAppUpgradeImple.this.getAndParseFullPackageInfo();
                                    if (result == 0) {
                                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.UPDATE_SERVER_CONNECTION_FAILED);
                                        access$1300 = GnAppUpgradeImple.this.mLock;
                                    } else if (result == 0) {
                                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.HAS_NOT_NEW_VERSION);
                                        access$1300 = GnAppUpgradeImple.this.mLock;
                                    }
                                }
                                GnAppUpgradeImple.this.setHaveVersion(VerSionState.HASVERSION);
                                if (!(GnAppUpgradeImple.this.getLastCheckNewVersionNum().equals("") || GnAppUpgradeImple.this.mClientVersionInfo.getDisplayVersion().equals(GnAppUpgradeImple.this.getLastCheckNewVersionNum()))) {
                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getLastCheckNewVersionNum());
                                    GnAppUpgradeImple.this.setNotify(true);
                                }
                                GnAppUpgradeImple.this.setLastCheckNewVersionNum(GnAppUpgradeImple.this.mClientVersionInfo.getDisplayVersion());
                                GnAppUpgradeImple.this.setLastCheck(System.currentTimeMillis());
                                if (GnAppUpgradeImple.this.mState == State.CHECKING) {
                                    GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                                }
                                GnAppUpgradeImple.this.storeCurrentClientVersion();
                                GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.HAS_NEW_VERSION);
                                access$1300 = GnAppUpgradeImple.this.mLock;
                            } else {
                                GnAppUpgradeImple.this.clearData(System.currentTimeMillis(), VerSionState.NOVERSION);
                                GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.HAS_NOT_NEW_VERSION);
                                access$1300 = GnAppUpgradeImple.this.mLock;
                            }
                        } else {
                            LogUtils.loge(GnAppUpgradeImple.TAG, "startCheck() currentVersionInfo is null ");
                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.UPDATE_SERVER_CONNECTION_FAILED);
                            GnAppUpgradeImple.this.clearData(0, VerSionState.INITIAL);
                            access$1300 = GnAppUpgradeImple.this.mLock;
                        }
                    } else {
                        if (!isAuto) {
                            Intent networkIntent = new Intent("gn.android.intent.action.SHOW_3GWIFIALERT");
                            networkIntent.putExtra("appname", GnAppUpgradeImple.getClientContext(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName).getPackageName());
                            GnAppUpgradeImple.this.mContext.sendBroadcast(networkIntent);
                        }
                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.UPDATE_SERVER_CONNECTION_FAILED);
                        access$1300 = GnAppUpgradeImple.this.mLock;
                    }
                    access$1300.unlock();
                    return;
                }
                LogUtils.loge(GnAppUpgradeImple.TAG, "startCheck() already run");
            }
        };
    }

    public Runnable downLoadApk() {
        LogUtils.logd(TAG, "GnAppUpgradeManger startDownload()");
        if (this.mDownloadTask == null || !this.mDownloadTask.mIsRunning) {
            try {
                int length = getDownloadFileSize();
                boolean isPatchFile = getIsPatchFile();
                if (isPatchFile) {
                    length += getTotalFileSize();
                }
                String firstMountedStoragePath = Utils.getFirstMountedStoragePath(this.mContext);
                if (firstMountedStoragePath == null) {
                    sendMessageToMyHandler(MSG.NOTIFY_NO_SDCARD);
                    return null;
                }
                String downloadPath;
                if (Config.IS_GIONEE_PHONE) {
                    downloadPath = Utils.getDownloadedFilePathInAllMountedStorage(this.mContext, this.mClientName, getNewVersionNum(), isPatchFile);
                } else {
                    downloadPath = Utils.getFilePathInAppointedStorage(firstMountedStoragePath, this.mClientName, getNewVersionNum(), isPatchFile);
                }
                String path = setDownloadFilePath(downloadPath, length);
                if (path == null) {
                    return null;
                }
                if (this.mState == State.READY_TO_DOWNLOAD) {
                    sendMessageToMyHandler(MSG.START_DOWNLOAD);
                }
                setStatus(State.DOWNLOADING);
                if (this.mDownloadTask == null) {
                    this.mDownloadTask = new GnAppDownloadTask(this.mContext, this.mClientVersionInfo.getStrUrl(), getDownloadFileSize(), path, this.mClientVersionInfo.getMd5(), getClientVersion());
                }
                this.mDownloadTask.registerHandler(this.mHandler);
                return this.mDownloadTask.getDownLoadTask();
            } catch (NumberFormatException e) {
                LogUtils.loge(TAG, "startDownload() : prarseInt exception! mClientVersionInfo.getFileSize() = " + this.mClientVersionInfo.getFileSize());
                return null;
            }
        }
        sendMessageToMyHandler(MSG.ERROR_UPGRADING);
        return null;
    }

    public Runnable installApk(final Activity activity, final int requestCode) {
        if (activity == null || !(activity instanceof Activity)) {
            return null;
        }
        final Lock installLock = new ReentrantLock();
        return new Runnable() {
            public void run() {
                if (installLock.tryLock()) {
                    try {
                        String filePath = Utils.getDownloadFilePath(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), false);
                        if (filePath != null) {
                            boolean verifyResullt;
                            if (GnAppUpgradeImple.this.mClientVersionInfo.getFullPackageMd5().equals("")) {
                                verifyResullt = Utils.verifyFile(filePath, (long) GnAppUpgradeImple.this.getDownloadFileSize(), GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.getClientVersion());
                            } else {
                                verifyResullt = Utils.verifyFileByMd5(filePath, GnAppUpgradeImple.this.mClientVersionInfo.getFullPackageMd5());
                            }
                            if (verifyResullt) {
                                Intent intent = new Intent("android.intent.action.VIEW");
                                intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                                activity.startActivityForResult(intent, requestCode);
                            } else {
                                GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_LOCAL_FILE_VERIFY_ERROR);
                                Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion());
                            }
                        } else {
                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_LOCAL_FILE_NOT_FOUND);
                        }
                        installLock.unlock();
                    } catch (Throwable th) {
                        installLock.unlock();
                    }
                } else {
                    LogUtils.loge(GnAppUpgradeImple.TAG, "installApk() already run");
                }
            }
        };
    }

    public String getHaveVersion() {
        return this.mPreferences.getString(Utils.KEY_UPGRADE_HAVE_NEW_VERSION, VerSionState.INITIAL);
    }

    public boolean isNotify() {
        return this.mPreferences.getBoolean(Utils.KEY_UPGRADE_DISPLAY_THIS_VERSION, true);
    }

    public void setNotify(boolean isNotify) {
        this.mEditor.putBoolean(Utils.KEY_UPGRADE_DISPLAY_THIS_VERSION, isNotify).commit();
    }

    public void setInstalling(boolean installing) {
        this.mInstalling = installing;
    }

    public boolean getInstalling() {
        return this.mInstalling;
    }

    public boolean isForceMode() {
        return this.mClientVersionInfo.getUpgradeMode().equals(VersionType.FORCED_VERSION);
    }

    public String getClientVersion() {
        return this.mClientVersionInfo.getDisplayVersion();
    }

    public int getTotalSize() {
        try {
            return Integer.parseInt(this.mClientVersionInfo.getFileSize());
        } catch (NumberFormatException e) {
            LogUtils.loge(TAG, "getTotalSize() mClientVersionInfo.getFileSize() is 0");
            return 0;
        }
    }

    private String setDownloadFilePath(String downloadFilePath, int length) {
        int msgID;
        long needSpace = (long) length;
        if (downloadFilePath != null) {
            File file = new File(downloadFilePath);
            if (file.exists()) {
                needSpace -= file.length();
            }
        }
        if (Config.IS_GIONEE_PHONE) {
            if (downloadFilePath != null) {
                String storagePathOfFile = Utils.getStoragePathOfDownloadFile(downloadFilePath);
                if (Utils.getAppointedStorageAvailableSpace(storagePathOfFile) > needSpace) {
                    return downloadFilePath;
                }
                if (Utils.getExternalStoragePath().equals(storagePathOfFile)) {
                    msgID = MSG.NOTIFY_DICK_NOSPACE;
                } else if (storagePathOfFile.equals(Utils.getEmmcPathWhenSDcardInsert(this.mContext))) {
                    msgID = MSG.ERROR_EMMC_NOSPACE;
                } else {
                    msgID = MSG.NOTIFY_NO_SDCARD;
                }
            } else {
                String firstMountedStoragePath = Utils.getFirstMountedStoragePath(this.mContext);
                if (Utils.hasMultiMountedStorage(this.mContext)) {
                    if (Utils.getAppointedStorageAvailableSpace(Utils.getExternalStoragePath()) > needSpace) {
                        return Utils.getFilePathInAppointedStorage(firstMountedStoragePath, this.mClientName, getNewVersionNum(), getIsPatchFile());
                    }
                    String emmcPath = Utils.getEmmcPathWhenSDcardInsert(this.mContext);
                    if (Utils.getAppointedStorageAvailableSpace(emmcPath) > needSpace) {
                        return Utils.getFilePathInAppointedStorage(emmcPath, this.mClientName, getNewVersionNum(), getIsPatchFile());
                    }
                    msgID = MSG.NOTIFY_DICK_NOSPACE;
                } else if (Utils.getAppointedStorageAvailableSpace(firstMountedStoragePath) > needSpace) {
                    return Utils.getFilePathInAppointedStorage(firstMountedStoragePath, this.mClientName, getNewVersionNum(), getIsPatchFile());
                } else {
                    msgID = MSG.NOTIFY_DICK_NOSPACE;
                }
            }
        } else if (Utils.getAppointedStorageAvailableSpace(Utils.getStoragePathOfDownloadFile(downloadFilePath)) > needSpace) {
            return downloadFilePath;
        } else {
            msgID = MSG.NOTIFY_DICK_NOSPACE;
        }
        LogUtils.logd(TAG, "setDownloadFilePath() msgID = " + msgID);
        if (msgID != -1) {
            sendMessageToMyHandler(msgID);
        }
        return null;
    }

    public void installFailed() {
        LogUtils.loge(TAG, "installFailed()");
        setStatus(State.READY_TO_DOWNLOAD);
        setLastCheck(0);
        Utils.deleteDownloadFile(this.mContext, this.mClientName, getClientVersion());
        updateSet();
    }

    public void updateSet() {
        if (!Config.IS_LOW_ANDROID_SDK_LEVEL) {
            Set<String> set = this.mAppPreferences.getStringSet(Utils.UPGRADE_APP_KEY, null);
            if (set != null) {
                LogUtils.logd(TAG, "GnAppUpgradeImpleif contains  = " + this.mClientName);
                if (this.mStringBuffer != null && set.contains(this.mStringBuffer.toString())) {
                    set.remove(this.mStringBuffer.toString());
                    this.mAppEditor.clear().commit();
                    this.mAppEditor.putStringSet(Utils.UPGRADE_APP_KEY, set).commit();
                }
            }
        }
    }

    public void installSuccessful() {
        LogUtils.logd(TAG, "GnAppUpgradeImple installSuccessful()");
        setStatus(State.INITIAL);
        this.mClientVersionInfo.initial();
        this.mEditor.remove(Utils.KEY_UPGRADE_HAVE_NEW_VERSION);
        setLastCheck(0);
        this.mEditor.commit();
    }

    private String getClientUpgradeInfo(boolean fullOnly) {
        HttpManager httpManager = new HttpManager();
        try {
            String romVn = SystemProperties.get("ro.gn.gnromvernumber");
            Matcher matcher = Pattern.compile("\\d").matcher(romVn);
            int index = 0;
            if (matcher.find() && !"".equals(matcher.group())) {
                index = matcher.start();
            }
            Context clientContext = getClientContext(this.mContext, this.mClientName);
            String rom = romVn.substring(index);
            LogUtils.logd(TAG, "rom=" + rom);
            String model = SystemProperties.get("ro.product.model");
            LogUtils.logd(TAG, "model=" + model);
            StringBuffer sb = new StringBuffer();
            String serverUri = Config.getServerUri(this.mContext);
            sb.append(serverUri);
            LogUtils.logd(TAG, "GnAppUpgradeImplegetServerUri = " + serverUri);
            sb.append("product=");
            sb.append(clientContext.getPackageName());
            sb.append("&");
            sb.append("version=");
            sb.append(Utils.getVersion(clientContext));
            NetworkUtils.getNetworkTypeUrl(sb, this.mContext);
            sb.append("&imei=" + Utils.getDecodeImei(Utils.getImei(this.mContext)));
            sb.append("&rom=" + rom);
            sb.append("&model=" + model);
            String platform = SystemProperties.get("ro.mediatek.platform");
            if (platform == null || "".equals(platform)) {
                platform = SystemProperties.get("ro.hw_platform");
            }
            LogUtils.logd(TAG, "GnAppUpgradeImpleplatform = " + platform);
            if (!(platform == null || "".equals(platform))) {
                sb.append("&platform=" + platform);
            }
            if (Utils.isIncUpgradeSupport(clientContext) && !fullOnly && this.mIncUpgradeEnable && ApkCreater.hasIncSoFile()) {
                sb.append("&patch=true");
                sb.append("&diffVer=1");
            }
            sb.append("&md5=" + Utils.getFileMd5(new File(Utils.getClientApkPath(clientContext))));
            String checkUrl = sb.toString().replaceAll(Token.SEPARATOR, "");
            LogUtils.logd(TAG, "GnAppUpgradeImpleurl = " + checkUrl);
            return httpManager.executeHttpRequest(checkUrl, this.mContext);
        } catch (Exception e) {
            return null;
        }
    }

    public static Context getClientContext(Context context, String clientName) {
        if (clientName == null || clientName.length() == 0) {
            return null;
        }
        Context skinContext = null;
        try {
            return context.createPackageContext(clientName, 2);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return skinContext;
        }
    }

    public void setStatus(State state) {
        LogUtils.logd(TAG, "setStatus() state = " + state);
        this.mEditor.putInt(Utils.KEY_UPGRADE_STATE, state.getValue()).commit();
        this.mState = state;
        if (!Config.IS_LOW_ANDROID_SDK_LEVEL) {
            if (this.mState == State.INSTALLING) {
                this.mStringBuffer = new StringBuffer();
                this.mStringBuffer.append(this.mClientName);
                this.mStringBuffer.append(Utils.UPGRADE_APP_SPLITE);
                this.mStringBuffer.append(Utils.getDownloadFilePath(this.mContext, this.mClientName, getClientVersion(), false));
                Set<String> set = this.mAppPreferences.getStringSet(Utils.UPGRADE_APP_KEY, null);
                if (set == null) {
                    set = new HashSet();
                }
                if (!set.contains(this.mStringBuffer.toString())) {
                    set.add(this.mStringBuffer.toString());
                }
                this.mAppEditor.putStringSet(Utils.UPGRADE_APP_KEY, set).commit();
                LogUtils.logd(TAG, "GnAppUpgradeImple installing = " + this.mStringBuffer.toString());
            } else if (this.mState == State.INITIAL) {
                LogUtils.logd(TAG, "GnAppUpgradeImpleINITIAL = " + this.mClientName);
                updateSet();
            }
        }
    }

    public State getStatus() {
        int value = this.mPreferences.getInt(Utils.KEY_UPGRADE_STATE, State.INITIAL.mValue);
        for (State state : State.values()) {
            if (value == state.mValue) {
                return state;
            }
        }
        return State.INITIAL;
    }

    private void setHaveVersion(String state) {
        if (VerSionState.HASVERSION.equals(state) || VerSionState.NOVERSION.equals(state)) {
            this.mEditor.putString(Utils.KEY_UPGRADE_HAVE_NEW_VERSION, state).commit();
        } else {
            this.mEditor.putString(Utils.KEY_UPGRADE_HAVE_NEW_VERSION, VerSionState.INITIAL).commit();
        }
    }

    public void sendMessageToMyHandler(int what) {
        if (this.mHandler != null) {
            this.mHandler.obtainMessage(what).sendToTarget();
        } else {
            LogUtils.loge(TAG, "sendMessageToMyHandler() mHandler is null");
        }
    }

    public int getDownloadLength() {
        return this.mDownloadLength;
    }

    private void setLastCheck(long time) {
        this.mEditor.putLong(Utils.KEY_UPGRADE_LAST_CHECK_TIME, time).commit();
    }

    public String getReleaseNote() {
        if (this.mClientVersionInfo != null) {
            return this.mClientVersionInfo.getReleaseNote();
        }
        return "";
    }

    public int getDownloadFileSize() {
        try {
            return Integer.parseInt(this.mClientVersionInfo.getFileSize());
        } catch (NumberFormatException e) {
            LogUtils.loge(TAG, "getDownloadFileSize() mClientVersionInfo.getFileSize() is 0");
            return 0;
        }
    }

    public String getNewVersionNum() {
        return getClientVersion();
    }

    public boolean haveNewVersion() {
        return getHaveVersion().equals(VerSionState.HASVERSION);
    }

    public boolean getIsPatchFile() {
        return this.mClientVersionInfo.getIsPatchFile();
    }

    public int getTotalFileSize() {
        try {
            return Integer.parseInt(this.mClientVersionInfo.getTotalFileSize());
        } catch (NumberFormatException e) {
            LogUtils.loge(TAG, "getTotalSize() getTotalFileSize() is 0");
            return 0;
        }
    }

    public void setNewVersionNum(String versionNum) {
        this.mEditor.putString(Utils.KEY_UPGRADE_DISPLAY_VERSION, versionNum).commit();
    }

    public boolean getIsNotify() {
        return this.mPreferences.getBoolean(Utils.KEY_UPGRADE_DISPLAY_VERSION, true);
    }

    private String getLastCheckNewVersionNum() {
        return this.mPreferences.getString(Utils.KEY_UPGRADE_LAST_CHECK_NEW_VERSION_NUM, "");
    }

    private void setLastCheckNewVersionNum(String lastCheckNewVersionNum) {
        this.mEditor.putString(Utils.KEY_UPGRADE_LAST_CHECK_NEW_VERSION_NUM, lastCheckNewVersionNum).commit();
    }

    private String getLastFailedPatchMd5() {
        return this.mPreferences.getString(Utils.KEY_UPGRADE_LAST_FAILED_PATCH_MD5, "");
    }

    private void setLastFailedPatchMd5(String md5) {
        this.mEditor.putString(Utils.KEY_UPGRADE_LAST_FAILED_PATCH_MD5, md5).commit();
    }

    private int getAndParseFullPackageInfo() {
        String fullPackageInfo = getClientUpgradeInfo(true);
        LogUtils.logd(TAG, "getAndParseFullPackageInfo() fullPackageInfo = " + fullPackageInfo);
        if (fullPackageInfo != null) {
            if (fullPackageInfo.length() != 0) {
                try {
                    NewVersionParser.parse(fullPackageInfo, getClientContext(this.mContext, this.mClientName), this.mClientName);
                    setHaveVersion(VerSionState.HASVERSION);
                    setLastCheck(System.currentTimeMillis());
                    setStatus(State.READY_TO_DOWNLOAD);
                    setLastCheckNewVersionNum(this.mClientVersionInfo.getDisplayVersion());
                    return 1;
                } catch (Exception e) {
                    LogUtils.loge(TAG, "getAndParseFullPackageInfo() parse info error");
                }
            } else {
                clearData(System.currentTimeMillis(), VerSionState.NOVERSION);
                return 0;
            }
        }
        clearData(0, VerSionState.NOVERSION);
        return 0;
    }

    private void sendIncUpgradeFailedInfo(String pid) {
        String uuid = Utils.getAesUUID(Config.KEY);
        LogUtils.logd(TAG, "sendIncUpgradeFailedInfo() uuid = " + uuid);
        StringBuffer url = new StringBuffer();
        if (Config.isTestMode(this.mContext)) {
            url.append(Config.TEST_HOST);
        } else {
            url.append(Config.NORMARL_HOST_INC_FAILED);
        }
        url.append("/synth/open/upgradeFailed.do?pId=" + pid);
        if (uuid != null) {
            url.append("&");
            url.append("version=");
            url.append(Utils.getVersion(getClientContext(this.mContext, this.mClientName)));
            String romVn = SystemProperties.get("ro.gn.gnromvernumber");
            Matcher matcher = Pattern.compile("\\d").matcher(romVn);
            int index = 0;
            if (matcher.find() && !"".equals(matcher.group())) {
                index = matcher.start();
            }
            String rom = romVn.substring(index);
            String model = SystemProperties.get("ro.product.model");
            url.append("&rom=" + rom);
            url.append("&model=" + model);
            String platform = SystemProperties.get("ro.mediatek.platform");
            if (platform == null || "".equals(platform)) {
                platform = SystemProperties.get("ro.hw_platform");
            }
            LogUtils.logd(TAG, "GnAppUpgradeImpleplatform = " + platform);
            if (!(platform == null || "".equals(platform))) {
                url.append("&platform=" + platform);
            }
            url.append("&token=" + uuid);
            LogUtils.logd(TAG, "sendIncUpgradeFailedInfo() url = " + url.toString());
            HttpManager.sendHttpRequest(url.toString(), this.mContext);
        }
    }

    public void setIncUpgradeEnabled(boolean enable) {
        this.mIncUpgradeEnable = enable;
    }

    public void setIsActivityRunning(boolean isRunning) {
        this.mActivityRunning = isRunning;
    }

    public boolean getIsActivityRunning() {
        return this.mActivityRunning;
    }

    public boolean getIsAuto() {
        return this.mIsAuto;
    }

    private void clearData(long lastCheckTime, String hasVersionOrNot) {
        setStatus(State.INITIAL);
        setHaveVersion(hasVersionOrNot);
        setLastCheck(lastCheckTime);
        this.mClientVersionInfo.initial();
    }

    private void storeCurrentClientVersion() {
        Context context = getClientContext(this.mContext, this.mClientName);
        if (context != null) {
            this.mClientVersionInfo.setStoragedClientCurrentVersion(Utils.getVersion(context));
        }
    }

    private boolean verifyFullPackage() {
        String newVersion = getClientVersion();
        String path = Utils.getDownloadFilePath(this.mContext, this.mClientName, newVersion, false);
        String fullPackageMd5 = this.mClientVersionInfo.getFullPackageMd5();
        if (path == null) {
            return false;
        }
        if (fullPackageMd5.equals("")) {
            return Utils.verifyFile(path, (long) getDownloadFileSize(), this.mContext, newVersion);
        }
        return Utils.verifyFileByMd5(path, fullPackageMd5);
    }

    private void applyPatch() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    GnAppUpgradeImple gnAppUpgradeImple;
                    GnAppUpgradeImple.this.mIsPatching = true;
                    String oldApkPath = Utils.getClientApkPath(GnAppUpgradeImple.getClientContext(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName));
                    if (GnAppUpgradeImple.this.isRightOldApk(oldApkPath)) {
                        String patchPath = Utils.getDownloadedFilePathInAllMountedStorage(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), true);
                        if (patchPath == null) {
                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_LOCAL_FILE_NOT_FOUND);
                            GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                            gnAppUpgradeImple = GnAppUpgradeImple.this;
                        } else {
                            String newApkPath = patchPath.substring(0, patchPath.lastIndexOf(".patch")).concat(".apk");
                            int fileSize = GnAppUpgradeImple.this.getDownloadFileSize();
                            int needLength = GnAppUpgradeImple.this.getTotalFileSize() + 10485760;
                            String storagePathOfFile = Utils.getStoragePathOfDownloadFile(patchPath);
                            String firstMountedStoragePath = Utils.getFirstMountedStoragePath(GnAppUpgradeImple.this.mContext);
                            if ("mounted".equals(Utils.getStorageVolumeState(GnAppUpgradeImple.this.mContext, storagePathOfFile))) {
                                if (Config.IS_GIONEE_PHONE) {
                                    String externalStoragePath = Utils.getExternalStoragePath();
                                    if (storagePathOfFile.equals(externalStoragePath)) {
                                        if (Utils.getAppointedStorageAvailableSpace(externalStoragePath) < ((long) needLength)) {
                                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.NOTIFY_DICK_NOSPACE);
                                            gnAppUpgradeImple = GnAppUpgradeImple.this;
                                        }
                                    } else if (Utils.getAppointedStorageAvailableSpace(storagePathOfFile) < ((long) needLength)) {
                                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_EMMC_NOSPACE);
                                        gnAppUpgradeImple = GnAppUpgradeImple.this;
                                    }
                                } else if (Utils.getAppointedStorageAvailableSpace(storagePathOfFile) < ((long) needLength)) {
                                    LogUtils.loge(GnAppUpgradeImple.TAG, "handleMessage(MSG.FILE_DOWNLOAD_COMPLETE) SdcardAvailableSpace < needLength");
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.NOTIFY_DICK_NOSPACE);
                                    gnAppUpgradeImple = GnAppUpgradeImple.this;
                                }
                                int patchResult = ApkCreater.applyPatch(oldApkPath, newApkPath, patchPath);
                                LogUtils.logd(GnAppUpgradeImple.TAG, "handleMessage(MSG.FILE_DOWNLOAD_COMPLETE) , patchResult = " + patchResult);
                                if (patchResult != 0) {
                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion());
                                    switch (patchResult) {
                                        case 1:
                                            break;
                                        case 2:
                                        case 3:
                                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_LOCAL_FILE_NOT_FOUND);
                                            Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), true);
                                            GnAppUpgradeImple.this.setStatus(State.READY_TO_DOWNLOAD);
                                            break;
                                        case 4:
                                            GnAppUpgradeImple.this.sendIncUpgradeFailedInfo(GnAppUpgradeImple.this.mClientVersionInfo.getPatchId());
                                            break;
                                        case 5:
                                            GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_LOW_MEMORY);
                                            break;
                                    }
                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), true);
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_PATCH_FILE_ERROR);
                                    GnAppUpgradeImple.this.setStatus(State.INITIAL);
                                    gnAppUpgradeImple = GnAppUpgradeImple.this;
                                } else if (Utils.verifyFileByMd5(newApkPath, GnAppUpgradeImple.this.mClientVersionInfo.getFullPackageMd5())) {
                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), true);
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.DOWNLOAD_COMPLETE);
                                    gnAppUpgradeImple = GnAppUpgradeImple.this;
                                } else {
                                    Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion());
                                    GnAppUpgradeImple.this.sendIncUpgradeFailedInfo(GnAppUpgradeImple.this.mClientVersionInfo.getPatchId());
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_PATCH_FILE_ERROR);
                                    GnAppUpgradeImple.this.setLastFailedPatchMd5(GnAppUpgradeImple.this.mClientVersionInfo.getMd5());
                                    GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_LOCAL_FILE_VERIFY_ERROR);
                                    gnAppUpgradeImple = GnAppUpgradeImple.this;
                                }
                            } else {
                                GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.NOTIFY_NO_SDCARD);
                                gnAppUpgradeImple = GnAppUpgradeImple.this;
                            }
                        }
                    } else {
                        Utils.deleteDownloadFile(GnAppUpgradeImple.this.mContext, GnAppUpgradeImple.this.mClientName, GnAppUpgradeImple.this.getClientVersion(), true);
                        GnAppUpgradeImple.this.sendMessageToMyHandler(MSG.ERROR_OLD_APK_WRONG);
                        GnAppUpgradeImple.this.setStatus(State.INITIAL);
                        gnAppUpgradeImple = GnAppUpgradeImple.this;
                    }
                    gnAppUpgradeImple.mIsPatching = false;
                } catch (Throwable th) {
                    GnAppUpgradeImple.this.mIsPatching = false;
                }
            }
        }).start();
    }

    private boolean isRightOldApk(String oldApkPath) {
        if (oldApkPath == null) {
            return false;
        }
        if (this.mClientVersionInfo.getOldApkMd5().equals(Utils.getFileMd5(new File(oldApkPath)))) {
            return true;
        }
        return false;
    }
}
