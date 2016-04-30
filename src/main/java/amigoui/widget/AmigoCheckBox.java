package amigoui.widget;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.CheckBox;

import amigoui.changecolors.ChameleonColorManager;

public class AmigoCheckBox extends CheckBox {
    private static final int STATE_CHECKED = 1;
    private static final int STATE_DEFAULT = 0;
    private static final String TAG = "AmigoCheckBox";
    private Drawable mButtonDrawable;
    private int mState;

    public AmigoCheckBox(Context context) {
        this(context, null);
    }

    public AmigoCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public AmigoCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmigoCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        this.mState = -1;
    }

    protected void drawableStateChanged() {
        if (ChameleonColorManager.isNeedChangeColor() && this.mButtonDrawable != null) {
            changeButtonDrawable();
        }
        super.drawableStateChanged();
    }

    private void changeButtonDrawable() {
        if (stateIsChecked(getDrawableState())) {
            if (this.mState != 1) {
                this.mButtonDrawable.setColorFilter(ChameleonColorManager.getAccentColor_G1(), Mode.SRC_IN);
                this.mState = 1;
            }
        } else if (this.mState != 0) {
            this.mButtonDrawable.setColorFilter(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2(), Mode.SRC_IN);
            this.mState = 0;
        }
    }

    private boolean stateIsChecked(int[] myDrawableState) {
        for (int i : myDrawableState) {
            if (i == android.R.attr.state_checked) {
                return true;
            }
        }
        return false;
    }
}
