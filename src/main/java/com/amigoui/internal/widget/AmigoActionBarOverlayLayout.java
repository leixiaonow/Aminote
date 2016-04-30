package com.amigoui.internal.widget;

import amigoui.app.AmigoActionBarImpl;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class AmigoActionBarOverlayLayout extends RelativeLayout {
    static final int[] mActionBarSizeAttr = new int[]{16843499};
    private AmigoActionBarImpl mActionBar;
    private View mActionBarBottom;
    private int mActionBarHeight;
    private View mActionBarTop;
    private AmigoActionBarView mActionView;
    private AmigoActionBarContainer mContainerView;
    private View mContent;
    private int mLastSystemUiVisibility;
    private int mWindowVisibility = 0;
    private final Rect mZeroRect = new Rect(0, 0, 0, 0);

    public AmigoActionBarOverlayLayout(Context context) {
        super(context);
        init(context);
    }

    public AmigoActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(mActionBarSizeAttr);
        this.mActionBarHeight = ta.getDimensionPixelSize(0, 0);
        ta.recycle();
    }

    public void setActionBar(AmigoActionBarImpl impl) {
        this.mActionBar = impl;
        if (getWindowToken() != null) {
            this.mActionBar.setWindowVisibility(this.mWindowVisibility);
            if (this.mLastSystemUiVisibility != 0) {
                onWindowSystemUiVisibilityChanged(this.mLastSystemUiVisibility);
                requestFitSystemWindows();
            }
        }
    }

    public void setShowingForActionMode(boolean showing) {
        if (!showing) {
            setDisabledSystemUiVisibility(0);
        } else if ((getWindowSystemUiVisibility() & 1280) == 1280) {
            setDisabledSystemUiVisibility(4);
        }
    }

    public void onWindowSystemUiVisibilityChanged(int visible) {
        super.onWindowSystemUiVisibilityChanged(visible);
        pullChildren();
        int diff = this.mLastSystemUiVisibility ^ visible;
        this.mLastSystemUiVisibility = visible;
        boolean barVisible = (visible & 4) == 0;
        if (this.mActionBar != null) {
            boolean wasVisible = this.mActionBar.isSystemShowing();
        } else {
            int i = 1;
        }
        if (this.mActionBar != null) {
            if (barVisible) {
                this.mActionBar.showForSystem();
            } else {
                this.mActionBar.hideForSystem();
            }
        }
        if ((diff & 256) != 0 && this.mActionBar != null) {
            requestFitSystemWindows();
        }
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mWindowVisibility = visibility;
        if (this.mActionBar != null) {
            this.mActionBar.setWindowVisibility(visibility);
        }
    }

    private boolean applyInsets(View view, Rect insets, boolean left, boolean top, boolean bottom, boolean right) {
        if (view == null) {
            return false;
        }
        boolean changed = false;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (left && lp.leftMargin != insets.left) {
            changed = true;
            lp.leftMargin = insets.left;
        }
        if (top && lp.topMargin != insets.top) {
            changed = true;
            lp.topMargin = insets.top;
        }
        if (right && lp.rightMargin != insets.right) {
            changed = true;
            lp.rightMargin = insets.right;
        }
        if (!bottom || lp.bottomMargin == insets.bottom) {
            return changed;
        }
        lp.bottomMargin = insets.bottom;
        return true;
    }

    protected boolean fitSystemWindows(Rect insets) {
        pullChildren();
        int vis = getWindowSystemUiVisibility();
        boolean stable = (vis & 256) != 0;
        boolean changed = applyInsets(this.mActionBarTop, insets, true, true, false, true);
        if (this.mActionBarBottom != null) {
            changed |= applyInsets(this.mActionBarBottom, insets, true, false, true, true);
        }
        if ((vis & 1536) == 0) {
            changed |= applyInsets(this.mContent, insets, true, true, true, true);
            insets.set(0, 0, 0, 0);
        } else {
            changed |= applyInsets(this.mContent, this.mZeroRect, true, true, true, true);
        }
        if (stable || (this.mActionBarTop != null && this.mActionBarTop.getVisibility() == 0)) {
            insets.top += this.mActionBarHeight;
        }
        if (this.mActionBar != null && this.mActionBar.hasNonEmbeddedTabs()) {
            View tabs = this.mContainerView.getTabContainer();
            if (stable || (tabs != null && tabs.getVisibility() == 0)) {
                insets.top += this.mActionBarHeight;
            }
        }
        if (this.mActionView != null && this.mActionView.isSplitActionBar() && (stable || (this.mActionBarBottom != null && this.mActionBarBottom.getVisibility() == 0))) {
            insets.bottom += this.mActionBarHeight;
        }
        if (changed) {
            requestLayout();
        }
        return super.fitSystemWindows(insets);
    }

    void pullChildren() {
        if (this.mContent == null) {
            this.mContent = findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_content"));
            this.mActionBarTop = findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_action_bar_container"));
            this.mContainerView = (AmigoActionBarContainer) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_action_bar_container"));
            this.mActionView = (AmigoActionBarView) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_action_bar"));
        }
    }
}
