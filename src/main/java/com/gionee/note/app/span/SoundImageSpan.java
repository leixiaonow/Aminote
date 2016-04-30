package com.gionee.note.app.span;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.note.app.Config.EditPage;
import com.gionee.note.app.Config.SoundImageSpanConfig;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.attachment.SoundPlayer;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.note.app.span.JsonableSpan.Applyer;
import com.gionee.note.common.BitmapUtils;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class SoundImageSpan extends ReplacementSpan implements AbstractClickSpan, JsonableSpan, OnlyImageSpan {
    public static final Applyer<SoundImageSpan> APPLYER = new Applyer<SoundImageSpan>() {
        public SoundImageSpan applyFromJson(JSONObject json, SpannableStringBuilder builder, Context context) throws JSONException {
            int start = json.getInt(DataConvert.SPAN_ITEM_START);
            int end = json.getInt(DataConvert.SPAN_ITEM_END);
            int flag = json.getInt(DataConvert.SPAN_ITEM_FLAG);
            SoundImageSpan span = new SoundImageSpan(context, builder, json.getString(SoundImageSpan.ORIGIN_PATH), json.getInt(SoundImageSpan.SOUND_DURATION));
            builder.setSpan(span, start, end, flag);
            span.initSpan(start);
            return span;
        }
    };
    private static final SoundImageSpan[] EMPTY_ITEM = new SoundImageSpan[0];
    public static final String ORIGIN_PATH = "origin_path";
    public static final String SOUND_DURATION = "duration";
    private int DOT_COLOR = 0;
    private int PLAY_ICON_X = 0;
    private Context mContext;
    private int mDotCircleRadius = 0;
    private int mDotCircleX = 0;
    private int mDotCircleY = 0;
    private int mDurationInSec;
    private int mImageShiftSize = 0;
    private String mOriginPath;
    private Rect mRect = new Rect();
    private Drawable mSavedDrawable;
    private int mSoundHeight = 0;
    private int mSoundWidth = 0;
    private SoundSpanWatcher mSpanWatcher;
    private SpannableStringBuilder mText;
    private int mTextAccent;
    private int mTextColor = 0;
    private int mTextLeftMargin = 0;
    private float mTextSize = 0.0f;

    private class SoundSpanWatcher implements SpanWatcher {
        private SoundSpanWatcher() {
        }

        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            if (what == SoundImageSpan.this) {
                checkDeleteRedundancySoundTag(text, start, end);
                if (SoundImageSpan.this.mSpanWatcher != null) {
                    SoundImageSpan.this.mText.removeSpan(SoundImageSpan.this.mSpanWatcher);
                    SoundImageSpan.this.mSpanWatcher = null;
                }
                NoteUtils.deleteSoundFile(SoundImageSpan.this.mOriginPath);
            }
        }

        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
        }

        private void checkDeleteRedundancySoundTag(Spannable spanText, int start, int end) {
            int redundancySoundTagLength = Constants.MEDIA_SOUND.length() - 1;
            if (redundancySoundTagLength == end - start) {
                String redundancySoundTag = Constants.MEDIA_SOUND.substring(0, redundancySoundTagLength);
                String text = spanText.toString();
                if (!TextUtils.isEmpty(text) && text.length() >= end && redundancySoundTag.equals(text.substring(start, end))) {
                    SoundImageSpan.this.mText.delete(start, end);
                }
            }
        }
    }

    public SoundImageSpan(Context context, SpannableStringBuilder builder, String originPath, int durationInSec) {
        this.mContext = context;
        this.mText = builder;
        this.mOriginPath = originPath;
        this.mDurationInSec = durationInSec;
        this.mSpanWatcher = new SoundSpanWatcher();
        EditPage page = EditPage.get(context);
        SoundImageSpanConfig soundImageSpanConfig = SoundImageSpanConfig.get(context);
        this.mSoundWidth = page.mSoundWidth;
        this.mSoundHeight = page.mSoundHeight;
        this.mImageShiftSize = soundImageSpanConfig.mImageShiftSize;
        this.mDotCircleX = soundImageSpanConfig.mDotCircleX;
        this.mDotCircleY = soundImageSpanConfig.mDotCircleY;
        this.mDotCircleRadius = soundImageSpanConfig.mDotCircleRadius;
        this.mTextColor = soundImageSpanConfig.mTextColor;
        this.DOT_COLOR = soundImageSpanConfig.mDotColor;
        this.mTextSize = soundImageSpanConfig.mTextSize;
        this.mTextLeftMargin = soundImageSpanConfig.mTextLeftMargin;
        this.mRect.set(0, 0, this.mSoundWidth, this.mSoundHeight);
        this.mTextAccent = BitmapUtils.getTextPaint(context.getResources().getDimensionPixelSize(R.dimen.edit_note_content_text_size), -7829368, false).getFontMetricsInt().ascent;
    }

    public static SoundImageSpan[] get(SpannableStringBuilder text, int start, int end) {
        SoundImageSpan[] items = (SoundImageSpan[]) text.getSpans(start, end, SoundImageSpan.class);
        if (items.length != 1) {
            return items;
        }
        SoundImageSpan item = items[0];
        if (text.toString().startsWith(Constants.MEDIA_SOUND, text.getSpanStart(item))) {
            return items;
        }
        item.removeSoundImageSpan();
        return EMPTY_ITEM;
    }

    public void adjustCursorIfInvalid(int currSelection) {
        if (this.mText != null) {
            int start = this.mText.getSpanStart(this);
            if (currSelection == start) {
                Selection.setSelection(this.mText, start + Constants.MEDIA_SOUND.length());
            }
        }
    }

    public void updateSpanEditableText(SpannableStringBuilder stringBuilder) {
        if (this.mText != stringBuilder) {
            this.mText = stringBuilder;
        }
    }

    public void initSpan(int spanStart) {
        setSpanWatcher(spanStart);
    }

    private void setSpanWatcher(int spanStart) {
        this.mText.setSpan(this.mSpanWatcher, spanStart, Constants.MEDIA_SOUND.length() + spanStart, 33);
    }

    private void removeSoundImageSpan() {
        this.mText.removeSpan(this);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        Rect rect = getRect();
        if (fm != null) {
            fm.ascent = (-rect.bottom) - this.mTextAccent;
            fm.descent = 0;
            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return rect.right;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int baseLine, int bottom, Paint paint) {
        int lastLinePos = (baseLine - (bottom - top)) + this.mImageShiftSize;
        int nextLinePos = baseLine + this.mImageShiftSize;
        canvas.save();
        int transY = lastLinePos + (((nextLinePos - lastLinePos) - getRect().height()) / 2);
        if (transY < 0) {
            transY = 0;
        }
        canvas.translate(x, (float) transY);
        drawSoundSpan(canvas, paint);
        drawPlayIcon(canvas);
        canvas.restore();
    }

    private void drawSoundSpan(Canvas canvas, Paint paint) {
        Style defStyle = paint.getStyle();
        paint.setStyle(Style.FILL);
        paint.setColor(-1);
        Rect r = getRect();
        canvas.drawRect(r, paint);
        paint.setColor(this.DOT_COLOR);
        canvas.drawCircle((float) this.mDotCircleX, (float) this.mDotCircleY, (float) this.mDotCircleRadius, paint);
        String str = NoteUtils.formatTime(this.mDurationInSec, DataUpgrade.SPLIT);
        int baseX = this.mTextLeftMargin;
        int baseY = (int) (((float) (r.height() / 2)) - ((paint.ascent() + paint.descent()) / 2.0f));
        paint.setColor(this.mTextColor);
        paint.setTextSize(this.mTextSize);
        paint.setStyle(defStyle);
        canvas.drawText(str, (float) baseX, (float) baseY, paint);
    }

    private void drawPlayIcon(Canvas canvas) {
        Drawable drawable = getPlayDrawable();
        this.PLAY_ICON_X = NoteAppImpl.getContext().getResources().getDimensionPixelSize(R.dimen.record_play_margin_left);
        canvas.translate((float) this.PLAY_ICON_X, (float) ((getRect().height() / 2) - (drawable.getBounds().height() / 2)));
        drawable.draw(canvas);
    }

    private Rect getRect() {
        return this.mRect;
    }

    private Drawable getPlayDrawable() {
        if (this.mSavedDrawable == null) {
            this.mSavedDrawable = ContextCompat.getDrawable(NoteAppImpl.getContext(), R.drawable.sound_play_icon);
            this.mSavedDrawable.setBounds(0, 0, this.mSavedDrawable.getIntrinsicWidth(), this.mSavedDrawable.getIntrinsicHeight());
        }
        return this.mSavedDrawable;
    }

    public void onClick(View view) {
        if (this.mContext != null && !NoteUtils.fileNotFound(this.mContext, this.mOriginPath)) {
            new SoundPlayer(this.mContext).launchPlayer(this.mOriginPath, this.mDurationInSec);
        }
    }

    public boolean isClickValid(TextView widget, MotionEvent event, int lineBottom) {
        int paddingLeft = widget.getTotalPaddingLeft();
        int clickX = (int) event.getX();
        return clickX >= paddingLeft + 1 && clickX <= (this.mSoundWidth + paddingLeft) - 1 && ((int) event.getY()) < lineBottom;
    }

    public void writeToJson(JSONObject jsonObject) throws JSONException {
        int start = this.mText.getSpanStart(this);
        int end = this.mText.getSpanEnd(this);
        int flags = this.mText.getSpanFlags(this);
        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, flags);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, SoundImageSpan.class.getName());
        jsonObject.put(ORIGIN_PATH, this.mOriginPath);
        jsonObject.put(SOUND_DURATION, this.mDurationInSec);
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
