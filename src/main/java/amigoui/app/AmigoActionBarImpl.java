package amigoui.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.view.ActionBarPolicy;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.view.menu.SubMenuBuilder;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.SpinnerAdapter;

import com.amigoui.internal.view.AmigoActionBarPolicy;
import com.amigoui.internal.widget.AmigoActionBarContainer;
import com.amigoui.internal.widget.AmigoActionBarContextView;
import com.amigoui.internal.widget.AmigoActionBarOverlayLayout;
import com.amigoui.internal.widget.AmigoActionBarView;
import com.amigoui.internal.widget.AmigoTabContainerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import amigoui.changecolors.ChameleonColorManager;
import amigoui.reflection.AmigoReflectionUtil;
import amigoui.widget.AmigoMagicBar;
import amigoui.widget.AmigoWidgetResource;
import uk.co.senab.photoview.IPhotoView;

public class AmigoActionBarImpl extends AmigoActionBar {
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final int INVALID_POSITION = -1;
    private static final String TAG = "Amigo_WidgetDemoL.AmigoActionBarImpl";
    private boolean mActionBarOverlay;
    ActionModeImpl mActionMode;
    private AmigoActionBarView mActionView;
    private Activity mActivity;
    private AmigoActionBarContainer mContainerView;
    private View mContentView;
    private Context mContext;
    private int mContextDisplayMode;
    private AmigoActionBarContextView mContextView;
    private boolean mControlCreate = true;
    private int mCurWindowVisibility = 0;
    private Animator mCurrentShowAnim;
    ActionMode mDeferredDestroyActionMode;
    Callback mDeferredModeDestroyCallback;
    private Dialog mDialog;
    private boolean mDisplayHomeAsUpSet;
    private boolean mHasEmbeddedTabs;
    private boolean mHiddenByApp = false;
    private boolean mHiddenBySystem;
    final AnimatorListener mHideListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            if (AmigoActionBarImpl.this.mContentView != null) {
                AmigoActionBarImpl.this.mContentView.setTranslationY(0.0f);
                AmigoActionBarImpl.this.mTopVisibilityView.setTranslationY(0.0f);
            }
            AmigoActionBarImpl.this.mTopVisibilityView.setVisibility(View.GONE);
            AmigoActionBarImpl.this.mContainerView.setTransitioning(false);
            AmigoActionBarImpl.this.mCurrentShowAnim = null;
            AmigoActionBarImpl.this.completeDeferredDestroyActionMode();
            if (AmigoActionBarImpl.this.mOverlayLayout != null) {
                AmigoActionBarImpl.this.mOverlayLayout.requestFitSystemWindows();
            }
        }
    };
    private boolean mLastMenuVisibility;
    private ArrayList<OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList();
    private boolean mNowShowing = true;
    private AmigoActionBarOverlayLayout mOverlayLayout;
    private int mSavedTabPosition = -1;
    private TabImpl mSelectedTab;
    private boolean mShowHideAnimationEnabled;
    final AnimatorListener mShowListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            AmigoActionBarImpl.this.mCurrentShowAnim = null;
            AmigoActionBarImpl.this.mTopVisibilityView.requestLayout();
        }
    };
    private boolean mShowingForMode;
    private AmigoActionBarContainer mSpliteView;
    private AmigoTabContainerView mTabContainerView;
    private ArrayList<TabImpl> mTabs = new ArrayList();
    private Context mThemedContext;
    private ViewGroup mTopVisibilityView;

    public class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
        private Callback mCallback;
        private WeakReference<View> mCustomView;
        private MenuBuilder mMenu;

        public ActionModeImpl(Callback callback) {
            this.mCallback = callback;
            this.mMenu = new MenuBuilder(AmigoActionBarImpl.this.getThemedContext()).setDefaultShowAsAction(1);
            this.mMenu.setCallback(this);
        }

        public MenuInflater getMenuInflater() {
            return new MenuInflater(AmigoActionBarImpl.this.getThemedContext());
        }

        public Menu getMenu() {
            return this.mMenu;
        }

        public void finish() {
            if (AmigoActionBarImpl.this.mActionMode == this) {
                if (AmigoActionBarImpl.checkShowingFlags(AmigoActionBarImpl.this.mHiddenByApp, AmigoActionBarImpl.this.mHiddenBySystem, false)) {
                    this.mCallback.onDestroyActionMode(this);
                } else {
                    AmigoActionBarImpl.this.mDeferredDestroyActionMode = this;
                    AmigoActionBarImpl.this.mDeferredModeDestroyCallback = this.mCallback;
                }
                Menu optionMenu = ((AmigoActivity) AmigoActionBarImpl.this.mActivity).getOptionMenu();
                if (optionMenu != null) {
                    ((AmigoActivity) AmigoActionBarImpl.this.mActivity).parserMenuInfo(optionMenu);
                    ((AmigoActivity) AmigoActionBarImpl.this.mActivity).setOptionsMenuHideMode(false);
                } else {
                    ((AmigoActivity) AmigoActionBarImpl.this.mActivity).setOptionsMenuHideMode(true);
                }
                this.mCallback = null;
                AmigoMagicBar amigoMagicBar = ((AmigoActivity) AmigoActionBarImpl.this.mActivity).getAmigoMagicBar();
                if (amigoMagicBar != null) {
                    amigoMagicBar.setMagicBarVisibilityWithAnim(0);
                }
                AmigoActionBarImpl.this.animateToMode(false);
                AmigoActionBarImpl.this.mContextView.closeMode();
                AmigoActionBarImpl.this.mActionView.sendAccessibilityEvent(32);
                AmigoActionBarImpl.this.mActionMode = null;
            }
        }

        public void invalidate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                if (this.mCallback != null) {
                    this.mCallback.onPrepareActionMode(this, this.mMenu);
                }
                this.mMenu.startDispatchingItemsChanged();
            } catch (Throwable th) {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public boolean dispatchOnCreate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                boolean onCreateActionMode = this.mCallback.onCreateActionMode(this, this.mMenu);
                return onCreateActionMode;
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public void setCustomView(View view) {
            AmigoActionBarImpl.this.mContextView.setCustomView(view);
            this.mCustomView = new WeakReference(view);
        }

        public void setSubtitle(CharSequence subtitle) {
            AmigoActionBarImpl.this.mContextView.setSubtitle(subtitle);
        }

        public void setTitle(CharSequence title) {
            AmigoActionBarImpl.this.mContextView.setTitle(title);
        }

        public void setTitle(int resId) {
            setTitle(AmigoActionBarImpl.this.mContext.getResources().getString(resId));
        }

        public void setSubtitle(int resId) {
            setSubtitle(AmigoActionBarImpl.this.mContext.getResources().getString(resId));
        }

        public CharSequence getTitle() {
            return AmigoActionBarImpl.this.mContextView.getTitle();
        }

        public CharSequence getSubtitle() {
            return AmigoActionBarImpl.this.mContextView.getSubtitle();
        }

        public void setTitleOptionalHint(boolean titleOptional) {
            super.setTitleOptionalHint(titleOptional);
            AmigoActionBarImpl.this.mContextView.setTitleOptional(titleOptional);
        }

        public boolean isTitleOptional() {
            return AmigoActionBarImpl.this.mContextView.isTitleOptional();
        }

        public View getCustomView() {
            return this.mCustomView != null ? (View) this.mCustomView.get() : null;
        }

        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (this.mCallback != null) {
                return this.mCallback.onActionItemClicked(this, item);
            }
            return false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            if (this.mCallback == null) {
                return false;
            }
            if (!subMenu.hasVisibleItems()) {
                return true;
            }
            new MenuPopupHelper(AmigoActionBarImpl.this.getThemedContext(), subMenu).show();
            return true;
        }

        public void onCloseSubMenu(SubMenuBuilder menu) {
        }

        public void onMenuModeChange(MenuBuilder menu) {
            if (this.mCallback != null) {
                invalidate();
                AmigoActionBarImpl.this.mContextView.showOverflowMenu();
            }
        }

        public Callback getCallback() {
            return this.mCallback;
        }
    }

    public class TabImpl extends Tab {
        private TabListener mCallback;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private int mPosition = -1;
        private Object mTag;
        private CharSequence mText;

        public TabListener getCallback() {
            return this.mCallback;
        }

        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }

        public View getCustomView() {
            return this.mCustomView;
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public Object getTag() {
            return this.mTag;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public void select() {
            AmigoActionBarImpl.this.selectTab(this);
        }

        public Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                AmigoActionBarImpl.this.mTabContainerView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setContentDescription(int resId) {
            return setContentDescription(AmigoActionBarImpl.this.mContext.getResources().getText(resId));
        }

        public Tab setCustomView(int layoutResId) {
            return setCustomView(LayoutInflater.from(AmigoActionBarImpl.this.getThemedContext()).inflate(layoutResId, null));
        }

        public Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                AmigoActionBarImpl.this.mTabContainerView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setIcon(Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                AmigoActionBarImpl.this.mTabContainerView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setIcon(int resId) {
            return setIcon(AmigoActionBarImpl.this.mContext.getResources().getDrawable(resId));
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public Tab setTabListener(TabListener callback) {
            this.mCallback = callback;
            return this;
        }

        public Tab setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public Tab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                AmigoActionBarImpl.this.mTabContainerView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setText(int resId) {
            return setText(AmigoActionBarImpl.this.mContext.getResources().getText(resId));
        }
    }

    private static boolean checkShowingFlags(boolean hiddenByApp, boolean hiddenBySystem, boolean showingForMode) {
        if (showingForMode) {
            return true;
        }
        if (hiddenByApp || hiddenBySystem) {
            return false;
        }
        return true;
    }

    public AmigoActionBarImpl(Activity activity) {
        this.mActivity = activity;
        View decor = ((AmigoActivity) activity).getViewWithAmigoActionBar();
        this.mActionBarOverlay = this.mActivity.getWindow().hasFeature(9);
        init(decor);
        if (!this.mActionBarOverlay) {
            this.mContentView = decor.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_content"));
        }
    }

    public AmigoActionBarImpl(Dialog dialog) {
        this.mDialog = dialog;
        init(dialog.getWindow().getDecorView());
    }

    public void startOptionsMenu() {
        ((AmigoActivity) this.mActivity).startOptionsMenu();
    }

    public void invalidateOptionsMenu() {
        ((AmigoActivity) this.mActivity).invalOptionsMenu();
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.add(listener);
    }

    public void addTab(Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, boolean setSelected) {
        ensureTabsExist();
        this.mTabContainerView.addTab(tab, setSelected);
        configureTab(tab, this.mTabs.size());
        if (setSelected) {
            selectTab(tab);
        }
    }

    public void addTab(Tab tab, int position) {
        addTab(tab, position, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, int position, boolean setSelected) {
        ensureTabsExist();
        this.mTabContainerView.addTab(tab, position, setSelected);
        configureTab(tab, position);
        if (setSelected) {
            selectTab(tab);
        }
    }

    void animateToMode(boolean toActionMode) {
        int i;
        int i2 = 4;
        if (toActionMode) {
            showForActionMode();
        } else {
            hideForActionMode();
        }
        AmigoActionBarView amigoActionBarView = this.mActionView;
        if (toActionMode) {
            i = View.INVISIBLE;
        } else {
            i = View.VISIBLE;
        }
        amigoActionBarView.setVisibility(i);
        this.mContextView.animateToVisibility(toActionMode ? 0 : 8);
        if (this.mTabContainerView != null && !this.mActionView.hasEmbeddedTabs() && this.mActionView.isCollapsed()) {
            AmigoTabContainerView amigoTabContainerView = this.mTabContainerView;
            if (!toActionMode) {
                i2 = 0;
            }
            amigoTabContainerView.animateToVisibility(i2);
        }
    }

    private void cleanupTabs() {
        if (this.mSelectedTab != null) {
            selectTab(null);
        }
        this.mTabs.clear();
        if (this.mTabContainerView != null) {
            this.mTabContainerView.removeAllTabs();
        }
        this.mSavedTabPosition = -1;
    }

    void completeDeferredDestroyActionMode() {
        if (this.mDeferredModeDestroyCallback != null) {
            this.mDeferredModeDestroyCallback.onDestroyActionMode(this.mDeferredDestroyActionMode);
            this.mDeferredDestroyActionMode = null;
            this.mDeferredModeDestroyCallback = null;
        }
    }

    private void configureTab(Tab tab, int position) {
        TabImpl tabi = (TabImpl) tab;
        if (tabi.getCallback() == null) {
            throw new IllegalStateException("Action Bar Tab must have a Callback");
        }
        tabi.setPosition(position);
        this.mTabs.add(position, tabi);
        int count = this.mTabs.size();
        for (int i = position + 1; i < count; i++) {
            ((TabImpl) this.mTabs.get(i)).setPosition(i);
        }
    }

    public void dispatchMenuVisibilityChanged(boolean isVisible) {
        if (isVisible != this.mLastMenuVisibility) {
            this.mLastMenuVisibility = isVisible;
            int count = this.mMenuVisibilityListeners.size();
            for (int i = 0; i < count; i++) {
                ((OnMenuVisibilityListener) this.mMenuVisibilityListeners.get(i)).onMenuVisibilityChanged(isVisible);
            }
        }
    }

    public void doHide(boolean fromSystem) {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
        if (this.mCurWindowVisibility == 0 && (this.mShowHideAnimationEnabled || fromSystem)) {
            this.mTopVisibilityView.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
            this.mContainerView.setTransitioning(true);
            AnimatorSet anim = new AnimatorSet();
            float endingY = (float) (-this.mTopVisibilityView.getHeight());
            if (fromSystem) {
                int[] topLeft = new int[]{0, 0};
                this.mTopVisibilityView.getLocationInWindow(topLeft);
                endingY -= (float) topLeft[1];
            }
            Builder b = anim.play(ObjectAnimator.ofFloat(this.mTopVisibilityView, "translationY", new float[]{endingY}));
            if (this.mContentView != null) {
                b.with(ObjectAnimator.ofFloat(this.mContentView, "translationY", new float[]{0.0f, endingY}));
            }
            anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563650));
            anim.setDuration(250);
            anim.addListener(this.mHideListener);
            this.mCurrentShowAnim = anim;
            anim.start();
            return;
        }
        this.mHideListener.onAnimationEnd(null);
    }

    public void doShow(boolean fromSystem) {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
        this.mTopVisibilityView.setVisibility(View.VISIBLE);//0
        if (this.mCurWindowVisibility == 0 && (this.mShowHideAnimationEnabled || fromSystem)) {
            this.mTopVisibilityView.setTranslationY(0.0f);
            float startingY = (float) (-this.mTopVisibilityView.getHeight());
            if (fromSystem) {
                int[] topLeft = new int[]{0, 0};
                this.mTopVisibilityView.getLocationInWindow(topLeft);
                startingY -= (float) topLeft[1];
            }
            this.mTopVisibilityView.setTranslationY(startingY);
            AnimatorSet anim = new AnimatorSet();
            Builder b = anim.play(ObjectAnimator.ofFloat(this.mTopVisibilityView, "translationY", new float[]{0.0f}));
            if (this.mContentView != null) {
                b.with(ObjectAnimator.ofFloat(this.mContentView, "translationY", new float[]{startingY, 0.0f}));
            }
            anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563651));
            anim.setDuration(250);
            anim.addListener(this.mShowListener);
            this.mCurrentShowAnim = anim;
            anim.start();
        } else {
            this.mTopVisibilityView.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
            this.mTopVisibilityView.setTranslationY(0.0f);
            if (this.mContentView != null) {
                this.mContentView.setTranslationY(0.0f);
            }
            this.mShowListener.onAnimationEnd(null);
        }
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.requestFitSystemWindows();
        }
    }

    private void ensureTabsExist() {
        if (this.mTabContainerView == null) {
            AmigoTabContainerView tabContainerView = new AmigoTabContainerView(this.mContext);
            if (this.mHasEmbeddedTabs) {
                tabContainerView.setVisibility(View.VISIBLE);//0
                this.mActionView.setEmbeddedTabView(tabContainerView);
            } else {
                if (getNavigationMode() == 2) {
                    tabContainerView.setVisibility(View.VISIBLE);//0
                    if (this.mOverlayLayout != null) {
                        this.mOverlayLayout.requestFitSystemWindows();
                    }
                } else {
                    tabContainerView.setVisibility(View.GONE);//8
                }
                this.mContainerView.setTabContainer(tabContainerView);
            }
            this.mTabContainerView = tabContainerView;
            this.mTabContainerView.setActionBarOverlay(this.mActionBarOverlay);
            if (ChameleonColorManager.isNeedChangeColor() && !this.mActionBarOverlay) {
                this.mTabContainerView.changeColors();
            }
        }
    }

    public View getCustomView() {
        return this.mActionView.getCustomNavigationView();
    }

    public int getDisplayOptions() {
        return this.mActionView.getDisplayOptions();
    }

    public int getHeight() {
        return this.mContainerView.getHeight();
    }

    public int getNavigationItemCount() {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                SpinnerAdapter adapter = this.mActionView.getDropdownAdapter();
                return adapter != null ? adapter.getCount() : 0;
            case 2:
                return this.mTabs.size();
            default:
                return 0;
        }
    }

    public int getNavigationMode() {
        return this.mActionView.getNavigationMode();
    }

    public int getSelectedNavigationIndex() {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                return this.mActionView.getDropdownSelectedPosition();
            case 2:
                if (this.mSelectedTab != null) {
                    return this.mSelectedTab.getPosition();
                }
                return -1;
            default:
                return -1;
        }
    }

    public Tab getSelectedTab() {
        return this.mSelectedTab;
    }

    public CharSequence getSubtitle() {
        return this.mActionView.getSubtitle();
    }

    public Tab getTabAt(int index) {
        return (Tab) this.mTabs.get(index);
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    public Context getThemedContext() {
        if (this.mThemedContext == null) {
            TypedValue outValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(16843671, outValue, true);
            int targetThemeRes = outValue.resourceId;
            if (targetThemeRes == 0 || this.mContext.getThemeResId() == targetThemeRes) {
                this.mThemedContext = this.mContext;
            } else {
                this.mThemedContext = new ContextThemeWrapper(this.mContext, targetThemeRes);
            }
        }
        return this.mThemedContext;
    }

    public CharSequence getTitle() {
        return this.mActionView.getTitle();
    }

    public boolean hasNonEmbeddedTabs() {
        return !this.mHasEmbeddedTabs && getNavigationMode() == 2;
    }

    public void hide() {
        if (!this.mHiddenByApp) {
            this.mHiddenByApp = true;
            updateVisibility(false);
        }
    }

    private void hideForActionMode() {
        if (this.mShowingForMode) {
            this.mShowingForMode = false;
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.setShowingForActionMode(false);
            }
            updateVisibility(false);
        }
    }

    public void hideForSystem() {
        if (!this.mHiddenBySystem) {
            this.mHiddenBySystem = true;
            updateVisibility(true);
        }
    }

    private void init(View decor) {
        boolean z = false;
        this.mContext = decor.getContext();
        this.mOverlayLayout = (AmigoActionBarOverlayLayout) decor.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_bar_overlay_layout"));
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setActionBar(this);
        }
        ViewStub viewStub = (ViewStub) decor.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_actionbar_container_stub"));
        if (viewStub != null) {
            ViewGroup mActionbarContainerView = (ViewGroup) viewStub.inflate();
            this.mActionView = (AmigoActionBarView) mActionbarContainerView.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_bar"));
            this.mContextView = (AmigoActionBarContextView) mActionbarContainerView.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_context_bar"));
            this.mContainerView = (AmigoActionBarContainer) mActionbarContainerView.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_action_bar_container"));
        }
        if (this.mTopVisibilityView == null) {
            this.mTopVisibilityView = this.mContainerView;
        }
        if (this.mActionView == null || this.mContextView == null || this.mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with a compatible window decor layout");
        }
        int i;
        boolean homeAsUp;
        this.mActionView.setActivity((AmigoActivity) this.mActivity);
        this.mActionView.setContextView(this.mContextView);
        if (this.mActionView.isSplitActionBar()) {
            i = 1;
        } else {
            i = 0;
        }
        this.mContextDisplayMode = i;
        if ((this.mActionView.getDisplayOptions() & 4) != 0) {
            homeAsUp = true;
        } else {
            homeAsUp = false;
        }
        if (homeAsUp) {
            this.mDisplayHomeAsUpSet = true;
        }
        ActionBarPolicy abp = ActionBarPolicy.get(this.mContext);
        if (abp.enableHomeButtonByDefault() || homeAsUp) {
            z = true;
        }
        setHomeButtonEnabled(z);
        setHasEmbeddedTabs(abp.hasEmbeddedTabs());
        if (ChameleonColorManager.isNeedChangeColor() && !this.mActionBarOverlay) {
            int primaryBackgroundColor = ChameleonColorManager.getAppbarColor_A1();
            ColorDrawable backgroundDrawable = new ColorDrawable(primaryBackgroundColor);
            setBackgroundDrawable(backgroundDrawable);
            setStackedBackgroundDrawable(backgroundDrawable);
            setSplitBackgroundDrawable(backgroundDrawable);
            AmigoReflectionUtil.setStatusBarColor(this.mActivity.getWindow(), primaryBackgroundColor);
            this.mActionView.changeColors();
        }
    }

    public boolean isShowing() {
        return this.mNowShowing;
    }

    public boolean isSystemShowing() {
        return !this.mHiddenBySystem;
    }

    public Tab newTab() {
        return new TabImpl();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        setHasEmbeddedTabs(AmigoActionBarPolicy.get(this.mContext).hasEmbeddedTabs());
    }

    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (this.mTabContainerView != null) {
            this.mTabContainerView.onPageScrolled(arg0, arg1, arg2);
        }
    }

    public void onScrollToEnd(View v, MotionEvent event) {
    }

    public void removeAllTabs() {
        cleanupTabs();
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.remove(listener);
    }

    public void removeTab(Tab tab) {
        removeTabAt(tab.getPosition());
    }

    public void removeTabAt(int position) {
        if (this.mTabContainerView != null) {
            int selectedTabPosition = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : this.mSavedTabPosition;
            this.mTabContainerView.removeTabAt(position);
            TabImpl removedTab = (TabImpl) this.mTabs.remove(position);
            if (removedTab != null) {
                removedTab.setPosition(-1);
            }
            int newTabCount = this.mTabs.size();
            for (int i = position; i < newTabCount; i++) {
                ((TabImpl) this.mTabs.get(i)).setPosition(i);
            }
            if (selectedTabPosition == position) {
                Tab tab = null;
                if (this.mTabs.isEmpty()) {
                    tab = null;
                } else {
                    TabImpl tabImpl = (TabImpl) this.mTabs.get(Math.max(0, position - 1));
                }
                selectTab(tab);
            }
        }
    }

    public void selectTab(Tab tab) {
        int i = -1;
        if (getNavigationMode() != 2) {
            if (tab != null) {
                i = tab.getPosition();
            }
            this.mSavedTabPosition = i;
            return;
        }
        FragmentTransaction trans = this.mActivity.getFragmentManager().beginTransaction().disallowAddToBackStack();
        if (this.mSelectedTab != tab) {
            AmigoTabContainerView amigoTabContainerView = this.mTabContainerView;
            if (tab != null) {
                i = tab.getPosition();
            }
            amigoTabContainerView.setTabSelected(i);
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabUnselected(this.mSelectedTab, trans);
            }
            this.mSelectedTab = (TabImpl) tab;
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabSelected(this.mSelectedTab, trans);
            }
        } else if (this.mSelectedTab != null) {
            this.mSelectedTab.getCallback().onTabReselected(this.mSelectedTab, trans);
            this.mTabContainerView.setTabSelected(tab.getPosition());
        }
        if (!trans.isEmpty()) {
            trans.commit();
        }
    }

    public void setBackgroundDrawable(Drawable d) {
        this.mContainerView.setPrimaryBackground(d);
    }

    public void setCustomView(int resId) {
        setCustomView(LayoutInflater.from(getThemedContext()).inflate(resId, this.mActionView, false));
    }

    public void setCustomView(View view) {
        this.mActionView.setCustomNavigationView(view);
    }

    public void setCustomView(View view, LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        this.mActionView.setCustomNavigationView(view);
    }

    public void setDefaultDisplayHomeAsUpEnabled(boolean enable) {
        if (!this.mDisplayHomeAsUpSet) {
            setDisplayHomeAsUpEnabled(enable);
        }
    }

    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        setDisplayOptions(showHomeAsUp ? 4 : 0, 4);
    }

    public void setDisplayOptions(int options) {
        boolean z = true;
        if ((options & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mActionView.setDisplayOptions(options);
        if ((options & 32) == 0) {
            z = false;
        }
        setDisplayShowExtraViewEnabled(z);
    }

    public void setDisplayOptions(int options, int mask) {
        boolean z = true;
        int current = this.mActionView.getDisplayOptions();
        if ((mask & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mActionView.setDisplayOptions((options & mask) | ((mask ^ -1) & current));
        if ((options & 32) == 0) {
            z = false;
        }
        setDisplayShowExtraViewEnabled(z);
    }

    public void setDisplayShowCustomEnabled(boolean showCustom) {
        setDisplayOptions(showCustom ? 16 : 0, 16);
    }

    public void setDisplayShowHomeEnabled(boolean showHome) {
        setDisplayOptions(showHome ? 2 : 0, 2);
    }

    public void setDisplayShowTitleEnabled(boolean showTitle) {
        setDisplayOptions(showTitle ? 8 : 0, 8);
    }

    public void setDisplayUseLogoEnabled(boolean useLogo) {
        setDisplayOptions(useLogo ? 1 : 0, 1);
    }

    private void setHasEmbeddedTabs(boolean hasEmbeddedTabs) {
        boolean isInTabMode;
        boolean z = true;
        this.mHasEmbeddedTabs = hasEmbeddedTabs;
        if (this.mHasEmbeddedTabs) {
            this.mContainerView.setTabContainer(null);
            this.mActionView.setEmbeddedTabView(this.mTabContainerView);
        } else {
            this.mActionView.setEmbeddedTabView(null);
            this.mContainerView.setTabContainer(this.mTabContainerView);
        }
        if (getNavigationMode() == 2) {
            isInTabMode = true;
        } else {
            isInTabMode = false;
        }
        if (this.mTabContainerView != null) {
            if (isInTabMode) {
                this.mTabContainerView.setVisibility(View.VISIBLE);
                if (this.mOverlayLayout != null) {
                    this.mOverlayLayout.requestFitSystemWindows();
                }
            } else {
                this.mTabContainerView.setVisibility(View.GONE);
            }
        }
        AmigoActionBarView amigoActionBarView = this.mActionView;
        if (this.mHasEmbeddedTabs || !isInTabMode) {
            z = false;
        }
        amigoActionBarView.setCollapsable(z);
    }

    public void setHomeButtonEnabled(boolean enable) {
        this.mActionView.setHomeButtonEnabled(enable);
    }

    public void setIcon(Drawable icon) {
        this.mActionView.setIcon(icon);
    }

    public void setIcon(int resId) {
        this.mActionView.setIcon(resId);
    }

    public void setIndicatorBackgroundColor(int color) {
        ensureTabsExist();
        this.mTabContainerView.setIndicatorBackgroundColor(color);
    }

    public void setListNavigationCallbacks(SpinnerAdapter adapter, OnNavigationListener callback) {
        this.mActionView.setDropdownAdapter(adapter);
        this.mActionView.setCallback(callback);
    }

    public void setLogo(Drawable logo) {
        this.mActionView.setLogo(logo);
    }

    public void setLogo(int resId) {
        this.mActionView.setLogo(resId);
    }

    public void setNavigationMode(int mode) {
        boolean z = false;
        int oldMode = this.mActionView.getNavigationMode();
        switch (oldMode) {
            case 2:
                this.mSavedTabPosition = getSelectedNavigationIndex();
                selectTab(null);
                this.mTabContainerView.setVisibility(View.GONE);
                break;
        }
        if (!(oldMode == mode || this.mHasEmbeddedTabs || this.mOverlayLayout == null)) {
            this.mOverlayLayout.requestFitSystemWindows();
        }
        this.mActionView.setNavigationMode(mode);
        switch (mode) {
            case 2:
                ensureTabsExist();
                this.mTabContainerView.setVisibility(View.VISIBLE);
                if (this.mSavedTabPosition != -1) {
                    setSelectedNavigationItem(this.mSavedTabPosition);
                    this.mSavedTabPosition = -1;
                    break;
                }
                break;
        }
        AmigoActionBarView amigoActionBarView = this.mActionView;
        if (mode == 2 && !this.mHasEmbeddedTabs) {
            z = true;
        }
        amigoActionBarView.setCollapsable(z);
    }

    public void setSelectedNavigationItem(int position) {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                this.mActionView.setDropdownSelectedPosition(position);
                return;
            case 2:
                selectTab((Tab) this.mTabs.get(position));
                return;
            default:
                throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
        }
    }

    public void setShowHideAnimationEnabled(boolean enabled) {
        this.mShowHideAnimationEnabled = enabled;
        if (!enabled && this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
    }

    public void setSplitBackgroundDrawable(Drawable d) {
    }

    public void setStackedBackgroundDrawable(Drawable d) {
        this.mContainerView.setStackedBackground(d);
    }

    public void setSubtitle(CharSequence subtitle) {
        this.mActionView.setSubtitle(subtitle);
    }

    public void setSubtitle(int resId) {
        setSubtitle(this.mContext.getString(resId));
    }

    public void setTitle(CharSequence title) {
        this.mActionView.setTitle(title);
    }

    public void setTitle(int resId) {
        setTitle(this.mContext.getString(resId));
    }

    public void setWindowVisibility(int visibility) {
        this.mCurWindowVisibility = visibility;
    }

    public void show() {
        if (this.mHiddenByApp) {
            this.mHiddenByApp = false;
            updateVisibility(false);
        }
    }

    private void showForActionMode() {
        if (!this.mShowingForMode) {
            this.mShowingForMode = true;
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.setShowingForActionMode(true);
            }
            updateVisibility(false);
        }
    }

    public void showForSystem() {
        if (this.mHiddenBySystem) {
            this.mHiddenBySystem = false;
            updateVisibility(true);
        }
    }

    public ActionMode startActionMode(Callback callback) {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        this.mContextView.killMode();
        ActionModeImpl mode = new ActionModeImpl(callback);
        if (!mode.dispatchOnCreate()) {
            return null;
        }
        mode.invalidate();
        this.mContextView.initForMode(mode);
        animateToMode(true);
        this.mContextView.sendAccessibilityEvent(32);
        this.mActionMode = mode;
        Menu actionModeMenu = this.mActionMode.getMenu();
        if (actionModeMenu != null && actionModeMenu.size() > 0) {
            ((AmigoActivity) this.mActivity).setOptionsMenuUnExpand();
            ((AmigoActivity) this.mActivity).parserMenuInfo(actionModeMenu);
            ((AmigoActivity) this.mActivity).setOptionsMenuHideMode(false);
        }
        AmigoMagicBar amigoMagicBar = ((AmigoActivity) this.mActivity).getAmigoMagicBar();
        if (amigoMagicBar == null) {
            return mode;
        }
        amigoMagicBar.setMagicBarVisibilityWithAnim(0);
        return mode;
    }

    private void updateVisibility(boolean fromSystem) {
        if (checkShowingFlags(this.mHiddenByApp, this.mHiddenBySystem, this.mShowingForMode)) {
            if (!this.mNowShowing) {
                this.mNowShowing = true;
                doShow(fromSystem);
            }
        } else if (this.mNowShowing) {
            this.mNowShowing = false;
            doHide(fromSystem);
        }
    }

    public void setActivityContent(View view) {
        this.mContainerView.setActivityContent(view);
    }

    public void setExtraView(View view) {
        this.mContainerView.setExtraView(view);
    }

    public void setOnExtraViewDragListener(OnExtraViewDragListener listener) {
        this.mContainerView.setOnExtraViewDragListener(listener);
    }

    public boolean isActionModeShowing() {
        if (this.mContextView != null) {
            return this.mContextView.isActionModeShowing();
        }
        return false;
    }

    public boolean isActionModeHasMenu() {
        if (this.mActionMode == null) {
            return false;
        }
        Menu actionModeMenu = this.mActionMode.getMenu();
        if (actionModeMenu == null || actionModeMenu.size() <= 0) {
            return false;
        }
        return true;
    }

    public ActionMode getActionMode() {
        return this.mActionMode;
    }

    public void setOnBackClickListener(OnClickListener listener) {
        this.mActionView.setOnBackClickListener(listener);
    }

    public void setOnActionBarDoubleClickListener(OnClickListener listener) {
        this.mActionView.setOnActionBarDoubleClickListener(listener);
    }

    public void updateActionMode() {
        if (this.mActionMode != null) {
            Menu actionModeMenu = this.mActionMode.getMenu();
            if (actionModeMenu != null && actionModeMenu.size() > 0) {
                ((AmigoActivity) this.mActivity).setOptionsMenuUnExpand();
                ((AmigoActivity) this.mActivity).parserMenuInfo(actionModeMenu);
                ((AmigoActivity) this.mActivity).setOptionsMenuHideMode(false);
            }
        }
    }

    public void setDisplayShowExtraViewEnabled(boolean showExtraView) {
        this.mContainerView.setDragEnable(showExtraView);
    }

    public void setCustomMenu(Menu menu) {
        if (menu.size() > 0 && this.mActionView != null) {
            this.mActionView.setMenu(menu);
        }
    }

    public void changeColors() {
    }
}
