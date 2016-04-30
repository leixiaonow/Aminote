package com.amigoui.internal.widget;



import amigoui.changecolors.IChangeColors;
import amigoui.reflection.AmigoActionMenuPresenter;
import amigoui.widget.AmigoWidgetResource;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public abstract class AmigoAbsActionBarView extends ViewGroup implements IChangeColors {
    private static final int FADE_DURATION = 200;
    private static final TimeInterpolator sAlphaInterpolator = new DecelerateInterpolator();
    protected AmigoActionMenuPresenter mActionMenuPresenter;
    protected int mContentHeight;
    protected boolean mIsActionMode = false;
    protected boolean mIsActionModeShowing = false;
    protected ViewGroup mMenuView;
    protected boolean mSplitActionBar = false;
    protected AmigoActionBarContainer mSplitView;
    protected boolean mSplitWhenNarrow;
    protected final VisibilityAnimListener mVisAnimListener = new VisibilityAnimListener();
    protected Animator mVisibilityAnim;

    protected class VisibilityAnimListener implements AnimatorListener {
        private boolean mCanceled = false;
        int mFinalVisibility;

        protected VisibilityAnimListener() {
        }

        public VisibilityAnimListener withFinalVisibility(int visibility) {
            this.mFinalVisibility = visibility;
            return this;
        }

        public void onAnimationStart(Animator animation) {
            AmigoAbsActionBarView.this.setVisibility(0);
            AmigoAbsActionBarView.this.mVisibilityAnim = animation;
            this.mCanceled = false;
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mCanceled) {
                AmigoAbsActionBarView.this.mVisibilityAnim = null;
                AmigoAbsActionBarView.this.setVisibility(this.mFinalVisibility);
                if (!(AmigoAbsActionBarView.this.mSplitView == null || AmigoAbsActionBarView.this.mMenuView == null)) {
                    AmigoAbsActionBarView.this.mMenuView.setVisibility(this.mFinalVisibility);
                }
                if (AmigoAbsActionBarView.this.mIsActionMode) {
                    AmigoAbsActionBarView.this.mIsActionModeShowing = this.mFinalVisibility == 0;
                }
            }
        }

        public void onAnimationCancel(Animator animation) {
            this.mCanceled = true;
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    public AmigoAbsActionBarView(Context context) {
        super(context);
    }

    public AmigoAbsActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AmigoAbsActionBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.AmigoActionBar, AmigoWidgetResource.getIdentifierByAttr(this.mContext, "amigoactionBarStyle"), 0);
        setContentHeight(a.getLayoutDimension(R.styleable.AmigoActionBar_amigoheight, 0));
        a.recycle();
        if (this.mSplitWhenNarrow) {
            setSplitActionBar(true);
        }
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.onConfigurationChanged(newConfig);
        }
    }

    public void setSplitActionBar(boolean split) {
    }

    public void setSplitWhenNarrow(boolean splitWhenNarrow) {
        this.mSplitWhenNarrow = splitWhenNarrow;
    }

    public void setContentHeight(int height) {
        this.mContentHeight = height;
        requestLayout();
    }

    public int getContentHeight() {
        return this.mContentHeight;
    }

    public void setSplitView(AmigoActionBarContainer splitView) {
        this.mSplitView = splitView;
    }

    public int getAnimatedVisibility() {
        if (this.mVisibilityAnim != null) {
            return this.mVisAnimListener.mFinalVisibility;
        }
        return getVisibility();
    }

    public void animateToVisibility(int visibility) {
        if (this.mVisibilityAnim != null) {
            this.mVisibilityAnim.end();
        }
        ObjectAnimator anim;
        if (visibility == 0) {
            anim = ObjectAnimator.ofFloat(this, "TranslationY", new float[]{(float) (-getContentHeight()), 0.0f});
            anim.setDuration(200);
            anim.setInterpolator(sAlphaInterpolator);
            if (this.mSplitView == null || this.mMenuView == null) {
                anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
                anim.start();
                return;
            }
            AnimatorSet set = new AnimatorSet();
            ObjectAnimator splitAnim = ObjectAnimator.ofFloat(this.mMenuView, "TranslationY", new float[]{(float) (-getContentHeight()), 0.0f});
            splitAnim.setDuration(200);
            set.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
            set.play(anim).with(splitAnim);
            set.start();
            return;
        }
        anim = ObjectAnimator.ofFloat(this, "TranslationY", new float[]{0.0f, (float) (-getContentHeight())});
        anim.setDuration(200);
        anim.setInterpolator(sAlphaInterpolator);
        if (this.mSplitView == null || this.mMenuView == null) {
            anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
            anim.start();
            return;
        }
        set = new AnimatorSet();
        splitAnim = ObjectAnimator.ofFloat(this.mMenuView, "TranslationY", new float[]{0.0f, (float) (-getContentHeight())});
        splitAnim.setDuration(200);
        set.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
        set.play(anim).with(splitAnim);
        set.start();
    }

    public void setVisibility(int visibility) {
        if (visibility != getVisibility()) {
            if (this.mVisibilityAnim != null) {
                this.mVisibilityAnim.end();
            }
            super.setVisibility(visibility);
        }
    }

    public boolean showOverflowMenu() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.showOverflowMenu();
        }
        return false;
    }

    public void postShowOverflowMenu() {
        post(new Runnable() {
            public void run() {
                AmigoAbsActionBarView.this.showOverflowMenu();
            }
        });
    }

    public boolean hideOverflowMenu() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.hideOverflowMenu();
        }
        return false;
    }

    public boolean isOverflowMenuShowing() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.isOverflowMenuShowing();
        }
        return false;
    }

    public boolean isOverflowReserved() {
        return this.mActionMenuPresenter != null && this.mActionMenuPresenter.isOverflowReserved();
    }

    public void dismissPopupMenus() {
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.dismissPopupMenus();
        }
    }

    protected int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        child.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), childSpecHeight);
        return Math.max(0, (availableWidth - child.getMeasuredWidth()) - spacing);
    }

    protected static int next(int x, int val, boolean isRtl) {
        return isRtl ? x - val : x + val;
    }

    protected int positionChild(View child, int x, int y, int contentHeight, boolean reverse) {
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        int childTop = y + ((contentHeight - childHeight) / 2);
        if (reverse) {
            child.layout(x - childWidth, childTop, x, childTop + childHeight);
        } else {
            child.layout(x, childTop, x + childWidth, childTop + childHeight);
        }
        return reverse ? -childWidth : childWidth;
    }
}
