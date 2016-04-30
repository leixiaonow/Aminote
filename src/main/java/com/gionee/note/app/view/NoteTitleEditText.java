package com.gionee.note.app.view;

import amigoui.widget.AmigoEditText;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import com.gionee.aminote.R;
import com.gionee.note.app.utils.InputTextNumLimitHelp;
import com.gionee.note.app.utils.InputTextNumLimitHelp.TextChangedListener;

public class NoteTitleEditText extends AmigoEditText {
    private static final String TAG = "NoteTitleEditText";
    private InputTextNumLimitHelp mInputTextNumLimitHelp;
    private Paint mLinePaint;
    private boolean mTextChanged = false;
    private TextChangedListener mTextChangedListener = new TextChangedListener() {
        public void onTextChange() {
            NoteTitleEditText.this.setTextChanged(true);
        }
    };

    public NoteTitleEditText(Context context) {
        super(context);
        init(context);
    }

    public NoteTitleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NoteTitleEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        initConstField(context);
        setSaveEnabled(false);
        initPaint();
    }

    private void initPaint() {
        this.mLinePaint = new Paint(getPaint());
        this.mLinePaint.setColor(getResources().getColor(R.color.note_edit_text_line_color));
        this.mLinePaint.setAntiAlias(false);
        this.mLinePaint.setStyle(Style.STROKE);
        this.mLinePaint.setStrokeWidth(0.0f);
    }

    private void initConstField(Context context) {
        this.mContext = context;
    }

    protected void onDraw(Canvas canvas) {
        drawLine(canvas);
        super.onDraw(canvas);
    }

    private void drawLine(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w != 0 && h != 0) {
            canvas.drawLine(0.0f, (float) (h - 1), (float) w, (float) (h - 1), this.mLinePaint);
        }
    }

    public void setTextChanged(boolean changed) {
        synchronized (this) {
            this.mTextChanged = changed;
        }
    }

    public boolean getAndResetTextChanged() {
        boolean textChanged;
        synchronized (this) {
            textChanged = this.mTextChanged;
            this.mTextChanged = false;
        }
        return textChanged;
    }

    public void initWatcher() {
        this.mInputTextNumLimitHelp = new InputTextNumLimitHelp(this, 30, 15, 30);
        this.mInputTextNumLimitHelp.setTextChangedListener(this.mTextChangedListener);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mInputTextNumLimitHelp != null) {
            this.mInputTextNumLimitHelp.unRegisterWatcher();
        }
    }
}
