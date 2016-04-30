package com.gionee.feedback.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.utils.Log;

public class FeedBackReceiver extends BroadcastReceiver {
    private static final String TAG = "TAGFeedBackReceiver";

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive action = " + intent.getAction() + "  package = " + context.getPackageName());
        DataManager.getInstance(context).getAllRecordsNotify(true);
        new AlarmGetRecordImpl().setAlarmGetRecord(context);
    }
}
