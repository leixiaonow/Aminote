package com.gionee.note.app.reminder;

import amigoui.changecolors.ColorConfigConstants;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.gionee.aminote.R;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.NewNoteActivity;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.data.LocalNoteItem;
import com.gionee.note.data.NoteItem;
import com.gionee.note.data.NoteParser;
import com.gionee.note.data.Path;
import com.gionee.note.provider.NoteContract.NoteContent;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ReminderManager {
    private static final String[] ALARM_PROJECT = new String[]{"_id", NoteContent.COLUMN_REMINDER};
    private static final int INDEX_ID = 0;
    private static final int INDEX_REMINDER = 1;
    private static final long ONE_DAY_IN_MILLISECOND = 86400000;
    private static final int WIDGET_BG_REQUEST_CODE = -1;

    public static void scheduleReminder(final Context context) {
        NoteAppImpl.getContext().getThreadPool().submit(new Job<Object>() {
            public Object run(JobContext jc) {
                long current = System.currentTimeMillis();
                Cursor cursor = context.getContentResolver().query(NoteContent.CONTENT_URI, ReminderManager.ALARM_PROJECT, "reminder>" + current, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        try {
                            ReminderManager.setReminder(context, cursor.getLong(0), cursor.getLong(1));
                        } finally {
                            NoteUtils.closeSilently(cursor);
                        }
                    }
                }
                return null;
            }
        });
    }

    public static void setWidgetBackgroundReminder(Context context) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, -1, new Intent(ReminderReceiver.ACTION_WIDGET_REMINDER), 134217728);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        long trrigleTime = System.currentTimeMillis() + ONE_DAY_IN_MILLISECOND;
        if (VERSION.SDK_INT >= 19) {
            alarmManager.setExact(0, trrigleTime, pendingIntent);
        } else {
            alarmManager.set(0, trrigleTime, pendingIntent);
        }
    }

    public static void setReminder(Context context, long id, long reminder) {
        if (reminder >= System.currentTimeMillis()) {
            Intent intent = new Intent(ReminderReceiver.ACTION_POP_REMINDER);
            intent.putExtra("_id", id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, intent, 134217728);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
            if (VERSION.SDK_INT >= 19) {
                alarmManager.setExact(0, reminder, pendingIntent);
            } else {
                alarmManager.set(0, reminder, pendingIntent);
            }
        }
    }

    public static void cancelReminder(Context context, long id) {
        Intent intent = new Intent(ReminderReceiver.ACTION_POP_REMINDER);
        intent.putExtra("_id", id);
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, (int) id, intent, ColorConfigConstants.DEFAULT_EDIT_TEXT_BACKGROUND_COLOR_B3));
        ((NotificationManager) context.getSystemService("notification")).cancel((int) id);
    }

    public static void popReminder(final Context context, Intent intent) {
        final long id = intent.getLongExtra("_id", -1);
        if (id != -1) {
            final NoteAppImpl app = NoteAppImpl.getContext();
            app.getThreadPool().submit(new Job<Object>() {
                public Object run(JobContext jc) {
                    Path path = LocalNoteItem.ITEM_PATH.getChild(id);
                    NoteItem item = (NoteItem) app.getDataManager().getMediaObject(path);
                    String title = item.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        title = app.getString(R.string.app_name);
                    }
                    String content = ReminderManager.getReminderContent(item.getContent());
                    Intent innerIntent = new Intent(context, NewNoteActivity.class);
                    innerIntent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
                    PendingIntent wrapIntent = PendingIntent.getActivity(context, (int) id, innerIntent, 134217728);
                    Builder builder = new Builder(context);
                    builder.setAutoCancel(true);
                    builder.setContentIntent(wrapIntent);
                    builder.setContentTitle(title);
                    builder.setContentText(content);
                    builder.setSmallIcon(R.drawable.ic_launcher);
                    builder.setPriority(1);
                    builder.setWhen(System.currentTimeMillis());
                    Notification notification = builder.build();
                    notification.defaults |= 1;
                    notification.defaults |= 2;
                    notification.defaults |= 4;
                    ((NotificationManager) context.getSystemService("notification")).notify((int) id, notification);
                    return null;
                }
            });
        }
    }

    private static String getReminderContent(String json) {
        if (json == null || json.length() == 0) {
            return "";
        }
        String reminderContent = "";
        try {
            return NoteParser.parserText(((JSONObject) new JSONTokener(json).nextValue()).getString(DataConvert.JSON_CONTENT_KEY));
        } catch (Exception e) {
            return reminderContent;
        }
    }
}
