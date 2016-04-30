package com.gionee.note.app.span;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.Config.EditPage;

public class BillImageSpan extends ReplacementSpan implements AbstractClickSpan {
    private static final String TAG = "BillImageSpan";
    private int LINE_PADDING_BOTTOM;
    private int LINE_PADDING_TOP;
    private final BillItem mBillItem;
    private TextPaint mBluePaint;
    private Drawable mCheckedDrawable = null;
    private final Context mContext;
    private int mImageWidth;
    private TextPaint mRedPaint;
    private Drawable mUncheckedDrawable = null;

    public BillImageSpan(Context context, BillItem item) {
        this.mContext = context;
        this.mBillItem = item;
        init();
    }

    private void init() {
        this.mImageWidth = EditPage.get(this.mContext).mBillWidth;
        this.LINE_PADDING_TOP = this.mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_top);
        this.LINE_PADDING_BOTTOM = this.mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);
        this.mBluePaint = new TextPaint();
        this.mBluePaint.setColor(-16776961);
        this.mRedPaint = new TextPaint();
        this.mRedPaint.setColor(-65536);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        Rect rect = getDrawable().getBounds();
        FontMetricsInt fmi = new FontMetricsInt();
        if (fm != null) {
            fm.ascent = fmi.ascent;
            fm.descent = fmi.descent;
            fm.top = fmi.top;
            fm.bottom = fmi.bottom;
        }
        return rect.right;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int baseLine, int bottom, Paint paint) {
        Drawable drawable = getDrawable();
        int visualTop = top - this.LINE_PADDING_TOP;
        int transY = visualTop + ((((baseLine + this.LINE_PADDING_BOTTOM) - visualTop) - drawable.getBounds().height()) / 2);
        canvas.translate(x, (float) transY);
        drawable.draw(canvas);
        canvas.translate(-x, (float) (-transY));
    }

    public Drawable getDrawable() {
        if (this.mBillItem.isChecked()) {
            if (this.mCheckedDrawable == null) {
                this.mCheckedDrawable = decodeDrawable(this.mContext, R.drawable.bill_complete);
            }
            return this.mCheckedDrawable;
        }
        if (this.mUncheckedDrawable == null) {
            this.mUncheckedDrawable = decodeDrawable(this.mContext, R.drawable.bill_uncomplete);
        }
        return this.mUncheckedDrawable;
    }

    public static Drawable decodeDrawable(Context context, int resId) {
        try {
            Drawable drawable = ContextCompat.getDrawable(context, resId);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        } catch (Exception e) {
            Logger.printLog(TAG, "Unable to find resource: " + resId);
            return null;
        }
    }

    public void onClick(View view) {
        if (this.mBillItem != null) {
            this.mBillItem.onBillClick(view);
        }
    }

    public boolean isClickValid(TextView widget, MotionEvent event, int lineBottom) {
        int paddingLeft = widget.getTotalPaddingLeft();
        int clickX = (int) event.getX();
        return clickX >= paddingLeft && clickX <= paddingLeft + this.mImageWidth && ((int) event.getY()) < lineBottom;
    }
}
