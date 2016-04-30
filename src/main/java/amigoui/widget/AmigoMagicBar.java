package amigoui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.view.menu.MenuItemImpl;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gionee.aminote.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import amigoui.changecolors.ChameleonColorManager;
import uk.co.senab.photoview.IPhotoView;

//import com.android.internal.view.menu.MenuItemImpl;

public class AmigoMagicBar extends RelativeLayout implements OnClickListener, OnItemClickListener, OnTouchListener, OnLongClickListener {
    private static final int MAGICBAR_LISTVIEW_LANDSCAPE_MAX_LINE = 2;
    private static final int MAGICBAR_LISTVIEW_PORTRAIT_MAX_LINE = 4;
    public static final int MAGIC_BAR_MENU_MAX_COUNT = 4;
    public static final int MAX_BAR_ITEM_COUNT = 4;
    private static final int MAX_ICON_SIZE = 60;
    private static final int TRANSLATION_ANIMATION_DURATION = 220;
    private boolean isHideMode;
    private LinearLayout mAmigoMagicbarLayout;
    private Drawable mAmigoOptionMenuMoreBg;
    private Drawable mBackground;
    private Context mContext;
    private int mItemHeight;
    private int mListItemHeight;
    private float mListModeAnimationHeght;
    private int mListModeHeight;
    private int mListViewBottomMargin;
    private int mListViewHorizontalMargin;
    private int mListViewTopMargin;
    private onOptionsItemSelectedListener mListener;
    private onOptionsItemLongClickListener mLongClickListener;
    private ListView mMagicListView;
    private LinearLayout mMagicbarBackgroud;
    private int mMaxIconSize;
    private int mMaxListViewheight;
    private onMoreItemSelectedListener mMoreListener;
    private OnMagicBarVisibleChangedListener mOnMagicBarVisibleChangedListener;
    private View mShadow;
    private LinearLayout mSubstanceLayout;
    private Button[] mTabButtons;
    private int mTitleBottomPadding;
    private int mTitleModeHeight;
    private OnTransparentTouchListener mTouchListener;
    private Drawable mTransParent;
    private TranslateAnimation mTranslateAnimation;
    private List<MenuItem> menusOnList;
    private Map<Button, MenuItem> menusOnTab;

    class MenuListAdapter extends BaseAdapter {

        class Holder {
            public CheckBox checkBox;
            public TextView textView;

            Holder() {
            }
        }

        public int getCount() {
            return AmigoMagicBar.this.menusOnList.size();
        }

        public Object getItem(int position) {
            return AmigoMagicBar.this.menusOnList.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public boolean isEnabled(int position) {
            if (AmigoMagicBar.this.menusOnList.size() <= position || ((MenuItem) AmigoMagicBar.this.menusOnList.get(position)).isEnabled()) {
                return super.isEnabled(position);
            }
            return false;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                convertView = LayoutInflater.from(AmigoMagicBar.this.mContext).inflate(AmigoWidgetResource.getIdentifierByLayout(AmigoMagicBar.this.mContext, "amigo_magicbar_listview_item"), null);
                holder.textView = (TextView) convertView.findViewById(AmigoWidgetResource.getIdentifierById(AmigoMagicBar.this.mContext, "amigo_magic_listitem_textview"));
                holder.checkBox = (CheckBox) convertView.findViewById(AmigoWidgetResource.getIdentifierById(AmigoMagicBar.this.mContext, "amigo_magic_listitem_checkbox"));
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            MenuItem menuItem = (MenuItem) AmigoMagicBar.this.menusOnList.get(position);
            if (menuItem != null) {
                holder.textView.setText(menuItem.getTitle());
                holder.textView.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
                holder.checkBox.setVisibility(8);
                holder.checkBox.setChecked(false);
                if (menuItem.isEnabled()) {
                    holder.textView.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
                } else {
                    holder.textView.setAlpha(0.3f);
                }
                if (menuItem.isCheckable()) {
                    holder.checkBox.setVisibility(0);
                } else {
                    holder.checkBox.setVisibility(8);
                }
                if (menuItem.isChecked()) {
                    holder.checkBox.setChecked(true);
                } else {
                    holder.checkBox.setChecked(false);
                }
            }
            return convertView;
        }
    }

    public interface OnMagicBarVisibleChangedListener {
        void onMagicBarVisibleChanged(int i);
    }

    public interface OnTransparentTouchListener {
        boolean OnTransparentTouch(View view, MotionEvent motionEvent);
    }

    public interface onMoreItemSelectedListener {
        boolean onMoreItemSelected(View view);
    }

    public interface onOptionsItemLongClickListener {
        boolean onOptionsItemLongClick(MenuItem menuItem);
    }

    public interface onOptionsItemSelectedListener {
        boolean onOptionsItemSelected(MenuItem menuItem);
    }

    public AmigoMagicBar(Context context) {
        this(context, null);
    }

    public AmigoMagicBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.menusOnTab = new HashMap();
        this.menusOnList = new ArrayList();
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initData(context, attrs);
        initView();
        initAnim();
    }

    private void initAnim() {
        this.mTranslateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void initData(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoActionBar);
        this.mBackground = a.getDrawable(R.styleable.AmigoActionBar_amigobackgroundSplit);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, R.styleable.amigoOptionMenu);
        this.mAmigoOptionMenuMoreBg = a.getDrawable(R.styleable.amigoOptionMenu_amigooptionMenuMoreBg);
        a.recycle();
        Resources resources = context.getResources();
        this.mItemHeight = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_item_height"));
        this.mListItemHeight = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_list_item_height"));
        this.mTitleModeHeight = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_title_mode_height"));
        this.mMaxListViewheight = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_max_listview_height"));
        this.mListViewHorizontalMargin = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_listview_left_right_margin"));
        this.mListViewBottomMargin = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_listview_bottom_margin"));
        this.mMaxIconSize = (int) ((60.0f * resources.getDisplayMetrics().density) + 8.5f);
        this.mTitleBottomPadding = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_title_bottom_padding"));
        this.mListViewTopMargin = (int) resources.getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_magicbar_listview_top_margin"));
        this.mTransParent = getResources().getDrawable(17170445);
    }

    private void initView() {
        Context context = getContext();
        this.mAmigoMagicbarLayout = (LinearLayout) ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_magicbar_menu_item"), this, false);
        this.mMagicbarBackgroud = (LinearLayout) this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_magicbar_backgroud"));
        if (this.mBackground != null) {
            this.mMagicbarBackgroud.setBackground(this.mBackground);
        }
        this.mSubstanceLayout = (LinearLayout) this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_substance_bar"));
        initTabButtons();
        this.mShadow = this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_magicbar_shadow"));
        this.mShadow.setVisibility(8);
        this.mMagicListView = (ListView) this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_listview"));
        LayoutParams params = new LayoutParams(-1, -2);
        params.leftMargin = this.mListViewHorizontalMargin;
        params.rightMargin = this.mListViewHorizontalMargin;
        params.bottomMargin = this.mListViewBottomMargin;
        params.topMargin = this.mListViewTopMargin;
        this.mMagicListView.setLayoutParams(params);
        removeAllViews();
        addView(this.mAmigoMagicbarLayout);
    }

    private void initTabButtons() {
        this.mTabButtons = new Button[4];
        this.mTabButtons[0] = (Button) this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_icon0"));
        this.mTabButtons[1] = (Button) this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_icon1"));
        this.mTabButtons[2] = (Button) this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_icon2"));
        this.mTabButtons[3] = (Button) this.mAmigoMagicbarLayout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_icon3"));
        this.mTabButtons[0].setOnClickListener(this);
        this.mTabButtons[0].setOnLongClickListener(this);
        this.mTabButtons[1].setOnClickListener(this);
        this.mTabButtons[1].setOnLongClickListener(this);
        this.mTabButtons[2].setOnClickListener(this);
        this.mTabButtons[2].setOnLongClickListener(this);
        this.mTabButtons[3].setOnClickListener(this);
        this.mTabButtons[3].setOnLongClickListener(this);
    }

    public void clearMagicBarData() {
        initView();
        setVisibility(8);
    }

    public void setMenus(Menu menu) {
        if (isMenusEmpty(menu)) {
            setMagicBarVisibilityWithoutAnim(8);
            return;
        }
        int i;
        this.menusOnList.clear();
        this.menusOnTab.clear();
        for (Button btn : this.mTabButtons) {
            btn.setEnabled(false);
            btn.setVisibility(8);
        }
        List<MenuItem> alwaysMenuItem = new ArrayList();
        List<MenuItem> ifRoomMenuItem = new ArrayList();
        List<MenuItem> othersMenuItem = new ArrayList();
        int menuItemCount = menu.size();
        for (i = 0; i < menuItemCount; i++) {
            if (menu.getItem(i).isVisible()) {
                MenuItemImpl menuItem = (MenuItemImpl) menu.getItem(i);
                if (menuItem.requiresActionButton()) {
                    alwaysMenuItem.add(menuItem);
                } else if (menuItem.requestsActionButton()) {
                    ifRoomMenuItem.add(menuItem);
                } else {
                    othersMenuItem.add(menuItem);
                }
            }
        }
        List<MenuItem> tabMenuItem = new ArrayList();
        tabMenuItem.addAll(alwaysMenuItem);
        tabMenuItem.addAll(ifRoomMenuItem);
        MenuItem menuItem2;
        if (tabMenuItem.size() > 4) {
            setMoreIconAndText();
            this.mTabButtons[3].setVisibility(0);
            this.mTabButtons[3].setEnabled(true);
            for (i = 0; i < tabMenuItem.size(); i++) {
                menuItem2 = (MenuItem) tabMenuItem.get(i);
                if (i < 3) {
                    this.menusOnTab.put(this.mTabButtons[i], menuItem2);
                } else {
                    this.menusOnList.add(menuItem2);
                }
            }
        } else {
            for (i = 0; i < 3; i++) {
                if (i < tabMenuItem.size()) {
                    this.menusOnTab.put(this.mTabButtons[i], (MenuItem) tabMenuItem.get(i));
                }
            }
            if (othersMenuItem.size() > 0) {
                setMoreIconAndText();
                this.mTabButtons[3].setVisibility(0);
                this.mTabButtons[3].setEnabled(true);
                if (tabMenuItem.size() > 3) {
                    menuItem2 = (MenuItem) tabMenuItem.get(3);
                    if (menuItem2 != null) {
                        this.menusOnList.add(menuItem2);
                    }
                }
            } else if (tabMenuItem.size() > 3) {
                menuItem2 = (MenuItem) tabMenuItem.get(3);
                if (menuItem2 != null) {
                    this.menusOnTab.put(this.mTabButtons[3], menuItem2);
                }
            }
        }
        this.menusOnList.addAll(othersMenuItem);
        setTabButtonMenuIconAndText();
        this.mMagicListView.setAdapter(new MenuListAdapter());
        this.mMagicListView.setOnItemClickListener(this);
        setListViewMaxHeight();
        if (this.menusOnList.isEmpty() && this.menusOnTab.isEmpty()) {
            setMagicBarVisibilityWithoutAnim(8);
        } else if (!this.isHideMode) {
            setMagicBarVisibilityWithoutAnim(0);
        }
        updateShadow();
    }

    private void setTabButtonMenuIconAndText() {
        for (Button tabBtn : this.mTabButtons) {
            MenuItem menuItem = (MenuItem) this.menusOnTab.get(tabBtn);
            if (menuItem != null) {
                tabBtn.setCompoundDrawablesWithIntrinsicBounds(null, zoomIcon(getChameleonDrawable(menuItem.getIcon())), null, null);
                tabBtn.setEnabled(menuItem.isEnabled());
                tabBtn.setText(menuItem.getTitle());
                tabBtn.setTag(Integer.valueOf(menuItem.getItemId()));
                tabBtn.setVisibility(0);
            }
        }
    }

    private boolean haveMagicListView() {
        return isHaveData();
    }

    private boolean isMenusEmpty(Menu menu) {
        if (menu == null || menu.size() == 0) {
            return true;
        }
        return false;
    }

    public void setMoreIconAndText() {
        Drawable moreIcon = getChameleonDrawable(this.mAmigoOptionMenuMoreBg);
        Drawable closeIcon = getChameleonDrawable(this.mContext.getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_magic_menu_close_icon")));
        if (isExpand()) {
            this.mTabButtons[3].setCompoundDrawablesWithIntrinsicBounds(null, closeIcon, null, null);
            this.mTabButtons[3].setText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_actionbar_magic_item_close"));
        } else {
            this.mTabButtons[3].setCompoundDrawablesWithIntrinsicBounds(null, moreIcon, null, null);
            this.mTabButtons[3].setText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_actionbar_magic_item_more"));
        }
        if (this.menusOnList.isEmpty()) {
            this.mTabButtons[3].setVisibility(8);
            this.mTabButtons[3].setEnabled(false);
            return;
        }
        this.mTabButtons[3].setVisibility(0);
        this.mTabButtons[3].setEnabled(true);
    }

    private Drawable zoomIcon(Drawable icon) {
        if (icon == null) {
            return null;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        if (width > this.mMaxIconSize || height > this.mMaxIconSize) {
            return new BitmapDrawable(drawableToBitmap(icon));
        }
        return icon;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private Drawable getChameleonDrawable(Drawable drawable) {
        if (drawable != null && ChameleonColorManager.isNeedChangeColor()) {
            drawable.setColorFilter(ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1(), Mode.SRC_IN);
        }
        return drawable;
    }

    private void setListViewMaxHeight() {
        if (this.mMagicListView != null) {
            ViewGroup.LayoutParams p = this.mMagicListView.getLayoutParams();
            if (p != null) {
                int maxItemCount;
                if (getResources().getConfiguration().orientation == 2) {
                    maxItemCount = 2;
                } else {
                    maxItemCount = 4;
                }
                if (this.menusOnList.size() > maxItemCount) {
                    p.height = this.mMaxListViewheight;
                    this.mListModeAnimationHeght = (float) this.mMaxListViewheight;
                    return;
                }
                p.height = -2;
                this.mListModeAnimationHeght = (float) (this.menusOnList.size() * getListItemHeight());
            }
        }
    }

    public int getItemHeight() {
        return this.mItemHeight;
    }

    public int getListItemHeight() {
        return this.mListItemHeight;
    }

    public int getTitleModeHeight() {
        return this.mTitleModeHeight;
    }

    public boolean isExpand() {
        return this.mMagicListView.getVisibility() == 0;
    }

    private void updateView() {
        updateShadow();
        if (this.menusOnList.size() > 0) {
            setMoreIconAndText();
        }
    }

    private void updateShadow() {
        if (isExpand()) {
            this.mShadow.setBackgroundResource(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_magicbar_gradient"));
            setViewHeight((int) (8.0f * this.mContext.getResources().getDisplayMetrics().density));
        } else {
            setViewHeight(1);
            this.mShadow.setBackgroundColor(1350598784);
        }
        if (this.menusOnList.isEmpty() && this.menusOnTab.isEmpty()) {
            if (this.mShadow.getVisibility() != 8) {
                this.mShadow.setVisibility(8);
            }
        } else if (this.mShadow.getVisibility() != 0) {
            this.mShadow.setVisibility(0);
        }
    }

    private void setViewHeight(int height) {
        ViewGroup.LayoutParams lp = this.mShadow.getLayoutParams();
        lp.height = height;
        this.mShadow.setLayoutParams(lp);
        this.mShadow.invalidate();
    }

    public boolean isHaveData() {
        return this.mMagicListView != null && this.mMagicListView.getCount() > 0;
    }

    public void setonTransparentTouchListener(OnTransparentTouchListener l) {
        this.mTouchListener = l;
    }

    public void setonOptionsItemSelectedListener(onOptionsItemSelectedListener l) {
        this.mListener = l;
    }

    public void setonMoreItemSelectedListener(onMoreItemSelectedListener l) {
        this.mMoreListener = l;
    }

    public void setonOptionsItemLongClickListener(onOptionsItemLongClickListener l) {
        this.mLongClickListener = l;
    }

    public void setOnMagicBarVisibleChangedListener(OnMagicBarVisibleChangedListener l) {
        this.mOnMagicBarVisibleChangedListener = l;
    }

    public boolean onTouch(View arg0, MotionEvent arg1) {
        return false;
    }

    public void onClick(View v) {
        if (v.getId() == AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_icon3") && this.menusOnTab.get(v) == null) {
            if (this.mMoreListener != null) {
                this.mMoreListener.onMoreItemSelected(v);
            }
        } else if (this.mListener != null) {
            MenuItem menuItem = (MenuItem) this.menusOnTab.get(v);
            if (menuItem != null && menuItem.isEnabled()) {
                this.mListener.onOptionsItemSelected(menuItem);
            }
        }
    }

    public boolean onLongClick(View v) {
        if (!(this.mLongClickListener == null || (v.getId() == AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_icon3") && this.menusOnTab.get(v) == null))) {
            this.mLongClickListener.onOptionsItemLongClick((MenuItem) this.menusOnTab.get(v));
        }
        return true;
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mListener != null) {
            this.mListener.onOptionsItemSelected((MenuItem) this.menusOnList.get(position));
        }
    }

    public boolean isHideMode() {
        return this.isHideMode;
    }

    public void setHideMode(boolean isHideMode) {
        this.isHideMode = isHideMode;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        this.mMaxListViewheight = (int) this.mContext.getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_magicbar_max_listview_height"));
        setListViewMaxHeight();
    }

    public void changeListViewVisiable(boolean withAnim) {
        if (!haveMagicListView()) {
            return;
        }
        if (isExpand()) {
            if (withAnim) {
                setListViewVisibilityWithAnim(8);
            } else {
                setListViewVisibilityWithoutAnim(8);
            }
        } else if (withAnim) {
            setListViewVisibilityWithAnim(0);
        } else {
            setListViewVisibilityWithoutAnim(0);
        }
    }

    public void setMagicBarVisibilityWithoutAnim(int visibility) {
        setVisibility(visibility);
        if (this.mOnMagicBarVisibleChangedListener != null) {
            this.mOnMagicBarVisibleChangedListener.onMagicBarVisibleChanged(getVisibility());
        }
    }

    public void setMagicBarVisibilityWithAnim(int visibility) {
        this.mTranslateAnimation.cancel();
        if (visibility == 0) {
            setVisibility(0);
            setAnimParam(0.0f, 0.0f, (float) getHeight(), 0.0f);
        } else {
            setVisibility(8);
            setAnimParam(0.0f, 0.0f, 0.0f, (float) getHeight());
        }
        if (this.mOnMagicBarVisibleChangedListener != null) {
            this.mOnMagicBarVisibleChangedListener.onMagicBarVisibleChanged(getVisibility());
        }
        startAnimation(this.mTranslateAnimation);
    }

    public void setListViewVisibilityWithoutAnim(int visibility) {
        this.mMagicListView.setVisibility(visibility);
        updateView();
    }

    public void setListViewVisibilityWithAnim(int visibility) {
        this.mTranslateAnimation.cancel();
        if (visibility == 0) {
            this.mMagicListView.setVisibility(0);
            setAnimParam(0.0f, 0.0f, this.mListModeAnimationHeght, 0.0f);
            updateView();
        } else {
            setAnimParam(0.0f, 0.0f, 0.0f, this.mListModeAnimationHeght);
            this.mTranslateAnimation.setAnimationListener(new AnimationListener() {
                public void onAnimationEnd(Animation arg0) {
                    AmigoMagicBar.this.mMagicListView.setVisibility(8);
                    AmigoMagicBar.this.updateView();
                }

                public void onAnimationRepeat(Animation arg0) {
                }

                public void onAnimationStart(Animation arg0) {
                }
            });
        }
        startAnimation(this.mTranslateAnimation);
    }

    private void setAnimParam(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        this.mTranslateAnimation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        this.mTranslateAnimation.setDuration(220);
        this.mTranslateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
    }
}
