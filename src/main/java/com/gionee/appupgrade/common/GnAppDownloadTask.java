package com.gionee.appupgrade.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.gionee.appupgrade.common.utils.LogUtils;
import com.gionee.appupgrade.common.utils.Utils;
import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GnAppDownloadTask {
    private static final int CONTINUE_DOWNLOAD = 1000001;
    private static final int DOWNLOAD_PAUSE = 1000002;
    protected static final int FLAG_DOWNLOAD_COMPLETED = 2;
    protected static final int FLAG_START_DOWNLOAD = 1;
    private static final int MAX_REQ_LENGTH = 8388608;
    private static final String TAG = "GnAppDownloadTask";
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            GnAppDownloadTask.this.Logd("onReceive, mIsRunning = " + GnAppDownloadTask.this.mIsRunning + " mpause = " + GnAppDownloadTask.this.mpause);
            if (GnAppDownloadTask.this.mIsRunning) {
                GnAppDownloadTask.this.mpause = true;
                GnAppDownloadTask.this.NotifyToObserver(MSG.DOWNLOAD_TASK_COMPLETED, MSG.NOTIFY_NO_SDCARD);
            }
        }
    };
    private Context mContext;
    private String mFileName = null;
    private int mFileTotalSise = 0;
    private Handler mHandler = null;
    protected boolean mIsRunning = false;
    private Lock mLock = new ReentrantLock();
    private String mMd5;
    private boolean mReceiverRegistered = false;
    private long mThreadId = 0;
    private String mUrl;
    private String mVersion;
    private WakeLock mWakeLock;
    private boolean mpause = false;

    private void registerReceiver() {
        if (!this.mReceiverRegistered) {
            Logd("registerReceiver");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MEDIA_EJECT");
            intentFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
            intentFilter.addDataScheme("file");
            this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
            this.mReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (this.mReceiverRegistered) {
            Logd("unregisterReceiver");
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mReceiverRegistered = false;
        }
    }

    public GnAppDownloadTask(Context context, String url, int length, String fileName, String md5, String version) {
        this.mContext = context;
        this.mUrl = url;
        this.mFileName = fileName;
        this.mFileTotalSise = length;
        File parent = new File(this.mFileName).getParentFile();
        if (!(parent.exists() || parent.mkdir())) {
            Loge("GnAppDownloadTask() create parent directory failed");
        }
        this.mMd5 = md5;
        this.mVersion = version;
    }

    public Runnable getDownLoadTask() {
        return new Runnable() {
            public void run() {
                if (GnAppDownloadTask.this.mLock.tryLock()) {
                    GnAppDownloadTask.this.mIsRunning = true;
                    GnAppDownloadTask.this.registerReceiver();
                    GnAppDownloadTask.this.Logd("running!");
                    try {
                        int err;
                        GnAppDownloadTask.this.mWakeLock = ((PowerManager) GnAppDownloadTask.this.mContext.getSystemService("power")).newWakeLock(1, GnAppDownloadTask.TAG + Thread.currentThread().getId());
                        GnAppDownloadTask.this.mWakeLock.acquire();
                        synchronized (GnAppDownloadTask.this) {
                            GnAppDownloadTask.this.mpause = false;
                        }
                        do {
                            err = GnAppDownloadTask.this.downloadwork(GnAppDownloadTask.this.mUrl, GnAppDownloadTask.this.mFileTotalSise, GnAppDownloadTask.this.mFileName, true);
                        } while (err == GnAppDownloadTask.CONTINUE_DOWNLOAD);
                        if (!GnAppDownloadTask.this.mpause) {
                            if (err == 10000) {
                                boolean verifyResult = false;
                                try {
                                    if (GnAppDownloadTask.this.mMd5.equals("")) {
                                        verifyResult = Utils.verifyFile(GnAppDownloadTask.this.mFileName, (long) GnAppDownloadTask.this.mFileTotalSise, GnAppDownloadTask.this.mContext, GnAppDownloadTask.this.mVersion);
                                    } else {
                                        verifyResult = Utils.verifyFileByMd5(GnAppDownloadTask.this.mFileName, GnAppDownloadTask.this.mMd5);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (!verifyResult) {
                                    GnAppDownloadTask.this.Loge("verify download file  failed !!!");
                                    File downloadFile = new File(GnAppDownloadTask.this.mFileName);
                                    if (downloadFile.exists()) {
                                        downloadFile.delete();
                                    }
                                    GnAppDownloadTask.this.NotifyToObserver(MSG.NOTIFY_FILE_ERROR, 0);
                                } else if (GnAppDownloadTask.this.mFileName.endsWith(".patch")) {
                                    GnAppDownloadTask.this.NotifyToObserver(MSG.DOWNLOAD_TASK_COMPLETED, MSG.PATCH_FILE_DOWNLOAD_COMPLETE);
                                } else {
                                    GnAppDownloadTask.this.NotifyToObserver(MSG.DOWNLOAD_TASK_COMPLETED, MSG.DOWNLOAD_COMPLETE);
                                }
                            } else {
                                GnAppDownloadTask.this.NotifyToObserver(MSG.DOWNLOAD_TASK_COMPLETED, err);
                                GnAppDownloadTask.this.Logd("KDownloadTask.work() finish, err is " + err);
                            }
                        }
                        GnAppDownloadTask.this.Logd("GnAppDownloadTask.work() finish, current thread id is " + GnAppDownloadTask.this.mThreadId);
                        if (GnAppDownloadTask.this.mWakeLock.isHeld()) {
                            GnAppDownloadTask.this.mWakeLock.release();
                        }
                        GnAppDownloadTask.this.mLock.unlock();
                        GnAppDownloadTask.this.mIsRunning = false;
                        GnAppDownloadTask.this.unregisterReceiver();
                    } catch (Throwable th) {
                        GnAppDownloadTask.this.mLock.unlock();
                        GnAppDownloadTask.this.mIsRunning = false;
                        GnAppDownloadTask.this.unregisterReceiver();
                    }
                } else {
                    GnAppDownloadTask.this.Loge("start() downloadtask already running");
                }
            }
        };
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int downloadwork(java.lang.String r32, int r33, java.lang.String r34, boolean r35) {
        /*
        r31 = this;
        r27 = "KDownloadTask.work() Entry! ";
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);
        r27 = java.lang.Thread.currentThread();
        r28 = r27.getId();
        r0 = r28;
        r2 = r31;
        r2.mThreadId = r0;
        r6 = 0;
        r0 = r31;
        r0 = r0.mpause;
        r27 = r0;
        if (r27 != 0) goto L_0x04e0;
    L_0x0020:
        r8 = new java.io.File;	 Catch:{ Exception -> 0x0073 }
        r0 = r34;
        r8.<init>(r0);	 Catch:{ Exception -> 0x0073 }
        monitor-enter(r31);	 Catch:{ Exception -> 0x0073 }
        r0 = r31;
        r0 = r0.mpause;	 Catch:{ all -> 0x0070 }
        r27 = r0;
        if (r27 != 0) goto L_0x007e;
    L_0x0030:
        r27 = r8.exists();	 Catch:{ all -> 0x0070 }
        if (r27 == 0) goto L_0x007c;
    L_0x0036:
        r28 = r8.length();	 Catch:{ all -> 0x0070 }
        r0 = r28;
        r6 = (int) r0;	 Catch:{ all -> 0x0070 }
        if (r33 <= 0) goto L_0x0077;
    L_0x003f:
        r0 = r33;
        if (r6 <= r0) goto L_0x005f;
    L_0x0043:
        r8.delete();	 Catch:{ all -> 0x0070 }
        r6 = 0;
    L_0x0047:
        monitor-exit(r31);	 Catch:{ all -> 0x0070 }
        r27 = r8.getParentFile();
        r27 = r27.exists();
        if (r27 != 0) goto L_0x0082;
    L_0x0052:
        r27 = r8.getParentFile();
        r27 = r27.mkdir();
        if (r27 != 0) goto L_0x0082;
    L_0x005c:
        r27 = 10002; // 0x2712 float:1.4016E-41 double:4.9416E-320;
    L_0x005e:
        return r27;
    L_0x005f:
        r0 = r33;
        if (r6 != r0) goto L_0x0047;
    L_0x0063:
        r27 = 10004; // 0x2714 float:1.4019E-41 double:4.9426E-320;
        r0 = r31;
        r1 = r27;
        r0.NotifyToObserver(r1, r6);	 Catch:{ all -> 0x0070 }
        r27 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        monitor-exit(r31);	 Catch:{ all -> 0x0070 }
        goto L_0x005e;
    L_0x0070:
        r27 = move-exception;
        monitor-exit(r31);	 Catch:{ all -> 0x0070 }
        throw r27;	 Catch:{ Exception -> 0x0073 }
    L_0x0073:
        r7 = move-exception;
        r27 = 10002; // 0x2712 float:1.4016E-41 double:4.9416E-320;
        goto L_0x005e;
    L_0x0077:
        r8.delete();	 Catch:{ all -> 0x0070 }
        r6 = 0;
        goto L_0x0047;
    L_0x007c:
        r6 = 0;
        goto L_0x0047;
    L_0x007e:
        r27 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        monitor-exit(r31);	 Catch:{ all -> 0x0070 }
        goto L_0x005e;
    L_0x0082:
        r27 = 10004; // 0x2714 float:1.4019E-41 double:4.9426E-320;
        r0 = r31;
        r1 = r27;
        r0.NotifyToObserver(r1, r6);
        r14 = 0;
        r18 = 0;
        r9 = 0;
        r12 = 0;
        r27 = new java.lang.StringBuilder;	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r27.<init>();	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r28 = "curlen:";
        r27 = r27.append(r28);	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r0 = r27;
        r27 = r0.append(r6);	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r27 = r27.toString();	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r26 = new org.apache.http.conn.scheme.SchemeRegistry;	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r26.<init>();	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r17 = new org.apache.http.params.BasicHttpParams;	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r17.<init>();	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r27 = new org.apache.http.conn.scheme.Scheme;	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r28 = "http";
        r29 = org.apache.http.conn.scheme.PlainSocketFactory.getSocketFactory();	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r30 = 80;
        r27.<init>(r28, r29, r30);	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r26.register(r27);	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r5 = new org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r0 = r17;
        r1 = r26;
        r5.<init>(r0, r1);	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r10 = new org.apache.http.impl.client.DefaultHttpClient;	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r0 = r17;
        r10.<init>(r5, r0);	 Catch:{ ConnectException -> 0x066a, Exception -> 0x0655 }
        r27 = new java.lang.StringBuilder;	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r27.<init>();	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r28 = "downloadwork() : url = ";
        r27 = r27.append(r28);	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r0 = r27;
        r1 = r32;
        r27 = r0.append(r1);	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r27 = r27.toString();	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r13 = new org.apache.http.client.methods.HttpGet;	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r0 = r32;
        r13.<init>(r0);	 Catch:{ ConnectException -> 0x066d, Exception -> 0x0658, all -> 0x0643 }
        r0 = r31;
        r0 = r0.mContext;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r0;
        r27 = com.gionee.appupgrade.common.utils.NetworkUtils.isWapConnection(r27);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        if (r27 == 0) goto L_0x01f4;
    L_0x0107:
        r20 = new org.apache.http.HttpHost;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = com.gionee.appupgrade.common.utils.Constants.CONNECTION_MOBILE_DEFAULT_HOST;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = com.gionee.appupgrade.common.utils.Constants.CONNECTION_MOBILE_DEFAULT_PORT;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r20;
        r1 = r27;
        r2 = r28;
        r0.<init>(r1, r2);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r10.getParams();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "http.route.default-proxy";
        r0 = r27;
        r1 = r28;
        r2 = r20;
        r0.setParameter(r1, r2);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = 8388608; // 0x800000 float:1.17549435E-38 double:4.144523E-317;
        r21 = r6 + r27;
        r0 = r21;
        r1 = r33;
        if (r0 <= r1) goto L_0x0131;
    L_0x012f:
        r21 = r33;
    L_0x0131:
        r27 = "Range";
        r28 = new java.lang.StringBuilder;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28.<init>();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r29 = "bytes=";
        r28 = r28.append(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r28;
        r28 = r0.append(r6);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r29 = "-";
        r28 = r28.append(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r28;
        r1 = r21;
        r28 = r0.append(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = r28.toString();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r27;
        r1 = r28;
        r13.setHeader(r0, r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
    L_0x015d:
        r27 = r10.getParams();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "http.socket.timeout";
        r29 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        r29 = java.lang.Integer.valueOf(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27.setParameter(r28, r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r10.getParams();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "http.connection.timeout";
        r29 = 15000; // 0x3a98 float:2.102E-41 double:7.411E-320;
        r29 = java.lang.Integer.valueOf(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27.setParameter(r28, r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r10.getParams();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "http.useragent";
        r0 = r31;
        r0 = r0.mContext;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r29 = r0;
        r29 = com.gionee.appupgrade.common.utils.Utils.getImei(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r29 = com.gionee.appupgrade.common.utils.Utils.getUaString(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27.setParameter(r28, r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r24 = r10.execute(r13);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r24.getStatusLine();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r23 = r27.getStatusCode();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = 200; // 0xc8 float:2.8E-43 double:9.9E-322;
        r0 = r23;
        r1 = r27;
        if (r0 == r1) goto L_0x027c;
    L_0x01a6:
        r27 = 206; // 0xce float:2.89E-43 double:1.02E-321;
        r0 = r23;
        r1 = r27;
        if (r0 == r1) goto L_0x027c;
    L_0x01ae:
        r27 = new java.lang.StringBuilder;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27.<init>();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "KDownloadTask.work() Server Error, ResponseCode is";
        r27 = r27.append(r28);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r27;
        r1 = r23;
        r27 = r0.append(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r27.toString();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = 10003; // 0x2713 float:1.4017E-41 double:4.942E-320;
        if (r10 == 0) goto L_0x01e2;
    L_0x01d0:
        if (r13 == 0) goto L_0x01e2;
    L_0x01d2:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x01db;
    L_0x01d8:
        r13.abort();
    L_0x01db:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x01e2:
        if (r14 == 0) goto L_0x01e8;
    L_0x01e4:
        r14.close();	 Catch:{ IOException -> 0x05ff }
    L_0x01e7:
        r14 = 0;
    L_0x01e8:
        if (r18 == 0) goto L_0x005e;
    L_0x01ea:
        r18.flush();	 Catch:{ IOException -> 0x0605 }
        r18.close();	 Catch:{ IOException -> 0x0605 }
    L_0x01f0:
        r18 = 0;
        goto L_0x005e;
    L_0x01f4:
        if (r6 == 0) goto L_0x015d;
    L_0x01f6:
        r27 = "Range";
        r28 = new java.lang.StringBuilder;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28.<init>();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r29 = "bytes=";
        r28 = r28.append(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r28;
        r28 = r0.append(r6);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r29 = "-";
        r28 = r28.append(r29);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = r28.toString();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r27;
        r1 = r28;
        r13.setHeader(r0, r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        goto L_0x015d;
    L_0x021c:
        r7 = move-exception;
        r12 = r13;
        r9 = r10;
    L_0x021f:
        r27 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0641 }
        r27.<init>();	 Catch:{ all -> 0x0641 }
        r28 = "KDownloadTask.work() Exception ";
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r28 = r7.toString();	 Catch:{ all -> 0x0641 }
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r28 = ", current thread id is ";
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r0 = r31;
        r0 = r0.mThreadId;	 Catch:{ all -> 0x0641 }
        r28 = r0;
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r27 = r27.toString();	 Catch:{ all -> 0x0641 }
        r0 = r31;
        r1 = r27;
        r0.Loge(r1);	 Catch:{ all -> 0x0641 }
        r7.printStackTrace();	 Catch:{ all -> 0x0641 }
        r27 = 10007; // 0x2717 float:1.4023E-41 double:4.944E-320;
        if (r9 == 0) goto L_0x0266;
    L_0x0254:
        if (r12 == 0) goto L_0x0266;
    L_0x0256:
        r28 = r12.isAborted();
        if (r28 != 0) goto L_0x025f;
    L_0x025c:
        r12.abort();
    L_0x025f:
        r28 = r9.getConnectionManager();
        r28.shutdown();
    L_0x0266:
        if (r14 == 0) goto L_0x026c;
    L_0x0268:
        r14.close();	 Catch:{ IOException -> 0x05f3 }
    L_0x026b:
        r14 = 0;
    L_0x026c:
        if (r18 == 0) goto L_0x005e;
    L_0x026e:
        r18.flush();	 Catch:{ IOException -> 0x0276 }
        r18.close();	 Catch:{ IOException -> 0x0276 }
        goto L_0x01f0;
    L_0x0276:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x027c:
        r0 = r31;
        r0 = r0.mpause;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r0;
        if (r27 != 0) goto L_0x046c;
    L_0x0284:
        if (r6 != 0) goto L_0x0293;
    L_0x0286:
        r27 = 10028; // 0x272c float:1.4052E-41 double:4.9545E-320;
        r28 = 1;
        r0 = r31;
        r1 = r27;
        r2 = r28;
        r0.NotifyToObserver(r1, r2);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
    L_0x0293:
        r11 = r24.getEntity();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r25 = 0;
        r27 = r11.getContentType();	 Catch:{ NullPointerException -> 0x0354 }
        r25 = r27.getValue();	 Catch:{ NullPointerException -> 0x0354 }
    L_0x02a1:
        r27 = new java.lang.StringBuilder;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27.<init>();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "downloadwork() getContentType = ";
        r27 = r27.append(r28);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r27;
        r1 = r25;
        r27 = r0.append(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r27.toString();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        if (r25 == 0) goto L_0x02e5;
    L_0x02c1:
        r27 = "application/vnd.android.package-archive";
        r0 = r25;
        r1 = r27;
        r27 = r0.equals(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        if (r27 != 0) goto L_0x02e5;
    L_0x02cd:
        r27 = "text/html";
        r0 = r25;
        r1 = r27;
        r27 = r0.contains(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        if (r27 != 0) goto L_0x04fa;
    L_0x02d9:
        r27 = "text/xml";
        r0 = r25;
        r1 = r27;
        r27 = r0.contains(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        if (r27 != 0) goto L_0x04fa;
    L_0x02e5:
        r15 = new java.io.BufferedInputStream;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r11.getContent();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r27;
        r15.<init>(r0);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        r0 = r27;
        r4 = new byte[r0];	 Catch:{ ConnectException -> 0x0671, Exception -> 0x065c, all -> 0x0647 }
        r19 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x03ba }
        r27 = 1;
        r0 = r19;
        r1 = r27;
        r0.<init>(r8, r1);	 Catch:{ FileNotFoundException -> 0x03ba }
        r22 = 0;
        r16 = 1;
    L_0x0305:
        r27 = 0;
        r28 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        r0 = r27;
        r1 = r28;
        r22 = r15.read(r4, r0, r1);	 Catch:{ ConnectException -> 0x0408, Exception -> 0x0662, all -> 0x064d }
        r27 = -1;
        r0 = r22;
        r1 = r27;
        if (r0 == r1) goto L_0x0469;
    L_0x0319:
        r0 = r31;
        r0 = r0.mpause;	 Catch:{ ConnectException -> 0x0408, Exception -> 0x0662, all -> 0x064d }
        r27 = r0;
        if (r27 != 0) goto L_0x043d;
    L_0x0321:
        r27 = r8.exists();	 Catch:{ Exception -> 0x0410, ConnectException -> 0x0408, all -> 0x064d }
        if (r27 != 0) goto L_0x03f0;
    L_0x0327:
        r27 = 1000001; // 0xf4241 float:1.4013E-39 double:4.94066E-318;
        if (r10 == 0) goto L_0x033e;
    L_0x032c:
        if (r13 == 0) goto L_0x033e;
    L_0x032e:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x0337;
    L_0x0334:
        r13.abort();
    L_0x0337:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x033e:
        if (r15 == 0) goto L_0x067e;
    L_0x0340:
        r15.close();	 Catch:{ IOException -> 0x0617 }
    L_0x0343:
        r14 = 0;
    L_0x0344:
        if (r19 == 0) goto L_0x0677;
    L_0x0346:
        r19.flush();	 Catch:{ IOException -> 0x034e }
        r19.close();	 Catch:{ IOException -> 0x034e }
        goto L_0x01f0;
    L_0x034e:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x0354:
        r7 = move-exception;
        r7.printStackTrace();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        goto L_0x02a1;
    L_0x035a:
        r7 = move-exception;
        r12 = r13;
        r9 = r10;
    L_0x035d:
        r27 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0641 }
        r27.<init>();	 Catch:{ all -> 0x0641 }
        r28 = "KDownloadTask.work() Exception ";
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r28 = r7.toString();	 Catch:{ all -> 0x0641 }
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r28 = ", current thread id is ";
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r0 = r31;
        r0 = r0.mThreadId;	 Catch:{ all -> 0x0641 }
        r28 = r0;
        r27 = r27.append(r28);	 Catch:{ all -> 0x0641 }
        r27 = r27.toString();	 Catch:{ all -> 0x0641 }
        r0 = r31;
        r1 = r27;
        r0.Loge(r1);	 Catch:{ all -> 0x0641 }
        r7.printStackTrace();	 Catch:{ all -> 0x0641 }
        r27 = 10007; // 0x2717 float:1.4023E-41 double:4.944E-320;
        if (r9 == 0) goto L_0x03a4;
    L_0x0392:
        if (r12 == 0) goto L_0x03a4;
    L_0x0394:
        r28 = r12.isAborted();
        if (r28 != 0) goto L_0x039d;
    L_0x039a:
        r12.abort();
    L_0x039d:
        r28 = r9.getConnectionManager();
        r28.shutdown();
    L_0x03a4:
        if (r14 == 0) goto L_0x03aa;
    L_0x03a6:
        r14.close();	 Catch:{ IOException -> 0x05f9 }
    L_0x03a9:
        r14 = 0;
    L_0x03aa:
        if (r18 == 0) goto L_0x005e;
    L_0x03ac:
        r18.flush();	 Catch:{ IOException -> 0x03b4 }
        r18.close();	 Catch:{ IOException -> 0x03b4 }
        goto L_0x01f0;
    L_0x03b4:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x03ba:
        r7 = move-exception;
        r27 = "downloadwork() FileNotFoundException exception, maybe sdcard not exists";
        r0 = r31;
        r1 = r27;
        r0.Loge(r1);	 Catch:{ ConnectException -> 0x0671, Exception -> 0x065c, all -> 0x0647 }
        r27 = 10008; // 0x2718 float:1.4024E-41 double:4.9446E-320;
        if (r10 == 0) goto L_0x03da;
    L_0x03c8:
        if (r13 == 0) goto L_0x03da;
    L_0x03ca:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x03d3;
    L_0x03d0:
        r13.abort();
    L_0x03d3:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x03da:
        if (r15 == 0) goto L_0x0684;
    L_0x03dc:
        r15.close();	 Catch:{ IOException -> 0x060b }
    L_0x03df:
        r14 = 0;
    L_0x03e0:
        if (r18 == 0) goto L_0x005e;
    L_0x03e2:
        r18.flush();	 Catch:{ IOException -> 0x03ea }
        r18.close();	 Catch:{ IOException -> 0x03ea }
        goto L_0x01f0;
    L_0x03ea:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x03f0:
        r27 = 0;
        r0 = r19;
        r1 = r27;
        r2 = r22;
        r0.write(r4, r1, r2);	 Catch:{ Exception -> 0x0410, ConnectException -> 0x0408, all -> 0x064d }
        r6 = r6 + r22;
        r27 = 10004; // 0x2714 float:1.4019E-41 double:4.9426E-320;
        r0 = r31;
        r1 = r27;
        r0.NotifyToObserver(r1, r6);	 Catch:{ ConnectException -> 0x0408, Exception -> 0x0662, all -> 0x064d }
        goto L_0x0305;
    L_0x0408:
        r7 = move-exception;
        r12 = r13;
        r9 = r10;
        r18 = r19;
        r14 = r15;
        goto L_0x021f;
    L_0x0410:
        r7 = move-exception;
        r27 = 10005; // 0x2715 float:1.402E-41 double:4.943E-320;
        if (r10 == 0) goto L_0x0427;
    L_0x0415:
        if (r13 == 0) goto L_0x0427;
    L_0x0417:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x0420;
    L_0x041d:
        r13.abort();
    L_0x0420:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x0427:
        if (r15 == 0) goto L_0x0681;
    L_0x0429:
        r15.close();	 Catch:{ IOException -> 0x0611 }
    L_0x042c:
        r14 = 0;
    L_0x042d:
        if (r19 == 0) goto L_0x0677;
    L_0x042f:
        r19.flush();	 Catch:{ IOException -> 0x0437 }
        r19.close();	 Catch:{ IOException -> 0x0437 }
        goto L_0x01f0;
    L_0x0437:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x043d:
        r27 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        if (r10 == 0) goto L_0x0453;
    L_0x0441:
        if (r13 == 0) goto L_0x0453;
    L_0x0443:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x044c;
    L_0x0449:
        r13.abort();
    L_0x044c:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x0453:
        if (r15 == 0) goto L_0x067b;
    L_0x0455:
        r15.close();	 Catch:{ IOException -> 0x061d }
    L_0x0458:
        r14 = 0;
    L_0x0459:
        if (r19 == 0) goto L_0x0677;
    L_0x045b:
        r19.flush();	 Catch:{ IOException -> 0x0463 }
        r19.close();	 Catch:{ IOException -> 0x0463 }
        goto L_0x01f0;
    L_0x0463:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x0469:
        r18 = r19;
        r14 = r15;
    L_0x046c:
        r27 = new java.lang.StringBuilder;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27.<init>();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "GnAppDownloadTask";
        r27 = r27.append(r28);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r31;
        r0 = r0.mThreadId;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = r0;
        r27 = r27.append(r28);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = r27.toString();	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r28 = "mWriting = false";
        com.gionee.appupgrade.common.utils.LogUtils.logd(r27, r28);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        monitor-enter(r31);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r0 = r31;
        r0 = r0.mpause;	 Catch:{ all -> 0x05be }
        r27 = r0;
        if (r27 != 0) goto L_0x0587;
    L_0x0493:
        r0 = r33;
        if (r6 < r0) goto L_0x052f;
    L_0x0497:
        r27 = new java.lang.StringBuilder;	 Catch:{ all -> 0x05be }
        r27.<init>();	 Catch:{ all -> 0x05be }
        r28 = "KDownloadTask.work() : ";
        r27 = r27.append(r28);	 Catch:{ all -> 0x05be }
        r0 = r27;
        r1 = r34;
        r27 = r0.append(r1);	 Catch:{ all -> 0x05be }
        r28 = "download complete";
        r27 = r27.append(r28);	 Catch:{ all -> 0x05be }
        r27 = r27.toString();	 Catch:{ all -> 0x05be }
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);	 Catch:{ all -> 0x05be }
        monitor-exit(r31);	 Catch:{ all -> 0x05be }
        if (r10 == 0) goto L_0x04d0;
    L_0x04be:
        if (r13 == 0) goto L_0x04d0;
    L_0x04c0:
        r27 = r13.isAborted();
        if (r27 != 0) goto L_0x04c9;
    L_0x04c6:
        r13.abort();
    L_0x04c9:
        r27 = r10.getConnectionManager();
        r27.shutdown();
    L_0x04d0:
        if (r14 == 0) goto L_0x04d6;
    L_0x04d2:
        r14.close();	 Catch:{ IOException -> 0x0623 }
    L_0x04d5:
        r14 = 0;
    L_0x04d6:
        if (r18 == 0) goto L_0x04e0;
    L_0x04d8:
        r18.flush();	 Catch:{ IOException -> 0x0629 }
        r18.close();	 Catch:{ IOException -> 0x0629 }
    L_0x04de:
        r18 = 0;
    L_0x04e0:
        r27 = "out while(mrunning)";
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);
        r27 = 10028; // 0x272c float:1.4052E-41 double:4.9545E-320;
        r28 = 2;
        r0 = r31;
        r1 = r27;
        r2 = r28;
        r0.NotifyToObserver(r1, r2);
        r27 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        goto L_0x005e;
    L_0x04fa:
        r27 = "downloadwork() : return data type is not correct";
        r0 = r31;
        r1 = r27;
        r0.Loge(r1);	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
        r27 = 10006; // 0x2716 float:1.4021E-41 double:4.9436E-320;
        if (r10 == 0) goto L_0x0519;
    L_0x0507:
        if (r13 == 0) goto L_0x0519;
    L_0x0509:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x0512;
    L_0x050f:
        r13.abort();
    L_0x0512:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x0519:
        if (r14 == 0) goto L_0x051f;
    L_0x051b:
        r14.close();	 Catch:{ IOException -> 0x063b }
    L_0x051e:
        r14 = 0;
    L_0x051f:
        if (r18 == 0) goto L_0x005e;
    L_0x0521:
        r18.flush();	 Catch:{ IOException -> 0x0529 }
        r18.close();	 Catch:{ IOException -> 0x0529 }
        goto L_0x01f0;
    L_0x0529:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x052f:
        r27 = new java.lang.StringBuilder;	 Catch:{ all -> 0x05be }
        r27.<init>();	 Catch:{ all -> 0x05be }
        r28 = "KDownloadTask.work() : ";
        r27 = r27.append(r28);	 Catch:{ all -> 0x05be }
        r0 = r27;
        r1 = r34;
        r27 = r0.append(r1);	 Catch:{ all -> 0x05be }
        r28 = "download continue, lengtn : ";
        r27 = r27.append(r28);	 Catch:{ all -> 0x05be }
        r0 = r27;
        r27 = r0.append(r6);	 Catch:{ all -> 0x05be }
        r27 = r27.toString();	 Catch:{ all -> 0x05be }
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);	 Catch:{ all -> 0x05be }
        r27 = 1000001; // 0xf4241 float:1.4013E-39 double:4.94066E-318;
        monitor-exit(r31);	 Catch:{ all -> 0x05be }
        if (r10 == 0) goto L_0x0571;
    L_0x055f:
        if (r13 == 0) goto L_0x0571;
    L_0x0561:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x056a;
    L_0x0567:
        r13.abort();
    L_0x056a:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x0571:
        if (r14 == 0) goto L_0x0577;
    L_0x0573:
        r14.close();	 Catch:{ IOException -> 0x062f }
    L_0x0576:
        r14 = 0;
    L_0x0577:
        if (r18 == 0) goto L_0x005e;
    L_0x0579:
        r18.flush();	 Catch:{ IOException -> 0x0581 }
        r18.close();	 Catch:{ IOException -> 0x0581 }
        goto L_0x01f0;
    L_0x0581:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x0587:
        r27 = "stop";
        r0 = r31;
        r1 = r27;
        r0.Logd(r1);	 Catch:{ all -> 0x05be }
        r27 = 1000002; // 0xf4242 float:1.401301E-39 double:4.940666E-318;
        monitor-exit(r31);	 Catch:{ all -> 0x05be }
        if (r10 == 0) goto L_0x05a8;
    L_0x0596:
        if (r13 == 0) goto L_0x05a8;
    L_0x0598:
        r28 = r13.isAborted();
        if (r28 != 0) goto L_0x05a1;
    L_0x059e:
        r13.abort();
    L_0x05a1:
        r28 = r10.getConnectionManager();
        r28.shutdown();
    L_0x05a8:
        if (r14 == 0) goto L_0x05ae;
    L_0x05aa:
        r14.close();	 Catch:{ IOException -> 0x0635 }
    L_0x05ad:
        r14 = 0;
    L_0x05ae:
        if (r18 == 0) goto L_0x005e;
    L_0x05b0:
        r18.flush();	 Catch:{ IOException -> 0x05b8 }
        r18.close();	 Catch:{ IOException -> 0x05b8 }
        goto L_0x01f0;
    L_0x05b8:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x05be:
        r27 = move-exception;
        monitor-exit(r31);	 Catch:{ all -> 0x05be }
        throw r27;	 Catch:{ ConnectException -> 0x021c, Exception -> 0x035a, all -> 0x05c1 }
    L_0x05c1:
        r27 = move-exception;
        r12 = r13;
        r9 = r10;
    L_0x05c4:
        if (r9 == 0) goto L_0x05d8;
    L_0x05c6:
        if (r12 == 0) goto L_0x05d8;
    L_0x05c8:
        r28 = r12.isAborted();
        if (r28 != 0) goto L_0x05d1;
    L_0x05ce:
        r12.abort();
    L_0x05d1:
        r28 = r9.getConnectionManager();
        r28.shutdown();
    L_0x05d8:
        if (r14 == 0) goto L_0x05de;
    L_0x05da:
        r14.close();	 Catch:{ IOException -> 0x05e9 }
    L_0x05dd:
        r14 = 0;
    L_0x05de:
        if (r18 == 0) goto L_0x05e8;
    L_0x05e0:
        r18.flush();	 Catch:{ IOException -> 0x05ee }
        r18.close();	 Catch:{ IOException -> 0x05ee }
    L_0x05e6:
        r18 = 0;
    L_0x05e8:
        throw r27;
    L_0x05e9:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x05dd;
    L_0x05ee:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x05e6;
    L_0x05f3:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x026b;
    L_0x05f9:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x03a9;
    L_0x05ff:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01e7;
    L_0x0605:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x01f0;
    L_0x060b:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x03df;
    L_0x0611:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x042c;
    L_0x0617:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x0343;
    L_0x061d:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x0458;
    L_0x0623:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x04d5;
    L_0x0629:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x04de;
    L_0x062f:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x0576;
    L_0x0635:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x05ad;
    L_0x063b:
        r7 = move-exception;
        r7.printStackTrace();
        goto L_0x051e;
    L_0x0641:
        r27 = move-exception;
        goto L_0x05c4;
    L_0x0643:
        r27 = move-exception;
        r9 = r10;
        goto L_0x05c4;
    L_0x0647:
        r27 = move-exception;
        r12 = r13;
        r9 = r10;
        r14 = r15;
        goto L_0x05c4;
    L_0x064d:
        r27 = move-exception;
        r12 = r13;
        r9 = r10;
        r18 = r19;
        r14 = r15;
        goto L_0x05c4;
    L_0x0655:
        r7 = move-exception;
        goto L_0x035d;
    L_0x0658:
        r7 = move-exception;
        r9 = r10;
        goto L_0x035d;
    L_0x065c:
        r7 = move-exception;
        r12 = r13;
        r9 = r10;
        r14 = r15;
        goto L_0x035d;
    L_0x0662:
        r7 = move-exception;
        r12 = r13;
        r9 = r10;
        r18 = r19;
        r14 = r15;
        goto L_0x035d;
    L_0x066a:
        r7 = move-exception;
        goto L_0x021f;
    L_0x066d:
        r7 = move-exception;
        r9 = r10;
        goto L_0x021f;
    L_0x0671:
        r7 = move-exception;
        r12 = r13;
        r9 = r10;
        r14 = r15;
        goto L_0x021f;
    L_0x0677:
        r18 = r19;
        goto L_0x005e;
    L_0x067b:
        r14 = r15;
        goto L_0x0459;
    L_0x067e:
        r14 = r15;
        goto L_0x0344;
    L_0x0681:
        r14 = r15;
        goto L_0x042d;
    L_0x0684:
        r14 = r15;
        goto L_0x03e0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.appupgrade.common.GnAppDownloadTask.downloadwork(java.lang.String, int, java.lang.String, boolean):int");
    }

    private void NotifyToObserver(int msgid, int arg) {
        if (this.mHandler != null) {
            Logd("NotifyToObserver msgid = " + msgid + " arg = " + arg);
            Message message = this.mHandler.obtainMessage();
            message.what = msgid;
            message.arg1 = arg;
            message.sendToTarget();
            return;
        }
        Loge("NotifyToObserver() mHandler is null");
    }

    private void Logd(String msg) {
        LogUtils.logd(TAG, "mFileName = " + this.mFileName + "  " + msg);
    }

    private void Loge(String msg) {
        LogUtils.loge(TAG, "mFileName = " + this.mFileName + "  " + msg);
    }

    protected void registerHandler(Handler handler) {
        this.mHandler = handler;
    }

    protected void unregisterHandler() {
        this.mHandler = null;
    }
}
