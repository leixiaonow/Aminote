package com.gionee.note.web;

import android.app.DownloadManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.common.Constants;
import com.gionee.note.common.StatisticsModule;

public class NoteWebView extends WebView {
    private Context mContext;
    private DownloadListener mDownloadListener = new DownloadListener() {
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            if (TextUtils.isEmpty(mimetype) || !mimetype.startsWith("image/")) {
                Toast.makeText(NoteWebView.this.mContext, R.string.download_unsurpport_hint, 0).show();
                return;
            }
            String fileName = String.valueOf(System.currentTimeMillis());
            if (!TextUtils.isEmpty(contentDisposition)) {
                fileName = NoteWebView.this.getFileName(contentDisposition);
            }
            OnlineImageDownloadHandler.download((DownloadManager) NoteWebView.this.mContext.getSystemService("download"), url, fileName, NoteWebView.this.getResources().getString(R.string.notification_title));
            StatisticsModule.onEvent(NoteWebView.this.mContext, NoteWebView.this.getResources().getString(R.string.youju_download_picture), fileName);
            StringBuilder hint = new StringBuilder();
            hint.append(NoteWebView.this.mContext.getResources().getString(R.string.download_poto_hint)).append(Constants.ROOT_FILE.getPath()).append(Constants.NOTE);
            Toast.makeText(NoteWebView.this.mContext.getApplicationContext(), hint.toString(), 0).show();
        }
    };

    public NoteWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initWebSetting();
        setDownloadListener(this.mDownloadListener);
    }

    public void initWebSetting() {
        requestFocus();
        setSelected(true);
        setScrollBarStyle(33554432);
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        initCache();
    }

    private void initCache() {
        WebSettings webSettings = getSettings();
        webSettings.setAppCacheEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(-1);
        webSettings.setAppCachePath(getContext().getDir("web_appcache", 0).getPath());
    }

    private String getFileName(String contentDisposition) {
        String fileName = String.valueOf(System.currentTimeMillis());
        if (TextUtils.isEmpty(contentDisposition)) {
            return fileName;
        }
        String[] typeArray = contentDisposition.split("\"");
        if (typeArray.length > 1) {
            return typeArray[1];
        }
        return fileName;
    }
}
