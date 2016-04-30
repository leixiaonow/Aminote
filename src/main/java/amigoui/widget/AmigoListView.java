package amigoui.widget;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;

import com.amigoui.internal.view.menu.AmigoContextMenuBuilder;
import com.gionee.note.common.Constants;

import java.util.ArrayList;
import java.util.List;

public class AmigoListView extends ListView implements ActivityLifecycleCallbacks, OnScrollListener {
    private final String TAG;
    private Context mContext;
    private OnCreateContextMenuListener mContextMenuListener;
    private Drawable mDivider;
    private boolean mFastScrollAlwaysVisible;
    private boolean mFastScrollEnabled;
    private int mFastScrollStyle;
    private Fragment mFragment;
    private int mLastScrollState;
    private int mMaximumVelocity;
    private AmigoContextMenuBuilder mMenuBuilder;
    private boolean mModifiedDivider;
    private int mMotionPosition;
    private int mMotionY;
    private OnScrollListener mOnscrListener;
    private final Thread mOwnerThread;
    private Drawable mScrollBarDrawable;
    private AmigoStretchAnimationa mStretchAnimationa;
    private boolean mStretchEnable;
    private boolean mVPEffectEnable;
    private VelocityTracker mVelocityTracker;

    public void setModifiedDiveder(boolean modify) {
        this.mModifiedDivider = modify;
    }

    public boolean needModifiedDivider() {
        return this.mModifiedDivider;
    }

    private void modifyDivider() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && child.getBackground() == null) {
                child.setBackground(this.mDivider);
            }
        }
    }

    public AmigoListView(Context context) {
        this(context, null);
    }

    public AmigoListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public AmigoListView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoListView(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle);
        this.mMenuBuilder = null;
        this.mStretchEnable = false;
        this.TAG = "AmigoListView-->";
        this.mModifiedDivider = false;
        this.mLastScrollState = -1;
        this.mScrollBarDrawable = null;
        this.mVPEffectEnable = false;
        this.mContext = context;
        this.mOwnerThread = Thread.currentThread();
        if (this.mStretchEnable) {
            setOverScrollMode(2);
            this.mMaximumVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        }
        super.setOnScrollListener(this);
        this.mDivider = this.mContext.getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_bg_decorator_adapter"));
    }

    private boolean isOwnerThread() {
        return this.mOwnerThread == Thread.currentThread();
    }

    public void setOnItemLongClickListener(final OnItemLongClickListener listener) {
        super.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener == null || !listener.onItemLongClick(parent, view, position, id)) {
                    AmigoListView.this.showContextMenuDialog(view, position, id);
                }
                return true;
            }
        });
    }

    private void showContextMenuDialog(View view, int position, long id) {
        ContextMenuInfo menuInfo = createContextMenuInfo(view, position, id);
        this.mMenuBuilder = new AmigoContextMenuBuilder(this.mContext);
        this.mMenuBuilder.setCurrentMenuInfo(menuInfo);
        this.mMenuBuilder.setFragment(this.mFragment);
        if (this.mContextMenuListener != null) {
            this.mContextMenuListener.onCreateContextMenu(this.mMenuBuilder, this, menuInfo);
        }
        if (this.mContext instanceof Activity) {
            ((Activity) this.mContext).getApplication().registerActivityLifecycleCallbacks(this);
        }
        if (this.mMenuBuilder.size() > 0) {
            this.mMenuBuilder.show(this, null);
        }
    }

    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        this.mContextMenuListener = l;
        setOnItemLongClickListener(getOnItemLongClickListener());
    }

    ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
    }

    public void registerFragmentForContextMenu(Fragment fragment) {
        this.mFragment = fragment;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    }

    private void hideContextMenuDialog() {
        if (this.mMenuBuilder != null) {
            this.mMenuBuilder.close();
            this.mMenuBuilder = null;
            if (this.mContext instanceof Activity) {
                ((Activity) this.mContext).getApplication().unregisterActivityLifecycleCallbacks(this);
            }
        }
    }

    public void onActivityCreated(Activity arg0, Bundle arg1) {
    }

    public void onActivityDestroyed(Activity arg0) {
    }

    public void onActivityPaused(Activity arg0) {
        hideContextMenuDialog();
    }

    public void onActivityResumed(Activity arg0) {
    }

    public void onActivitySaveInstanceState(Activity arg0, Bundle arg1) {
    }

    public void onActivityStarted(Activity arg0) {
    }

    public void onActivityStopped(Activity arg0) {
    }

    public void setStretchEnable(boolean enable) {
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.mStretchEnable) {
            return super.onTouchEvent(ev);
        }
        int action = ev.getAction();
        int y = (int) ev.getY();
        this.mMotionPosition = pointToPosition((int) ev.getX(), y);
        this.mMotionY = y;
        initVelocityTrackerIfNotExists();
        this.mVelocityTracker.addMovement(ev);
        switch (action & 255) {
            case 0:
                if (this.mStretchAnimationa != null && this.mStretchAnimationa.isRunning()) {
                    this.mStretchAnimationa.revertViewSize();
                    break;
                }
            case 2:
            case 4:
            case 5:
            case 6:
                break;
            default:
                if (this.mStretchAnimationa != null && this.mStretchAnimationa.isRunning()) {
                    this.mStretchAnimationa.overAnimation(0.0f, false);
                    break;
                }
        }
        return super.onTouchEvent(ev);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, getMaxScrollAmount(), isTouchEvent);
    }

    private List<View> getChildren() {
        List<View> children = new ArrayList<>();
        int i;
        View child;
        if (this.mStretchAnimationa.isGoUp()) {
            for (i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                if (child != null) {
                    children.add(child);
                }
            }
        } else {
            for (i = getChildCount() - 1; i > -1; i--) {
                child = getChildAt(i);
                if (child != null) {
                    children.add(child);
                }
            }
        }
        return children;
    }

    protected void dispatchDraw(Canvas canvas) {
        if (needModifiedDivider()) {
            modifyDivider();
        }
        if (this.mStretchEnable && this.mStretchAnimationa != null && !this.mStretchAnimationa.isGoUp() && (this.mStretchAnimationa.isRunning() || this.mStretchAnimationa.isLastUpdate())) {
            correctLayout();
        }
        super.dispatchDraw(canvas);
    }

    private void correctLayout() {
        int bottom = getMeasuredHeight() - getPaddingBottom();
        int dividerHeight = getDividerHeight();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            int l = child.getLeft();
            int r = child.getMeasuredWidth() + l;
            int h = child.getMeasuredHeight();
            if (validTop(bottom, h, child.getTop())) {
                child.layout(l, bottom - h, r, bottom);
                bottom = (bottom - h) - dividerHeight;
            }
        }
    }

    private void printChildInfo() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null) {
                Log.d("AmigoListView-->", "child " + i + " : " + child);
                Log.d("AmigoListView-->", "height " + i + " : " + child.getHeight());
                Log.d("AmigoListView-->", Constants.STR_NEW_LINE);
            }
        }
    }

    private boolean isOverBottom() {
        boolean isBottom = true;
        View bottom = getChildAt(getChildCount() - 1);
        if (bottom != null) {
            if (bottom.getBottom() < getMeasuredHeight() - getPaddingBottom()) {
                isBottom = true;
            } else {
                isBottom = false;
            }
        }
        if (getFirstVisiblePosition() == 0 && getLastVisiblePosition() == getCount() - 1 && isBottom) {
            return true;
        }
        return false;
    }

    private boolean isFlingState() {
        if (this.mLastScrollState == 2) {
            return true;
        }
        return false;
    }

    private boolean validTop(int bottom, int h, int top) {
        return Math.abs(top - (bottom - h)) < h * 2;
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.mOnscrListener = l;
        super.setOnScrollListener(this);
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.mOnscrListener != null) {
            this.mOnscrListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (!(scrollState == this.mLastScrollState || this.mOnscrListener == null)) {
            this.mLastScrollState = scrollState;
            this.mOnscrListener.onScrollStateChanged(view, scrollState);
        }
        if (this.mStretchEnable) {
            scrollStateChanged(view, scrollState);
        }
    }

    private void scrollStateChanged(AbsListView view, int scrollState) {
        if (this.mLastScrollState == 2 && scrollState == 0 && !isOverBottom()) {
            if (this.mStretchAnimationa == null) {
                this.mStretchAnimationa = new AmigoStretchAnimationa();
            }
            if (getFirstVisiblePosition() == 0 || getLastVisiblePosition() == getCount() - 1) {
                float incrase = computeIncrease();
                if (!(incrase == 0.0f || this.mStretchAnimationa.isRunning())) {
                    this.mStretchAnimationa.addChildren(getChildren());
                    this.mStretchAnimationa.overAnimation(incrase, true);
                }
            }
        }
        this.mLastScrollState = scrollState;
    }

    private float computeIncrease() {
        if (this.mVelocityTracker == null) {
            return 0.0f;
        }
        VelocityTracker velocityTracker = this.mVelocityTracker;
        velocityTracker.computeCurrentVelocity(1, (float) this.mMaximumVelocity);
        int initialVelocity = (int) velocityTracker.getYVelocity(0);
        Log.d("AmigoListView-->", "initialVelocity--> " + initialVelocity);
        if (initialVelocity < 0) {
            this.mStretchAnimationa.setGoUp(false);
        } else {
            this.mStretchAnimationa.setGoUp(true);
        }
        if (Math.abs(initialVelocity) > 5 && Math.abs(initialVelocity) < 10) {
            return 1.1f;
        }
        if (Math.abs(initialVelocity) >= 10 && Math.abs(initialVelocity) < 15) {
            return 1.2f;
        }
        if (Math.abs(initialVelocity) >= 15) {
            return 1.3f;
        }
        return 0.0f;
    }

    private void revertChildrenTrans() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && child.getTranslationX() < 0.0f) {
                child.setTranslationX(0.0f);
            }
        }
    }

    public void setViewPagerEffectEnable(boolean enable) {
        this.mVPEffectEnable = enable;
    }

    public boolean getViewPagerEffectEnable() {
        return this.mVPEffectEnable;
    }

    protected void layoutChildren() {
        if (getViewPagerEffectEnable()) {
            revertChildrenTrans();
        }
        super.layoutChildren();
    }

    protected void invokeScrollStatedChanged(AbsListView view, int scrollState) {
        if (this.mStretchEnable) {
            scrollStateChanged(view, scrollState);
        }
    }
}
