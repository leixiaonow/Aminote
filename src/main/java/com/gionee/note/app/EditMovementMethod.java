package com.gionee.note.app;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;
import com.gionee.note.app.span.AbstractClickSpan;

public class EditMovementMethod extends ScrollingMovementMethod {
    private static final int MOVE_DISTANCE = 5;
    private NewNoteActivity mActivity;
    private int mDownX;
    private int mDownY;

    public EditMovementMethod(NewNoteActivity activity) {
        this.mActivity = activity;
    }

    private boolean isMoved(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        return ((int) Math.sqrt((double) (((x - this.mDownX) * (x - this.mDownX)) + ((y - this.mDownY) * (y - this.mDownY))))) > 5;
    }

    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getActionMasked();
        if (action == 0) {
            this.mDownX = (int) event.getX();
            this.mDownY = (int) event.getY();
            this.mActivity.hideSoftInput();
        }
        if (action == 1 || action == 0) {
            int x = (((int) event.getX()) - widget.getTotalPaddingLeft()) + widget.getScrollX();
            int y = (((int) event.getY()) - widget.getTotalPaddingTop()) + widget.getScrollY();
            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, (float) x);
            int lineBottom = layout.getLineBottom(line);
            AbstractClickSpan[] link = (AbstractClickSpan[]) buffer.getSpans(off, off, AbstractClickSpan.class);
            if (action == 1) {
                if (link.length == 0 || isMoved(event) || !link[0].isClickValid(widget, event, lineBottom)) {
                    this.mActivity.enterEditMode();
                } else {
                    link[0].onClick(widget);
                    return true;
                }
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    public boolean canSelectArbitrarily() {
        return true;
    }
}
