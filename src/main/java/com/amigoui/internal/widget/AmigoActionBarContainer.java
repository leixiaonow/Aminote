package com.amigoui.internal.widget;

import amigoui.app.AmigoActionBar.OnExtraViewDragListener;
import com.gionee.aminote.R;
import amigoui.widget.AmigoWidgetResource;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import com.amigoui.internal.view.AmigoActionBarPolicy;
import uk.co.senab.photoview.IPhotoView;

public class AmigoActionBarContainer extends FrameLayout {
    private static final int ANIM_DURATION = 300;
    private AmigoActionBarView mActionBarView;
    private View mActivityContent;
    private ValueAnimator mAnimator;
    private Drawable mBackground;
    private int mContainerHeight;
    private Context mContext;
    private int mDistance;
    private OnExtraViewDragListener mDragListener;
    private View mExtraView;
    private AmigoExtraViewContainer mExtraViewContainer;
    private int mExtraViewContainerHeight;
    private int mFisActivityContentTop;
    private boolean mHasEmbeddedTabs;
    private int mInitActionBarBottom;
    private int mInitActivityTop;
    private boolean mIsDragCloseEnd;
    private boolean mIsDragClosed;
    private boolean mIsDragEnable;
    private boolean mIsDragOpenEnd;
    private boolean mIsDragOpenStart;
    private boolean mIsDragOpened;
    private boolean mIsSplit;
    private boolean mIsStacked;
    private boolean mIsTransitioning;
    private int mOriActionBarVieBottom;
    private int mOriActionBarViewTop;
    private int mOriActivityContentTop;
    private int mOriBottom;
    private int mOriIntellgentContainerBottom;
    private int mOriTabBottom;
    private int mOriTabTop;
    private float mOriY;
    private Drawable mSplitBackground;
    private Drawable mStackedBackground;
    private AmigoTabContainerView mTabContainer;

    public AmigoActionBarContainer(Context context) {
        this(context, null);
    }

    public AmigoActionBarContainer(Context context, AttributeSet attrs) {
        boolean z = true;
        super(context, attrs);
        this.mIsDragOpenStart = false;
        this.mIsDragOpenEnd = false;
        this.mIsDragCloseEnd = false;
        this.mIsDragOpened = false;
        this.mIsDragClosed = false;
        this.mHasEmbeddedTabs = false;
        this.mIsSplit = false;
        this.mIsDragEnable = false;
        this.mFisActivityContentTop = -1;
        setBackgroundDrawable(null);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoActionBar);
        this.mBackground = a.getDrawable(R.styleable.AmigoActionBar_amigobackground);
        this.mStackedBackground = a.getDrawable(R.styleable.AmigoActionBar_amigobackgroundStacked);
        a.recycle();
        if (this.mIsSplit) {
            if (this.mSplitBackground != null) {
                z = false;
            }
        } else if (!(this.mBackground == null && this.mStackedBackground == null)) {
            z = false;
        }
        setWillNotDraw(z);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mActionBarView = (AmigoActionBarView) findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_bar"));
        this.mExtraViewContainer = (AmigoExtraViewContainer) findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_bar_intellgent_container"));
    }

    public void setPrimaryBackground(Drawable bg) {
        boolean z = true;
        if (this.mBackground != null) {
            this.mBackground.setCallback(null);
            unscheduleDrawable(this.mBackground);
        }
        this.mBackground = bg;
        if (bg != null) {
            bg.setCallback(this);
        }
        if (this.mIsSplit) {
            if (this.mSplitBackground != null) {
                z = false;
            }
        } else if (!(this.mBackground == null && this.mStackedBackground == null)) {
            z = false;
        }
        setWillNotDraw(z);
        invalidate();
    }

    public void setStackedBackground(Drawable bg) {
        boolean z = true;
        if (this.mStackedBackground != null) {
            this.mStackedBackground.setCallback(null);
            unscheduleDrawable(this.mStackedBackground);
        }
        this.mStackedBackground = bg;
        if (bg != null) {
            bg.setCallback(this);
        }
        if (this.mIsSplit) {
            if (this.mSplitBackground != null) {
                z = false;
            }
        } else if (!(this.mBackground == null && this.mStackedBackground == null)) {
            z = false;
        }
        setWillNotDraw(z);
        invalidate();
    }

    public void setSplitBackground(Drawable bg) {
    }

    public void setVisibility(int visibility) {
        boolean isVisible;
        super.setVisibility(visibility);
        if (visibility == 0) {
            isVisible = true;
        } else {
            isVisible = false;
        }
        if (this.mBackground != null) {
            this.mBackground.setVisible(isVisible, false);
        }
        if (this.mStackedBackground != null) {
            this.mStackedBackground.setVisible(isVisible, false);
        }
    }

    protected boolean verifyDrawable(Drawable who) {
        return (who == this.mBackground && !this.mIsSplit) || ((who == this.mStackedBackground && this.mIsStacked) || ((who == this.mSplitBackground && this.mIsSplit) || super.verifyDrawable(who)));
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mBackground != null && this.mBackground.isStateful()) {
            this.mBackground.setState(getDrawableState());
        }
        if (this.mStackedBackground != null && this.mStackedBackground.isStateful()) {
            this.mStackedBackground.setState(getDrawableState());
        }
        if (this.mSplitBackground != null && this.mSplitBackground.isStateful()) {
            this.mSplitBackground.setState(getDrawableState());
        }
    }

    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mBackground != null) {
            this.mBackground.jumpToCurrentState();
        }
        if (this.mStackedBackground != null) {
            this.mStackedBackground.jumpToCurrentState();
        }
        if (this.mSplitBackground != null) {
            this.mSplitBackground.jumpToCurrentState();
        }
    }

    public void setTransitioning(boolean isTransitioning) {
        this.mIsTransitioning = isTransitioning;
        setDescendantFocusability(isTransitioning ? 393216 : 262144);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mIsTransitioning || super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    public boolean onHoverEvent(MotionEvent ev) {
        super.onHoverEvent(ev);
        return true;
    }

    public void setTabContainer(AmigoTabContainerView tabContainer) {
        if (this.mTabContainer != null) {
            removeView(this.mTabContainer);
        }
        this.mTabContainer = tabContainer;
        if (tabContainer != null) {
            addView(tabContainer);
            LayoutParams lp = tabContainer.getLayoutParams();
            lp.width = -1;
            lp.height = -2;
            tabContainer.setAllowCollapse(false);
        }
    }

    public View getTabContainer() {
        return this.mTabContainer;
    }

    public void onDraw(Canvas canvas) {
        if (getWidth() != 0 && getHeight() != 0) {
            if (!this.mIsSplit) {
                if (this.mBackground != null) {
                    if (this.mActionBarView != null) {
                        this.mBackground.setBounds(this.mActionBarView.getLeft(), this.mActionBarView.getTop(), this.mActionBarView.getRight(), this.mActionBarView.getBottom());
                    }
                    this.mBackground.draw(canvas);
                }
                if (this.mStackedBackground != null && this.mIsStacked) {
                    if (this.mTabContainer != null) {
                        this.mStackedBackground.setBounds(this.mTabContainer.getLeft(), this.mTabContainer.getTop(), this.mTabContainer.getRight(), this.mTabContainer.getBottom());
                    }
                    this.mStackedBackground.draw(canvas);
                }
            } else if (this.mSplitBackground != null) {
                this.mSplitBackground.draw(canvas);
            }
        }
    }

    public ActionMode startActionModeForChild(View child, Callback callback) {
        return null;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mActionBarView != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mActionBarView.getLayoutParams();
            int actionBarViewHeight = (this.mActionBarView.getMeasuredHeight() + lp.topMargin) + lp.bottomMargin;
            setMeasuredDimension(getMeasuredWidth(), MeasureSpec.makeMeasureSpec(actionBarViewHeight, Integer.MIN_VALUE));
            if (this.mTabContainer != null && this.mTabContainer.getVisibility() != 8 && MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE) {
                setMeasuredDimension(getMeasuredWidth(), Math.min(this.mTabContainer.getMeasuredHeight() + actionBarViewHeight, MeasureSpec.getSize(heightMeasureSpec)));
            }
        }
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInitActionBarBottom = getBottom();
        this.mInitActivityTop = this.mInitActionBarBottom;
        this.mExtraViewContainer.setBottom(0);
        if (this.mIsDragEnable) {
            this.mExtraView = this.mExtraViewContainer.getExtraView();
            if (this.mExtraView != null) {
                this.mExtraViewContainerHeight = this.mExtraView.getHeight();
            }
        }
        boolean hasTabs = (this.mTabContainer == null || this.mTabContainer.getVisibility() == 8) ? false : true;
        if (!(this.mTabContainer == null || this.mTabContainer.getVisibility() == 8)) {
            int containerHeight = getMeasuredHeight();
            int tabHeight = this.mTabContainer.getMeasuredHeight();
            if ((this.mActionBarView.getDisplayOptions() & 2) == 0) {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = getChildAt(i);
                    if (!(child == this.mTabContainer || this.mActionBarView.isCollapsed())) {
                        child.offsetTopAndBottom(tabHeight);
                    }
                }
                this.mTabContainer.layout(l, 0, r, tabHeight);
            } else {
                this.mTabContainer.layout(l, containerHeight - tabHeight, r, containerHeight);
            }
        }
        boolean needsInvalidate = false;
        if (!this.mIsSplit) {
            if (this.mBackground != null) {
                this.mBackground.setBounds(this.mActionBarView.getLeft(), this.mActionBarView.getTop(), this.mActionBarView.getRight(), this.mActionBarView.getBottom());
                needsInvalidate = true;
            }
            boolean z = hasTabs && this.mStackedBackground != null;
            this.mIsStacked = z;
            if (z) {
                this.mStackedBackground.setBounds(this.mTabContainer.getLeft(), this.mTabContainer.getTop(), this.mTabContainer.getRight(), this.mTabContainer.getBottom());
                needsInvalidate = true;
            }
        } else if (this.mSplitBackground != null) {
            this.mSplitBackground.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            needsInvalidate = true;
        }
        if (needsInvalidate) {
            invalidate();
        }
    }

    public void setDragEnable(boolean enable) {
        this.mIsDragEnable = enable;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!this.mIsDragEnable || this.mExtraView == null) {
            return super.dispatchTouchEvent(event);
        }
        switch (event.getAction() & 255) {
            case 0:
                if (this.mAnimator != null) {
                    this.mAnimator.cancel();
                }
                this.mOriY = event.getRawY();
                this.mOriBottom = getBottom();
                this.mOriIntellgentContainerBottom = this.mExtraViewContainer.getBottom();
                this.mOriTabTop = this.mActionBarView.getTop();
                this.mOriTabBottom = this.mActionBarView.getBottom();
                if (!(this.mHasEmbeddedTabs || this.mTabContainer == null)) {
                    this.mOriTabTop = this.mTabContainer.getTop();
                    this.mOriTabBottom = this.mTabContainer.getBottom();
                }
                this.mOriActivityContentTop = getActivityContent().getTop();
                if (this.mFisActivityContentTop == -1) {
                    this.mFisActivityContentTop = getActivityContent().getTop();
                    break;
                }
                break;
            case 1:
            case 3:
                if (getBottom() != this.mInitActionBarBottom) {
                    if (!this.mIsDragOpenEnd) {
                        actionbarCloseAnimation(this.mExtraViewContainer.getBottom());
                        break;
                    }
                }
                if (this.mTabContainer != null) {
                    this.mTabContainer.setClickable(true);
                }
                this.mActionBarView.setClickable(true);
                break;
                break;
            case 2:
                this.mDistance = (int) (event.getRawY() - this.mOriY);
                if (this.mDistance != 0) {
                    int bottom = this.mOriBottom + this.mDistance;
                    int tabContainerTop = this.mOriTabTop + this.mDistance;
                    int tabContainerBottom = this.mOriTabBottom + this.mDistance;
                    int activityTop = this.mOriActivityContentTop + this.mDistance;
                    int extraViewBottom = this.mOriIntellgentContainerBottom + this.mDistance;
                    if (bottom < this.mInitActionBarBottom) {
                        bottom = this.mInitActionBarBottom;
                        tabContainerTop = 0;
                        tabContainerBottom = this.mInitActionBarBottom;
                        activityTop = this.mFisActivityContentTop;
                        extraViewBottom = 0;
                        this.mIsDragCloseEnd = true;
                    } else if (this.mExtraViewContainerHeight <= 0 || bottom <= this.mExtraViewContainerHeight + this.mInitActionBarBottom) {
                        this.mIsDragOpenEnd = false;
                        this.mIsDragOpened = false;
                        this.mIsDragCloseEnd = false;
                        this.mIsDragClosed = false;
                    } else {
                        bottom = this.mExtraViewContainerHeight + this.mInitActionBarBottom;
                        tabContainerTop = this.mExtraViewContainerHeight;
                        tabContainerBottom = bottom;
                        activityTop = this.mExtraViewContainerHeight + this.mFisActivityContentTop;
                        extraViewBottom = this.mExtraViewContainerHeight;
                        this.mIsDragOpenEnd = true;
                        this.mDistance = this.mExtraViewContainerHeight;
                    }
                    setBottom(bottom);
                    getActivityContent().setTop(activityTop);
                    this.mExtraViewContainer.setBottom(extraViewBottom);
                    layoutExtraView(this.mExtraViewContainer.getHeight());
                    if (this.mTabContainer != null) {
                        this.mTabContainer.setTop(tabContainerTop);
                        this.mTabContainer.setBottom(tabContainerBottom);
                        this.mTabContainer.setClickable(false);
                    }
                    this.mActionBarView.setTop(tabContainerTop);
                    this.mActionBarView.setBottom(bottom);
                    this.mActionBarView.setClickable(false);
                    if (this.mDragListener != null) {
                        if (!this.mIsDragOpenEnd) {
                            if (!this.mIsDragCloseEnd) {
                                if (!this.mIsDragOpenStart) {
                                    this.mIsDragOpenStart = true;
                                    this.mDragListener.onDragOpenStart();
                                    break;
                                }
                                this.mDragListener.onDragUpdate(((float) extraViewBottom) / ((float) this.mExtraViewContainerHeight), extraViewBottom);
                                break;
                            } else if (!this.mIsDragClosed) {
                                this.mIsDragClosed = true;
                                this.mIsDragOpenStart = false;
                                this.mDragListener.onDragUpdate(0.0f, 0);
                                this.mDragListener.onDragCloseEnd();
                                break;
                            }
                        } else if (!this.mIsDragOpened) {
                            this.mIsDragOpened = true;
                            this.mIsDragOpenStart = false;
                            this.mDragListener.onDragUpdate(IPhotoView.DEFAULT_MIN_SCALE, extraViewBottom);
                            this.mDragListener.onDragOpenEnd();
                            break;
                        }
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AmigoActionBarPolicy policy = AmigoActionBarPolicy.get(this.mContext);
        this.mHasEmbeddedTabs = policy.hasEmbeddedTabs();
        if (this.mDragListener != null) {
            this.mDragListener.onDragUpdate(0.0f, 0);
            this.mDragListener.onDragCloseEnd();
        }
        this.mContainerHeight = policy.getTabContainerHeight();
    }

    private void layoutExtraView(int containerHeight) {
        int viewHeight = this.mExtraView.getHeight();
        int top = (containerHeight - viewHeight) / 2;
        int bottom = top + viewHeight;
        this.mExtraView.setTop(top);
        this.mExtraView.setBottom(bottom);
    }

    private void actionbarCloseAnimation(int y) {
        this.mAnimator = ValueAnimator.ofInt(new int[]{y, 0});
        this.mAnimator.setDuration(300);
        this.mAnimator.start();
        this.mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int y = ((Integer) animation.getAnimatedValue()).intValue();
                AmigoActionBarContainer.this.setBottom(AmigoActionBarContainer.this.mInitActionBarBottom + y);
                AmigoActionBarContainer.this.getActivityContent().setTop(AmigoActionBarContainer.this.mFisActivityContentTop + y);
                AmigoActionBarContainer.this.mExtraViewContainer.setBottom(y);
                AmigoActionBarContainer.this.layoutExtraView(AmigoActionBarContainer.this.mExtraViewContainer.getHeight());
                if (AmigoActionBarContainer.this.mTabContainer != null) {
                    AmigoActionBarContainer.this.mTabContainer.setTop(y);
                    AmigoActionBarContainer.this.mTabContainer.setBottom(AmigoActionBarContainer.this.mInitActionBarBottom + y);
                }
                AmigoActionBarContainer.this.mActionBarView.setTop(y);
                AmigoActionBarContainer.this.mActionBarView.setBottom(AmigoActionBarContainer.this.mInitActionBarBottom + y);
                if (AmigoActionBarContainer.this.mDragListener != null) {
                    AmigoActionBarContainer.this.mDragListener.onDragUpdate(((float) y) / ((float) AmigoActionBarContainer.this.mExtraViewContainerHeight), y);
                }
            }
        });
        this.mAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                if (AmigoActionBarContainer.this.mTabContainer != null) {
                    AmigoActionBarContainer.this.mTabContainer.setClickable(true);
                }
                AmigoActionBarContainer.this.mActionBarView.setClickable(true);
                if (AmigoActionBarContainer.this.mDragListener != null) {
                    AmigoActionBarContainer.this.mDragListener.onDragUpdate(0.0f, 0);
                    AmigoActionBarContainer.this.mDragListener.onDragCloseEnd();
                }
            }

            public void onAnimationCancel(Animator animation) {
                if (AmigoActionBarContainer.this.mTabContainer != null) {
                    AmigoActionBarContainer.this.mTabContainer.setClickable(true);
                }
                AmigoActionBarContainer.this.mActionBarView.setClickable(true);
                if (AmigoActionBarContainer.this.mDragListener != null) {
                    AmigoActionBarContainer.this.mDragListener.onDragUpdate(0.0f, 0);
                    AmigoActionBarContainer.this.mDragListener.onDragCloseEnd();
                }
            }
        });
    }

    public void setActivityContent(View view) {
        this.mActivityContent = view;
    }

    public View getActivityContent() {
        if (this.mActivityContent == null) {
            this.mActivityContent = ((Activity) this.mContext).findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_content"));
        }
        return this.mActivityContent;
    }

    public void setExtraView(View view) {
        this.mExtraViewContainer.setExtraView(view);
    }

    public void setOnExtraViewDragListener(OnExtraViewDragListener listener) {
        this.mDragListener = listener;
    }
}
