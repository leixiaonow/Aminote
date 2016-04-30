package com.gionee.note.app.view;

import amigoui.widget.AmigoEditText;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.framework.utils.StringUtils;
import com.gionee.note.app.Config.EditPage;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.span.BillItem;
import com.gionee.note.app.span.JsonableSpan;
import com.gionee.note.app.span.OnImageSpanChangeListener;
import com.gionee.note.app.span.OnlyImageSpan;
import com.gionee.note.app.span.PhotoImageSpan;
import com.gionee.note.app.span.SoundImageSpan;
import com.gionee.note.app.utils.EditUtils;
import com.gionee.note.common.BitmapUtils;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.data.NoteParser;
import com.gionee.note.provider.NoteShareDataManager;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import uk.co.senab.photoview.IPhotoView;

public class NoteContentEditText extends AmigoEditText implements OnImageSpanChangeListener {
    private static final int DEFAULT_MAX_CONTENT_SIZE = 10000;
    private static final double DEFAULT_MAX_CONTENT_SIZE_IN_M = 524288.0d;
    private static final SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static int LINE_PADDING_BOTTOM = 0;
    private static int LINE_PADDING_TOP = 0;
    private static final String TAG = "NoteContentEditText";
    private OnPrimaryClipChangedListener mClipDataChangedListener;
    private Context mContext;
    private float mCustomerSpacingAdd;
    private boolean mHasDrawAmiTag;
    private Paint mLinePaint;
    private boolean mLocked;
    private double mMaxContentSize;
    private int mNoteSignatureWidth;
    private Paint mNoteTimePaint;
    private int mNoteTimeWidth;
    private int mPaddingBottomNoSignature;
    private int mPaddingButtomSignature;
    private int mReacheMaxLengthCharacterCount;
    private String mReminder;
    private Drawable mReminderDrawable;
    private Paint mReminderPaint;
    private int mSelection;
    private boolean mShouldFixCursor;
    private String mShowTime;
    private String mSignature;
    private Paint mSignaturePaint;
    private Drawable mTagDrawable;
    private SpannableStringBuilder mText;
    private boolean mTextChanged;
    private TextWatcher mTextWatcher;

    public NoteContentEditText(Context context) {
        this(context, null);
    }

    public NoteContentEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 16842862);
    }

    public NoteContentEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public NoteContentEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTextChanged = false;
        this.mShouldFixCursor = false;
        this.mLocked = false;
        this.mHasDrawAmiTag = false;
        init(context);
        setSaveEnabled(false);
        adjustViewSizeBySignature();
    }

    private void init(Context context) {
        this.mTagDrawable = ContextCompat.getDrawable(context, R.drawable.ami_share_content_tag);
        if (VERSION.SDK_INT >= 21) {
            setElegantTextHeight(true);
        }
        this.mContext = context;
        this.mText = (SpannableStringBuilder) getText();
        LINE_PADDING_TOP = this.mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_top);
        LINE_PADDING_BOTTOM = this.mContext.getResources().getDimensionPixelSize(R.dimen.edit_note_content_line_padding_bottom);
        this.mCustomerSpacingAdd = (float) (LINE_PADDING_BOTTOM + LINE_PADDING_TOP);
        this.mMaxContentSize = (double) (Resources.getSystem().getInteger(getResources().getIdentifier("config_cursorWindowSize", "integer", "android")) * 1024);
        setLineSpacing(this.mCustomerSpacingAdd, IPhotoView.DEFAULT_MIN_SCALE);
        initPaint();
        initClipDataListener();
        this.mSignature = getSignatureText();
        this.mPaddingBottomNoSignature = (int) this.mContext.getResources().getDimension(R.dimen.edit_note_content_padding_bottom);
        this.mPaddingButtomSignature = (int) this.mContext.getResources().getDimension(R.dimen.edit_note_content_padding_bottom_signal);
        this.mNoteSignatureWidth = (int) Math.ceil((double) this.mNoteTimePaint.measureText(this.mSignature));
    }

    public void adjustViewSizeBySignature() {
        if (TextUtils.isEmpty(this.mSignature)) {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), this.mPaddingBottomNoSignature);
        } else {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), this.mPaddingButtomSignature);
        }
    }

    private void initPaint() {
        this.mLinePaint = new Paint(getPaint());
        this.mLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.note_edit_text_line_color));
        this.mLinePaint.setAntiAlias(false);
        this.mLinePaint.setStyle(Style.STROKE);
        this.mLinePaint.setStrokeWidth(0.0f);
        EditPage page = EditPage.get(getContext());
        this.mNoteTimePaint = BitmapUtils.getTextPaint(page.mTimeSize, page.mTimeColor, false);
        this.mReminderPaint = BitmapUtils.getTextPaint(page.mReminderSize, page.mReminderColor, true);
        this.mSignaturePaint = BitmapUtils.getTextPaint(page.mSignatureSize, page.mSignatureColor, false);
    }

    public void initClipDataListener() {
        final ClipboardManager clip = (ClipboardManager) NoteAppImpl.getContext().getSystemService("clipboard");
        this.mClipDataChangedListener = new OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                ClipData clipData = clip.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    CharSequence text = clipData.getItemAt(0).getText();
                    if (text != null && !(text instanceof String)) {
                        clip.setPrimaryClip(ClipData.newPlainText(null, NoteParser.replaceMediaString(text.toString())));
                    }
                }
            }
        };
        clip.addPrimaryClipChangedListener(this.mClipDataChangedListener);
    }

    public void initWatcher(final View shareView, final View deleteView) {
        this.mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (NoteContentEditText.this.checkInputContentExceedMaxSize(s)) {
                    NoteContentEditText.this.setInputContentMaxSize(start);
                    if (TextUtils.isEmpty(s)) {
                        NoteContentEditText.this.setText("");
                    } else {
                        NoteContentEditText.this.setText(s.subSequence(0, start));
                    }
                    Toast.makeText(NoteContentEditText.this.mContext, NoteContentEditText.this.mContext.getString(R.string.max_content_input_mum_limit), 0).show();
                }
            }

            public void afterTextChanged(Editable editable) {
                boolean z = false;
                NoteContentEditText.this.setHint(R.string.content_hint);
                if (shareView != null) {
                    shareView.setEnabled(!TextUtils.isEmpty(editable.toString()));
                }
                if (deleteView != null) {
                    View view = deleteView;
                    if (!TextUtils.isEmpty(editable.toString())) {
                        z = true;
                    }
                    view.setEnabled(z);
                }
                NoteContentEditText.this.setTextChanged(true);
            }
        };
        addTextChangedListener(this.mTextWatcher);
    }

    public void onImageChanged() {
        invalidate();
        setEnabled(false);
        setEnabled(true);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mTextWatcher != null) {
            removeTextChangedListener(this.mTextWatcher);
        }
        ((ClipboardManager) NoteAppImpl.getContext().getSystemService("clipboard")).removePrimaryClipChangedListener(this.mClipDataChangedListener);
    }

    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        updateSpanEditableText();
    }

    private void updateSpanEditableText() {
        for (JsonableSpan span : (JsonableSpan[]) this.mText.getSpans(0, this.mText.length(), JsonableSpan.class)) {
            span.updateSpanEditableText(this.mText);
        }
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
        this.mText = (SpannableStringBuilder) getText();
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (event.getAction() == 1 && this.mShouldFixCursor) {
            this.mShouldFixCursor = false;
            if (getSelectionEnd() != this.mSelection) {
                setSelection(this.mSelection);
            }
        }
        return result;
    }

    protected void onDraw(Canvas canvas) {
        drawDecoration(canvas);
        super.onDraw(canvas);
    }

    private void drawDecoration(Canvas canvas) {
        int height = getHeight();
        int xStop = getWidth();
        Layout layout = getLayout();
        int lineHeight = getLineHeight();
        int yPos = 0;
        int totalTextLine = layout.getLineCount();
        Paint linePaint = this.mLinePaint;
        int paddingTop = getCompoundPaddingTop();
        canvas.translate(0.0f, (float) paddingTop);
        for (int i = 0; i < totalTextLine - 1; i++) {
            yPos = layout.getLineBaseline(i) + LINE_PADDING_BOTTOM;
            canvas.drawLine(0.0f, (float) yPos, (float) xStop, (float) yPos, linePaint);
        }
        if (layout.getLineStart(totalTextLine - 1) == getText().length()) {
            yPos += lineHeight;
            canvas.drawLine(0.0f, (float) yPos, (float) xStop, (float) yPos, linePaint);
        } else {
            yPos = layout.getLineBaseline(totalTextLine - 1) + LINE_PADDING_BOTTOM;
            canvas.drawLine(0.0f, (float) yPos, (float) xStop, (float) yPos, linePaint);
        }
        while (yPos + lineHeight < height) {
            int nextLineYPos = yPos + lineHeight;
            if (nextLineYPos + paddingTop >= height) {
                break;
            }
            yPos = nextLineYPos;
            canvas.drawLine(0.0f, (float) yPos, (float) xStop, (float) yPos, linePaint);
        }
        drawSignature(canvas, yPos, lineHeight);
        drawBottom(canvas, yPos, lineHeight);
        canvas.translate(0.0f, (float) (-paddingTop));
    }

    private void drawBottom(Canvas canvas, int lastLineYPos, int lineHeight) {
        int baseY = lastLineYPos - lineHeight;
        canvas.save();
        canvas.translate(0.0f, (float) baseY);
        drawNoteTime(canvas, lineHeight);
        if (this.mHasDrawAmiTag) {
            drawAmiTag(canvas, lineHeight);
        } else {
            drawReminder(canvas, lineHeight);
        }
        canvas.restore();
    }

    private void drawSignature(Canvas canvas, int lastLineYPos, int lineHeight) {
        if (!TextUtils.isEmpty(this.mSignature)) {
            int baseY = lastLineYPos - (lineHeight * 2);
            canvas.save();
            canvas.translate(0.0f, (float) baseY);
            float yPos = ((((float) lineHeight) / 2.0f) - ((this.mSignaturePaint.ascent() - this.mSignaturePaint.descent()) / 2.0f)) + this.mSignaturePaint.descent();
            canvas.drawText(this.mSignature, (float) ((getWidth() - getPaddingRight()) - this.mNoteSignatureWidth), yPos, this.mSignaturePaint);
            canvas.restore();
        }
    }

    private String getSignatureText() {
        return NoteShareDataManager.getSignatureText(getContext());
    }

    private void drawNoteTime(Canvas canvas, int lineHeight) {
        if (!TextUtils.isEmpty(this.mShowTime)) {
            float yPos = ((((float) lineHeight) / 2.0f) - ((this.mNoteTimePaint.ascent() - this.mNoteTimePaint.descent()) / 2.0f)) + this.mNoteTimePaint.descent();
            canvas.drawText(this.mShowTime, (float) ((getWidth() - getPaddingRight()) - this.mNoteTimeWidth), yPos, this.mNoteTimePaint);
        }
    }

    private void drawAmiTag(Canvas canvas, int lineHeight) {
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextSize((float) getResources().getDimensionPixelSize(R.dimen.share_ami_tag_textsize));
        paint.setColor(ContextCompat.getColor(getContext(), R.color.share_ami_tag_text_color));
        float textY = ((float) lineHeight) / 2.0f;
        canvas.drawBitmap(((BitmapDrawable) this.mTagDrawable).getBitmap(), (float) getResources().getDimensionPixelSize(R.dimen.ami_tag_text_coorx), textY, paint);
    }

    private void drawReminder(Canvas canvas, int lineHeight) {
        if (this.mReminder != null) {
            int bitmapY = lineHeight / 2;
            int bitmapX = getPaddingLeft();
            canvas.translate((float) bitmapX, (float) bitmapY);
            this.mReminderDrawable.draw(canvas);
            canvas.translate((float) (-bitmapX), (float) (-bitmapY));
            float textY = ((((float) lineHeight) / 2.0f) - ((this.mReminderPaint.ascent() - this.mReminderPaint.descent()) / 2.0f)) + this.mReminderPaint.descent();
            canvas.drawText(this.mReminder, (float) ((getPaddingLeft() + this.mReminderDrawable.getIntrinsicWidth()) + EditPage.get(getContext()).mReminderGap), textY, this.mReminderPaint);
        }
    }

    public void setAmiTagEnable(boolean isDraw) {
        this.mHasDrawAmiTag = isDraw;
    }

    public void setReminderTime(long reminderTime) {
        if (reminderTime == 0) {
            this.mReminder = null;
            invalidate();
            return;
        }
        this.mReminder = NoteUtils.formatDateTime(reminderTime, DateFormatter);
        invalidate();
        if (this.mReminderDrawable == null) {
            this.mReminderDrawable = ContextCompat.getDrawable(getContext(), R.drawable.edit_page_reminder);
            this.mReminderDrawable.setBounds(0, 0, this.mReminderDrawable.getIntrinsicWidth(), this.mReminderDrawable.getIntrinsicHeight());
        }
    }

    public void setNoteTime(long noteTime) {
        this.mShowTime = NoteUtils.formatDateTime(noteTime, DateFormatter);
        this.mNoteTimeWidth = (int) Math.ceil((double) this.mNoteTimePaint.measureText(this.mShowTime));
    }

    public void shouldFixCursor(int selection) {
        this.mShouldFixCursor = true;
        this.mSelection = selection;
    }

    public void setTextChanged(boolean changed) {
        synchronized (this) {
            this.mTextChanged = changed;
        }
    }

    public boolean getAndResetTextChanged() {
        boolean z = false;
        boolean billItemChanged = isBillItemChanged();
        synchronized (this) {
            boolean textChanged = this.mTextChanged;
            this.mTextChanged = false;
            if (billItemChanged || textChanged) {
                z = true;
            }
        }
        return z;
    }

    private boolean isBillItemChanged() {
        boolean changed = false;
        SpannableStringBuilder text = this.mText;
        for (BillItem billItem : (BillItem[]) text.getSpans(0, text.length(), BillItem.class)) {
            if (billItem.getAndResetChanged()) {
                changed = true;
            }
        }
        return changed;
    }

    protected void onSelectionChanged(int selStart, int selEnd) {
        if (this.mText != null && selStart == selEnd && !isLocked()) {
            try {
                lock();
                adjustCursorIfNeed(selStart);
            } finally {
                unLock();
            }
        }
    }

    private void adjustCursorIfNeed(int currSelection) {
        SpannableStringBuilder text = this.mText;
        BillItem[] bills = BillItem.get(text, currSelection, currSelection);
        if (bills.length == 1) {
            bills[0].adjustCursorIfInvalid(currSelection);
            return;
        }
        PhotoImageSpan[] photoSpans = PhotoImageSpan.get(text, currSelection, currSelection);
        if (photoSpans.length == 1) {
            photoSpans[0].adjustCursorIfInvalid(currSelection);
            return;
        }
        SoundImageSpan[] soundSpans = SoundImageSpan.get(text, currSelection, currSelection);
        if (soundSpans.length == 1) {
            soundSpans[0].adjustCursorIfInvalid(currSelection);
        }
    }

    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (!isLocked()) {
            try {
                lock();
                SpannableStringBuilder builder = (SpannableStringBuilder) text;
                if (!createNewBillItemIfNeed(builder, start, lengthBefore, lengthAfter)) {
                    insertLineBreakIfNeed(builder, start, lengthBefore, lengthAfter);
                    unLock();
                }
            } finally {
                unLock();
            }
        }
    }

    private boolean createNewBillItemIfNeed(SpannableStringBuilder text, int start, int lengthBefore, int lengthAfter) {
        boolean inputLineBreak;
        if (lengthBefore == 0 && lengthAfter == 1 && text.charAt(start) == '\n') {
            inputLineBreak = true;
        } else {
            inputLineBreak = false;
        }
        if (!inputLineBreak) {
            return false;
        }
        BillItem[] bills = BillItem.get(text, start, start);
        if (bills.length != 1) {
            return false;
        }
        BillItem bill = bills[0];
        int billStart = text.getSpanStart(bill);
        int billEnd = text.getSpanEnd(bill);
        bill.adjustRange(billStart, start);
        int newBillStart = start + 1;
        int newBillEnd = billEnd;
        if (start == billEnd) {
            newBillEnd = start + 1;
        }
        createBillItem(text, newBillStart, newBillEnd);
        return true;
    }

    private void insertLineBreakIfNeed(SpannableStringBuilder text, int start, int lengthBefore, int lengthAfter) {
        int selStart = start;
        int selEnd = start;
        if (lengthAfter > 0) {
            selEnd = start + lengthAfter;
        }
        boolean insertByDelete = lengthBefore > 0 && lengthAfter <= 0;
        ensurePreSpan(text, selStart, insertByDelete);
        ensureNextSpan(text, selEnd);
    }

    private void ensurePreSpan(SpannableStringBuilder text, int currSelection, boolean insertByDelete) {
        if (!ensurePreOnlyImageSpan(text, currSelection, insertByDelete)) {
            ensurePreBillItem(text, currSelection);
        }
    }

    private void ensureNextSpan(SpannableStringBuilder text, int currSelection) {
        if (!ensureNextOnlyImageSpan(text, currSelection)) {
            ensureNextBillItem(text, currSelection);
        }
    }

    private boolean ensurePreOnlyImageSpan(SpannableStringBuilder text, int currSelection, boolean insertByDelete) {
        OnlyImageSpan[] onlyImageSpans = (OnlyImageSpan[]) text.getSpans(currSelection, currSelection, OnlyImageSpan.class);
        if (onlyImageSpans.length <= 0) {
            return false;
        }
        OnlyImageSpan onlyImageSpan = null;
        int imageSpanEnd = 0;
        for (OnlyImageSpan span : onlyImageSpans) {
            imageSpanEnd = text.getSpanEnd(span);
            if (imageSpanEnd == currSelection) {
                onlyImageSpan = span;
                break;
            }
        }
        if (onlyImageSpan == null) {
            return false;
        }
        if (!(imageSpanEnd == text.length() || text.charAt(imageSpanEnd) == '\n')) {
            BillItem[] bills = (BillItem[]) text.getSpans(imageSpanEnd, imageSpanEnd, BillItem.class);
            BillItem bill = null;
            int billStart = 0;
            int billEnd = 0;
            if (bills.length == 1) {
                billStart = text.getSpanStart(bills[0]);
                billEnd = text.getSpanEnd(bills[0]);
                if (billStart == imageSpanEnd) {
                    bill = bills[0];
                    bill.adjustRange(billStart + 1, billEnd);
                }
            }
            text.insert(imageSpanEnd, Constants.STR_NEW_LINE);
            if (insertByDelete) {
                Selection.setSelection(text, imageSpanEnd);
            }
            if (bill != null) {
                bill.adjustRange(billStart + 1, billEnd + 1);
            }
        }
        return true;
    }

    private void ensurePreBillItem(SpannableStringBuilder text, int currSelection) {
        BillItem[] bills = BillItem.get(text, currSelection, currSelection);
        if (bills.length > 0) {
            JsonableSpan preBill = null;
            int preStart = 0;
            for (BillItem bill : bills) {
                preStart = text.getSpanStart(bill);
                if (preStart < currSelection) {
                    preBill = bill;
                    break;
                }
            }
            if (preBill != null) {
                int newEnd = text.length();
                int index = TextUtils.indexOf(text, '\n', preStart);
                if (index != -1) {
                    newEnd = index;
                }
                JsonableSpan[] jsonableSpans = (JsonableSpan[]) text.getSpans(preStart, newEnd, JsonableSpan.class);
                if (jsonableSpans.length > 1) {
                    JsonableSpan span = null;
                    for (JsonableSpan temp : jsonableSpans) {
                        if (temp != preBill) {
                            span = temp;
                            break;
                        }
                    }
                    newEnd = text.getSpanStart(span);
                    int nextEnd = text.getSpanEnd(span);
                    if (span instanceof BillItem) {
                        ((BillItem) span).adjustRange(newEnd + 1, nextEnd);
                    }
                    text.insert(newEnd, Constants.STR_NEW_LINE);
                    if (span instanceof BillItem) {
                        ((BillItem) span).adjustRange(newEnd + 1, nextEnd + 1);
                    }
                }
                preBill.adjustRange(preStart, newEnd);
            }
        }
    }

    private boolean ensureNextOnlyImageSpan(SpannableStringBuilder text, int currSelection) {
        OnlyImageSpan[] onlyImageSpans = (OnlyImageSpan[]) text.getSpans(currSelection, currSelection, OnlyImageSpan.class);
        if (onlyImageSpans.length <= 0) {
            return false;
        }
        OnlyImageSpan onlyImageSpan = null;
        int start = 0;
        for (OnlyImageSpan span : onlyImageSpans) {
            start = text.getSpanStart(span);
            if (start == currSelection) {
                onlyImageSpan = span;
                break;
            }
        }
        if (onlyImageSpan == null) {
            return false;
        }
        if (!(start == 0 || text.charAt(start - 1) == '\n')) {
            text.insert(start, Constants.STR_NEW_LINE);
        }
        return true;
    }

    private void ensureNextBillItem(SpannableStringBuilder text, int currSelection) {
        BillItem[] bills = BillItem.get(text, currSelection, currSelection);
        if (bills.length > 0) {
            BillItem bill = null;
            int start = 0;
            int end = 0;
            for (BillItem item : bills) {
                start = text.getSpanStart(item);
                end = text.getSpanEnd(item);
                if (start >= currSelection) {
                    bill = item;
                    break;
                }
            }
            if (bill != null && start != 0 && text.charAt(start - 1) != '\n') {
                bill.adjustRange(start + 1, end);
                text.insert(start, Constants.STR_NEW_LINE);
                bill.adjustRange(start + 1, end + 1);
            }
        }
    }

    public void toggleBillItem() {
        int start = 0;
        SpannableStringBuilder text = this.mText;
        if (text.length() <= 0) {
            try {
                lock();
                createBillItem(text, 0, 0);
            } finally {
                unLock();
            }
        } else {
            int curPositionStart = getSelectionStart();
            if (curPositionStart == getSelectionEnd()) {
                if (curPositionStart < 0) {
                    setSelection(0);
                }
                if (curPositionStart >= 0) {
                    start = curPositionStart;
                }
                int billStart = start;
                int billEnd = start;
                try {
                    lock();
                    if (isNextToOnlyImageSpan(text, start, start)) {
                        text.insert(start, Constants.STR_NEW_LINE);
                        billStart = start + 1;
                        billEnd = billStart;
                    } else {
                        billStart = EditUtils.getCurParagraphStart(text, start);
                        billEnd = EditUtils.getCurParagraphEnd(text, start);
                    }
                    BillItem[] items = BillItem.get(text, billStart, billEnd);
                    if (items.length <= 0) {
                        createBillItem(text, billStart, billEnd);
                    } else {
                        items[0].destroy();
                    }
                    unLock();
                } catch (Throwable th) {
                    unLock();
                }
            }
        }
    }

    public boolean isSelectPositionReachMaxSize() {
        if (this.mReacheMaxLengthCharacterCount == 0 || this.mReacheMaxLengthCharacterCount < getSelectionEnd() - 20) {
            return false;
        }
        return true;
    }

    public void insertPhoto(Uri thumbUri, Uri originUri, Bitmap bitmap) {
        SpannableStringBuilder text = this.mText;
        int currSelection = getSelectionEnd();
        currSelection = NoteUtils.clamp(currSelection, 0, currSelection);
        try {
            lock();
            currSelection = ensurePreChar(text, currSelection, '\n');
            PhotoImageSpan span = new PhotoImageSpan(this.mContext, text, thumbUri, originUri, bitmap);
            EditUtils.insertPhotoImageSpan(text, span, currSelection);
            span.initSpan(currSelection);
            span.setOnImageSpanChangeListener(this);
            ensureNextChar(text, Constants.MEDIA_PHOTO.length() + currSelection, '\n');
        } finally {
            unLock();
        }
    }

    public void insertSound(String originSoundPath, int durationInSec) {
        SpannableStringBuilder text = this.mText;
        int currSelection = getSelectionEnd();
        currSelection = NoteUtils.clamp(currSelection, 0, currSelection);
        try {
            lock();
            currSelection = ensurePreChar(text, currSelection, '\n');
            SoundImageSpan span = new SoundImageSpan(this.mContext, text, originSoundPath, durationInSec);
            EditUtils.insertSoundImageSpan(text, span, currSelection);
            span.initSpan(currSelection);
            ensureNextChar(text, Constants.MEDIA_SOUND.length() + currSelection, '\n');
        } finally {
            unLock();
        }
    }

    private int ensurePreChar(SpannableStringBuilder text, int currSelection, char c) {
        if (currSelection == 0) {
            return currSelection;
        }
        if (text.charAt(currSelection - 1) != c) {
            BillItem[] bills = (BillItem[]) text.getSpans(currSelection, currSelection, BillItem.class);
            BillItem bill = null;
            int start = 0;
            if (bills.length == 1) {
                start = text.getSpanStart(bills[0]);
                if (start < currSelection) {
                    bill = bills[0];
                }
            }
            text.insert(currSelection, Constants.STR_NEW_LINE);
            if (bill != null) {
                bill.adjustRange(start, currSelection);
            }
            currSelection++;
        }
        return currSelection;
    }

    private void ensureNextChar(SpannableStringBuilder text, int currSelection, char c) {
        if (currSelection == text.length() || text.charAt(currSelection) != c) {
            text.insert(currSelection, Constants.STR_NEW_LINE);
        }
    }

    private boolean isNextToOnlyImageSpan(SpannableStringBuilder text, int start, int end) {
        OnlyImageSpan[] spans = (OnlyImageSpan[]) text.getSpans(start, end, OnlyImageSpan.class);
        int length = spans.length;
        if (length <= 0 || text.getSpanEnd(spans[length - 1]) != end) {
            return false;
        }
        return true;
    }

    private void createBillItem(SpannableStringBuilder text, int pStart, int pEnd) {
        new BillItem(this.mContext, text).init(false, pStart, pEnd);
    }

    private void lock() {
        this.mLocked = true;
    }

    private void unLock() {
        this.mLocked = false;
    }

    private boolean isLocked() {
        return this.mLocked;
    }

    private void setInputContentMaxSize(int maxSize) {
        int size = maxSize;
        if (size < 10000) {
            size = 10000;
        }
        this.mReacheMaxLengthCharacterCount = size;
        setFilters(new InputFilter[]{new TextLengthFilter(size)});
    }

    private boolean checkInputContentExceedMaxSize(CharSequence s) {
        if (((double) getCurrentContentSize(s)) >= 0.05d * getMaxInputContentSize()) {
            return true;
        }
        return false;
    }

    private int getCurrentContentSize(CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        int size = 0;
        try {
            return new StringBuilder(s).toString().getBytes(StringUtils.ENCODING_UTF8).length;
        } catch (UnsupportedEncodingException e) {
            return size;
        }
    }

    private double getMaxInputContentSize() {
        if (this.mMaxContentSize == 0.0d) {
            this.mMaxContentSize = DEFAULT_MAX_CONTENT_SIZE_IN_M;
        }
        return this.mMaxContentSize;
    }
}
