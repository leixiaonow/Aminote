package com.gionee.feedback.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.gionee.feedback.IAlarmGetRecord;
import com.gionee.feedback.utils.Log;
import java.util.Calendar;

public class AlarmGetRecordImpl implements IAlarmGetRecord {
    private static final String ALARM_ACTION = "gionee.intent.action.FEEDBACK_ALARM";
    private static final String TAG = "AlarmGetRecordImpl";

    public void setAlarmGetRecord(Context context) {
        Log.d(TAG, context.getPackageName() + " ->setAlarmGetRecord()");
        Intent intent = new Intent(ALARM_ACTION);
        intent.setClassName(context.getPackageName(), FeedBackReceiver.class.getName());
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(10, 24);
        ((AlarmManager) context.getSystemService("alarm")).set(0, calendar.getTimeInMillis(), sender);
    }

    public void cancelAlarmGetRecord(Context context) {
        Log.d(TAG, context.getPackageName() + " ->cancelAlarmGetRecord()");
        Intent intent = new Intent(ALARM_ACTION);
        intent.setClassName(context.getPackageName(), FeedBackReceiver.class.getName());
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, intent, 0));
    }
}
