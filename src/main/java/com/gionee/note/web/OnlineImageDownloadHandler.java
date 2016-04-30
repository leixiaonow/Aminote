package com.gionee.note.web;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.common.Constants;

public class OnlineImageDownloadHandler {
    private static final String DOWNLOAD_ONLINE_PIC_DIR;
    private static final String TAG = "OnlineImageDownloadHandler";
    private Activity mActivity;
    private DownloadStateReceiver mDownloadStateReceiver;

    private class DownloadStateReceiver extends BroadcastReceiver {
        private DownloadStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.DOWNLOAD_COMPLETE".equals(action)) {
                Toast.makeText(OnlineImageDownloadHandler.this.mActivity.getApplicationContext(), OnlineImageDownloadHandler.this.mActivity.getResources().getString(R.string.download_compelete), 0).show();
            } else if ("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED".equals(action)) {
                OnlineImageDownloadHandler.this.startDownloadsActivity();
            }
        }
    }

    static {
        String[] rootArray = Constants.ROOT_FILE.getPath().split("/");
        DOWNLOAD_ONLINE_PIC_DIR = rootArray[rootArray.length - 1] + Constants.NOTE;
    }

    public OnlineImageDownloadHandler(Activity activity) {
        this.mActivity = activity;
    }

    public void register() {
        this.mDownloadStateReceiver = new DownloadStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.DOWNLOAD_COMPLETE");
        filter.addAction("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
        this.mActivity.registerReceiver(this.mDownloadStateReceiver, filter);
    }

    public void destroy() {
        this.mActivity.unregisterReceiver(this.mDownloadStateReceiver);
        this.mActivity = null;
        this.mDownloadStateReceiver = null;
    }

    public static void download(DownloadManager downloadManager, String url, String fileName, String title) {
        Request request = new Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(DOWNLOAD_ONLINE_PIC_DIR, fileName);
        request.setTitle(title);
        request.setNotificationVisibility(0);
        downloadManager.enqueue(request);
    }

    private void startDownloadsActivity() {
        try {
            this.mActivity.startActivity(new Intent("android.intent.action.VIEW_DOWNLOADS"));
        } catch (ActivityNotFoundException e) {
        }
    }
}
