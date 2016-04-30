package amigoui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RemoteViews.RemoteView;
import android.widget.TextView;

import amigoui.changecolors.ChameleonColorManager;

@RemoteView
public class AmigoButton extends TextView {
    public static final int BUTTON_CONTRA_STYLE = 2;
    public static final int BUTTON_LOADING_INFINITY_STYLE = 4;
    public static final int BUTTON_LOADING_STYLE = 5;
    public static final int BUTTON_NORMAL_STYLE = 0;
    public static final int BUTTON_RECOM_STYLE = 1;
    private static final String TAG = "AmigoButton";
    private AnimationDrawable mAnimationDrawable;
    private int mButtonStyle;
    private float mMinHeight;
    private int mOldBtnHeight;
    private int mOldBtnWidth;
    private CharSequence mOldText;
    private ColorStateList mOldTextColorStateList;
    private float mSmallFontSize;

    public AmigoButton(Context context) {
        this(context, null);
    }

    public AmigoButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public AmigoButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mSmallFontSize = getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_loading_button_small_size"));
        changeColor();
    }

    private void changeColor() {
    }

    private void changeStateListDrawable(StateListDrawable stateListDrawble) {
        for (int index = 0; index < stateListDrawble.getStateCount(); index++) {
            Drawable drawable = stateListDrawble.getStateDrawable(index);
            if (!stateIsDisable(stateListDrawble.getStateSet(index))) {
                drawable.setColorFilter(ChameleonColorManager.getPopupBackgroudColor_B2(), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private boolean stateIsDisable(int[] myDrawableState) {
        for (int i : myDrawableState) {
            if (i == -16842910) {
                return true;
            }
        }
        return false;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AmigoButton.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AmigoButton.class.getName());
    }

    public void setButtonStyle(int style) {
        this.mButtonStyle = style;
        switch (style) {
        }
    }

    public void setUpdate(int val) {
    }

    public void reset() {
    }
}
