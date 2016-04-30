package com.gionee.note.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.SlidingWindow.NoteEntry;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.data.LocalNoteSet;
import com.gionee.note.data.NoteParser;
import com.gionee.note.provider.NoteContract.NoteContent;
import java.text.SimpleDateFormat;

public class WidgetUtil {
    public static void updateWidget(String title, String jsonContent, int noteId, long modifiedTime, long reminderInMs) {
        NoteEntry entry = new NoteEntry();
        entry.title = title;
        entry.time = NoteUtils.formatDateTime(modifiedTime, new SimpleDateFormat(NoteParser.DATE_FORMAT));
        entry.reminder = reminderInMs;
        entry.id = noteId;
        entry.timeMillis = modifiedTime;
        new NoteParser().parseNoteContent(jsonContent, entry);
        updateAllWidgets(entry);
    }

    public static void updateAllWidgets() {
        Context context = NoteAppImpl.getContext();
        updateAllWidgetsFromDb(context, getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_2x.class)), 1);
        updateAllWidgetsFromDb(context, getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_4x.class)), 2);
    }

    public static void updateOneWidget(int widgetId, int widgetType, NoteEntry entry) {
        NoteWidgetProvider widgetProvide;
        if (1 == widgetType) {
            widgetProvide = NoteWidgetProvider_2x.getInstance();
        } else {
            widgetProvide = NoteWidgetProvider_4x.getInstance();
        }
        widgetProvide.update(widgetId, entry);
    }

    public static void updateAllWidgets(NoteEntry entry) {
        Context context = NoteAppImpl.getContext();
        updateAllWidgets(getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_2x.class)), 1, entry);
        updateAllWidgets(getAppwidgetIds(new ComponentName(context, NoteWidgetProvider_4x.class)), 2, entry);
    }

    private static void updateAllWidgets(int[] appWidgetIds, int widgetType, NoteEntry entry) {
        if (appWidgetIds.length != 0) {
            for (int updateOneWidget : appWidgetIds) {
                updateOneWidget(updateOneWidget, widgetType, entry);
            }
        }
    }

    private static void updateAllWidgetsFromDb(final Context context, final int[] appWidgetIds, final int widgetType) {
        if (appWidgetIds.length != 0 && appWidgetIds.length > 0) {
            new Thread(new Runnable() {
                public void run() {
                    NoteEntry entry = WidgetUtil.getLatestNoteEntry(context);
                    for (int updateOneWidget : appWidgetIds) {
                        WidgetUtil.updateOneWidget(updateOneWidget, widgetType, entry);
                    }
                }
            }).start();
        }
    }

    public static int[] getAppwidgetIds(ComponentName componentName) {
        return AppWidgetManager.getInstance(NoteAppImpl.getContext()).getAppWidgetIds(componentName);
    }

    public static NoteEntry getLatestNoteEntry(Context context) {
        Cursor cursor = context.getContentResolver().query(NoteContent.CONTENT_URI, LocalNoteSet.NOTE_PROJECTION, null, null, "date_modified DESC");
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String content = cursor.getString(2);
                long dateModifiedInMs = cursor.getLong(5);
                long dateReminderInMs = cursor.getLong(6);
                NoteEntry entry = new NoteEntry();
                entry.title = title;
                entry.time = NoteUtils.formatDateTime(dateModifiedInMs, new SimpleDateFormat(NoteParser.DATE_FORMAT));
                entry.reminder = dateReminderInMs;
                entry.id = id;
                entry.timeMillis = dateModifiedInMs;
                new NoteParser().parseNoteContent(content, entry);
                return entry;
            }
            NoteUtils.closeSilently(cursor);
            return null;
        } finally {
            NoteUtils.closeSilently(cursor);
        }
    }
}
