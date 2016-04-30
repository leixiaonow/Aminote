package amigoui.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import uk.co.senab.photoview.IPhotoView;

public abstract class AmigoViewToolbar {
    private static final int TOLERANCE_TOUCH = 3;
    private static final int TOOLBAR_ARROW_OFFSET_TO_EDGE = 15;
    private static final int TOOLBAR_ITEM_PADDING_BOTTOM = 3;
    private static final int TOOLBAR_ITEM_PADDING_LEFT_AND_RIGHT = 12;
    private static final int TOOLBAR_POSITION_OFFSET_TO_SCREEN_LEFT = 14;
    private static final int TOOLBAR_POSITION_OFFSET_TO_SCREEN_RIGHT = 14;
    private final int TOOLBAR_TOP_OFFSET = 5;
    private Drawable mArrowAboveDrawable;
    private Drawable mArrowBelowDrawable;
    private int mCenterDrawableResId;
    protected Context mContext;
    protected View mHostView;
    protected LayoutInflater mLayoutInflater;
    protected LayoutParams mLayoutParams = null;
    private int mLeftDrawableResId;
    private int mOffsetToolbar = 5;
    private int mPositionX;
    private int mPositionY;
    private int mRightDrawableResId;
    private float mScale;
    protected boolean mShowing = false;
    private Drawable mSingleDrawable;
    private int mStatusBarHeight;
    protected int mToleranceTouch;
    protected ViewGroup mToolbarGroup;
    protected int mToolbarItemPaddingBottom;
    protected int mToolbarItemPaddingLeftAndRight;
    private int mToolbarPositionArrowHeight;
    protected ImageView mToolbarPositionArrowView;
    private int mToolbarPositionArrowWidth;
    protected View mToolbarView;
    protected WindowManager mWindowManager;

    protected abstract void updateToolbarItems();

    public AmigoViewToolbar(View hostView) {
        this.mHostView = hostView;
        this.mContext = this.mHostView.getContext();
        this.mScale = this.mContext.getResources().getDisplayMetrics().density;
        Resources resources = this.mHostView.getResources();
        this.mLeftDrawableResId = AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_text_toolbar_left");
        this.mCenterDrawableResId = AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_text_toolbar_center");
        this.mRightDrawableResId = AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_text_toolbar_right");
        this.mSingleDrawable = resources.getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_text_toolbar_single"));
        this.mArrowAboveDrawable = resources.getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_text_toolbar_position_arrow_above"));
        this.mArrowBelowDrawable = resources.getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_text_toolbar_position_arrow_below"));
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE);
        calculateTolerance();
        this.mStatusBarHeight = getStatusBarHeight();
        this.mLayoutInflater = LayoutInflater.from(this.mContext);
        this.mToolbarView = this.mLayoutInflater.inflate(AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_text_toolbar"), null);
        this.mToolbarGroup = (ViewGroup) this.mToolbarView.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_toolbar_group"));
        this.mToolbarPositionArrowView = (ImageView) this.mToolbarView.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_toolbar_position_arrow"));
        this.mToolbarView.measure(0, 0);
        this.mToolbarPositionArrowWidth = this.mToolbarPositionArrowView.getMeasuredWidth();
        this.mToolbarPositionArrowHeight = this.mToolbarPositionArrowView.getMeasuredHeight();
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public void show(int screenX, int screenY, boolean selected) {
        if (!this.mShowing) {
            showInternal(screenX, screenY, 0, selected);
        }
    }

    public void move(int screenX, int screenY, boolean selected) {
        if (this.mShowing) {
            moveInternal(screenX, screenY, 0, selected);
        }
    }

    public void hide() {
        if (this.mShowing) {
            try {
                this.mToolbarPositionArrowView.setPadding(0, 0, 0, 0);
                this.mWindowManager.removeViewImmediate(this.mToolbarView);
            } finally {
                this.mShowing = false;
            }
        }
    }

    protected void showInternal(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        update();
        if (this.mToolbarGroup.getChildCount() < 1) {
            hide();
            return;
        }
        prepare(screenX, screenY, cursorLineHeight, selected);
        LayoutParams lp = new LayoutParams();
        lp.token = this.mHostView.getApplicationWindowToken();
        lp.x = this.mPositionX;
        lp.y = this.mPositionY;
        lp.width = -2;
        lp.height = -2;
        lp.gravity = 51;
        lp.format = -3;
        lp.type = LayoutParams.TYPE_APPLICATION_PANEL;
        if (this.mContext.getResources().getConfiguration().orientation == 1) {
            lp.flags = 131848;
            lp.softInputMode = 16;
        } else {
            lp.flags = 776;
            lp.softInputMode = 0;
        }
        lp.packageName = this.mContext.getPackageName();
        this.mLayoutParams = lp;
        this.mWindowManager.addView(this.mToolbarView, lp);
        this.mShowing = true;
    }

    protected void moveInternal(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        if (this.mToolbarGroup.getChildCount() < 1) {
            hide();
            return;
        }
        prepare(screenX, screenY, cursorLineHeight, selected);
        LayoutParams lp = this.mLayoutParams;
        lp.x = this.mPositionX;
        lp.y = this.mPositionY;
        this.mWindowManager.updateViewLayout(this.mToolbarView, lp);
    }

    private void prepare(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        this.mToolbarView.measure(0, 0);
        boolean aboveCursor = calculatePosition(screenX, screenY, cursorLineHeight, selected);
        int paddingLeft = Math.min(((this.mToolbarGroup.getMeasuredWidth() - this.mToolbarPositionArrowWidth) - 15) - 14, Math.max(29, ((screenX - this.mPositionX) - (this.mToolbarPositionArrowWidth / 2)) - 20));
        if (aboveCursor) {
            this.mToolbarPositionArrowView.setImageDrawable(this.mArrowBelowDrawable);
            this.mToolbarPositionArrowView.setPadding(paddingLeft, this.mToolbarGroup.getMeasuredHeight() - this.mArrowBelowDrawable.getIntrinsicHeight(), 0, 0);
            return;
        }
        this.mToolbarPositionArrowView.setImageDrawable(this.mArrowAboveDrawable);
        this.mToolbarGroup.setPadding(0, this.mOffsetToolbar, 0, 0);
        this.mToolbarPositionArrowView.setPadding(paddingLeft, this.mOffsetToolbar - 2, 0, -2);
    }

    private void update() {
        updateToolbarItems();
        int childCount = this.mToolbarGroup.getChildCount();
        View view;
        if (childCount >= 2) {
            this.mOffsetToolbar = (int) (5.0f * this.mScale);
            for (int i = 0; i < childCount; i++) {
                view = this.mToolbarGroup.getChildAt(i);
                if (i == 0) {
                    view.setBackgroundResource(this.mLeftDrawableResId);
                    view.setPadding((this.mToolbarItemPaddingLeftAndRight * 2) - 1, 0, this.mToolbarItemPaddingLeftAndRight, this.mToolbarItemPaddingBottom);
                } else if (i == childCount - 1) {
                    view.setBackgroundResource(this.mRightDrawableResId);
                    view.setPadding(this.mToolbarItemPaddingLeftAndRight, 0, this.mToolbarItemPaddingLeftAndRight * 2, this.mToolbarItemPaddingBottom);
                } else {
                    view.setBackgroundResource(this.mCenterDrawableResId);
                    view.setPadding(this.mToolbarItemPaddingLeftAndRight, 0, this.mToolbarItemPaddingLeftAndRight, this.mToolbarItemPaddingBottom);
                }
            }
        } else if (childCount == 1) {
            this.mOffsetToolbar = 0;
            view = this.mToolbarGroup.getChildAt(0);
            view.setBackgroundDrawable(this.mSingleDrawable);
            view.setPadding(this.mToolbarItemPaddingLeftAndRight * 2, 0, this.mToolbarItemPaddingLeftAndRight * 2, this.mToolbarItemPaddingBottom);
        }
    }

    private boolean calculatePosition(int screenX, int screenY, int cursorLineHeight, boolean selected) {
        int x;
        int y;
        boolean aboveCursor;
        int px = screenX - this.mHostView.getRootView().getScrollX();
        int half = this.mToolbarGroup.getMeasuredWidth() / 2;
        int displayWidth = this.mWindowManager.getDefaultDisplay().getWidth();
        if (px + half < displayWidth) {
            x = px - half;
        } else {
            x = displayWidth - this.mToolbarGroup.getMeasuredWidth();
        }
        this.mPositionX = Math.max(0, x);
        int py = screenY - this.mHostView.getRootView().getScrollY();
        int th = this.mToolbarGroup.getMeasuredHeight() + this.mToolbarPositionArrowHeight;
        int lh = cursorLineHeight / 2;
        if ((py - th) - lh < this.mStatusBarHeight) {
            y = ((selected ? this.mToleranceTouch : 0) + (py + lh)) + 2;
            aboveCursor = false;
        } else {
            y = (((py - th) - lh) - (selected ? this.mToleranceTouch : 0)) + 6;
            aboveCursor = true;
        }
        this.mPositionY = Math.max(this.mStatusBarHeight, y);
        return aboveCursor;
    }

    private void calculateTolerance() {
        DisplayMetrics dm = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(dm);
        float ratio = (IPhotoView.DEFAULT_MIN_SCALE * ((float) dm.densityDpi)) / 160.0f;
        this.mToleranceTouch = Math.round(IPhotoView.DEFAULT_MAX_SCALE * ratio);
        this.mToolbarItemPaddingLeftAndRight = Math.round(12.0f * ratio);
        this.mToolbarItemPaddingBottom = Math.round(IPhotoView.DEFAULT_MAX_SCALE * ratio);
    }

    private int getStatusBarHeight() {
        Rect rect = new Rect();
        Context context = this.mHostView.getContext();
        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            if (window != null) {
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                return rect.top;
            }
        }
        return 0;
    }
}
