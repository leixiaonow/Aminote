package com.gionee.note.widget;

import com.gionee.aminote.R;
import com.gionee.note.app.SlidingWindow.NoteEntry;

public class NoteWidgetProvider_2x extends NoteWidgetProvider {
    private static NoteWidgetProvider_2x sNoteWidgetProvide;

    public static NoteWidgetProvider_2x getInstance() {
        if (sNoteWidgetProvide == null) {
            sNoteWidgetProvide = new NoteWidgetProvider_2x();
        }
        return sNoteWidgetProvide;
    }

    protected int getWidgetType() {
        return 1;
    }

    protected int getRemoteViewLayoutId(int mediaType) {
        return R.layout.widget_2x;
    }

    protected boolean shouldDisplayImage(NoteEntry entry) {
        return false;
    }

    protected int[] getBackgroundBitmapSize() {
        return getBackgroundBitmapSize(R.dimen.widget_bg_widht_2x, R.dimen.widget_bg_height_2x);
    }
}
