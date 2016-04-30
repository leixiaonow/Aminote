package amigoui.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class AmigoPreferenceFrameLayout extends FrameLayout {
    private static final int DEFAULT_BORDER_BOTTOM = 0;
    private static final int DEFAULT_BORDER_LEFT = 0;
    private static final int DEFAULT_BORDER_RIGHT = 0;
    private static final int DEFAULT_BORDER_TOP = 0;
    private final int mBorderBottom;
    private final int mBorderLeft;
    private final int mBorderRight;
    private final int mBorderTop;
    private boolean mPaddingApplied;

    public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        public boolean removeBorders;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.removeBorders = false;
            this.removeBorders = false;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.removeBorders = false;
        }
    }

    public AmigoPreferenceFrameLayout(Context context) {
        this(context, null);
    }

    public AmigoPreferenceFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmigoPreferenceFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        float density = context.getResources().getDisplayMetrics().density;
        int defaultBorderTop = (int) ((density * 0.0f) + 0.5f);
        int defaultBottomPadding = (int) ((density * 0.0f) + 0.5f);
        int defaultLeftPadding = (int) ((density * 0.0f) + 0.5f);
        int defaultRightPadding = (int) ((density * 0.0f) + 0.5f);
        this.mBorderTop = 0;
        this.mBorderBottom = 0;
        this.mBorderLeft = 0;
        this.mBorderRight = 0;
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public void addView(View child) {
        int borderTop = getPaddingTop();
        int borderBottom = getPaddingBottom();
        int borderLeft = getPaddingLeft();
        int borderRight = getPaddingRight();
        LayoutParams layoutParams = child.getLayoutParams() instanceof LayoutParams ? (LayoutParams) child.getLayoutParams() : null;
        if (layoutParams == null || !layoutParams.removeBorders) {
            if (!this.mPaddingApplied) {
                borderTop += this.mBorderTop;
                borderBottom += this.mBorderBottom;
                borderLeft += this.mBorderLeft;
                borderRight += this.mBorderRight;
                this.mPaddingApplied = true;
            }
        } else if (this.mPaddingApplied) {
            borderTop -= this.mBorderTop;
            borderBottom -= this.mBorderBottom;
            borderLeft -= this.mBorderLeft;
            borderRight -= this.mBorderRight;
            this.mPaddingApplied = false;
        }
        int previousTop = getPaddingTop();
        int previousBottom = getPaddingBottom();
        int previousLeft = getPaddingLeft();
        int previousRight = getPaddingRight();
        if (!(previousTop == borderTop && previousBottom == borderBottom && previousLeft == borderLeft && previousRight == borderRight)) {
            setPadding(borderLeft, borderTop, borderRight, borderBottom);
        }
        super.addView(child);
    }
}
