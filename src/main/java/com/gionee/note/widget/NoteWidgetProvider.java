package com.gionee.note.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.gionee.aminote.R;
import com.gionee.note.app.Config.WidgetPage;
import com.gionee.note.app.NewNoteActivity;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.SlidingWindow.NoteEntry;
import com.gionee.note.app.effect.DrawableManager;
import com.gionee.note.app.effect.EffectUtil;
import com.gionee.note.common.ThumbnailDecodeProcess;
import com.gionee.note.common.ThumbnailDecodeProcess.ThumbnailDecodeMode;

public abstract class NoteWidgetProvider extends AppWidgetProvider {
    public static final int WIDGET_TYPE_2X = 1;
    public static final int WIDGET_TYPE_4X = 2;
    private Object mLock = new Object();
    private Handler mMainHandler;
    private Handler mThreadHandle;

    protected abstract int[] getBackgroundBitmapSize();

    protected abstract int getRemoteViewLayoutId(int i);

    protected abstract int getWidgetType();

    protected abstract boolean shouldDisplayImage(NoteEntry noteEntry);

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (final int widgetId : appWidgetIds) {
            getThreadHandle().post(new Runnable() {
                public void run() {
                    final NoteEntry entry = WidgetUtil.getLatestNoteEntry(NoteAppImpl.getContext());
                    NoteWidgetProvider.this.getMainHandler().post(new Runnable() {
                        public void run() {
                            NoteWidgetProvider.this.update(widgetId, entry);
                        }
                    });
                }
            });
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public void update(int widgetId, NoteEntry entry) {
        if (entry == null) {
            updateDefaultWidget(widgetId);
        } else {
            updateWidget(widgetId, entry);
        }
    }

    public void updateDefaultWidget(int appWidgetId) {
        Context context = NoteAppImpl.getContext();
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), getRemoteViewLayoutId(-1));
        setTitleAndAlarmState(remoteView, null);
        remoteView.setTextViewText(R.id.widget_content, "");
        remoteView.setTextViewText(R.id.widget_time, "");
        setWidgetBackground(remoteView, System.currentTimeMillis());
        remoteView.setOnClickPendingIntent(R.id.widget_layout, getPendingIntent(appWidgetId, -1));
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteView);
    }

    private void setTitleAndAlarmState(RemoteViews remoteView, NoteEntry entry) {
        int i = 8;
        if (shouldHideTitleLayout(entry)) {
            remoteView.setViewVisibility(R.id.widget_title, 8);
            remoteView.setViewVisibility(R.id.widget_alarm, 8);
            remoteView.setViewVisibility(R.id.title_layout, 8);
            return;
        }
        remoteView.setViewVisibility(R.id.title_layout, 0);
        remoteView.setViewVisibility(R.id.widget_title, 0);
        if (0 != entry.reminder) {
            i = 0;
        }
        remoteView.setViewVisibility(R.id.widget_alarm, i);
        remoteView.setImageViewResource(R.id.widget_alarm, R.drawable.note_item_reminder);
        remoteView.setTextViewText(R.id.widget_title, entry.title);
    }

    private boolean shouldHideTitleLayout(NoteEntry entry) {
        return entry == null || (TextUtils.isEmpty(entry.title) && 0 == entry.reminder);
    }

    protected Bitmap getDisplayBitmap(Context context, NoteEntry entry) {
        int width = WidgetPage.getInstance(context).mWidth;
        int height = WidgetPage.getInstance(context).mHeight;
        Bitmap bitmap = new ThumbnailDecodeProcess(context, entry.thumbnailUri, width, height, ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT).getThumbnail();
        if (bitmap != null) {
            return bitmap;
        }
        return new ThumbnailDecodeProcess(context, entry.originUri, width, height, ThumbnailDecodeMode.CUT_WIDTH_AND_HEIGHT).getThumbnail();
    }

    protected PendingIntent getPendingIntent(int appWidgetId, int noteId) {
        Intent intent = new Intent();
        intent.setClass(NoteAppImpl.getContext(), NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, "/" + noteId);
        return PendingIntent.getActivity(NoteAppImpl.getContext(), appWidgetId, intent, 134217728);
    }

    public void updateWidget(final int appWidgetId, final NoteEntry entry) {
        getThreadHandle().post(new Runnable() {
            public void run() {
                Context context = NoteAppImpl.getContext();
                RemoteViews remoteView = new RemoteViews(context.getPackageName(), NoteWidgetProvider.this.getRemoteViewLayoutId(entry.mediaType));
                NoteWidgetProvider.this.setTitleAndAlarmState(remoteView, entry);
                remoteView.setTextViewText(R.id.widget_content, entry.content);
                remoteView.setTextViewText(R.id.widget_time, entry.time);
                NoteWidgetProvider.this.setWidgetBackground(remoteView, entry.timeMillis);
                if (NoteWidgetProvider.this.shouldDisplayImage(entry)) {
                    Bitmap bitmap = NoteWidgetProvider.this.getDisplayBitmap(context, entry);
                    if (bitmap != null) {
                        remoteView.setImageViewBitmap(R.id.widget_photo, bitmap);
                    } else {
                        remoteView.setImageViewResource(R.id.widget_photo, R.drawable.image_span_default_drawable);
                    }
                }
                remoteView.setOnClickPendingIntent(R.id.widget_layout, NoteWidgetProvider.this.getPendingIntent(appWidgetId, entry.id));
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteView);
            }
        });
    }

    protected final void setWidgetBackground(RemoteViews remoteViews, long noteModifiedTime) {
        int effect = new EffectUtil(System.currentTimeMillis()).getEffect(noteModifiedTime);
        int[] size = getBackgroundBitmapSize();
        remoteViews.setImageViewBitmap(R.id.widget_backgroud, DrawableManager.getWidgetEffectBitmap(NoteAppImpl.getContext(), getWidgetType(), effect, size[0], size[1]));
    }

    protected int[] getBackgroundBitmapSize(int widthResId, int heightResId) {
        int[] size = new int[2];
        int width = NoteAppImpl.getContext().getResources().getDimensionPixelOffset(widthResId);
        int height = NoteAppImpl.getContext().getResources().getDimensionPixelOffset(heightResId);
        size[0] = width;
        size[1] = height;
        return size;
    }

    protected final Handler getThreadHandle() {
        Handler handler;
        synchronized (this.mLock) {
            if (this.mThreadHandle == null) {
                this.mThreadHandle = new Handler(NoteAppImpl.getContext().getSaveNoteDataLooper());
            }
            handler = this.mThreadHandle;
        }
        return handler;
    }

    protected final Handler getMainHandler() {
        Handler handler;
        synchronized (this.mLock) {
            if (this.mMainHandler == null) {
                this.mMainHandler = new Handler(Looper.getMainLooper());
            }
            handler = this.mMainHandler;
        }
        return handler;
    }
}
