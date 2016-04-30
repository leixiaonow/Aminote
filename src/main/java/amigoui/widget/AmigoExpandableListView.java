package amigoui.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateInterpolator;
import android.widget.ExpandableListAdapter;
import android.widget.ListAdapter;

import com.gionee.aminote.R;

import java.util.ArrayList;
import java.util.List;

import amigoui.changecolors.ChameleonColorManager;
import amigoui.changecolors.ColorConfigConstants;
import amigoui.reflection.AmigoReflectionUtil;
import amigoui.widget.AmigoExpandableListConnector.PositionMetadata;
import uk.co.senab.photoview.IPhotoView;

public class AmigoExpandableListView extends AmigoListView {
    public static final int CHILD_INDICATOR_INHERIT = -1;
    private static final int[] CHILD_LAST_STATE_SET = new int[]{16842918};
    private static final int[] EMPTY_STATE_SET = new int[0];
    private static final int[] GROUP_EMPTY_STATE_SET = new int[]{16842921};
    private static final int[] GROUP_EXPANDED_EMPTY_STATE_SET = new int[]{16842920, 16842921};
    private static final int[] GROUP_EXPANDED_STATE_SET = new int[]{16842920};
    private static final int[][] GROUP_STATE_SETS = new int[][]{EMPTY_STATE_SET, GROUP_EXPANDED_STATE_SET, GROUP_EMPTY_STATE_SET, GROUP_EXPANDED_EMPTY_STATE_SET};
    private static final long PACKED_POSITION_INT_MASK_CHILD = -1;
    private static final long PACKED_POSITION_INT_MASK_GROUP = 2147483647L;
    private static final long PACKED_POSITION_MASK_CHILD = 4294967295L;
    private static final long PACKED_POSITION_MASK_GROUP = 9223372032559808512L;
    private static final long PACKED_POSITION_MASK_TYPE = Long.MIN_VALUE;
    private static final long PACKED_POSITION_SHIFT_GROUP = 32;
    private static final long PACKED_POSITION_SHIFT_TYPE = 63;
    public static final int PACKED_POSITION_TYPE_CHILD = 1;
    public static final int PACKED_POSITION_TYPE_GROUP = 0;
    public static final int PACKED_POSITION_TYPE_NULL = 2;
    public static final long PACKED_POSITION_VALUE_NULL = 4294967295L;
    private ExpandableListAdapter mAdapter;
    private boolean mAnimatorEnabled;
    private Drawable mChildDivider;
    private Drawable mChildIndicator;
    private int mChildIndicatorLeft;
    private int mChildIndicatorRight;
    private AmigoExpandableListConnector mConnector;
    private Context mContext;
    private Drawable mGroupIndicator;
    public int[] mHeight;
    private int mIndicatorLeft;
    private final Rect mIndicatorRect;
    private int mIndicatorRight;
    private int mItemPadding;
    int mL;
    private OnChildClickListener mOnChildClickListener;
    private OnGroupClickListener mOnGroupClickListener;
    private OnGroupCollapseListener mOnGroupCollapseListener;
    private OnGroupExpandListener mOnGroupExpandListener;
    int mR;

    public static class ExpandableListContextMenuInfo implements ContextMenuInfo {
        public long id;
        public long packedPosition;
        public View targetView;

        public ExpandableListContextMenuInfo(View targetView, long packedPosition, long id) {
            this.targetView = targetView;
            this.packedPosition = packedPosition;
            this.id = id;
        }
    }

    public interface OnChildClickListener {
        boolean onChildClick(AmigoExpandableListView amigoExpandableListView, View view, int i, int i2, long j);
    }

    public interface OnGroupClickListener {
        boolean onGroupClick(AmigoExpandableListView amigoExpandableListView, View view, int i, long j);
    }

    public interface OnGroupCollapseListener {
        void onGroupCollapse(int i);
    }

    public interface OnGroupExpandListener {
        void onGroupExpand(int i);
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
        ArrayList<AmigoExpandableListConnector.GroupMetadata> expandedGroupMetadataList;

        SavedState(Parcelable superState, ArrayList<AmigoExpandableListConnector.GroupMetadata> expandedGroupMetadataList) {
            super(superState);
            this.expandedGroupMetadataList = expandedGroupMetadataList;
        }

        private SavedState(Parcel in) {
            super(in);
            this.expandedGroupMetadataList = new ArrayList<>();
            in.readList(this.expandedGroupMetadataList, AmigoExpandableListConnector.class.getClassLoader());
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeList(this.expandedGroupMetadataList);
        }
    }

    public AmigoExpandableListView(Context context) {
        this(context, null);
    }

    public AmigoExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, AmigoWidgetResource.getIdentifierByAttr(context, "amigoExpandableListViewStyle"));
    }

    public AmigoExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIndicatorRect = new Rect();
        this.mL = 0;
        this.mR = 0;
        this.mItemPadding = 0;
        this.mHeight = new int[]{128, 118, 98, 78, 58, 38, 18};
        this.mAnimatorEnabled = false;
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoExpandableListView, defStyle, 0);
        this.mGroupIndicator = a.getDrawable(R.styleable.AmigoExpandableListView_amigogroupIndicator);
        if (this.mGroupIndicator instanceof StateListDrawable) {
            indicatorSetColorFilter((StateListDrawable) this.mGroupIndicator);
        }
        this.mChildIndicator = a.getDrawable(R.styleable.AmigoExpandableListView_amigochildIndicator);
        this.mIndicatorLeft = a.getDimensionPixelSize(R.styleable.AmigoExpandableListView_amigoindicatorLeft, 0);
        this.mIndicatorRight = a.getDimensionPixelSize(R.styleable.AmigoExpandableListView_amigoindicatorRight, 0);
        if (this.mIndicatorRight == 0 && this.mGroupIndicator != null) {
            this.mIndicatorRight = this.mIndicatorLeft + this.mGroupIndicator.getIntrinsicWidth();
        }
        this.mChildIndicatorLeft = a.getDimensionPixelSize(R.styleable.AmigoExpandableListView_amigochildIndicatorLeft, -1);
        this.mChildIndicatorRight = a.getDimensionPixelSize(R.styleable.AmigoExpandableListView_amigochildIndicatorRight, -1);
        this.mChildDivider = a.getDrawable(R.styleable.AmigoExpandableListView_amigochildDivider);
        setChildDivider(context.getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_transparent")));
        this.mItemPadding = (int) this.mContext.getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_expandablelistview_item_padding"));
        Log.d("AmigoExpandListView", "mItemPadding = " + this.mItemPadding);
        a.recycle();
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawGroupDivider(canvas);
        if (this.mChildIndicator != null || this.mGroupIndicator != null) {
            int saveCount = 0;
            boolean clipToPadding = (this.mGroupFlags & 34) == 34;
            if (clipToPadding) {
                saveCount = canvas.save();
                int scrollX = this.mScrollX;
                int scrollY = this.mScrollY;
                canvas.clipRect(this.mPaddingLeft + scrollX, this.mPaddingTop + scrollY, ((this.mRight + scrollX) - this.mLeft) - this.mPaddingRight, ((this.mBottom + scrollY) - this.mTop) - this.mPaddingBottom);
            }
            int headerViewsCount = getHeaderViewsCount();
            int lastChildFlPos = ((getCount() - getFooterViewsCount()) - headerViewsCount) - 1;
            int myB = this.mBottom;
            int lastItemType = -4;
            Rect indicatorRect = this.mIndicatorRect;
            int childCount = getChildCount();
            int i = 0;
            int childFlPos = getFirstVisiblePosition() - headerViewsCount;
            while (i < childCount) {
                if (childFlPos >= 0) {
                    if (childFlPos > lastChildFlPos) {
                        break;
                    }
                    View item = getChildAt(i);
                    int t = item.getTop();
                    int b = item.getBottom();
                    if (b >= 0 && t <= myB) {
                        PositionMetadata pos = this.mConnector.getUnflattenedPos(childFlPos);
                        if (this.mAnimatorEnabled) {
                            if (pos.isExpanded() && pos.position.type == 2) {
                                this.mL = item.getLeft();
                                this.mR = item.getRight();
                            }
                            if (pos.isExpanded() && pos.position.type == 1) {
                                item.setLeft(this.mL + this.mItemPadding);
                                item.setRight(this.mR - this.mItemPadding);
                            }
                        }
                        if (pos.position.type != lastItemType) {
                            if (pos.position.type == 1) {
                                indicatorRect.left = this.mChildIndicatorLeft == -1 ? this.mIndicatorLeft : this.mChildIndicatorLeft;
                                indicatorRect.right = this.mChildIndicatorRight == -1 ? this.mIndicatorRight : this.mChildIndicatorRight;
                                if (this.mAnimatorEnabled) {
                                    indicatorRect.left += this.mPaddingLeft + this.mItemPadding;
                                    indicatorRect.right += this.mPaddingLeft + this.mItemPadding;
                                }
                            } else {
                                indicatorRect.left = this.mIndicatorLeft;
                                indicatorRect.right = this.mIndicatorRight;
                            }
                            indicatorRect.left += this.mPaddingLeft;
                            indicatorRect.right += this.mPaddingLeft;
                            lastItemType = pos.position.type;
                        }
                        if (indicatorRect.left != indicatorRect.right) {
                            if (isStackFromBottom()) {
                                indicatorRect.top = t;
                                indicatorRect.bottom = b;
                            } else {
                                indicatorRect.top = t;
                                indicatorRect.bottom = b;
                            }
                            Drawable indicator = getIndicator(pos);
                            if (indicator != null) {
                                indicator.setBounds(indicatorRect);
                                indicator.draw(canvas);
                            }
                        }
                        pos.recycle();
                    }
                }
                i++;
                childFlPos++;
            }
            if (clipToPadding) {
                canvas.restoreToCount(saveCount);
            }
        }
    }

    private void drawGroupDivider(Canvas canvas) {
        int headerViewsCount = getHeaderViewsCount();
        int childCount = getChildCount();
        Paint paint = new Paint();
        paint.setColor(ColorConfigConstants.DEFAULT_CONTENT_COLOR_THIRDLY_ON_BACKGROUD_C3);
        paint.setStrokeWidth(IPhotoView.DEFAULT_MIN_SCALE);
        if (ChameleonColorManager.isNeedChangeColor()) {
            paint.setColor(ChameleonColorManager.getContentColorSecondaryOnBackgroud_C2());
        }
        int i = 0;
        int childFlPos = getFirstVisiblePosition() - headerViewsCount;
        while (i < childCount) {
            PositionMetadata pos = this.mConnector.getUnflattenedPos(childFlPos);
            int top = getChildAt(i).getTop();
            if (pos.position.type == 2 && needShowDivider(childFlPos)) {
                canvas.drawLine((float) (this.mGroupIndicator == null ? 0 : this.mGroupIndicator.getIntrinsicWidth() + (this.mIndicatorLeft * 2)), (float) top, (float) getRight(), (float) top, paint);
            }
            i++;
            childFlPos++;
        }
    }

    private boolean needShowDivider(int flatListPos) {
        if (flatListPos == 0) {
            return false;
        }
        PositionMetadata posMetadata = this.mConnector.getUnflattenedPos(flatListPos);
        PositionMetadata prePosMetadata = this.mConnector.getUnflattenedPos(flatListPos - 1);
        if (posMetadata.isExpanded() || prePosMetadata.isExpanded()) {
            return true;
        }
        return false;
    }

    private Drawable getIndicator(PositionMetadata pos) {
        int i = 1;
        int i2 = 0;
        Drawable indicator;
        if (pos.position.type == 2) {
            indicator = this.mGroupIndicator;
            if (indicator != null && indicator.isStateful()) {
                boolean isEmpty = pos.groupMetadata == null || pos.groupMetadata.lastChildFlPos == pos.groupMetadata.flPos;
                if (!pos.isExpanded()) {
                    i = 0;
                }
                if (isEmpty) {
                    i2 = 2;
                }
                indicator.setState(GROUP_STATE_SETS[i | i2]);
            }
            return (this.mAnimatorEnabled && this.mAdapter.getChildrenCount(pos.position.groupPos) == 0) ? this.mContext.getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_expander_open_dark")) : indicator;
        } else {
            indicator = this.mChildIndicator;
            if (indicator == null || !indicator.isStateful()) {
                return indicator;
            }
            indicator.setState(pos.position.flatListPos == pos.groupMetadata.lastChildFlPos ? CHILD_LAST_STATE_SET : EMPTY_STATE_SET);
            return indicator;
        }
    }

    private void indicatorSetColorFilter(StateListDrawable stateListDrawble) {
        for (int index = 0; index < stateListDrawble.getStateCount(); index++) {
            changeColorGroupIndicator(stateListDrawble.getStateDrawable(index), isStateExpanded(stateListDrawble.getStateSet(index)));
        }
    }

    private void changeColorGroupIndicator(Drawable drawable, boolean isExpanded) {
        if (isExpanded) {
            AmigoReflectionUtil.setTint(drawable, -28672);
        }
        if (ChameleonColorManager.isNeedChangeColor()) {
            AmigoReflectionUtil.setTint(drawable, isExpanded ? ChameleonColorManager.getAccentColor_G1() : ChameleonColorManager.getContentColorPrimaryOnBackgroud_C1());
        }
    }

    private boolean isStateExpanded(int[] myDrawableState) {
        for (int i : myDrawableState) {
            if (i == 16842920) {
                return true;
            }
        }
        return false;
    }

    public void setChildDivider(Drawable childDivider) {
        this.mChildDivider = childDivider;
    }

    public void setAdapter(ListAdapter adapter) {
        throw new RuntimeException("For ExpandableListView, use setAdapter(ExpandableListAdapter) instead of setAdapter(ListAdapter)");
    }

    public ListAdapter getAdapter() {
        return super.getAdapter();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }

    public void setAdapter(ExpandableListAdapter adapter) {
        this.mAdapter = adapter;
        if (adapter != null) {
            this.mConnector = new AmigoExpandableListConnector(this.mContext, adapter);
        } else {
            this.mConnector = null;
        }
        super.setAdapter(this.mConnector);
    }

    public ExpandableListAdapter getExpandableListAdapter() {
        return this.mAdapter;
    }

    private boolean isHeaderOrFooterPosition(int position) {
        return position < getHeaderViewsCount() || position >= getCount() - getFooterViewsCount();
    }

    private int getFlatPositionForConnector(int flatListPosition) {
        return flatListPosition - getHeaderViewsCount();
    }

    private int getAbsoluteFlatPosition(int flatListPosition) {
        return getHeaderViewsCount() + flatListPosition;
    }

    public boolean performItemClick(View v, int position, long id) {
        if (isHeaderOrFooterPosition(position)) {
            return super.performItemClick(v, position, id);
        }
        return handleItemClick(v, getFlatPositionForConnector(position), id);
    }

    boolean handleItemClick(View v, int position, long id) {
        PositionMetadata posMetadata = this.mConnector.getUnflattenedPos(position);
        id = getChildOrGroupId(posMetadata.position);
        this.mConnector.setExpandAnimFlg(false);
        if (posMetadata.position.type == 2) {
            if (this.mOnGroupClickListener != null) {
                if (this.mOnGroupClickListener.onGroupClick(this, v, posMetadata.position.groupPos, id)) {
                    posMetadata.recycle();
                    return true;
                }
            }
            if (posMetadata.isExpanded()) {
                Log.v("AmigoExpandListView", "handleItemClick setExpandAnimFlg(false)");
                if (!this.mAnimatorEnabled) {
                    this.mConnector.collapseGroup(posMetadata);
                    playSoundEffect(0);
                    if (this.mOnGroupCollapseListener != null) {
                        this.mOnGroupCollapseListener.onGroupCollapse(posMetadata.position.groupPos);
                    }
                    posMetadata.recycle();
                } else if (this.mAdapter.getChildrenCount(posMetadata.position.groupPos) == 0) {
                    return false;
                } else {
                    startCollapseGroupAnimation(posMetadata);
                }
            } else {
                Log.v("AmigoExpandListView", "handleItemClick setExpandAnimFlg(true)");
                this.mConnector.setExpandAnimFlg(true);
                this.mConnector.expandGroup(posMetadata);
                playSoundEffect(0);
                if (this.mOnGroupExpandListener != null) {
                    this.mOnGroupExpandListener.onGroupExpand(posMetadata.position.groupPos);
                }
                int groupPos = posMetadata.position.groupPos;
                int groupFlatPos = posMetadata.position.flatListPos;
                if (this.mAnimatorEnabled) {
                    this.mConnector.setGroupPos(groupPos);
                }
                int shiftedGroupPosition = groupFlatPos + getHeaderViewsCount();
                smoothScrollToPosition(this.mAdapter.getChildrenCount(groupPos) + shiftedGroupPosition, shiftedGroupPosition);
                posMetadata.recycle();
            }
            return true;
        } else if (this.mOnChildClickListener != null) {
            playSoundEffect(0);
            return this.mOnChildClickListener.onChildClick(this, v, posMetadata.position.groupPos, posMetadata.position.childPos, id);
        } else {
            posMetadata.recycle();
            return false;
        }
    }

    public boolean expandGroup(int groupPos) {
        return expandGroup(groupPos, false);
    }

    public boolean expandGroup(int groupPos, boolean animate) {
        AmigoExpandableListPosition elGroupPos = AmigoExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = this.mConnector.getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        boolean retValue = this.mConnector.expandGroup(pm);
        if (this.mOnGroupExpandListener != null) {
            this.mOnGroupExpandListener.onGroupExpand(groupPos);
        }
        if (animate) {
            int shiftedGroupPosition = pm.position.flatListPos + getHeaderViewsCount();
            smoothScrollToPosition(this.mAdapter.getChildrenCount(groupPos) + shiftedGroupPosition, shiftedGroupPosition);
        }
        pm.recycle();
        return retValue;
    }

    public boolean collapseGroup(int groupPos) {
        boolean retValue = this.mConnector.collapseGroup(groupPos);
        if (this.mOnGroupCollapseListener != null) {
            this.mOnGroupCollapseListener.onGroupCollapse(groupPos);
        }
        return retValue;
    }

    public void setOnGroupCollapseListener(OnGroupCollapseListener onGroupCollapseListener) {
        this.mOnGroupCollapseListener = onGroupCollapseListener;
    }

    public void setOnGroupExpandListener(OnGroupExpandListener onGroupExpandListener) {
        this.mOnGroupExpandListener = onGroupExpandListener;
    }

    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
        this.mOnGroupClickListener = onGroupClickListener;
    }

    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        this.mOnChildClickListener = onChildClickListener;
    }

    public long getExpandableListPosition(int flatListPosition) {
        if (isHeaderOrFooterPosition(flatListPosition)) {
            return 4294967295L;
        }
        PositionMetadata pm = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
        long packedPos = pm.position.getPackedPosition();
        pm.recycle();
        return packedPos;
    }

    public int getFlatListPosition(long packedPosition) {
        AmigoExpandableListPosition elPackedPos = AmigoExpandableListPosition.obtainPosition(packedPosition);
        PositionMetadata pm = this.mConnector.getFlattenedPos(elPackedPos);
        elPackedPos.recycle();
        int flatListPosition = pm.position.flatListPos;
        pm.recycle();
        return getAbsoluteFlatPosition(flatListPosition);
    }

    public long getSelectedPosition() {
        return getExpandableListPosition(getSelectedItemPosition());
    }

    public long getSelectedId() {
        long packedPos = getSelectedPosition();
        if (packedPos == 4294967295L) {
            return -1;
        }
        int groupPos = getPackedPositionGroup(packedPos);
        if (getPackedPositionType(packedPos) == 0) {
            return this.mAdapter.getGroupId(groupPos);
        }
        return this.mAdapter.getChildId(groupPos, getPackedPositionChild(packedPos));
    }

    public void setSelectedGroup(int groupPosition) {
        AmigoExpandableListPosition elGroupPos = AmigoExpandableListPosition.obtainGroupPosition(groupPosition);
        PositionMetadata pm = this.mConnector.getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        super.setSelection(getAbsoluteFlatPosition(pm.position.flatListPos));
        pm.recycle();
    }

    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        AmigoExpandableListPosition elChildPos = AmigoExpandableListPosition.obtainChildPosition(groupPosition, childPosition);
        PositionMetadata flatChildPos = this.mConnector.getFlattenedPos(elChildPos);
        if (flatChildPos == null) {
            if (!shouldExpandGroup) {
                return false;
            }
            expandGroup(groupPosition);
            flatChildPos = this.mConnector.getFlattenedPos(elChildPos);
            if (flatChildPos == null) {
                throw new IllegalStateException("Could not find child");
            }
        }
        super.setSelection(getAbsoluteFlatPosition(flatChildPos.position.flatListPos));
        elChildPos.recycle();
        flatChildPos.recycle();
        return true;
    }

    public boolean isGroupExpanded(int groupPosition) {
        return this.mConnector.isGroupExpanded(groupPosition);
    }

    public static int getPackedPositionType(long packedPosition) {
        if (packedPosition == 4294967295L) {
            return 2;
        }
        return (packedPosition & PACKED_POSITION_MASK_TYPE) == PACKED_POSITION_MASK_TYPE ? 1 : 0;
    }

    public static int getPackedPositionGroup(long packedPosition) {
        if (packedPosition == 4294967295L) {
            return -1;
        }
        return (int) ((PACKED_POSITION_MASK_GROUP & packedPosition) >> 32);
    }

    public static int getPackedPositionChild(long packedPosition) {
        if (packedPosition != 4294967295L && (packedPosition & PACKED_POSITION_MASK_TYPE) == PACKED_POSITION_MASK_TYPE) {
            return (int) (packedPosition & 4294967295L);
        }
        return -1;
    }

    public static long getPackedPositionForChild(int groupPosition, int childPosition) {
        return (PACKED_POSITION_MASK_TYPE | ((((long) groupPosition) & PACKED_POSITION_INT_MASK_GROUP) << 32)) | (((long) childPosition) & -1);
    }

    public static long getPackedPositionForGroup(int groupPosition) {
        return (((long) groupPosition) & PACKED_POSITION_INT_MASK_GROUP) << 32;
    }

    ContextMenuInfo createContextMenuInfo(View view, int flatListPosition, long id) {
        if (isHeaderOrFooterPosition(flatListPosition)) {
            return new AdapterContextMenuInfo(view, flatListPosition, id);
        }
        PositionMetadata pm = this.mConnector.getUnflattenedPos(getFlatPositionForConnector(flatListPosition));
        AmigoExpandableListPosition pos = pm.position;
        id = getChildOrGroupId(pos);
        long packedPosition = pos.getPackedPosition();
        pm.recycle();
        return new ExpandableListContextMenuInfo(view, packedPosition, id);
    }

    private long getChildOrGroupId(AmigoExpandableListPosition position) {
        if (position.type == 1) {
            return this.mAdapter.getChildId(position.groupPos, position.childPos);
        }
        return this.mAdapter.getGroupId(position.groupPos);
    }

    public void setChildIndicator(Drawable childIndicator) {
        this.mChildIndicator = childIndicator;
    }

    public void setChildIndicatorBounds(int left, int right) {
        this.mChildIndicatorLeft = left;
        this.mChildIndicatorRight = right;
    }

    public void setGroupIndicator(Drawable groupIndicator) {
        this.mGroupIndicator = groupIndicator;
        if (this.mIndicatorRight == 0 && this.mGroupIndicator != null) {
            this.mIndicatorRight = this.mIndicatorLeft + this.mGroupIndicator.getIntrinsicWidth();
        }
    }

    public void setIndicatorBounds(int left, int right) {
        this.mIndicatorLeft = left;
        this.mIndicatorRight = right;
    }

    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this.mConnector != null ? this.mConnector.getExpandedGroupMetadataList() : null);
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            if (this.mConnector != null && ss.expandedGroupMetadataList != null) {
                this.mConnector.setExpandedGroupMetadataList(ss.expandedGroupMetadataList);
                return;
            }
            return;
        }
        super.onRestoreInstanceState(state);
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AmigoExpandableListView.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AmigoExpandableListView.class.getName());
    }

    private void startCollapseGroupAnimation(PositionMetadata posMetadata) {
        Log.v("AmigoExpandListView", "startCollapseGroupAnimation");
        collapseAnimation(posMetadata);
    }

    public void collapseAnimation(PositionMetadata posMetadata) {
        List<View> views = new ArrayList();
        Log.v("AmigoExpandListView", "getChildCount() = " + getChildCount());
        int childCount = getChildCount();
        int headerViewsCount = getHeaderViewsCount();
        int lastChildFlPos = ((getCount() - getFooterViewsCount()) - headerViewsCount) - 1;
        int i = 0;
        int childFlPos = getFirstVisiblePosition() - headerViewsCount;
        while (i < childCount) {
            if (childFlPos >= 0) {
                if (childFlPos > lastChildFlPos) {
                    break;
                }
                PositionMetadata pos = this.mConnector.getUnflattenedPos(childFlPos);
                if (pos.isExpanded() && pos.position.groupPos == posMetadata.position.groupPos && pos.position.type == 1) {
                    View vi = getChildAt(i);
                    Log.v("AmigoExpandListView", "vi = " + vi);
                    views.add(vi);
                }
            }
            i++;
            childFlPos++;
        }
        if (views.isEmpty()) {
            startCollapseGroup(posMetadata);
            return;
        }
        List<Animator> animators = new ArrayList();
        Log.v("AmigoExpandListView", "views.size() = " + views.size());
        for (i = 0; i < views.size(); i++) {
            View view = (View) views.get(i);
            if (view != null) {
                animators.add(createcollapseAnimatorForView(view, i));
            }
        }
        AnimatorSet animatorSet = new AnimatorSet();
        Animator[] animatorsArray = new Animator[animators.size()];
        for (i = 0; i < animatorsArray.length; i++) {
            animatorsArray[i] = (Animator) animators.get(i);
        }
        animatorSet.playTogether(animatorsArray);
        final PositionMetadata positionMetadata = posMetadata;
        animatorSet.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                Log.v("AmigoExpandListView", "animateDismiss onAnimationEnd");
                AmigoExpandableListView.this.startCollapseGroup(positionMetadata);
            }

            public void onAnimationCancel(Animator arg0) {
            }
        });
        animatorSet.start();
    }

    private Animator createcollapseAnimatorForView(final View view, int pos) {
        int height;
        final LayoutParams lp = (LayoutParams) view.getLayoutParams();
        final int originalHeight = view.getHeight();
        if (pos < 7) {
            height = originalHeight - this.mHeight[pos];
        } else {
            height = 0;
        }
        Log.v("AmigoExpandListView", "close  heght = " + height);
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{originalHeight, height});
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
                Log.v("AmigoExpandListView", "createcollapseAnimatorForView onAnimationEnd");
                lp.height = 0;
                view.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
                view.setLayoutParams(lp);
            }

            public void onAnimationCancel(Animator arg0) {
            }
        });
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                Log.v("AmigoExpandListView", "createcollapseAnimatorForView onAnimationUpdate lp.height = " + lp.height);
                if (lp.height < originalHeight - 10) {
                    view.setAlpha(0.0f);
                } else {
                    view.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
                }
                view.setLayoutParams(lp);
            }
        });
        return animator;
    }

    private void startCollapseGroup(PositionMetadata posMetadata) {
        Log.v("AmigoExpandListView", "startCollapseGroup");
        this.mConnector.collapseGroup(posMetadata);
        playSoundEffect(0);
        if (this.mOnGroupCollapseListener != null) {
            this.mOnGroupCollapseListener.onGroupCollapse(posMetadata.position.groupPos);
        }
        posMetadata.recycle();
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (this.mConnector != null) {
            this.mConnector.setExpandAnimFlg(false);
        }
    }

    public void setAnimatorEnabled(boolean enabled) {
        this.mAnimatorEnabled = enabled;
        if (this.mConnector != null) {
            this.mConnector.setConnectorAnimatorEnabled(enabled);
        }
        if (this.mAnimatorEnabled) {
            setSelector(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_transparent"));
            setDivider(null);
        }
        Log.v("AmigoExpandListView", "setAnimatorEnabled mAnimatorEnabled = " + this.mAnimatorEnabled);
    }

    public boolean getAnimatorEnabled() {
        Log.v("AmigoExpandListView", "getAnimatorEnabled mAnimatorEnabled = " + this.mAnimatorEnabled);
        return this.mAnimatorEnabled;
    }
}
