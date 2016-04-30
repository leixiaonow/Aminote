package com.amigoui.internal.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import uk.co.senab.photoview.IPhotoView;

public class AmigoTabIndicator extends LinearLayout {
    private static final int ACTIONBAR_TAB_INDICATOR_BOTTOM_PADDING = 1;
    private static final int ACTIONBAR_TAB_INDICATOR_HEIGHT = 2;
    private int mIndexForSelection;
    private int mSelectedUnderlineBottomPadding;
    private final Paint mSelectedUnderlinePaint;
    private int mSelectedUnderlineThickness;
    private float mSelectionOffset;

    public AmigoTabIndicator(Context context) {
        this(context, null);
    }

    public AmigoTabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 16843508);
    }

    public AmigoTabIndicator(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        Resources res = context.getResources();
        this.mSelectedUnderlineThickness = (int) (2.0f * res.getDisplayMetrics().density);
        this.mSelectedUnderlineBottomPadding = (int) (IPhotoView.DEFAULT_MIN_SCALE * res.getDisplayMetrics().density);
        int underlineColor = res.getColor(17170443);
        int backgroundColor = res.getColor(17170445);
        this.mSelectedUnderlinePaint = new Paint();
        this.mSelectedUnderlinePaint.setColor(underlineColor);
        setBackgroundColor(backgroundColor);
        setWillNotDraw(false);
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        this.mIndexForSelection = position;
        this.mSelectionOffset = positionOffset;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        if (getChildCount() > 0) {
            View selectedTitle = getChildAt(this.mIndexForSelection);
            int selectedLeft = selectedTitle.getLeft();
            int selectedRight = selectedTitle.getRight();
            boolean isRtl = isRtl();
            boolean hasNextTab = isRtl ? this.mIndexForSelection > 0 : this.mIndexForSelection < getChildCount() + -1;
            if (this.mSelectionOffset > 0.0f && hasNextTab) {
                View nextTitle = getChildAt((isRtl ? -1 : 1) + this.mIndexForSelection);
                selectedLeft = (int) ((this.mSelectionOffset * ((float) nextTitle.getLeft())) + ((IPhotoView.DEFAULT_MIN_SCALE - this.mSelectionOffset) * ((float) selectedLeft)));
                selectedRight = (int) ((this.mSelectionOffset * ((float) nextTitle.getRight())) + ((IPhotoView.DEFAULT_MIN_SCALE - this.mSelectionOffset) * ((float) selectedRight)));
            }
            int height = getHeight();
            canvas.drawRect((float) selectedLeft, (float) ((height - this.mSelectedUnderlineThickness) - this.mSelectedUnderlineBottomPadding), (float) selectedRight, (float) (height - this.mSelectedUnderlineBottomPadding), this.mSelectedUnderlinePaint);
        }
    }

    private boolean isRtl() {
        return getLayoutDirection() == 1;
    }

    public void setIndicatorBackgroundColor(int color) {
        this.mSelectedUnderlinePaint.setColor(color);
    }
}
