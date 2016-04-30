package com.amigoui.internal.widget;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v7.view.menu.ActionMenuItem;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window.Callback;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.gionee.aminote.R;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActionBar.OnNavigationListener;
import amigoui.app.AmigoActivity;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.preference.AmigoPreference;
import amigoui.reflection.AmigoActionMenuPresenter;
import amigoui.widget.AmigoWidgetResource;

//import com.gionee.aminote.R;
//import com.android.internal.view.menu.ActionMenuItem;
//import com.android.internal.view.menu.MenuBuilder;
//import com.android.internal.view.menu.MenuItemImpl;
//import com.android.internal.view.menu.MenuPresenter;
//import com.android.internal.view.menu.MenuView;
//import com.android.internal.view.menu.SubMenuBuilder;

public class AmigoActionBarView extends AmigoAbsActionBarView {
    private static final int CLICK_INTERVAL_TIME = 500;
    private static final int DEFAULT_CUSTOM_GRAVITY = 8388627;
    public static final int DISPLAY_DEFAULT = 0;
    private static final int DISPLAY_RELAYOUT_MASK = 31;
    private static final int MAX_HOME_SLOP = 32;
    private static final String TAG = "ActionBarView";
    private OnClickListener mActionBarDoubleClickListener;
    private AmigoActivity mActivity;
    private OnClickListener mBackClickListener;
    private OnNavigationListener mCallback;
    private boolean mClickable = true;
    private AmigoActionBarContextView mContextView;
    private long mCurTime = 0;
    private View mCustomNavView;
    private int mDisplayOptions = -1;
    private View mEmptyView;
    View mExpandedActionView;
    private final OnClickListener mExpandedActionViewUpListener = new OnClickListener() {
        public void onClick(View v) {
            MenuItemImpl item = AmigoActionBarView.this.mExpandedMenuPresenter.mCurrentExpandedItem;
            if (item != null) {
                item.collapseActionView();
            }
        }
    };
    private HomeView mExpandedHomeLayout;
    private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private HomeView mHomeLayout;
    private final int mHomeResId;
    private Drawable mIcon;
    private boolean mIncludeTabs;
    private int mIndeterminateProgressStyle;
    private ProgressBar mIndeterminateProgressView;
    private boolean mIsCollapsable;
    private boolean mIsCollapsed;
    private int mItemPadding;
    private LinearLayout mListNavLayout;
    private Drawable mLogo;
    private ActionMenuItem mLogoNavItem;
    private int mMaxHomeSlop;
    private final OnItemSelectedListener mNavItemSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView parent, View view, int position, long id) {
            if (AmigoActionBarView.this.mCallback != null) {
                AmigoActionBarView.this.mCallback.onNavigationItemSelected(position, id);
            }
        }

        public void onNothingSelected(AdapterView parent) {
        }
    };
    private int mNavigationMode;
    private MenuBuilder mOptionsMenu;
    private int mProgressBarPadding;
    private int mProgressStyle;
    private ProgressBar mProgressView;
    private Spinner mSpinner;
    private SpinnerAdapter mSpinnerAdapter;
    private CharSequence mSubtitle;
    private int mSubtitleStyleRes;
    private TextView mSubtitleView;
    private AmigoTabContainerView mTabContainer;
    private Runnable mTabSelector;
    private final Rect mTempRect = new Rect();
    private CharSequence mTitle;
    private LinearLayout mTitleLayout;
    private int mTitleStyleRes;
    private View mTitleUpView;
    private TextView mTitleView;
    private final OnClickListener mUpClickListener = new OnClickListener() {
        public void onClick(View v) {
            if ((AmigoActionBarView.this.mDisplayOptions & 4) != 0) {
                if (AmigoActionBarView.this.mBackClickListener != null) {
                    AmigoActionBarView.this.mBackClickListener.onClick(v);
                } else if (AmigoActionBarView.this.mActivity != null) {
                    AmigoActionBarView.this.mActivity.finish();
                }
            } else if (AmigoActionBarView.this.mWindowCallback != null) {
                AmigoActionBarView.this.mWindowCallback.onMenuItemSelected(0, AmigoActionBarView.this.mLogoNavItem);
            }
        }
    };
    private boolean mUserTitle;
    Callback mWindowCallback;

    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        private ExpandedActionViewMenuPresenter() {
        }

        public void initForMenu(Context context, MenuBuilder menu) {
            if (!(this.mMenu == null || this.mCurrentExpandedItem == null)) {
                this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
            }
            this.mMenu = menu;
        }

        public MenuView getMenuView(ViewGroup root) {
            return null;
        }

        public void updateMenuView(boolean cleared) {
            if (this.mCurrentExpandedItem != null) {
                boolean found = false;
                if (this.mMenu != null) {
                    int count = this.mMenu.size();
                    for (int i = 0; i < count; i++) {
                        if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
                }
            }
        }

        public void setCallback(MenuPresenter.Callback cb) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            return false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean flagActionItems() {
            return false;
        }

        public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
            AmigoActionBarView.this.mExpandedActionView = item.getActionView();
            if (AmigoActionBarView.this.mIcon != null) {
                AmigoActionBarView.this.mExpandedHomeLayout.setIcon(AmigoActionBarView.this.mIcon.getConstantState().newDrawable(AmigoActionBarView.this.getResources()));
            }
            this.mCurrentExpandedItem = item;
            if (AmigoActionBarView.this.mExpandedActionView.getParent() != AmigoActionBarView.this) {
                AmigoActionBarView.this.addView(AmigoActionBarView.this.mExpandedActionView);
            }
            if (AmigoActionBarView.this.mExpandedHomeLayout.getParent() != AmigoActionBarView.this) {
                AmigoActionBarView.this.addView(AmigoActionBarView.this.mExpandedHomeLayout);
            }
            AmigoActionBarView.this.mHomeLayout.setVisibility(8);
            if (AmigoActionBarView.this.mTitleLayout != null) {
                AmigoActionBarView.this.mTitleLayout.setVisibility(8);
            }
            if (AmigoActionBarView.this.mTabContainer != null) {
                AmigoActionBarView.this.mTabContainer.setVisibility(8);
            }
            if (AmigoActionBarView.this.mSpinner != null) {
                AmigoActionBarView.this.mSpinner.setVisibility(8);
            }
            if (AmigoActionBarView.this.mCustomNavView != null) {
                AmigoActionBarView.this.mCustomNavView.setVisibility(8);
            }
            AmigoActionBarView.this.requestLayout();
            item.setActionViewExpanded(true);
            if (AmigoActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) AmigoActionBarView.this.mExpandedActionView).onActionViewExpanded();
            }
            return true;
        }

        public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
            if (AmigoActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) AmigoActionBarView.this.mExpandedActionView).onActionViewCollapsed();
            }
            AmigoActionBarView.this.removeView(AmigoActionBarView.this.mExpandedActionView);
            AmigoActionBarView.this.removeView(AmigoActionBarView.this.mExpandedHomeLayout);
            AmigoActionBarView.this.mExpandedActionView = null;
            if ((AmigoActionBarView.this.mDisplayOptions & 2) != 0) {
                AmigoActionBarView.this.mHomeLayout.setVisibility(0);
            }
            if ((AmigoActionBarView.this.mDisplayOptions & 8) != 0) {
                if (AmigoActionBarView.this.mTitleLayout == null) {
                    AmigoActionBarView.this.initTitle();
                } else {
                    AmigoActionBarView.this.mTitleLayout.setVisibility(0);
                }
            }
            if (AmigoActionBarView.this.mTabContainer != null && AmigoActionBarView.this.mNavigationMode == 2) {
                AmigoActionBarView.this.mTabContainer.setVisibility(0);
            }
            if (AmigoActionBarView.this.mSpinner != null && AmigoActionBarView.this.mNavigationMode == 1) {
                AmigoActionBarView.this.mSpinner.setVisibility(0);
            }
            if (!(AmigoActionBarView.this.mCustomNavView == null || (AmigoActionBarView.this.mDisplayOptions & 16) == 0)) {
                AmigoActionBarView.this.mCustomNavView.setVisibility(0);
            }
            AmigoActionBarView.this.mExpandedHomeLayout.setIcon(null);
            this.mCurrentExpandedItem = null;
            AmigoActionBarView.this.requestLayout();
            item.setActionViewExpanded(false);
            return true;
        }

        public int getId() {
            return 0;
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onRestoreInstanceState(Parcelable state) {
        }
    }

    @SuppressLint({"Instantiatable"})
    private static class HomeView extends FrameLayout {
        private static final long DEFAULT_TRANSITION_DURATION = 150;
        private OnClickListener mBackButtonlistener;
        private ImageView mIconView;
        private View mUpView;
        private int mUpWidth;

        public HomeView(Context context) {
            this(context, null);
        }

        public HomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutTransition t = getLayoutTransition();
            if (t != null) {
                t.setDuration(DEFAULT_TRANSITION_DURATION);
            }
        }

        public void setUp(boolean isUp) {
            int i;
            int i2 = 8;
            View view = this.mUpView;
            if (isUp) {
                i = 0;
            } else {
                i = 8;
            }
            view.setVisibility(i);
            ImageView imageView = this.mIconView;
            if (!isUp) {
                i2 = 0;
            }
            imageView.setVisibility(i2);
        }

        public void setIcon(Drawable icon) {
            if (icon != null) {
                this.mIconView.setImageDrawable(icon);
            }
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            onPopulateAccessibilityEvent(event);
            return true;
        }

        public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(event);
            CharSequence cdesc = getContentDescription();
            if (!TextUtils.isEmpty(cdesc)) {
                event.getText().add(cdesc);
            }
        }

        public boolean dispatchHoverEvent(MotionEvent event) {
            return onHoverEvent(event);
        }

        protected void onFinishInflate() {
            this.mUpView = findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_up"));
            this.mIconView = (ImageView) findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_home"));
        }

        public void changeColor() {
        }

        public int getStartOffset() {
            return 0;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            measureChildWithMargins(this.mUpView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            LayoutParams upLp = (LayoutParams) this.mUpView.getLayoutParams();
            this.mUpWidth = (upLp.leftMargin + this.mUpView.getMeasuredWidth()) + upLp.rightMargin;
            int width = this.mUpView.getVisibility() == 8 ? 0 : this.mUpWidth;
            int height = (upLp.topMargin + this.mUpView.getMeasuredHeight()) + upLp.bottomMargin;
            if (this.mIconView.getVisibility() != 8) {
                measureChildWithMargins(this.mIconView, widthMeasureSpec, width, heightMeasureSpec, 0);
            }
            LayoutParams iconLp = (LayoutParams) this.mIconView.getLayoutParams();
            width += (iconLp.leftMargin + this.mIconView.getMeasuredWidth()) + iconLp.rightMargin;
            height = Math.max(height, (iconLp.topMargin + this.mIconView.getMeasuredHeight()) + iconLp.bottomMargin);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            switch (widthMode) {
                case Integer.MIN_VALUE:
                    width = Math.min(width, widthSize);
                    break;
                case 1073741824:
                    width = widthSize;
                    break;
            }
            switch (heightMode) {
                case Integer.MIN_VALUE:
                    height = Math.min(height, heightSize);
                    break;
                case 1073741824:
                    height = heightSize;
                    break;
            }
            setMeasuredDimension(width, height);
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int iconRight;
            int iconLeft;
            int vCenter = (b - t) / 2;
            boolean isLayoutRtl = isLayoutRtl();
            int width = getWidth();
            int upOffset = 0;
            if (this.mUpView.getVisibility() != 8) {
                int upRight;
                int upLeft;
                LayoutParams upLp = (LayoutParams) this.mUpView.getLayoutParams();
                int upHeight = this.mUpView.getMeasuredHeight();
                int upWidth = this.mUpView.getMeasuredWidth();
                upOffset = (upLp.leftMargin + upWidth) + upLp.rightMargin;
                int upTop = vCenter - (upHeight / 2);
                int upBottom = upTop + upHeight;
                if (isLayoutRtl) {
                    upRight = width;
                    upLeft = upRight - upWidth;
                    r -= upOffset;
                } else {
                    upRight = upWidth;
                    upLeft = 0;
                    l += upOffset;
                }
                this.mUpView.layout(upLeft, upTop, upRight, upBottom);
            }
            LayoutParams iconLp = (LayoutParams) this.mIconView.getLayoutParams();
            int iconHeight = this.mIconView.getMeasuredHeight();
            int iconWidth = this.mIconView.getMeasuredWidth();
            int hCenter = (r - l) / 2;
            int iconTop = Math.max(iconLp.topMargin, vCenter - (iconHeight / 2));
            int iconBottom = iconTop + iconHeight;
            int delta = Math.max(iconLp.getMarginStart(), hCenter - (iconWidth / 2));
            if (isLayoutRtl) {
                iconRight = (width - upOffset) - delta;
                iconLeft = iconRight - iconWidth;
            } else {
                iconLeft = upOffset + delta;
                iconRight = iconLeft + iconWidth;
            }
            this.mIconView.layout(iconLeft, iconTop, iconRight, iconBottom);
        }

        public void setIconViewLayoutParams(ViewGroup.LayoutParams lp) {
            this.mIconView.setLayoutParams(lp);
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int expandedMenuItemId;
        boolean isOverflowOpen;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.expandedMenuItemId = in.readInt();
            this.isOverflowOpen = in.readInt() != 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.expandedMenuItemId);
            out.writeInt(this.isOverflowOpen ? 1 : 0);
        }
    }

    public AmigoActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoActionBar);
        ApplicationInfo appInfo = context.getApplicationInfo();
        PackageManager pm = context.getPackageManager();
        this.mNavigationMode = a.getInt(R.styleable.AmigoActionBar_amigonavigationMode, 0);
        this.mTitle = a.getText(R.styleable.AmigoActionBar_amigotitle);
        this.mSubtitle = a.getText(R.styleable.AmigoActionBar_amigosubtitle);
        if (this.mTitle == null) {
            if (context instanceof Activity) {
                try {
                    this.mTitle = ((Activity) context).getTitle();
                } catch (Exception e) {
                    Log.e(TAG, "Activity title name not found!", e);
                }
            }
            if (this.mLogo == null) {
                this.mLogo = appInfo.loadLogo(pm);
            }
        }
        this.mLogo = a.getDrawable(R.styleable.AmigoActionBar_amigologo);
        if (this.mLogo == null) {
            if (context instanceof Activity) {
                try {
                    this.mLogo = pm.getActivityLogo(((Activity) context).getComponentName());
                } catch (NameNotFoundException e2) {
                    Log.e(TAG, "Activity component name not found!", e2);
                }
            }
            if (this.mLogo == null) {
                this.mLogo = appInfo.loadLogo(pm);
            }
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        this.mHomeResId = a.getResourceId(R.styleable.AmigoActionBar_amigohomeLayout, AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_action_bar_home"));
        this.mHomeLayout = (HomeView) inflater.inflate(this.mHomeResId, this, false);
        this.mExpandedHomeLayout = (HomeView) inflater.inflate(this.mHomeResId, this, false);
        this.mExpandedHomeLayout.setUp(true);
        this.mExpandedHomeLayout.setOnClickListener(this.mExpandedActionViewUpListener);
        this.mExpandedHomeLayout.setContentDescription(getResources().getText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_action_bar_up_description")));
        this.mTitleStyleRes = a.getResourceId(R.styleable.AmigoActionBar_amigotitleTextStyle, 0);
        this.mSubtitleStyleRes = a.getResourceId(R.styleable.AmigoActionBar_amigosubtitleTextStyle, 0);
        this.mProgressStyle = a.getResourceId(R.styleable.AmigoActionBar_amigoprogressBarStyle, 0);
        this.mIndeterminateProgressStyle = a.getResourceId(R.styleable.AmigoActionBar_amigoindeterminateProgressStyle, 0);
        this.mProgressBarPadding = a.getDimensionPixelOffset(R.styleable.AmigoActionBar_amigoprogressBarPadding, 0);
        this.mItemPadding = a.getDimensionPixelOffset(R.styleable.AmigoActionBar_amigoitemPadding, 0);
        setDisplayOptions(a.getInt(R.styleable.AmigoActionBar_amigodisplayOptions, 0));
        int customNavId = a.getResourceId(R.styleable.AmigoActionBar_amigocustomNavigationLayout, 0);
        if (customNavId != 0) {
            this.mCustomNavView = inflater.inflate(customNavId, this, false);
            this.mNavigationMode = 0;
            setDisplayOptions(this.mDisplayOptions | 16);
        }
        this.mContentHeight = a.getLayoutDimension(R.styleable.AmigoActionBar_amigoheight, 0);
        a.recycle();
        Context context2 = context;
        this.mLogoNavItem = new ActionMenuItem(context2, 0, AmigoWidgetResource.getIdentifierById(context, "amigo_home"), 0, 0, this.mTitle);
        this.mHomeLayout.setOnClickListener(this.mUpClickListener);
        this.mHomeLayout.setClickable(true);
        this.mHomeLayout.setFocusable(true);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        this.mMaxHomeSlop = (int) ((32.0f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void setHomeButtonEnabled(boolean enable) {
        this.mHomeLayout.setEnabled(enable);
        this.mHomeLayout.setFocusable(enable);
        if (enable) {
            this.mHomeLayout.setImportantForAccessibility(0);
            if ((this.mDisplayOptions & 4) != 0) {
                this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_action_bar_up_description")));
                return;
            } else {
                this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_action_bar_home_description")));
                return;
            }
        }
        this.mHomeLayout.setContentDescription(null);
        this.mHomeLayout.setImportantForAccessibility(2);
    }

    public void setDisplayOptions(int options) {
        int flagsChanged = -1;
        int i = 8;
        boolean z = true;
        if (this.mDisplayOptions != -1) {
            flagsChanged = options ^ this.mDisplayOptions;
        }
        this.mDisplayOptions = options;
        if ((flagsChanged & DISPLAY_RELAYOUT_MASK) != 0) {
            boolean showHome;
            int vis;
            if ((options & 2) != 0) {
                showHome = true;
            } else {
                showHome = false;
            }
            if (showHome && this.mExpandedActionView == null) {
                vis = 0;
            } else {
                vis = 8;
            }
            this.mHomeLayout.setVisibility(vis);
            if ((flagsChanged & 4) != 0) {
                boolean setUp;
                if ((options & 4) != 0) {
                    setUp = true;
                } else {
                    setUp = false;
                }
                this.mHomeLayout.setUp(setUp);
                if (setUp) {
                    setHomeButtonEnabled(true);
                }
            }
            if ((flagsChanged & 1) != 0) {
                boolean logoVis = (this.mLogo == null || (options & 1) == 0) ? false : true;
                this.mHomeLayout.setIcon(logoVis ? this.mLogo : this.mIcon);
            }
            if ((flagsChanged & 8) != 0) {
                if ((options & 8) != 0) {
                    initTitle();
                } else {
                    removeView(this.mTitleLayout);
                }
            }
            if (!(this.mTitleLayout == null || (flagsChanged & 6) == 0)) {
                boolean homeAsUp;
                boolean z2;
                if ((this.mDisplayOptions & 4) != 0) {
                    homeAsUp = true;
                } else {
                    homeAsUp = false;
                }
                View view = this.mTitleUpView;
                int i2 = !showHome ? homeAsUp ? 0 : 8 : 8;
                view.setVisibility(i2);
                if (this.mEmptyView != null) {
                    View view2 = this.mEmptyView;
                    if (!(showHome || homeAsUp)) {
                        i = 0;
                    }
                    view2.setVisibility(i);
                }
                LinearLayout linearLayout = this.mTitleLayout;
                if (showHome || !homeAsUp) {
                    z2 = false;
                } else {
                    z2 = true;
                }
                linearLayout.setEnabled(z2);
                LinearLayout linearLayout2 = this.mTitleLayout;
                if (showHome || !homeAsUp) {
                    z = false;
                }
                linearLayout2.setClickable(z);
            }
            if (!((flagsChanged & 16) == 0 || this.mCustomNavView == null)) {
                if ((options & 16) != 0) {
                    addView(this.mCustomNavView);
                } else {
                    removeView(this.mCustomNavView);
                }
            }
            requestLayout();
        } else {
            invalidate();
        }
        if (this.mHomeLayout.isEnabled()) {
            this.mHomeLayout.setImportantForAccessibility(0);
            if ((options & 4) != 0) {
                this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_action_bar_up_description")));
                return;
            } else {
                this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_action_bar_home_description")));
                return;
            }
        }
        this.mHomeLayout.setContentDescription(null);
        this.mHomeLayout.setImportantForAccessibility(2);
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        if (icon != null && ((this.mDisplayOptions & 1) == 0 || this.mLogo == null)) {
            this.mHomeLayout.setIcon(icon);
        }
        if (this.mExpandedActionView != null) {
            this.mExpandedHomeLayout.setIcon(this.mIcon.getConstantState().newDrawable(getResources()));
        }
    }

    public void setIcon(int resId) {
        setIcon(this.mContext.getResources().getDrawable(resId));
    }

    public void setLogo(Drawable logo) {
        this.mLogo = logo;
        if (logo != null && (this.mDisplayOptions & 1) != 0) {
            this.mHomeLayout.setIcon(logo);
        }
    }

    public void setLogo(int resId) {
        setLogo(this.mContext.getResources().getDrawable(resId));
    }

    public void setNavigationMode(int mode) {
        int oldMode = this.mNavigationMode;
        if (mode != oldMode) {
            switch (oldMode) {
                case 2:
                    if (this.mTabContainer != null && this.mIncludeTabs) {
                        removeView(this.mTabContainer);
                        break;
                    }
            }
            switch (mode) {
                case 1:
                    if (this.mSpinner == null) {
                        this.mSpinner = new Spinner(this.mContext, null, 16843479);
                        this.mListNavLayout = new LinearLayout(this.mContext, null, 16843508);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
                        params.gravity = 17;
                        this.mListNavLayout.addView(this.mSpinner, params);
                    }
                    if (this.mSpinner.getAdapter() != this.mSpinnerAdapter) {
                        this.mSpinner.setAdapter(this.mSpinnerAdapter);
                    }
                    this.mSpinner.setOnItemSelectedListener(this.mNavItemSelectedListener);
                    addView(this.mListNavLayout);
                    break;
                case 2:
                    if (this.mTabContainer != null && this.mIncludeTabs) {
                        addView(this.mTabContainer);
                        break;
                    }
            }
            this.mNavigationMode = mode;
            requestLayout();
        }
    }

    public void setDropdownAdapter(SpinnerAdapter adapter) {
        this.mSpinnerAdapter = adapter;
        if (this.mSpinner != null) {
            this.mSpinner.setAdapter(adapter);
        }
    }

    public SpinnerAdapter getDropdownAdapter() {
        return this.mSpinnerAdapter;
    }

    public void setDropdownSelectedPosition(int position) {
        this.mSpinner.setSelection(position);
    }

    public int getDropdownSelectedPosition() {
        return this.mSpinner.getSelectedItemPosition();
    }

    public int getNavigationMode() {
        return this.mNavigationMode;
    }

    public View getCustomNavigationView() {
        return this.mCustomNavView;
    }

    public int getDisplayOptions() {
        return this.mDisplayOptions;
    }

    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new AmigoActionBar.LayoutParams((int) DEFAULT_CUSTOM_GRAVITY);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(this.mHomeLayout);
        if (this.mCustomNavView != null && (this.mDisplayOptions & 16) != 0) {
            AmigoActionBarView parent = this.mCustomNavView.getParent();
            if (parent != this) {
                if (parent instanceof ViewGroup) {
                    parent.removeView(this.mCustomNavView);
                }
                addView(this.mCustomNavView);
            }
        }
    }

    private void initTitle() {
        boolean z = true;
        if (this.mTitleLayout == null) {
            boolean homeAsUp;
            boolean showHome;
            boolean showTitleUp;
            boolean z2;
            this.mTitleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_action_bar_title_item"), this, false);
            this.mTitleView = (TextView) this.mTitleLayout.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_action_bar_title"));
            this.mSubtitleView = (TextView) this.mTitleLayout.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_action_bar_subtitle"));
            this.mTitleUpView = this.mTitleLayout.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_up"));
            this.mEmptyView = this.mTitleLayout.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_empty_view"));
            this.mTitleUpView.setOnClickListener(this.mUpClickListener);
            if (this.mTitleStyleRes != 0) {
                this.mTitleView.setTextAppearance(getContext(), this.mTitleStyleRes);
            }
            if (this.mTitle != null) {
                this.mTitleView.setText(this.mTitle);
            }
            if (this.mSubtitleStyleRes != 0) {
                this.mSubtitleView.setTextAppearance(getContext(), this.mSubtitleStyleRes);
            }
            if (this.mSubtitle != null) {
                this.mSubtitleView.setText(this.mSubtitle);
                this.mSubtitleView.setVisibility(0);
            }
            if ((this.mDisplayOptions & 4) != 0) {
                homeAsUp = true;
            } else {
                homeAsUp = false;
            }
            if ((this.mDisplayOptions & 2) != 0) {
                showHome = true;
            } else {
                showHome = false;
            }
            if (showHome) {
                showTitleUp = false;
            } else {
                showTitleUp = true;
            }
            View view = this.mTitleUpView;
            int i = showTitleUp ? homeAsUp ? 0 : 8 : 8;
            view.setVisibility(i);
            if (this.mTitleLayout.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_empty_view")) != null) {
                view = this.mEmptyView;
                if (showHome || homeAsUp) {
                    i = 8;
                } else {
                    i = 0;
                }
                view.setVisibility(i);
            }
            LinearLayout linearLayout = this.mTitleLayout;
            if (homeAsUp && showTitleUp) {
                z2 = true;
            } else {
                z2 = false;
            }
            linearLayout.setEnabled(z2);
            LinearLayout linearLayout2 = this.mTitleLayout;
            if (!(homeAsUp && showTitleUp)) {
                z = false;
            }
            linearLayout2.setClickable(z);
        }
        addView(this.mTitleLayout);
        if (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle)) {
            this.mTitleLayout.setVisibility(8);
        }
    }

    public void setContextView(AmigoActionBarContextView view) {
        this.mContextView = view;
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new AmigoActionBar.LayoutParams(getContext(), attrs);
    }

    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp == null) {
            return generateDefaultLayoutParams();
        }
        return lp;
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        if (!(this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null)) {
            state.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        state.isOverflowOpen = isOverflowMenuShowing();
        return state;
    }

    public void onRestoreInstanceState(Parcelable p) {
        SavedState state = (SavedState) p;
        super.onRestoreInstanceState(state.getSuperState());
        if (!(state.expandedMenuItemId == 0 || this.mExpandedMenuPresenter == null || this.mOptionsMenu == null)) {
            MenuItem item = this.mOptionsMenu.findItem(state.expandedMenuItemId);
            if (item != null) {
                item.expandActionView();
            }
        }
        if (state.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    public void setCollapsable(boolean collapsable) {
        this.mIsCollapsable = collapsable;
    }

    public boolean isCollapsed() {
        return this.mIsCollapsed;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int childCount = getChildCount();
        if (this.mIsCollapsable) {
            int visibleChildren = 0;
            for (i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (!(child.getVisibility() == 8 || (child == this.mMenuView && this.mMenuView.getChildCount() == 0))) {
                    if (this.mHomeLayout != child && this.mTitleLayout != child) {
                        visibleChildren++;
                    } else if (child.getVisibility() != 8) {
                        visibleChildren++;
                    }
                }
            }
            if (visibleChildren == 0) {
                setMeasuredDimension(0, 0);
                this.mIsCollapsed = true;
                return;
            }
        }
        this.mIsCollapsed = false;
        if (MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_width=\"match_parent\" (or fill_parent)");
        } else if (MeasureSpec.getMode(heightMeasureSpec) != Integer.MIN_VALUE) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_height=\"wrap_content\"");
        } else {
            ViewGroup.LayoutParams lp;
            int contentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int maxHeight = this.mContentHeight >= 0 ? this.mContentHeight : MeasureSpec.getSize(heightMeasureSpec);
            int verticalPadding = getPaddingTop() + getPaddingBottom();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int height = maxHeight - verticalPadding;
            int childSpecHeight = MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
            int exactHeightSpec = MeasureSpec.makeMeasureSpec(height, 1073741824);
            int availableWidth = (contentWidth - paddingLeft) - paddingRight;
            int leftOfCenter = availableWidth / 2;
            int rightOfCenter = leftOfCenter;
            HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
            if (homeLayout.getVisibility() != 8) {
                int homeWidthSpec;
                lp = homeLayout.getLayoutParams();
                if (lp.width < 0) {
                    homeWidthSpec = MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE);
                } else {
                    homeWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, 1073741824);
                }
                homeLayout.measure(homeWidthSpec, exactHeightSpec);
                int homeWidth = homeLayout.getMeasuredWidth() + homeLayout.getStartOffset();
                availableWidth = Math.max(0, availableWidth - homeWidth);
                leftOfCenter = Math.max(0, availableWidth - homeWidth);
            }
            if (this.mMenuView != null && this.mMenuView.getParent() == this) {
                availableWidth = measureChildView(this.mMenuView, availableWidth, exactHeightSpec, 0);
                rightOfCenter = Math.max(0, rightOfCenter - this.mMenuView.getMeasuredWidth());
            }
            if (!(this.mIndeterminateProgressView == null || this.mIndeterminateProgressView.getVisibility() == 8)) {
                availableWidth = measureChildView(this.mIndeterminateProgressView, availableWidth, childSpecHeight, 0);
                rightOfCenter = Math.max(0, rightOfCenter - this.mIndeterminateProgressView.getMeasuredWidth());
            }
            boolean showTitle = (this.mTitleLayout == null || this.mTitleLayout.getVisibility() == 8 || (this.mDisplayOptions & 8) == 0) ? false : true;
            if (this.mExpandedActionView == null) {
                int itemPaddingSize;
                switch (this.mNavigationMode) {
                    case 1:
                        if (this.mListNavLayout != null) {
                            if (showTitle) {
                                itemPaddingSize = this.mItemPadding * 2;
                            } else {
                                itemPaddingSize = this.mItemPadding;
                            }
                            availableWidth = Math.max(0, availableWidth - itemPaddingSize);
                            leftOfCenter = Math.max(0, leftOfCenter - itemPaddingSize);
                            this.mListNavLayout.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(height, 1073741824));
                            int listNavWidth = this.mListNavLayout.getMeasuredWidth();
                            availableWidth = Math.max(0, availableWidth - listNavWidth);
                            leftOfCenter = Math.max(0, leftOfCenter - listNavWidth);
                            break;
                        }
                        break;
                    case 2:
                        if (this.mTabContainer != null) {
                            if (showTitle) {
                                itemPaddingSize = this.mItemPadding * 2;
                            } else {
                                itemPaddingSize = this.mItemPadding;
                            }
                            availableWidth = Math.max(0, availableWidth - itemPaddingSize);
                            leftOfCenter = Math.max(0, leftOfCenter - itemPaddingSize);
                            this.mTabContainer.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(height, 1073741824));
                            int tabWidth = this.mTabContainer.getMeasuredWidth();
                            availableWidth = Math.max(0, availableWidth - tabWidth);
                            leftOfCenter = Math.max(0, leftOfCenter - tabWidth);
                            break;
                        }
                        break;
                }
            }
            View customView = null;
            if (this.mExpandedActionView != null) {
                customView = this.mExpandedActionView;
            } else if (!((this.mDisplayOptions & 16) == 0 || this.mCustomNavView == null)) {
                customView = this.mCustomNavView;
            }
            if (customView != null) {
                int customNavHeightMode;
                int min;
                lp = generateLayoutParams(customView.getLayoutParams());
                AmigoActionBar.LayoutParams ablp = lp instanceof AmigoActionBar.LayoutParams ? (AmigoActionBar.LayoutParams) lp : null;
                int horizontalMargin = 0;
                int verticalMargin = 0;
                if (ablp != null) {
                    horizontalMargin = ablp.leftMargin + ablp.rightMargin;
                    verticalMargin = ablp.topMargin + ablp.bottomMargin;
                }
                if (this.mContentHeight <= 0) {
                    customNavHeightMode = Integer.MIN_VALUE;
                } else {
                    customNavHeightMode = lp.height != -2 ? 1073741824 : Integer.MIN_VALUE;
                }
                if (lp.height >= 0) {
                    height = Math.min(lp.height, height);
                }
                int customNavHeight = Math.max(0, height - verticalMargin);
                int customNavWidthMode = lp.width != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp.width >= 0) {
                    min = Math.min(lp.width, availableWidth);
                } else {
                    min = availableWidth;
                }
                int customNavWidth = Math.max(0, min - horizontalMargin);
                if (((ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY) & 7) == 1 && lp.width == -1) {
                    customNavWidth = Math.min(leftOfCenter, rightOfCenter) * 2;
                }
                customView.measure(MeasureSpec.makeMeasureSpec(customNavWidth, customNavWidthMode), MeasureSpec.makeMeasureSpec(customNavHeight, customNavHeightMode));
                availableWidth -= customView.getMeasuredWidth() + horizontalMargin;
            }
            if (this.mExpandedActionView == null && showTitle) {
                availableWidth = measureChildView(this.mTitleLayout, availableWidth, MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824), 0);
                leftOfCenter = Math.max(0, leftOfCenter - this.mTitleLayout.getMeasuredWidth());
            }
            if (this.mContentHeight <= 0) {
                int measuredHeight = 0;
                for (i = 0; i < childCount; i++) {
                    int paddedViewHeight = getChildAt(i).getMeasuredHeight() + verticalPadding;
                    if (paddedViewHeight > measuredHeight) {
                        measuredHeight = paddedViewHeight;
                    }
                }
                setMeasuredDimension(contentWidth, measuredHeight);
            } else {
                setMeasuredDimension(contentWidth, maxHeight);
            }
            if (this.mContextView != null) {
                this.mContextView.setContentHeight(getMeasuredHeight());
            }
            if (this.mProgressView != null && this.mProgressView.getVisibility() != 8) {
                this.mProgressView.measure(MeasureSpec.makeMeasureSpec(contentWidth - (this.mProgressBarPadding * 2), 1073741824), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), Integer.MIN_VALUE));
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int contentHeight = ((b - t) - getPaddingTop()) - getPaddingBottom();
        if (contentHeight > 0) {
            int menuStart;
            boolean isLayoutRtl = isLayoutRtl();
            int direction = isLayoutRtl ? 1 : -1;
            int menuStart2 = isLayoutRtl ? getPaddingLeft() : (r - l) - getPaddingRight();
            int x = isLayoutRtl ? (r - l) - getPaddingRight() : getPaddingLeft();
            int y = getPaddingTop();
            HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
            boolean needsTouchDelegate = false;
            int homeSlop = this.mMaxHomeSlop;
            int homeRight = 0;
            if (homeLayout.getVisibility() != 8) {
                int startOffset = homeLayout.getStartOffset();
                x = AmigoAbsActionBarView.next(x + positionChild(homeLayout, AmigoAbsActionBarView.next(x, startOffset, isLayoutRtl), y, contentHeight, isLayoutRtl), startOffset, isLayoutRtl);
                needsTouchDelegate = homeLayout == this.mHomeLayout;
                homeRight = x;
            }
            if (this.mExpandedActionView == null) {
                boolean showTitle = (this.mTitleLayout == null || this.mTitleLayout.getVisibility() == 8 || (this.mDisplayOptions & 8) == 0) ? false : true;
                if (showTitle) {
                    x += positionChild(this.mTitleLayout, x, y, contentHeight, isLayoutRtl);
                }
                switch (this.mNavigationMode) {
                    case 1:
                        if (this.mListNavLayout != null) {
                            if (showTitle) {
                                x = AmigoAbsActionBarView.next(x, this.mItemPadding, isLayoutRtl);
                            }
                            homeSlop = Math.min(homeSlop, Math.max(x - homeRight, 0));
                            x = AmigoAbsActionBarView.next(x + positionChild(this.mListNavLayout, x, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                            break;
                        }
                        break;
                    case 2:
                        if (this.mTabContainer != null) {
                            if (showTitle) {
                                x = AmigoAbsActionBarView.next(x, this.mItemPadding, isLayoutRtl);
                            }
                            homeSlop = Math.min(homeSlop, Math.max(x - homeRight, 0));
                            x = AmigoAbsActionBarView.next(x + positionChild(this.mTabContainer, x, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                            break;
                        }
                        break;
                }
            }
            if (this.mMenuView == null || this.mMenuView.getParent() != this) {
                menuStart = menuStart2;
            } else {
                positionChild(this.mMenuView, menuStart2, y, contentHeight, !isLayoutRtl);
                menuStart = menuStart2 + (this.mMenuView.getMeasuredWidth() * direction);
            }
            if (!(this.mIndeterminateProgressView == null || this.mIndeterminateProgressView.getVisibility() == 8)) {
                positionChild(this.mIndeterminateProgressView, menuStart, y, contentHeight, !isLayoutRtl);
                menuStart += this.mIndeterminateProgressView.getMeasuredWidth() * direction;
            }
            View customView = null;
            if (this.mExpandedActionView != null) {
                customView = this.mExpandedActionView;
            } else if (!((this.mDisplayOptions & 16) == 0 || this.mCustomNavView == null)) {
                customView = this.mCustomNavView;
            }
            if (customView != null) {
                int layoutDirection = getLayoutDirection();
                ViewGroup.LayoutParams lp = customView.getLayoutParams();
                AmigoActionBar.LayoutParams ablp = lp instanceof AmigoActionBar.LayoutParams ? (AmigoActionBar.LayoutParams) lp : null;
                int gravity = ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY;
                int navWidth = customView.getMeasuredWidth();
                int topMargin = 0;
                int bottomMargin = 0;
                if (ablp != null) {
                    x = AmigoAbsActionBarView.next(x, ablp.getMarginStart(), isLayoutRtl);
                    menuStart += ablp.getMarginEnd() * direction;
                    topMargin = ablp.topMargin;
                    bottomMargin = ablp.bottomMargin;
                }
                int hgravity = gravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK;
                if (hgravity == 1) {
                    int centeredLeft = ((this.mRight - this.mLeft) - navWidth) / 2;
                    int centeredEnd;
                    if (isLayoutRtl) {
                        centeredEnd = centeredLeft;
                        if (centeredLeft + navWidth > x) {
                            hgravity = 5;
                        } else if (centeredEnd < menuStart) {
                            hgravity = 3;
                        }
                    } else {
                        centeredEnd = centeredLeft + navWidth;
                        if (centeredLeft < x) {
                            hgravity = 3;
                        } else if (centeredEnd > menuStart) {
                            hgravity = 5;
                        }
                    }
                } else if (gravity == 0) {
                    hgravity = 8388611;
                }
                int xpos = 0;
                switch (Gravity.getAbsoluteGravity(hgravity, layoutDirection)) {
                    case 1:
                        xpos = ((this.mRight - this.mLeft) - navWidth) / 2;
                        break;
                    case 3:
                        if (isLayoutRtl) {
                            xpos = menuStart;
                        } else {
                            xpos = x;
                        }
                        break;
                    case 5:
                        xpos = isLayoutRtl ? x - navWidth : menuStart - navWidth;
                        break;
                }
                int vgravity = gravity & 112;
                if (gravity == 0) {
                    vgravity = 16;
                }
                int ypos = 0;
                switch (vgravity) {
                    case 16:
                        ypos = ((((this.mBottom - this.mTop) - getPaddingBottom()) - getPaddingTop()) - customView.getMeasuredHeight()) / 2;
                        break;
                    case 48:
                        ypos = getPaddingTop() + topMargin;
                        break;
                    case 80:
                        ypos = ((getHeight() - getPaddingBottom()) - customView.getMeasuredHeight()) - bottomMargin;
                        break;
                }
                int customWidth = customView.getMeasuredWidth();
                customView.layout(xpos, ypos, xpos + customWidth, customView.getMeasuredHeight() + ypos);
                homeSlop = Math.min(homeSlop, Math.max(xpos - homeRight, 0));
                x = AmigoAbsActionBarView.next(x, customWidth, isLayoutRtl);
            }
            if (this.mProgressView != null) {
                this.mProgressView.bringToFront();
                int halfProgressHeight = this.mProgressView.getMeasuredHeight() / 2;
                this.mProgressView.layout(this.mProgressBarPadding, -halfProgressHeight, this.mProgressBarPadding + this.mProgressView.getMeasuredWidth(), halfProgressHeight);
            }
            if (needsTouchDelegate) {
                this.mTempRect.set(homeLayout.getLeft(), homeLayout.getTop(), homeLayout.getRight() + homeSlop, homeLayout.getBottom());
                setTouchDelegate(new TouchDelegate(this.mTempRect, homeLayout));
                return;
            }
            setTouchDelegate(null);
        }
    }

    public void setClickable(boolean clickable) {
        if (this.mTabContainer != null) {
            this.mTabContainer.setClickable(clickable);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        Log.e("AmigoActionBarView_onTouchEvent", "event.getAction()-->" + (event.getAction() & 255));
        if (event.getAction() == 0) {
            doActionBarDoubleClick();
        }
        return super.onTouchEvent(event);
    }

    public void setOnActionBarDoubleClickListener(OnClickListener listener) {
        this.mActionBarDoubleClickListener = listener;
    }

    private void doActionBarDoubleClick() {
        if (this.mActionBarDoubleClickListener != null) {
            long time = System.currentTimeMillis();
            long gap = time - this.mCurTime;
            if (gap <= 0 || gap > 500) {
                this.mCurTime = time;
                return;
            }
            this.mCurTime = 0;
            this.mActionBarDoubleClickListener.onClick(null);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mTitleView = null;
        this.mSubtitleView = null;
        this.mTitleUpView = null;
        if (this.mTitleLayout != null && this.mTitleLayout.getParent() == this) {
            removeView(this.mTitleLayout);
        }
        this.mTitleLayout = null;
        if ((this.mDisplayOptions & 8) != 0) {
            initTitle();
        }
        if (this.mTabContainer != null && this.mIncludeTabs) {
            ViewGroup.LayoutParams lp = this.mTabContainer.getLayoutParams();
            if (lp != null) {
                lp.width = -2;
                lp.height = -1;
            }
            this.mTabContainer.setAllowCollapse(true);
        }
        HomeView tempHomeLayout = (HomeView) LayoutInflater.from(getContext()).inflate(this.mHomeResId, this, false);
        if (tempHomeLayout != null) {
            ImageView tempIconView = (ImageView) tempHomeLayout.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_home"));
            if (tempIconView != null) {
                (this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout).setIconViewLayoutParams(tempIconView.getLayoutParams());
            }
        }
    }

    public void setWindowCallback(Callback cb) {
        this.mWindowCallback = cb;
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mTabSelector);
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.hideOverflowMenu();
            this.mActionMenuPresenter.hideSubMenus();
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void initProgress() {
    }

    public void initIndeterminateProgress() {
    }

    public void setSplitActionBar(boolean splitActionBar) {
    }

    public boolean isSplitActionBar() {
        return false;
    }

    public boolean hasEmbeddedTabs() {
        return this.mIncludeTabs;
    }

    public void setEmbeddedTabView(AmigoTabContainerView tabs) {
        if (this.mTabContainer != null) {
            removeView(this.mTabContainer);
        }
        this.mTabContainer = tabs;
        this.mIncludeTabs = tabs != null;
        if (this.mIncludeTabs && this.mNavigationMode == 2) {
            addView(this.mTabContainer);
            ViewGroup.LayoutParams lp = this.mTabContainer.getLayoutParams();
            lp.width = -2;
            lp.height = -1;
            tabs.setAllowCollapse(true);
        }
    }

    public void setCallback(OnNavigationListener callback) {
        this.mCallback = callback;
    }

    public void setMenu(Menu menu, MenuPresenter.Callback cb) {
        if (menu != this.mOptionsMenu) {
            ViewGroup oldParent;
            ViewGroup menuView;
            if (this.mOptionsMenu != null) {
                this.mOptionsMenu.removeMenuPresenter((MenuPresenter) this.mActionMenuPresenter.getmAmigoActionMenuPresenter());
                this.mOptionsMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
            }
            MenuBuilder builder = (MenuBuilder) menu;
            this.mOptionsMenu = builder;
            if (this.mMenuView != null) {
                oldParent = (ViewGroup) this.mMenuView.getParent();
                if (oldParent != null) {
                    oldParent.removeView(this.mMenuView);
                }
            }
            if (this.mActionMenuPresenter == null) {
                this.mActionMenuPresenter = new AmigoActionMenuPresenter(this.mContext);
                this.mActionMenuPresenter.setCallback(cb);
                this.mActionMenuPresenter.setId(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_menu_presenter"));
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
            if (this.mSplitActionBar) {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                this.mActionMenuPresenter.setItemLimit(AmigoPreference.DEFAULT_ORDER);
                layoutParams.width = -1;
                configPresenters(builder);
                menuView = this.mActionMenuPresenter.getMenuView(this);
                if (this.mSplitView != null) {
                    oldParent = (ViewGroup) menuView.getParent();
                    if (!(oldParent == null || oldParent == this.mSplitView)) {
                        oldParent.removeView(menuView);
                    }
                    menuView.setVisibility(getAnimatedVisibility());
                    this.mSplitView.addView(menuView, layoutParams);
                } else {
                    menuView.setLayoutParams(layoutParams);
                }
            } else {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(AmigoWidgetResource.getIdentifierByBool(this.mContext, "amigo_action_bar_expanded_action_views_exclusive")));
                configPresenters(builder);
                menuView = this.mActionMenuPresenter.getMenuView(this);
                oldParent = (ViewGroup) menuView.getParent();
                if (!(oldParent == null || oldParent == this)) {
                    oldParent.removeView(menuView);
                }
                addView(menuView, layoutParams);
            }
            this.mMenuView = menuView;
        }
    }

    private void configPresenters(MenuBuilder builder) {
        if (builder != null) {
            builder.addMenuPresenter((MenuPresenter) this.mActionMenuPresenter.getmAmigoActionMenuPresenter());
            builder.addMenuPresenter(this.mExpandedMenuPresenter);
            return;
        }
        this.mActionMenuPresenter.initForMenu(this.mContext, null);
        this.mExpandedMenuPresenter.initForMenu(this.mContext, null);
        this.mActionMenuPresenter.updateMenuView(true);
        this.mExpandedMenuPresenter.updateMenuView(true);
    }

    public boolean hasExpandedActionView() {
        return (this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null) ? false : true;
    }

    public void collapseActionView() {
        MenuItemImpl item = this.mExpandedMenuPresenter == null ? null : this.mExpandedMenuPresenter.mCurrentExpandedItem;
        if (item != null) {
            item.collapseActionView();
        }
    }

    public void setCustomNavigationView(View view) {
        boolean showCustom = (this.mDisplayOptions & 16) != 0;
        if (this.mCustomNavView != null && showCustom) {
            removeView(this.mCustomNavView);
        }
        this.mCustomNavView = view;
        if (this.mCustomNavView != null && showCustom) {
            addView(this.mCustomNavView);
        }
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setTitle(CharSequence title) {
        this.mUserTitle = true;
        setTitleImpl(title);
    }

    public void setWindowTitle(CharSequence title) {
        if (!this.mUserTitle) {
            setTitleImpl(title);
        }
    }

    private void setTitleImpl(CharSequence title) {
        int i = 0;
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
            boolean visible = (this.mExpandedActionView != null || (this.mDisplayOptions & 8) == 0 || (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle))) ? false : true;
            LinearLayout linearLayout = this.mTitleLayout;
            if (!visible) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
        if (this.mLogoNavItem != null) {
            this.mLogoNavItem.setTitle(title);
        }
    }

    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    public void setSubtitle(CharSequence subtitle) {
        int i = 0;
        this.mSubtitle = subtitle;
        if (this.mSubtitleView != null) {
            boolean visible;
            this.mSubtitleView.setText(subtitle);
            this.mSubtitleView.setVisibility(subtitle != null ? 0 : 8);
            if (this.mExpandedActionView != null || (this.mDisplayOptions & 8) == 0 || (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle))) {
                visible = false;
            } else {
                visible = true;
            }
            LinearLayout linearLayout = this.mTitleLayout;
            if (!visible) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
    }

    public void setActivity(AmigoActivity activity) {
        this.mActivity = activity;
    }

    public void setOnBackClickListener(OnClickListener listener) {
        this.mBackClickListener = listener;
    }

    public void setMenu(Menu menu) {
        if (this.mMenuView != null) {
            ViewGroup oldParent = (ViewGroup) this.mMenuView.getParent();
            if (oldParent != null) {
                oldParent.removeView(this.mMenuView);
            }
        }
        LayoutInflater inflater = LayoutInflater.from(this.mActivity);
        LinearLayout menuView = new LinearLayout(this.mContext);
        int i = 0;
        while (i < menu.size()) {
            final MenuItem menuItem = menu.getItem(i);
            if (menu.getItem(i).isVisible() && menu.getItem(i).getIcon() != null) {
                ImageView menuIv = (ImageView) inflater.inflate(AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_actionbar_menu_item"), null);
                menuIv.setImageDrawable(menu.getItem(i).getIcon());
                menuIv.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        AmigoActionBarView.this.mActivity.onOptionsItemSelected(menuItem);
                    }
                });
                menuIv.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        AmigoActionBarView.this.mActivity.onOptionsItemLongClick(menuItem);
                        return false;
                    }
                });
                menuView.addView(menuIv, new ViewGroup.LayoutParams(getContentHeight(), getContentHeight()));
                if (menuView.getChildCount() > 1) {
                    break;
                }
            }
            i++;
        }
        addView(menuView, new ViewGroup.LayoutParams(getContentHeight(), -2));
        this.mMenuView = menuView;
    }

    public void changeColors() {
        if (this.mTitleView != null) {
            this.mTitleView.setTextColor(ChameleonColorManager.getContentColorPrimaryOnAppbar_T1());
        }
        this.mHomeLayout.changeColor();
    }
}
