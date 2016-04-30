package com.amigoui.internal.widget;

import amigoui.app.AmigoActionBar.Tab;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.changecolors.IChangeColors;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.amigoui.internal.view.AmigoActionBarPolicy;
import uk.co.senab.photoview.IPhotoView;

public class AmigoTabContainerView extends RelativeLayout implements IChangeColors {
    private static final int FADE_DURATION = 200;
    private static final int INDICATORVIEW_MARGIN_BOTTOM = 3;
    private static final int INDICATORVIEW_MARGIN_LEFT = 6;
    private static final TimeInterpolator sAlphaInterpolator = new DecelerateInterpolator();
    private final int ANIM_DURATION = 200;
    private final int INDICATOR_DEFAULT_HEIGHT = 2;
    private final int INDICATOR_DEFAULT_WIDTH = 10;
    private boolean mActionBarOverlay = false;
    private boolean mAllowCollapse;
    private int mAnimatingHeightOffset;
    private boolean mClickable = true;
    private int mContentHeight;
    private RelativeLayout mContentLayout;
    private Context mContext;
    private int mCurrentIndex = 0;
    private int mIndicatorMarginLeft;
    private int mIndicatorTop;
    private int mIndicatroWidth;
    private int mMaxTabWidth;
    private int mPrevSelected = -1;
    private int mStackedTabMaxWidth;
    private TabClickListener mTabClickListener;
    private AmigoTabIndicator mTabLayout;
    private TabTouchListener mTabTouchListener;
    private boolean mToLeft = false;
    private boolean mToRight = false;
    protected final VisibilityAnimListener mVisAnimListener = new VisibilityAnimListener();
    protected Animator mVisibilityAnim;
    private float oriX;

    private class TabClickListener implements OnClickListener {
        private TabClickListener() {
        }

        public void onClick(View view) {
            ((TabView) view).getTab().select();
            int tabCount = AmigoTabContainerView.this.mTabLayout.getChildCount();
            for (int i = 0; i < tabCount; i++) {
                View child = AmigoTabContainerView.this.mTabLayout.getChildAt(i);
                child.setSelected(child == view);
            }
        }
    }

    private class TabTouchListener implements OnTouchListener {
        private TabTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & 255) {
                case 1:
                    if (!AmigoTabContainerView.this.mClickable) {
                        return true;
                    }
                    break;
            }
            if (AmigoTabContainerView.this.mClickable) {
            }
            return false;
        }
    }

    protected class VisibilityAnimListener implements AnimatorListener {
        private boolean mCanceled = false;
        private int mFinalVisibility;

        protected VisibilityAnimListener() {
        }

        public VisibilityAnimListener withFinalVisibility(int visibility) {
            this.mFinalVisibility = visibility;
            return this;
        }

        public void onAnimationStart(Animator animation) {
            AmigoTabContainerView.this.setVisibility(0);
            AmigoTabContainerView.this.mVisibilityAnim = animation;
            this.mCanceled = false;
        }

        public void onAnimationEnd(Animator animation) {
            if (!this.mCanceled) {
                AmigoTabContainerView.this.mVisibilityAnim = null;
                AmigoTabContainerView.this.setVisibility(this.mFinalVisibility);
            }
        }

        public void onAnimationCancel(Animator animation) {
            this.mCanceled = true;
        }

        public void onAnimationRepeat(Animator animation) {
        }
    }

    private class TabView extends LinearLayout implements OnLongClickListener, IChangeColors {
        private View mCustomView;
        private ImageView mIconView;
        private Tab mTab;
        private TextView mTextView;

        public TabView(Context context, Tab tab, boolean forList) {
            super(context, null, 16843507);
            this.mTab = tab;
            if (forList) {
                setGravity(8388627);
            } else {
                setGravity(17);
            }
            update();
            changeColors();
        }

        public void bindTab(Tab tab) {
            this.mTab = tab;
            update();
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (AmigoTabContainerView.this.mMaxTabWidth > 0 && getMeasuredWidth() > AmigoTabContainerView.this.mMaxTabWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(AmigoTabContainerView.this.mMaxTabWidth, 1073741824);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        public void update() {
            Tab tab = this.mTab;
            View custom = tab.getCustomView();
            if (custom != null) {
                TabView customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        customParent.removeView(custom);
                    }
                    addView(custom);
                }
                this.mCustomView = custom;
                if (this.mTextView != null) {
                    this.mTextView.setVisibility(8);
                }
                if (this.mIconView != null) {
                    this.mIconView.setVisibility(8);
                    this.mIconView.setImageDrawable(null);
                    return;
                }
                return;
            }
            boolean hasText;
            if (this.mCustomView != null) {
                removeView(this.mCustomView);
                this.mCustomView = null;
            }
            Drawable icon = tab.getIcon();
            CharSequence text = tab.getText();
            if (icon != null) {
                if (this.mIconView == null) {
                    ImageView iconView = new ImageView(getContext());
                    LayoutParams lp = new LayoutParams(-2, -2);
                    lp.gravity = 16;
                    iconView.setLayoutParams(lp);
                    addView(iconView, 0);
                    this.mIconView = iconView;
                }
                this.mIconView.setImageDrawable(icon);
                this.mIconView.setVisibility(0);
            } else if (this.mIconView != null) {
                this.mIconView.setVisibility(8);
                this.mIconView.setImageDrawable(null);
            }
            if (TextUtils.isEmpty(text)) {
                hasText = false;
            } else {
                hasText = true;
            }
            if (hasText) {
                if (this.mTextView == null) {
                    TextView textView = new TextView(getContext(), null, 16843509);
                    textView.setEllipsize(TruncateAt.END);
                    lp = new LayoutParams(-2, -2);
                    lp.gravity = 16;
                    textView.setLayoutParams(lp);
                    addView(textView);
                    this.mTextView = textView;
                }
                this.mTextView.setText(text);
                this.mTextView.setVisibility(0);
            } else if (this.mTextView != null) {
                this.mTextView.setVisibility(8);
                this.mTextView.setText(null);
            }
            if (this.mIconView != null) {
                this.mIconView.setContentDescription(tab.getContentDescription());
            }
            if (hasText || TextUtils.isEmpty(tab.getContentDescription())) {
                setOnLongClickListener(null);
                setLongClickable(false);
                return;
            }
            setOnLongClickListener(this);
        }

        public boolean onLongClick(View v) {
            int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);
            Context context = getContext();
            int width = getWidth();
            int height = getHeight();
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, this.mTab.getContentDescription(), 0);
            cheatSheet.setGravity(49, (screenPos[0] + (width / 2)) - (screenWidth / 2), height);
            cheatSheet.show();
            return true;
        }

        public Tab getTab() {
            return this.mTab;
        }

        public void setSelected(boolean selected) {
            super.setSelected(selected);
            if (ChameleonColorManager.isNeedChangeColor() && this.mTextView != null && !AmigoTabContainerView.this.mActionBarOverlay) {
                if (selected) {
                    this.mTextView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
                } else {
                    this.mTextView.setTextColor(ChameleonColorManager.getContentColorThirdlyOnAppbar_T3());
                }
            }
        }

        public void changeColors() {
            if (ChameleonColorManager.isNeedChangeColor() && !AmigoTabContainerView.this.mActionBarOverlay) {
                Drawable background = getBackground();
                if (background != null) {
                    background.setColorFilter(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1(), Mode.SRC_IN);
                }
                if (this.mTextView != null) {
                    this.mTextView.setTextColor(ChameleonColorManager.getContentColorThirdlyOnAppbar_T3());
                }
            }
        }
    }

    public AmigoTabContainerView(Context context) {
        super(context);
        this.mContext = context;
        setContentHeight(AmigoActionBarPolicy.get(context).getTabContainerHeight());
        this.mTabLayout = createTabLayout();
        addView(this.mTabLayout);
        this.mIndicatorMarginLeft = (int) (6.0f * context.getResources().getDisplayMetrics().density);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int childCount = this.mTabLayout.getChildCount();
        if (childCount <= 1 || !(widthMode == 1073741824 || widthMode == Integer.MIN_VALUE)) {
            this.mMaxTabWidth = -1;
        } else {
            if (childCount > 2) {
                this.mMaxTabWidth = (int) (((float) MeasureSpec.getSize(widthMeasureSpec)) * 0.4f);
            } else {
                this.mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec) / 2;
            }
            this.mMaxTabWidth = Math.min(this.mMaxTabWidth, this.mStackedTabMaxWidth);
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824);
        this.mTabLayout.measure(0, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AmigoActionBarPolicy abp = AmigoActionBarPolicy.get(this.mContext);
        this.mStackedTabMaxWidth = abp.getStackedTabMaxWidth();
        setContentHeight(abp.getTabContainerHeight());
    }

    public void setTabSelected(int position) {
        Log.v("AmigotabContainerView", "setTabSelected position=" + position + " mPrevSelected=" + this.mPrevSelected);
        View selectedChild = this.mTabLayout.getChildAt(position);
        if (selectedChild != null) {
            View prevChild = this.mTabLayout.getChildAt(this.mPrevSelected);
            if (prevChild != null) {
                prevChild.setSelected(false);
            }
            selectedChild.setSelected(true);
            this.mPrevSelected = position;
        }
    }

    public void setContentHeight(int contentHeight) {
        this.mContentHeight = contentHeight;
        requestLayout();
    }

    public void addTab(Tab tab, boolean setSelected) {
        TabView tabView = createTabView(tab, false);
        this.mTabLayout.addView(tabView, new LayoutParams(0, -1, IPhotoView.DEFAULT_MIN_SCALE));
        if (this.mPrevSelected < 0) {
            this.mPrevSelected = 0;
        }
        if (setSelected) {
            tabView.setSelected(true);
        }
    }

    public void addTab(Tab tab, int position, boolean setSelected) {
        TabView tabView = createTabView(tab, false);
        this.mTabLayout.addView(tabView, position, new LayoutParams(0, -1, IPhotoView.DEFAULT_MIN_SCALE));
        if (setSelected) {
            tabView.setSelected(true);
        }
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void updateTab(int position) {
        ((TabView) this.mTabLayout.getChildAt(position)).update();
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    private TabView createTabView(Tab tab, boolean forAdapter) {
        TabView tabView = new TabView(getContext(), tab, forAdapter);
        if (forAdapter) {
            tabView.setBackgroundDrawable(null);
            tabView.setLayoutParams(new AbsListView.LayoutParams(-1, this.mContentHeight));
        } else {
            tabView.setFocusable(true);
            if (this.mTabClickListener == null) {
                this.mTabClickListener = new TabClickListener();
            }
            tabView.setOnClickListener(this.mTabClickListener);
            if (this.mTabTouchListener == null) {
                this.mTabTouchListener = new TabTouchListener();
            }
            tabView.setOnTouchListener(this.mTabTouchListener);
        }
        return tabView;
    }

    private AmigoTabIndicator createTabLayout() {
        AmigoTabIndicator tabLayout = new AmigoTabIndicator(getContext(), null, 16843508);
        tabLayout.setMeasureWithLargestChildEnabled(true);
        tabLayout.setGravity(17);
        tabLayout.setLayoutParams(new LayoutParams(-1, -1));
        return tabLayout;
    }

    public void setClickable(boolean clickable) {
        this.mClickable = clickable;
    }

    public void onPageScrolled(int position, float percent, int offset) {
        int tabLayoutChildCount = this.mTabLayout.getChildCount();
        if (tabLayoutChildCount != 0 && position >= 0 && position < tabLayoutChildCount) {
            this.mTabLayout.onPageScrolled(position, percent, offset);
        }
    }

    public void setIndicatorBackgroundColor(int color) {
        if (this.mTabLayout != null) {
            this.mTabLayout.setIndicatorBackgroundColor(color);
        }
    }

    public void removeTabAt(int position) {
        this.mTabLayout.removeViewAt(position);
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void removeAllTabs() {
        this.mTabLayout.removeAllViews();
        if (this.mAllowCollapse) {
            requestLayout();
        }
    }

    public void animateToVisibility(int visibility) {
        if (this.mVisibilityAnim != null) {
            this.mVisibilityAnim.cancel();
        }
        if (visibility == 0) {
            if (getVisibility() != 0) {
                setAlpha(0.0f);
            }
            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", new float[]{IPhotoView.DEFAULT_MIN_SCALE});
            anim.setDuration(200);
            anim.setInterpolator(sAlphaInterpolator);
            anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
            anim.start();
            return;
        }
        anim = ObjectAnimator.ofFloat(this, "alpha", new float[]{0.0f});
        anim.setDuration(200);
        anim.setInterpolator(sAlphaInterpolator);
        anim.addListener(this.mVisAnimListener.withFinalVisibility(visibility));
        anim.start();
    }

    public void setAllowCollapse(boolean allowCollapse) {
        this.mAllowCollapse = allowCollapse;
    }

    public void changeColors() {
        int primaryBackgroundColor = ChameleonColorManager.getAppbarColor_A1();
    }

    public void setActionBarOverlay(boolean overlay) {
        this.mActionBarOverlay = overlay;
    }
}
