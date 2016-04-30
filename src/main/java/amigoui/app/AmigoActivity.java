package amigoui.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window.Callback;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gionee.aminote.R;

import java.lang.reflect.Field;

import amigoui.app.AmigoActionBarImpl.ActionModeImpl;
import amigoui.app.AmigoAlertDialog.Builder;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.reflection.AmigoReflectionUtil;
import amigoui.widget.AmigoMagicBar;
import amigoui.widget.AmigoMagicBar.OnMagicBarVisibleChangedListener;
import amigoui.widget.AmigoMagicBar.OnTransparentTouchListener;
import amigoui.widget.AmigoMagicBar.onMoreItemSelectedListener;
import amigoui.widget.AmigoMagicBar.onOptionsItemLongClickListener;
import amigoui.widget.AmigoMagicBar.onOptionsItemSelectedListener;
import amigoui.widget.AmigoWidgetResource;

public class AmigoActivity extends Activity implements onOptionsItemSelectedListener, onMoreItemSelectedListener, OnTransparentTouchListener, onOptionsItemLongClickListener {
    private static final int ACTION_MODE_TYPE_FLOATING = 1;
    private static final int ACTION_MODE_TYPE_PRIMARY = 0;
    private static final String TAG = "AmigoActivity";
    private ActionBar mActionBar = null;
    private AmigoActionBarImpl mAmigoActionBar = null;
    private AmigoMagicBar mAmigoMagicBar;
    private FrameLayout mContentLayout;
    private boolean mControlCreate = true;
    private boolean mDelay = true;
    private LinearLayout mEmptyLayout;
    private boolean mFeatureActionBarHide;
    private boolean mHideMode = false;
    private LayoutInflater mLayoutInflater;
    private LinearLayout mMagicbarBg;
    private MenuBuilder mMenu;
    private Menu mOptionMenu;
    private ViewGroup mScreenActionBarLayout;
    private boolean mShowAgain = true;
    private boolean mThemeActionBarHide;
    private TranslateAnimation mTranslateAnimation;
    OnTouchListener magicbarTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case 0:
                    if (AmigoActivity.this.mAmigoMagicBar != null && AmigoActivity.this.isMagicbarExpand()) {
                        AmigoActivity.this.OnTransparentTouch(v, event);
                        return true;
                    }
            }
            return false;
        }
    };

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ChameleonColorManager.isNeedChangeColor()) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(16842840, outValue, true);
            if (outValue.data == 0) {
                getWindow().setBackgroundDrawable(new ColorDrawable(ChameleonColorManager.getBackgroudColor_B1()));
            }
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mDelay) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    AmigoActivity.this.startOptionsMenu();
                }
            }, 50);
            this.mDelay = false;
        }
        setStatusBarColor();
    }

    protected void onStop() {
        if (this.mAmigoMagicBar != null) {
            this.mAmigoMagicBar.setListViewVisibilityWithoutAnim(8);
        }
        super.onStop();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (isUseOriginalActionBar()) {
            this.mAmigoActionBar.setCustomMenu(menu);
            return super.onCreateOptionsMenu(menu);
        } else if (this.mAmigoMagicBar == null) {
            return super.onCreateOptionsMenu(menu);
        } else {
            if (menu == null || menu.size() <= 0) {
                return true;
            }
            if (this.mAmigoActionBar != null && this.mAmigoActionBar.getActionMode() != null) {
                return true;
            }
            this.mAmigoMagicBar.setMenus(menu);
            return true;
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        this.mOptionMenu = menu;
        if (isUseOriginalActionBar()) {
            this.mAmigoActionBar.setCustomMenu(menu);
            return super.onPrepareOptionsMenu(menu);
        } else if (this.mAmigoMagicBar == null) {
            return super.onPrepareOptionsMenu(menu);
        } else {
            if (menu == null || menu.size() <= 0) {
                return true;
            }
            if (this.mAmigoActionBar != null && this.mAmigoActionBar.getActionMode() != null) {
                return true;
            }
            this.mAmigoMagicBar.setMenus(menu);
            return true;
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (isUseOriginalActionBar()) {
            if (this.mOptionMenu != null) {
                this.mAmigoActionBar.setCustomMenu(this.mOptionMenu);
            }
            return super.onOptionsItemSelected(menuItem);
        } else if (this.mAmigoMagicBar == null) {
            return super.onOptionsItemSelected(menuItem);
        } else {
            if (menuItem.hasSubMenu()) {
                final SubMenu subMenu = menuItem.getSubMenu();
                CharSequence[] subMenuTitles = new CharSequence[subMenu.size()];
                final int[] subMenuIds = new int[subMenu.size()];
                for (int i = 0; i < subMenu.size(); i++) {
                    subMenuTitles[i] = subMenu.getItem(i).getTitle();
                    subMenuIds[i] = subMenu.getItem(i).getItemId();
                }
                new Builder(this).setTitle(menuItem.getTitle()).setItems(subMenuTitles, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AmigoActivity.this.onOptionsItemSelected(subMenu.findItem(subMenuIds[which]));
                    }
                }).show();
            }
            if (this.mAmigoActionBar != null && this.mAmigoActionBar.isActionModeShowing()) {
                ActionMode actionMode = this.mAmigoActionBar.getActionMode();
                if (actionMode != null) {
                    ((ActionModeImpl) actionMode).getCallback().onActionItemClicked(actionMode, menuItem);
                }
            }
            if (isMagicbarExpand()) {
                handItemClick();
            }
            return true;
        }
    }

    public void updateOptionsMenu(Menu menu) {
        if (this.mAmigoMagicBar != null && menu != null && menu.size() > 0) {
            this.mAmigoMagicBar.setMenus(menu);
        }
    }

    public boolean onMoreItemSelected(View view) {
        if (!(this.mAmigoActionBar == null || isMagicbarExpand() || this.mAmigoActionBar.isActionModeShowing())) {
            startOptionsMenu();
        }
        handItemClick();
        return true;
    }

    public boolean OnTransparentTouch(View v, MotionEvent event) {
        handItemClick();
        return true;
    }

    public boolean onOptionsItemLongClick(MenuItem menuItem) {
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mAmigoActionBar != null) {
            this.mAmigoActionBar.onConfigurationChanged(newConfig);
        }
        if (this.mAmigoMagicBar != null) {
            this.mAmigoMagicBar.onConfigurationChanged(newConfig);
        }
    }

    public void SetAmigoMagicBarNull() {
        if (this.mAmigoMagicBar != null) {
            this.mAmigoMagicBar = null;
        }
    }

    public void setOptionsMenuHideMode(boolean is_hide) {
        if (this.mHideMode != is_hide) {
            this.mHideMode = is_hide;
            if (this.mAmigoMagicBar != null) {
                this.mAmigoMagicBar.setHideMode(this.mHideMode);
                if (this.mHideMode) {
                    cancelAnimationListener();
                    this.mAmigoMagicBar.setMagicBarVisibilityWithoutAnim(8);
                    return;
                }
                setLayoutVisibility(0);
            }
        }
    }

    private void cancelAnimationListener() {
        if (this.mTranslateAnimation != null) {
            this.mTranslateAnimation.setAnimationListener(null);
        }
    }

    public void setOptionsMenuHideMode(boolean is_hide, boolean show_again) {
        setOptionsMenuHideMode(is_hide);
        this.mShowAgain = show_again;
    }

    private void setLayoutVisibility(int visibility) {
        if (this.mAmigoMagicBar != null) {
            this.mAmigoMagicBar.setMagicBarVisibilityWithAnim(visibility);
        }
    }

    public boolean getOptionsMenuHideMode() {
        return this.mHideMode;
    }

    public void setOptionsMenuUnExpand() {
        if (this.mAmigoMagicBar != null) {
            this.mAmigoMagicBar.setMagicBarVisibilityWithoutAnim(8);
        }
    }

    private void handItemClick() {
        if (this.mAmigoMagicBar != null) {
            this.mAmigoMagicBar.changeListViewVisiable(true);
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAmigoMagicBar == null) {
            return super.onKeyUp(keyCode, event);
        }
        if (keyCode == 82) {
            if (this.mAmigoActionBar != null && this.mAmigoActionBar.isActionModeShowing() && !this.mAmigoActionBar.isActionModeHasMenu()) {
                return true;
            }
            if (!(isMagicbarExpand() || this.mAmigoActionBar == null || this.mAmigoActionBar.isActionModeShowing())) {
                startOptionsMenu();
            }
            if (!this.mShowAgain) {
                this.mAmigoMagicBar.setMagicBarVisibilityWithoutAnim(8);
                return super.onKeyUp(keyCode, event);
            } else if (this.mHideMode) {
                this.mHideMode = false;
                if (haveOptionsMenu()) {
                    setLayoutVisibility(0);
                    return true;
                }
                this.mAmigoMagicBar.setMagicBarVisibilityWithoutAnim(8);
                return true;
            } else if (this.mEmptyLayout.getVisibility() == View.GONE) {
                return true;
            } else {
                handItemClick();
                return true;
            }
        } else if (keyCode != 4) {
            return super.onKeyUp(keyCode, event);
        } else {
            if (isMagicbarExpand()) {
                if (this.mAmigoMagicBar == null) {
                    return true;
                }
                this.mAmigoMagicBar.changeListViewVisiable(true);
                return true;
            } else if (this.mAmigoActionBar == null || !this.mAmigoActionBar.isActionModeShowing()) {
                return super.onKeyUp(keyCode, event);
            } else {
                ActionMode actionMode = this.mAmigoActionBar.getActionMode();
                if (actionMode == null) {
                    return true;
                }
                actionMode.finish();
                return true;
            }
        }
    }

    public void parserMenuInfo(Menu menu) {
        if (this.mAmigoMagicBar != null) {
            if (menu == null || menu.size() == 0) {
                this.mAmigoMagicBar.clearMagicBarData();
            } else {
                this.mAmigoMagicBar.setMenus(menu);
            }
        }
    }

    public AmigoActionBar getAmigoActionBar() {
        initAmigoActionBar();
        return this.mAmigoActionBar;
    }

    private void initAmigoActionBar() {
        generalScreenLayout();
        if (!this.mFeatureActionBarHide && !this.mThemeActionBarHide) {
            if (this.mAmigoActionBar == null) {
                this.mAmigoActionBar = new AmigoActionBarImpl((Activity) this);
                this.mAmigoActionBar.setActivityContent(this.mContentLayout);
            }
            if (isChild() || this.mFeatureActionBarHide || this.mThemeActionBarHide) {
                this.mAmigoActionBar.hide();
            }
            hideOriginalActionBar();
        }
    }

    private void hideOriginalActionBar() {
        if (this.mActionBar == null) {
            this.mActionBar = getActionBar();
        }
        if (this.mActionBar != null && this.mActionBar.isShowing()) {
            this.mActionBar.hide();
        }
    }

    public void setContentView(int layoutResID) {
        this.mThemeActionBarHide = isThemeActionBarHide();
        setContentViewWithAmigoActionBar(layoutResID);
        initAmigoActionBar();
    }

    public void setContentView(View view) {
        this.mThemeActionBarHide = isThemeActionBarHide();
        setContentViewWithAmigoActionBar(view);
        initAmigoActionBar();
    }

    public void setContentView(View view, LayoutParams params) {
        this.mThemeActionBarHide = isThemeActionBarHide();
        setContentViewWithAmigoActionBar(view, params);
        initAmigoActionBar();
    }

    public void addContentView(View view, LayoutParams params) {
        addContentViewWithAmigoActionBar(view, params);
        initAmigoActionBar();
    }

    private void addContentViewWithAmigoActionBar(View view, LayoutParams params) {
        if (this.mContentLayout == null) {
            setContentViewWithAmigoActionBar(view, params);
        } else {
            this.mContentLayout.addView(view, params);
        }
        Callback cb = getWindow().getCallback();
        if (cb != null) {
            cb.onContentChanged();
        }
    }

    private void setContentViewWithAmigoActionBar(int layoutResID) {
        generalScreenLayout();
        this.mContentLayout = (FrameLayout) this.mScreenActionBarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this, "amigo_content"));
        this.mEmptyLayout = (LinearLayout) this.mScreenActionBarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this, "amigo_overlap"));
        this.mAmigoMagicBar = (AmigoMagicBar) this.mScreenActionBarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this, "amigo_magic_bar"));
        this.mAmigoMagicBar.setHideMode(this.mHideMode);
        this.mAmigoMagicBar.setonOptionsItemSelectedListener(this);
        this.mAmigoMagicBar.setonMoreItemSelectedListener(this);
        this.mAmigoMagicBar.setonTransparentTouchListener(this);
        this.mAmigoMagicBar.setonOptionsItemLongClickListener(this);
        this.mAmigoMagicBar.setOnMagicBarVisibleChangedListener(new OnMagicBarVisibleChangedListener() {
            public void onMagicBarVisibleChanged(int visibility) {
                AmigoActivity.this.mEmptyLayout.setVisibility(visibility);
            }
        });
        initMagicBarBgLayout();
        this.mContentLayout.removeAllViews();
        this.mLayoutInflater.inflate(layoutResID, this.mContentLayout);
        getWindow().setContentView(this.mScreenActionBarLayout);
    }

    private void setContentViewWithAmigoActionBar(View view) {
        setContentViewWithAmigoActionBar(view, new RelativeLayout.LayoutParams(-2, -2));
    }

    private void setContentViewWithAmigoActionBar(View view, LayoutParams params) {
        generalScreenLayout();
        this.mContentLayout = (FrameLayout) this.mScreenActionBarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this, "amigo_content"));
        this.mEmptyLayout = (LinearLayout) this.mScreenActionBarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this, "amigo_overlap"));
        this.mAmigoMagicBar = (AmigoMagicBar) this.mScreenActionBarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this, "amigo_magic_bar"));
        this.mAmigoMagicBar.setonOptionsItemSelectedListener(this);
        this.mAmigoMagicBar.setonMoreItemSelectedListener(this);
        this.mAmigoMagicBar.setonTransparentTouchListener(this);
        this.mAmigoMagicBar.setonOptionsItemLongClickListener(this);
        this.mAmigoMagicBar.setOnMagicBarVisibleChangedListener(new OnMagicBarVisibleChangedListener() {
            public void onMagicBarVisibleChanged(int visibility) {
                AmigoActivity.this.mEmptyLayout.setVisibility(visibility);
            }
        });
        initMagicBarBgLayout();
        this.mContentLayout.removeAllViews();
        this.mContentLayout.addView(view, params);
        getWindow().setContentView(this.mScreenActionBarLayout);
    }

    @Deprecated
    public ActionBar getActionBar() {
        return super.getActionBar();
    }

    private boolean isFeatureAcitonBarHide() {
        if (!getWindow().hasFeature(8) && getWindow().hasFeature(1)) {
            return true;
        }
        return false;
    }

    private boolean isThemeActionBarHide() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(16842838, outValue, true);
        if (outValue.data != 0) {
            return true;
        }
        getTheme().resolveAttribute(16843469, outValue, true);
        if (outValue.data != 0) {
            return false;
        }
        return true;
    }

    private void setWindowFeatureNoTitle() {
        try {
            requestWindowFeature(1);
        } catch (Exception e) {
        }
    }

    private boolean isAmigoActionBarHide() {
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(16842838, outValue, true);
        if (outValue.data != 0) {
            return true;
        }
        getTheme().resolveAttribute(16843469, outValue, true);
        if (outValue.data != 0) {
            return false;
        }
        return true;
    }

    public View getViewWithAmigoActionBar() {
        return this.mScreenActionBarLayout;
    }

    public void setTitle(CharSequence title) {
        AmigoActionBar actionBar = getAmigoActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    public void setTitle(int titleId) {
        setTitle(getResources().getString(titleId));
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        if (getActionModeType() == 1) {
            return super.onWindowStartingActionMode(callback);
        }
        initAmigoActionBar();
        if (this.mAmigoActionBar != null) {
            return this.mAmigoActionBar.startActionMode(callback);
        }
        return null;
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return onWindowStartingActionMode(callback);
    }

    private void generalScreenLayout() {
        if (this.mScreenActionBarLayout == null) {
            int layoutResource;
            this.mFeatureActionBarHide = isFeatureAcitonBarHide();
            setWindowFeatureNoTitle();
            if (this.mLayoutInflater == null) {
                this.mLayoutInflater = LayoutInflater.from(this);
            }
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(16842839, outValue, true);
            if (outValue.data != 0) {
                layoutResource = AmigoWidgetResource.getIdentifierByLayout(this, "amigo_screen_dialog");
            } else {
                getTheme().resolveAttribute(16843492, outValue, true);
                if (outValue.data != 0 || getWindow().hasFeature(9)) {
                    layoutResource = AmigoWidgetResource.getIdentifierByLayout(this, "amigo_screen_action_bar_overlay");
                } else {
                    layoutResource = AmigoWidgetResource.getIdentifierByLayout(this, "amigo_screen_action_bar");
                }
            }
            this.mScreenActionBarLayout = (ViewGroup) this.mLayoutInflater.inflate(layoutResource, null);
        }
    }

    public Menu getOptionMenu() {
        return this.mOptionMenu;
    }

    private void initMagicBarBgLayout() {
        this.mMagicbarBg = (LinearLayout) this.mScreenActionBarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this, "amigo_magic_bar_bg"));
        if (this.mMagicbarBg != null) {
            this.mMagicbarBg.setOnTouchListener(this.magicbarTouchListener);
        }
    }

    public void invalidateOptionsMenu() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                AmigoActivity.this.invalOptionsMenu();
            }
        }, 50);
    }

    public void startOptionsMenu() {
        initMenu();
        if (this.mControlCreate) {
            this.mMenu.clear();
            onCreateOptionsMenu(this.mMenu);
            this.mControlCreate = false;
        }
        onPrepareOptionsMenu(this.mMenu);
    }

    public void invalOptionsMenu() {
        initMenu();
        this.mMenu.clear();
        onCreatePanelMenu(0, this.mMenu);
        onPrepareOptionsMenu(this.mMenu);
    }

    private void initMenu() {
        if (this.mMenu == null) {
            this.mMenu = new MenuBuilder(this).setDefaultShowAsAction(1);
        }
    }

    public void setStatusBarColor() {
        try {
            int color = obtainStyledAttributes(null, R.styleable.AmigoActionBar, AmigoWidgetResource.getIdentifierByAttr(this, "amigoactionBarStyle"), 0).getColor(R.styleable.AmigoActionBar_amigobackground, -1);
            if (ChameleonColorManager.isNeedChangeColor() && !getWindow().hasFeature(9)) {
                color = ChameleonColorManager.getStatusbarBackgroudColor_S1();
            }
            if (-1 != color) {
                AmigoReflectionUtil.setStatusBarColor(getWindow(), color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUseOriginalActionBar() {
        boolean z = false;
        try {
            z = obtainStyledAttributes(null, R.styleable.AmigoActionBar, AmigoWidgetResource.getIdentifierByAttr(this, "amigoactionBarStyle"), 0).getBoolean(R.styleable.AmigoActionBar_amigoOptionsMenuAsUp, false);
        } catch (Exception e) {
            Log.w(TAG, "get amigoOptionMenuAsUp error");
        }
        return z;
    }

    public AmigoMagicBar getAmigoMagicBar() {
        return this.mAmigoMagicBar;
    }

    private boolean haveOptionsMenu() {
        return this.mOptionMenu != null && this.mOptionMenu.size() > 0;
    }

    private boolean isMagicbarExpand() {
        return this.mAmigoMagicBar != null && this.mAmigoMagicBar.isExpand();
    }

    private int getActionModeType() {
        Field field = getDeclaredField(this, "mActionModeTypeStarting");
        int type = 0;
        if (field == null) {
            return type;
        }
        try {
            field.setAccessible(true);
            type = field.getInt(this);
        } catch (IllegalAccessException e) {
            Log.d(TAG, "IllegalAccessException");
            e.printStackTrace();
        } catch (IllegalArgumentException e2) {
            Log.d(TAG, "IllegalArgumentException");
            e2.printStackTrace();
        }
        return type;
    }

    private Field getDeclaredField(Object object, String fieldName) {
        Field field = null;
        Class<?> clazz = object.getClass();
        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (Exception e) {
                Log.d(TAG, "getDeclaredField e=" + e.toString());
                clazz = clazz.getSuperclass();
            }
        }
        return field;
    }
}
