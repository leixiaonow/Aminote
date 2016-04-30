package com.amigoui.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.support.v7.view.menu.MenuBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gionee.aminote.R;

import amigoui.changecolors.ChameleonColorManager;
import amigoui.widget.AmigoWidgetResource;
import uk.co.senab.photoview.IPhotoView;

//import com.android.internal.view.menu.MenuBuilder;

public class AmigoActionBarContextView extends AmigoAbsActionBarView implements AnimatorListener {
    private static final int ANIMATE_IDLE = 0;
    private static final int ANIMATE_IN = 1;
    private static final int ANIMATE_OUT = 2;
    private static final String TAG = "ActionBarContextView";
    private boolean mAnimateInOnLayout;
    private int mAnimationMode;
    private View mClose;
    private Animator mCurrentAnimation;
    private View mCustomView;
    private MenuBuilder mMenu;
    private Drawable mSplitBackground;
    private CharSequence mSubtitle;
    private int mSubtitleStyleRes;
    private TextView mSubtitleView;
    private CharSequence mTitle;
    private LinearLayout mTitleLayout;
    private boolean mTitleOptional;
    private int mTitleStyleRes;
    private TextView mTitleView;

    public AmigoActionBarContextView(Context context) {
        this(context, null);
    }

    public AmigoActionBarContextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843668);
    }

    public AmigoActionBarContextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoActionBar, defStyle, 0);
        this.mTitleStyleRes = a.getResourceId(R.styleable.AmigoActionBar_amigotitleTextStyle, 0);
        this.mSubtitleStyleRes = a.getResourceId(R.styleable.AmigoActionBar_amigosubtitleTextStyle, 0);
        this.mContentHeight = a.getLayoutDimension(R.styleable.AmigoActionBar_amigoheight, 0);
        a.recycle();
        changeColors();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.hideOverflowMenu();
            this.mActionMenuPresenter.hideSubMenus();
        }
    }

    public void setContentHeight(int height) {
        this.mContentHeight = height;
    }

    public void setCustomView(View view) {
        if (this.mCustomView != null) {
            removeView(this.mCustomView);
        }
        this.mCustomView = view;
        if (this.mTitleLayout != null) {
            removeView(this.mTitleLayout);
            this.mTitleLayout = null;
        }
        if (view != null) {
            addView(view);
        }
        requestLayout();
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        initTitle();
    }

    public void setSubtitle(CharSequence subtitle) {
        this.mSubtitle = subtitle;
        initTitle();
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    private void initTitle() {
        boolean hasTitle;
        boolean hasSubtitle;
        int i;
        int i2 = 8;
        if (this.mTitleLayout == null) {
            LayoutInflater.from(getContext()).inflate(AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_action_bar_title_item"), this);
            this.mTitleLayout = (LinearLayout) getChildAt(getChildCount() - 1);
            this.mTitleView = (TextView) this.mTitleLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_bar_title"));
            this.mSubtitleView = (TextView) this.mTitleLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_bar_subtitle"));
            if (this.mTitleStyleRes != 0) {
                this.mTitleView.setTextAppearance(this.mContext, this.mTitleStyleRes);
            }
            if (this.mSubtitleStyleRes != 0) {
                this.mSubtitleView.setTextAppearance(this.mContext, this.mSubtitleStyleRes);
            }
        }
        this.mTitleView.setText(this.mTitle);
        this.mSubtitleView.setText(this.mSubtitle);
        if (TextUtils.isEmpty(this.mTitle)) {
            hasTitle = false;
        } else {
            hasTitle = true;
        }
        if (TextUtils.isEmpty(this.mSubtitle)) {
            hasSubtitle = false;
        } else {
            hasSubtitle = true;
        }
        TextView textView = this.mSubtitleView;
        if (hasSubtitle) {
            i = 0;
        } else {
            i = 8;
        }
        textView.setVisibility(i);
        LinearLayout linearLayout = this.mTitleLayout;
        if (hasTitle || hasSubtitle) {
            i2 = 0;
        }
        linearLayout.setVisibility(i2);
        if (this.mTitleLayout.getParent() == null) {
            addView(this.mTitleLayout);
        }
    }

    public void initForMode(final ActionMode mode) {
        if (this.mClose == null) {
            this.mClose = LayoutInflater.from(this.mContext).inflate(AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_action_mode_close_item"), this, false);
            addView(this.mClose);
        } else if (this.mClose.getParent() == null) {
            addView(this.mClose);
        }
        View closeButton = this.mClose.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_mode_close_button"));
        if (ChameleonColorManager.isNeedChangeColor()) {
            this.mClose.setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
            Drawable closeDrawable = ((ImageView) this.mClose.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_mode_close_imageview"))).getDrawable();
            if (closeDrawable != null) {
                closeDrawable.setColorFilter(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(), Mode.SRC_IN);
            }
        }
        closeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mode.finish();
            }
        });
        this.mMenu = (MenuBuilder) mode.getMenu();
        this.mAnimateInOnLayout = true;
        this.mIsActionMode = true;
    }

    public void closeMode() {
        if (this.mAnimationMode != 2) {
            if (this.mClose == null) {
                killMode();
                return;
            }
            finishAnimation();
            this.mAnimationMode = 2;
            this.mCurrentAnimation = makeOutAnimation();
            this.mCurrentAnimation.start();
        }
    }

    private void finishAnimation() {
        Animator a = this.mCurrentAnimation;
        if (a != null) {
            this.mCurrentAnimation = null;
            a.end();
        }
    }

    public void killMode() {
        finishAnimation();
        removeAllViews();
        if (this.mSplitView != null) {
            this.mSplitView.removeView(this.mMenuView);
        }
        this.mCustomView = null;
        this.mMenuView = null;
        this.mAnimateInOnLayout = false;
    }

    public boolean showOverflowMenu() {
        if (this.mActionMenuPresenter != null) {
            return this.mActionMenuPresenter.showOverflowMenu();
        }
        return false;
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

    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(-1, -2);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_width=\"match_parent\" (or fill_parent)");
        } else if (MeasureSpec.getMode(heightMeasureSpec) == 0) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_height=\"wrap_content\"");
        } else {
            int contentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int maxHeight = this.mContentHeight > 0 ? this.mContentHeight : MeasureSpec.getSize(heightMeasureSpec);
            int verticalPadding = getPaddingTop() + getPaddingBottom();
            int availableWidth = (contentWidth - getPaddingLeft()) - getPaddingRight();
            int height = maxHeight - verticalPadding;
            int childSpecHeight = MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
            if (this.mClose != null) {
                MarginLayoutParams lp = (MarginLayoutParams) this.mClose.getLayoutParams();
                availableWidth = measureChildView(this.mClose, availableWidth, childSpecHeight, 0) - (lp.leftMargin + lp.rightMargin);
            }
            if (this.mMenuView != null && this.mMenuView.getParent() == this) {
                availableWidth = measureChildView(this.mMenuView, availableWidth, childSpecHeight, 0);
            }
            if (this.mTitleLayout != null && this.mCustomView == null) {
                if (this.mTitleOptional) {
                    this.mTitleLayout.measure(MeasureSpec.makeMeasureSpec(0, 0), childSpecHeight);
                    int titleWidth = this.mTitleLayout.getMeasuredWidth();
                    boolean titleFits = titleWidth <= availableWidth;
                    if (titleFits) {
                        availableWidth -= titleWidth;
                    }
                    this.mTitleLayout.setVisibility(titleFits ? 0 : 8);
                } else {
                    availableWidth = measureChildView(this.mTitleLayout, availableWidth, childSpecHeight, 0);
                }
            }
            if (this.mCustomView != null) {
                int customWidth;
                int customHeight;
                LayoutParams lp2 = this.mCustomView.getLayoutParams();
                int customWidthMode = lp2.width != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp2.width >= 0) {
                    customWidth = Math.min(lp2.width, availableWidth);
                } else {
                    customWidth = availableWidth;
                }
                int customHeightMode = lp2.height != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp2.height >= 0) {
                    customHeight = Math.min(lp2.height, height);
                } else {
                    customHeight = height;
                }
                this.mCustomView.measure(MeasureSpec.makeMeasureSpec(customWidth, customWidthMode), MeasureSpec.makeMeasureSpec(customHeight, customHeightMode));
            }
            if (this.mContentHeight <= 0) {
                int measuredHeight = 0;
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    int paddedViewHeight = getChildAt(i).getMeasuredHeight() + verticalPadding;
                    if (paddedViewHeight > measuredHeight) {
                        measuredHeight = paddedViewHeight;
                    }
                }
                setMeasuredDimension(contentWidth, measuredHeight);
                return;
            }
            setMeasuredDimension(contentWidth, maxHeight);
        }
    }

    private Animator makeInAnimation() {
        this.mClose.setTranslationX((float) ((-this.mClose.getWidth()) - ((MarginLayoutParams) this.mClose.getLayoutParams()).leftMargin));
        ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(this.mClose, "translationX", new float[]{0.0f});
        buttonAnimator.setDuration(200);
        buttonAnimator.addListener(this);
        buttonAnimator.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set = new AnimatorSet();
        Builder b = set.play(buttonAnimator);
        if (this.mMenuView != null) {
            int count = this.mMenuView.getChildCount();
            if (count > 0) {
                int i = count - 1;
                int j = 0;
                while (i >= 0) {
                    View child = this.mMenuView.getChildAt(i);
                    child.setScaleY(0.0f);
                    ObjectAnimator a = ObjectAnimator.ofFloat(child, "scaleY", new float[]{0.0f, IPhotoView.DEFAULT_MIN_SCALE});
                    a.setDuration(300);
                    b.with(a);
                    i--;
                    j++;
                }
            }
        }
        return set;
    }

    private Animator makeOutAnimation() {
        ObjectAnimator buttonAnimator = ObjectAnimator.ofFloat(this.mClose, "translationX", new float[]{(float) ((-this.mClose.getWidth()) - ((MarginLayoutParams) this.mClose.getLayoutParams()).leftMargin)});
        buttonAnimator.setDuration(200);
        buttonAnimator.addListener(this);
        buttonAnimator.setInterpolator(new DecelerateInterpolator());
        AnimatorSet set = new AnimatorSet();
        Builder b = set.play(buttonAnimator);
        if (this.mMenuView != null && this.mMenuView.getChildCount() > 0) {
            for (int i = 0; i < 0; i++) {
                View child = this.mMenuView.getChildAt(i);
                child.setScaleY(0.0f);
                ObjectAnimator a = ObjectAnimator.ofFloat(child, "scaleY", new float[]{0.0f});
                a.setDuration(300);
                b.with(a);
            }
        }
        return set;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean isLayoutRtl = isLayoutRtl();
        int x = isLayoutRtl ? (r - l) - getPaddingRight() : getPaddingLeft();
        int y = getPaddingTop();
        int contentHeight = ((b - t) - getPaddingTop()) - getPaddingBottom();
        if (!(this.mClose == null || this.mClose.getVisibility() == 8)) {
            MarginLayoutParams lp = (MarginLayoutParams) this.mClose.getLayoutParams();
            int startMargin = isLayoutRtl ? lp.rightMargin : lp.leftMargin;
            int endMargin = isLayoutRtl ? lp.leftMargin : lp.rightMargin;
            x = AmigoAbsActionBarView.next(x, startMargin, isLayoutRtl);
            x = AmigoAbsActionBarView.next(x + positionChild(this.mClose, x, y, contentHeight, isLayoutRtl), endMargin, isLayoutRtl);
            if (this.mAnimateInOnLayout) {
                this.mAnimationMode = 1;
                this.mCurrentAnimation = makeInAnimation();
                this.mCurrentAnimation.start();
                this.mAnimateInOnLayout = false;
            }
        }
        if (!(this.mTitleLayout == null || this.mCustomView != null || this.mTitleLayout.getVisibility() == 8)) {
            x += positionChild(this.mTitleLayout, x, y, contentHeight, isLayoutRtl);
        }
        if (this.mCustomView != null) {
            x += positionChild(this.mCustomView, x, y, contentHeight, isLayoutRtl);
        }
        x = isLayoutRtl ? getPaddingLeft() : (r - l) - getPaddingRight();
        if (this.mMenuView != null) {
            x += positionChild(this.mMenuView, x, y, contentHeight, !isLayoutRtl);
        }
    }

    public void onAnimationStart(Animator animation) {
    }

    public void onAnimationEnd(Animator animation) {
        if (this.mAnimationMode == 2) {
            killMode();
        }
        this.mAnimationMode = 0;
    }

    public void onAnimationCancel(Animator animation) {
    }

    public void onAnimationRepeat(Animator animation) {
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == 32) {
            event.setSource(this);
            event.setClassName(getClass().getName());
            event.setPackageName(getContext().getPackageName());
            event.setContentDescription(this.mTitle);
            return;
        }
        super.onInitializeAccessibilityEvent(event);
    }

    public void setTitleOptional(boolean titleOptional) {
        if (titleOptional != this.mTitleOptional) {
            requestLayout();
        }
        this.mTitleOptional = titleOptional;
    }

    public boolean isTitleOptional() {
        return this.mTitleOptional;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return true;
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public boolean isActionModeShowing() {
        return this.mIsActionModeShowing;
    }

    public void changeColors() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            setBackgroundColor(ChameleonColorManager.getAppbarColor_A1());
        }
    }
}
