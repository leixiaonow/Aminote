package com.gionee.feedback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.ui.AlarmGetRecordImpl;
import com.gionee.feedback.ui.FeedBackActivity;
import com.gionee.feedback.utils.Log;

public final class FeedbackApi {
    private static final String TAG = "FeedbackApi";
    private static FeedbackApi sFeedbackApi;

    public static final synchronized FeedbackApi createFeedbackApi(Context context) {
        FeedbackApi feedbackApi;
        synchronized (FeedbackApi.class) {
            if (sFeedbackApi == null) {
                sFeedbackApi = new FeedbackApi(context);
            }
            feedbackApi = sFeedbackApi;
        }
        return feedbackApi;
    }

    private FeedbackApi(Context context) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "not found " + context.getPackageName());
        }
        if (appInfo != null) {
            String appKey = appInfo.metaData.getString("FeedBack_AppId");
            if (!TextUtils.isEmpty(appKey)) {
                Log.d(TAG, "appKey = " + appKey);
                DataManager.getInstance(context).storageAppKey(appKey);
            }
        }
    }

    public void gotoFeedback(Context context) throws FeedBackException {
        Intent intent = new Intent(context, FeedBackActivity.class);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return;
        }
        throw new FeedBackException("not find feedback activity, please regist it in mainfest !!!");
    }

    public IAlarmGetRecord alarmRecord() {
        return new AlarmGetRecordImpl();
    }
}
