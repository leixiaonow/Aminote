package com.gionee.note.app.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.gionee.framework.log.Logger;
import com.gionee.note.widget.WidgetUtil;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_CANCEL_ALARM = "com.gionee.note.action.cancel_alarm";
    public static final String ACTION_POP_REMINDER = "com.gionee.note.action.pop_reminder";
    public static final String ACTION_WIDGET_REMINDER = "com.gionee.note.action.widget_reminder";
    private static final String TAG = "ReminderReceiver";

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals("android.intent.action.BOOT_COMPLETED", action)) {
            Logger.printLog(TAG, "ACTION_BOOT_COMPLETED");
            ReminderManager.scheduleReminder(context);
            ReminderManager.setWidgetBackgroundReminder(context);
            WidgetUtil.updateAllWidgets();
        } else if (TextUtils.equals("android.intent.action.TIME_SET", action)) {
            ReminderManager.scheduleReminder(context);
            ReminderManager.setWidgetBackgroundReminder(context);
            WidgetUtil.updateAllWidgets();
        } else if (ACTION_POP_REMINDER.equals(action)) {
            ReminderManager.popReminder(context, intent);
        } else if (ACTION_WIDGET_REMINDER.equals(action)) {
            ReminderManager.setWidgetBackgroundReminder(context);
            WidgetUtil.updateAllWidgets();
        }
    }
}
