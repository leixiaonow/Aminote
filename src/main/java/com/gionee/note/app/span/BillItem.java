package com.gionee.note.app.span;

import android.content.Context;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ParagraphStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.WrapTogetherSpan;
import android.view.View;
import com.gionee.note.app.Config.EditPage;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.span.JsonableSpan.Applyer;
import com.gionee.note.app.utils.EditUtils;
import com.gionee.note.app.view.NoteContentEditText;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class BillItem implements ParagraphStyle, WrapTogetherSpan, JsonableSpan {
    public static final Applyer<BillItem> APPLYER = new Applyer<BillItem>() {
        public BillItem applyFromJson(JSONObject json, SpannableStringBuilder builder, Context context) throws JSONException {
            int start = json.getInt(DataConvert.SPAN_ITEM_START);
            int end = json.getInt(DataConvert.SPAN_ITEM_END);
            int flag = json.getInt(DataConvert.SPAN_ITEM_FLAG);
            boolean checked = json.getBoolean(BillItem.CHECKED_KEY);
            BillItem item = new BillItem(context, builder);
            item.init(checked, start, end);
            builder.setSpan(item, start, end, flag);
            return item;
        }
    };
    public static final String CHECKED_KEY = "checked";
    private static final BillItem[] EMPTY_ITEM = new BillItem[0];
    private static final String TAG = "BillItem";
    private int mBillForegroundColor;
    private boolean mChanged = false;
    private boolean mChecked = false;
    private Context mContext;
    private ForegroundColorSpan mForegroundColorSpan;
    private OnImageSpanChangeListener mListener;
    private BillSpanWatcher mSpanWatcher;
    private StrikethroughSpan mStrikethroughSpan;
    private SpannableStringBuilder mText;

    private class BillSpanWatcher implements SpanWatcher {
        private final BillItem mItem;

        BillSpanWatcher(BillItem item) {
            this.mItem = item;
        }

        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            if (what instanceof BillImageSpan) {
                checkDeleteRedundancyBillTag(text, start, end);
                this.mItem.removeAuxSpans();
            }
        }

        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        }

        private void checkDeleteRedundancyBillTag(Spannable spanText, int start, int end) {
            int redundancyBillTagLength = Constants.MEDIA_BILL.length() - 1;
            if (redundancyBillTagLength == end - start) {
                String redundancyBillTag = Constants.MEDIA_BILL.substring(0, redundancyBillTagLength);
                String text = spanText.toString();
                if (!TextUtils.isEmpty(text) && text.length() >= end && redundancyBillTag.equals(text.substring(start, end))) {
                    BillItem.this.mText.delete(start, end);
                }
            }
        }
    }

    public BillItem(Context context, SpannableStringBuilder text) {
        this.mContext = context;
        this.mText = text;
        this.mBillForegroundColor = EditPage.get(context).mBillForegroundColor;
    }

    public void init(boolean checked, int pStart, int pEnd) {
        this.mChecked = checked;
        initSpans(pStart, pEnd);
    }

    private void initSpans(int start, int end) {
        this.mText.setSpan(this, start, end, 18);
        EditUtils.insertBillImageSpan(this.mText, new BillImageSpan(this.mContext, this), start);
        end = this.mText.getSpanEnd(this);
        if (this.mChecked) {
            setStrikethroughSpan(start, end, 18);
        }
        setBillSpanWatcher(start, end, 18);
    }

    public void setOnImageSpanChangeListener(OnImageSpanChangeListener listener) {
        this.mListener = listener;
    }

    public void updateSpanEditableText(SpannableStringBuilder stringBuilder) {
        if (this.mText != stringBuilder) {
            this.mText = stringBuilder;
            setBillSpanWatcher(this.mText.getSpanStart(this), this.mText.getSpanEnd(this), 18);
        }
    }

    public void destroy() {
        int start = this.mText.getSpanStart(this);
        if (this.mText.toString().startsWith(Constants.MEDIA_BILL, start)) {
            this.mText.delete(start, Constants.MEDIA_BILL.length() + start);
        } else {
            removeAuxSpans();
        }
    }

    public void onBillClick(View widget) {
        this.mChecked = !this.mChecked;
        toggleStrikethroughSpan();
        setChanged(true);
        if (widget instanceof NoteContentEditText) {
            ((NoteContentEditText) widget).shouldFixCursor(this.mText.getSpanEnd(this));
        }
        if (this.mListener != null) {
            this.mListener.onImageChanged();
        }
    }

    public static BillItem[] get(SpannableStringBuilder text, int start, int end) {
        BillItem[] items = (BillItem[]) text.getSpans(start, end, BillItem.class);
        if (items.length != 1) {
            return items;
        }
        BillItem item = items[0];
        if (text.toString().startsWith(Constants.MEDIA_BILL, text.getSpanStart(item))) {
            return items;
        }
        item.removeBillItem();
        return EMPTY_ITEM;
    }

    public void removeBillItem() {
        removeImageSpan();
    }

    public void adjustRange(int newStart, int newEnd) {
        SpannableStringBuilder text = this.mText;
        text.setSpan(this, newStart, newEnd, 18);
        if (this.mStrikethroughSpan != null) {
            text.setSpan(this.mStrikethroughSpan, newStart, newEnd, 18);
        }
        if (this.mForegroundColorSpan != null) {
            text.setSpan(this.mForegroundColorSpan, newStart, newEnd, 18);
        }
        text.setSpan(this.mSpanWatcher, newStart, newEnd, 18);
    }

    public void adjustCursorIfInvalid(int currSelection) {
        if (this.mText != null) {
            int start = this.mText.getSpanStart(this);
            int billImageSpanEnd = start + Constants.MEDIA_BILL.length();
            if (currSelection >= start && currSelection < billImageSpanEnd) {
                Selection.setSelection(this.mText, billImageSpanEnd);
            }
        }
    }

    private void setBillSpanWatcher(int start, int end, int flags) {
        if (this.mSpanWatcher == null) {
            this.mSpanWatcher = new BillSpanWatcher(this);
        }
        this.mText.setSpan(this.mSpanWatcher, start, end, flags);
    }

    private void toggleStrikethroughSpan() {
        if (this.mChecked) {
            setStrikethroughSpan(this.mText.getSpanStart(this), this.mText.getSpanEnd(this), this.mText.getSpanFlags(this));
        } else {
            removeStrikethroughSpan();
        }
    }

    private void setStrikethroughSpan(int start, int end, int flags) {
        boolean z;
        boolean z2 = true;
        if (this.mStrikethroughSpan == null) {
            z = true;
        } else {
            z = false;
        }
        NoteUtils.assertTrue(z);
        StrikethroughSpan strikeSpan = new StrikethroughSpan();
        this.mText.setSpan(strikeSpan, start, end, flags);
        this.mStrikethroughSpan = strikeSpan;
        if (this.mForegroundColorSpan != null) {
            z2 = false;
        }
        NoteUtils.assertTrue(z2);
        ForegroundColorSpan foregroundSpan = new ForegroundColorSpan(this.mBillForegroundColor);
        this.mText.setSpan(foregroundSpan, start, end, flags);
        this.mForegroundColorSpan = foregroundSpan;
    }

    private void removeStrikethroughSpan() {
        if (this.mStrikethroughSpan != null) {
            this.mText.removeSpan(this.mStrikethroughSpan);
            this.mStrikethroughSpan = null;
        }
        if (this.mForegroundColorSpan != null) {
            this.mText.removeSpan(this.mForegroundColorSpan);
            this.mForegroundColorSpan = null;
        }
    }

    private void removeImageSpan() {
        for (BillImageSpan span : (BillImageSpan[]) this.mText.getSpans(this.mText.getSpanStart(this), this.mText.getSpanEnd(this), BillImageSpan.class)) {
            this.mText.removeSpan(span);
        }
    }

    private void removeAuxSpans() {
        boolean z = true;
        this.mText.removeSpan(this.mSpanWatcher);
        this.mText.removeSpan(this);
        if (this.mChecked) {
            boolean z2;
            if (this.mStrikethroughSpan != null) {
                z2 = true;
            } else {
                z2 = false;
            }
            NoteUtils.assertTrue(z2);
            if (this.mForegroundColorSpan == null) {
                z = false;
            }
            NoteUtils.assertTrue(z);
            removeStrikethroughSpan();
        }
    }

    public void setChanged(boolean changed) {
        synchronized (this) {
            this.mChanged = changed;
        }
    }

    public boolean getAndResetChanged() {
        boolean changed;
        synchronized (this) {
            changed = this.mChanged;
            this.mChanged = false;
        }
        return changed;
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public void writeToJson(JSONObject jsonObject) throws JSONException {
        int start = this.mText.getSpanStart(this);
        int end = this.mText.getSpanEnd(this);
        int flags = this.mText.getSpanFlags(this);
        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, flags);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, BillItem.class.getName());
        jsonObject.put(CHECKED_KEY, this.mChecked);
    }

    public void recycle() {
        if (this.mSpanWatcher != null) {
            this.mText.removeSpan(this.mSpanWatcher);
            this.mSpanWatcher = null;
        }
        this.mText = null;
        this.mContext = null;
    }
}
