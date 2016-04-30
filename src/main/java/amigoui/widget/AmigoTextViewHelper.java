package amigoui.widget;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.widget.TextView;

class AmigoTextViewHelper {
    AmigoTextViewHelper() {
    }

    public static void selectParagraph(TextView widget, float wx, float wy) {
        int stop;
        String text = widget.getText().toString();
        int offset = getOffset(widget, wx, wy);
        int offset1 = offset;
        int offset2 = offset;
        if (offset < text.length() && text.charAt(offset) == '\n') {
            offset1--;
        }
        int index = text.lastIndexOf(10, offset1);
        int start = index == -1 ? 0 : index + 1;
        index = text.indexOf(10, offset2);
        if (index == -1) {
            stop = text.length();
        } else {
            stop = index;
        }
        Selection.setSelection((Spannable) widget.getText(), start, stop);
    }

    public static int getLineNumber(TextView widget, float wy) {
        return widget.getLayout().getLineForVertical(Math.round(getVertical(widget, wy)));
    }

    public static int getOffsetByLine(TextView widget, int line, float wx) {
        return widget.getLayout().getOffsetForHorizontal(line, getHorizontal(widget, wx));
    }

    public static int getOffset(TextView widget, float wx, float wy) {
        return getOffsetByLine(widget, getLineNumber(widget, wy), wx);
    }

    public static CharSequence getLineText(TextView widget, float wy) {
        int line = getLineNumber(widget, wy);
        Layout layout = widget.getLayout();
        return layout.getText().subSequence(layout.getLineStart(line), layout.getLineEnd(line));
    }

    private static float getHorizontal(TextView widget, float wx) {
        float x = wx - ((float) widget.getTotalPaddingLeft());
        if (x < 0.0f) {
            x = 0.0f;
        } else if (x >= ((float) (widget.getWidth() - widget.getTotalPaddingRight()))) {
            x = (float) ((widget.getWidth() - widget.getTotalPaddingRight()) - 1);
        }
        return x + ((float) widget.getScrollX());
    }

    private static float getVertical(TextView widget, float wy) {
        float y = wy - ((float) widget.getTotalPaddingTop());
        if (y < 0.0f) {
            y = 0.0f;
        } else if (y >= ((float) (widget.getHeight() - widget.getTotalPaddingBottom()))) {
            y = (float) ((widget.getHeight() - widget.getTotalPaddingBottom()) - 1);
        }
        return y + ((float) widget.getScrollY());
    }
}
