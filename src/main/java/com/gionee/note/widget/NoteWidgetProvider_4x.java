package com.gionee.note.widget;

import com.gionee.aminote.R;
import com.gionee.note.app.SlidingWindow.NoteEntry;

public class NoteWidgetProvider_4x extends NoteWidgetProvider {
    private static NoteWidgetProvider_4x sNoteWidgetProvide;

    public static NoteWidgetProvider_4x getInstance() {
        if (sNoteWidgetProvide == null) {
            sNoteWidgetProvide = new NoteWidgetProvider_4x();
        }
        return sNoteWidgetProvide;
    }

    protected int getWidgetType() {
        return 2;
    }

    protected int getRemoteViewLayoutId(int mediaType) {
        return mediaType == 0 ? R.layout.widget_4x_image : R.layout.widget_4x_no_image;
    }

    protected boolean shouldDisplayImage(NoteEntry entry) {
        return entry != null && entry.mediaType == 0;
    }

    protected int[] getBackgroundBitmapSize() {
        return getBackgroundBitmapSize(R.dimen.widget_bg_widht_image_4x, R.dimen.widget_bg_height_image_4x);
    }
}
