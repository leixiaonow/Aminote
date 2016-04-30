package com.gionee.note.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class MultiTextView extends TextView {
    public MultiTextView(Context context) {
        this(context, null);
    }

    public MultiTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842884);
    }

    public MultiTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onDraw(Canvas canvas) {
        Layout layout = getLayout();
        Paint paint = getPaint();
        paint.setAlpha(128);
        CharSequence text = getText();
        float width = (float) getWidth();
        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        int lineHeight = getLineHeight();
        int lineCount = getLineCount();
        int paddingTop = getPaddingTop();
        int i = 0;
        while (i < lineCount) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            int baseline = layout.getLineBaseline(i);
            CharSequence line = TextUtils.substring(text, lineStart, lineEnd);
            int nextLine = i + 1;
            int nextLineBottom = paddingTop + ((nextLine + 1) * lineHeight);
            if (nextLine >= lineCount || nextLineBottom <= height) {
                canvas.drawText(line.toString(), (float) paddingLeft, (float) baseline, paint);
                i++;
            } else {
                canvas.drawText(TextUtils.ellipsize(TextUtils.substring(text, lineStart, text.length()), paint, width, TruncateAt.END).toString(), (float) paddingLeft, (float) baseline, paint);
                return;
            }
        }
    }
}
