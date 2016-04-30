package com.gionee.note.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.gionee.note.app.effect.DrawableManager;

public class NoteCardBottomView extends RelativeLayout {
    private int mEffect;
    private boolean mIsBgInitialized;

    public NoteCardBottomView(Context context) {
        this(context, null);
    }

    public NoteCardBottomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteCardBottomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!this.mIsBgInitialized) {
            setCardBg(this.mEffect);
        }
    }

    public void setCardBg(int effect) {
        this.mEffect = effect;
        int w = getWidth();
        int h = getHeight();
        if (w != 0 && h != 0) {
            this.mIsBgInitialized = true;
            setBackground(DrawableManager.getCardEffectDrawable(getContext(), effect, w, h));
        }
    }
}
