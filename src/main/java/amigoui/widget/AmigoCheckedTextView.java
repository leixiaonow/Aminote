package amigoui.widget;

import amigoui.changecolors.ChameleonColorManager;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

public class AmigoCheckedTextView extends CheckedTextView {
    private static final int STATE_CHECKED = 1;
    private static final int STATE_DEFAULT = 0;
    private Drawable mCheckMarkDrawable;
    private int mState;

    public AmigoCheckedTextView(Context context) {
        this(context, null);
    }

    public AmigoCheckedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843720);
    }

    public AmigoCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmigoCheckedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        this.mState = -1;
    }

    private boolean stateIsChecked(int[] myDrawableState) {
        for (int i : myDrawableState) {
            if (i == 16842912) {
                return true;
            }
        }
        return false;
    }

    protected void drawableStateChanged() {
        if (ChameleonColorManager.isNeedChangeColor() && this.mCheckMarkDrawable != null) {
            changeMarkDrawable();
        }
        super.drawableStateChanged();
    }

    private void changeMarkDrawable() {
        if (stateIsChecked(getDrawableState())) {
            if (this.mState != 1) {
                this.mCheckMarkDrawable.setColorFilter(ChameleonColorManager.getAccentColor_G1(), Mode.SRC_IN);
                this.mState = 1;
            }
        } else if (this.mState != 0) {
            this.mCheckMarkDrawable.setColorFilter(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2(), Mode.SRC_IN);
            this.mState = 0;
        }
    }
}
