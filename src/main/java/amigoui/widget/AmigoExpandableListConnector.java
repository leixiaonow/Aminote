package amigoui.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.HeterogeneousExpandableList;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import amigoui.preference.AmigoPreference;
import uk.co.senab.photoview.IPhotoView;

class AmigoExpandableListConnector extends BaseAdapter implements Filterable {
    static final int SYNC_MAX_DURATION_MILLIS = 100;
    private final int FRAME_LIST_BACKGROUND_BOTTOM = 4;
    private final int FRAME_LIST_BACKGROUND_FULL = 1;
    private final int FRAME_LIST_BACKGROUND_MIDDLE = 3;
    private final int FRAME_LIST_BACKGROUND_NULL = 0;
    private final int FRAME_LIST_BACKGROUND_TOP = 2;
    private final int FRAME_LIST_BACKGROUND_TOTAL = 5;
    boolean mAnimatorEnabled = false;
    private int mChildItemHeight = 0;
    private int[] mChildViewBackgroudRes;
    private final DataSetObserver mDataSetObserver = new MyDataSetObserver();
    private ArrayList<GroupMetadata> mExpGroupMetadataList = new ArrayList<>();
    private ExpandableListAdapter mExpandableListAdapter;
    private int mGroupPos = 0;
    public int[] mHeight = new int[]{13, 18, 28, 43, 63, 88, 118};
    private boolean mIsExpandGroup = false;
    private int mItemHeight = 0;
    private int mMaxExpGroupCount = AmigoPreference.DEFAULT_ORDER;
    private int mTotalExpChildrenCount;

    public static class GroupMetadata implements Parcelable, Comparable<GroupMetadata> {
        public static final Creator<GroupMetadata> CREATOR = new Creator<GroupMetadata>() {
            public GroupMetadata createFromParcel(Parcel in) {
                return GroupMetadata.obtain(in.readInt(), in.readInt(), in.readInt(), in.readLong());
            }

            public GroupMetadata[] newArray(int size) {
                return new GroupMetadata[size];
            }
        };
        static final int REFRESH = -1;
        int flPos;
        long gId;
        int gPos;
        int lastChildFlPos;

        private GroupMetadata() {
        }

        static GroupMetadata obtain(int flPos, int lastChildFlPos, int gPos, long gId) {
            GroupMetadata gm = new GroupMetadata();
            gm.flPos = flPos;
            gm.lastChildFlPos = lastChildFlPos;
            gm.gPos = gPos;
            gm.gId = gId;
            return gm;
        }

        public int compareTo(GroupMetadata another) {
            if (another != null) {
                return this.gPos - another.gPos;
            }
            throw new IllegalArgumentException();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.flPos);
            dest.writeInt(this.lastChildFlPos);
            dest.writeInt(this.gPos);
            dest.writeLong(this.gId);
        }
    }

    protected class MyDataSetObserver extends DataSetObserver {
        protected MyDataSetObserver() {
        }

        public void onChanged() {
            AmigoExpandableListConnector.this.refreshExpGroupMetadataList(true, true);
            AmigoExpandableListConnector.this.notifyDataSetChanged();
        }

        public void onInvalidated() {
            AmigoExpandableListConnector.this.refreshExpGroupMetadataList(true, true);
            AmigoExpandableListConnector.this.notifyDataSetInvalidated();
        }
    }

    public static class PositionMetadata {
        private static final int MAX_POOL_SIZE = 5;
        private static ArrayList<PositionMetadata> sPool = new ArrayList<>(5);
        public int groupInsertIndex;
        public GroupMetadata groupMetadata;
        public AmigoExpandableListPosition position;

        private void resetState() {
            if (this.position != null) {
                this.position.recycle();
                this.position = null;
            }
            this.groupMetadata = null;
            this.groupInsertIndex = 0;
        }

        private PositionMetadata() {
        }

        static PositionMetadata obtain(int flatListPos, int type, int groupPos, int childPos, GroupMetadata groupMetadata, int groupInsertIndex) {
            PositionMetadata pm = getRecycledOrCreate();
            pm.position = AmigoExpandableListPosition.obtain(type, groupPos, childPos, flatListPos);
            pm.groupMetadata = groupMetadata;
            pm.groupInsertIndex = groupInsertIndex;
            return pm;
        }

        private static PositionMetadata getRecycledOrCreate() {
            PositionMetadata pm;
            synchronized (sPool) {
                if (sPool.size() > 0) {
                    pm = sPool.remove(0);
                    pm.resetState();
                } else {
                    pm = new PositionMetadata();
                }
            }
            return pm;
        }

        public void recycle() {
            resetState();
            synchronized (sPool) {
                if (sPool.size() < 5) {
                    sPool.add(this);
                }
            }
        }

        public boolean isExpanded() {
            return this.groupMetadata != null;
        }
    }

    public AmigoExpandableListConnector(ExpandableListAdapter expandableListAdapter) {
        setExpandableListAdapter(expandableListAdapter);
    }

    public void setExpandableListAdapter(ExpandableListAdapter expandableListAdapter) {
        if (this.mExpandableListAdapter != null) {
            this.mExpandableListAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        this.mExpandableListAdapter = expandableListAdapter;
        expandableListAdapter.registerDataSetObserver(this.mDataSetObserver);
    }

    PositionMetadata getUnflattenedPos(int flPos) {
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        if (numExpGroups == 0) {
            int midExpGroupIndex = 0;
            return PositionMetadata.obtain(flPos, 2, flPos, -1, null, 0);
        }
        int insertPosition;
        int groupPos;
//        midExpGroupIndex = 0;
        int midExpGroupIndex = 0;
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = (GroupMetadata) egml.get(midExpGroupIndex);
            if (flPos > midExpGm.lastChildFlPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (flPos < midExpGm.flPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (flPos == midExpGm.flPos) {
                return PositionMetadata.obtain(flPos, 2, midExpGm.gPos, -1, midExpGm, midExpGroupIndex);
            } else if (flPos <= midExpGm.lastChildFlPos) {
                return PositionMetadata.obtain(flPos, 1, midExpGm.gPos, flPos - (midExpGm.flPos + 1), midExpGm, midExpGroupIndex);
            }
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = (GroupMetadata) egml.get(leftExpGroupIndex - 1);
            insertPosition = leftExpGroupIndex;
            groupPos = (flPos - leftExpGm.lastChildFlPos) + leftExpGm.gPos;
        } else if (rightExpGroupIndex < midExpGroupIndex) {
            rightExpGroupIndex++;
            GroupMetadata rightExpGm = (GroupMetadata) egml.get(rightExpGroupIndex);
            insertPosition = rightExpGroupIndex;
            groupPos = rightExpGm.gPos - (rightExpGm.flPos - flPos);
        } else {
            throw new RuntimeException("Unknown state");
        }
        return PositionMetadata.obtain(flPos, 2, groupPos, -1, null, insertPosition);
    }

    PositionMetadata getFlattenedPos(AmigoExpandableListPosition pos) {
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int numExpGroups = egml.size();
        int leftExpGroupIndex = 0;
        int rightExpGroupIndex = numExpGroups - 1;
        if (numExpGroups == 0) {
            int midExpGroupIndex = 0;
            return PositionMetadata.obtain(pos.groupPos, pos.type, pos.groupPos, pos.childPos, null, 0);
        }
        int midExpGroupIndex = 0;
        while (leftExpGroupIndex <= rightExpGroupIndex) {
            midExpGroupIndex = ((rightExpGroupIndex - leftExpGroupIndex) / 2) + leftExpGroupIndex;
            GroupMetadata midExpGm = (GroupMetadata) egml.get(midExpGroupIndex);
            if (pos.groupPos > midExpGm.gPos) {
                leftExpGroupIndex = midExpGroupIndex + 1;
            } else if (pos.groupPos < midExpGm.gPos) {
                rightExpGroupIndex = midExpGroupIndex - 1;
            } else if (pos.groupPos == midExpGm.gPos) {
                if (pos.type == 2) {
                    return PositionMetadata.obtain(midExpGm.flPos, pos.type, pos.groupPos, pos.childPos, midExpGm, midExpGroupIndex);
                }
                if (pos.type == 1) {
                    return PositionMetadata.obtain((midExpGm.flPos + pos.childPos) + 1, pos.type, pos.groupPos, pos.childPos, midExpGm, midExpGroupIndex);
                }
                return null;
            }
        }
        if (pos.type != 2) {
            return null;
        }
        if (leftExpGroupIndex > midExpGroupIndex) {
            GroupMetadata leftExpGm = (GroupMetadata) egml.get(leftExpGroupIndex - 1);
            return PositionMetadata.obtain(leftExpGm.lastChildFlPos + (pos.groupPos - leftExpGm.gPos), pos.type, pos.groupPos, pos.childPos, null, leftExpGroupIndex);
        } else if (rightExpGroupIndex >= midExpGroupIndex) {
            return null;
        } else {
            rightExpGroupIndex++;
            GroupMetadata rightExpGm = (GroupMetadata) egml.get(rightExpGroupIndex);
            return PositionMetadata.obtain(rightExpGm.flPos - (rightExpGm.gPos - pos.groupPos), pos.type, pos.groupPos, pos.childPos, null, rightExpGroupIndex);
        }
    }

    public boolean areAllItemsEnabled() {
        return this.mExpandableListAdapter.areAllItemsEnabled();
    }

    public boolean isEnabled(int flatListPos) {
        boolean retValue;
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        AmigoExpandableListPosition pos = metadata.position;
        if (pos.type == 1) {
            retValue = this.mExpandableListAdapter.isChildSelectable(pos.groupPos, pos.childPos);
        } else {
            retValue = true;
        }
        metadata.recycle();
        return retValue;
    }

    public int getCount() {
        return this.mExpandableListAdapter.getGroupCount() + this.mTotalExpChildrenCount;
    }

    public Object getItem(int flatListPos) {
        Object retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getGroup(posMetadata.position.groupPos);
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getChild(posMetadata.position.groupPos, posMetadata.position.childPos);
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    public long getItemId(int flatListPos) {
        long retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        long groupId = this.mExpandableListAdapter.getGroupId(posMetadata.position.groupPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getCombinedGroupId(groupId);
        } else if (posMetadata.position.type == 1) {
            retValue = this.mExpandableListAdapter.getCombinedChildId(groupId, this.mExpandableListAdapter.getChildId(posMetadata.position.groupPos, posMetadata.position.childPos));
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    public View getView(int flatListPos, View convertView, ViewGroup parent) {
        View retValue;
        PositionMetadata posMetadata = getUnflattenedPos(flatListPos);
        if (posMetadata.position.type == 2) {
            retValue = this.mExpandableListAdapter.getGroupView(posMetadata.position.groupPos, posMetadata.isExpanded(), convertView, parent);
            setViewTextColor(retValue, posMetadata.isExpanded());
            if (this.mAnimatorEnabled) {
                int childcount = this.mExpandableListAdapter.getChildrenCount(posMetadata.position.groupPos);
                if (posMetadata.isExpanded() && childcount > 0) {
                    retValue.setBackgroundResource(AmigoWidgetResource.getIdentifierByDrawable(retValue.getContext(), "amigo_list_selector_light"));
                } else if (childcount == 0) {
                    retValue.setBackgroundResource(AmigoWidgetResource.getIdentifierByDrawable(retValue.getContext(), "amigo_expandlist_group_normal_bg"));
                } else {
                    retValue.setBackgroundResource(AmigoWidgetResource.getIdentifierByDrawable(retValue.getContext(), "amigo_expandlist_group_bg_light"));
                }
            }
        } else if (posMetadata.position.type == 1) {
            boolean isLastChild;
            boolean isFirstChild;
            if (posMetadata.groupMetadata.lastChildFlPos == flatListPos) {
                isLastChild = true;
            } else {
                isLastChild = false;
            }
            if (posMetadata.groupMetadata.flPos + 1 == flatListPos) {
                isFirstChild = true;
            } else {
                isFirstChild = false;
            }
            retValue = this.mExpandableListAdapter.getChildView(posMetadata.position.groupPos, posMetadata.position.childPos, isLastChild, convertView, parent);
            retValue.setMinimumHeight(this.mChildItemHeight);
            if (this.mAnimatorEnabled) {
                retValue.setMinimumHeight(this.mChildItemHeight);
                setChildViewBackground(retValue, isLastChild, isFirstChild);
                if (this.mIsExpandGroup && this.mGroupPos == posMetadata.position.groupPos) {
                    createExpandAnimaForView(retValue, posMetadata.position.childPos).start();
                }
            }
        } else {
            throw new RuntimeException("Flat list position is of unknown type");
        }
        posMetadata.recycle();
        return retValue;
    }

    private void setViewTextColor(View view, boolean isExpanded) {
        if (view instanceof TextView) {
            setTextColor((TextView) view, isExpanded);
        }
        if (view instanceof ViewGroup) {
            setViewGroupTextColor((ViewGroup) view, isExpanded);
        }
    }

    private void setViewGroupTextColor(ViewGroup viewGroup, boolean isExpanded) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            setViewTextColor(viewGroup.getChildAt(i), isExpanded);
        }
    }

    private void setTextColor(TextView tv, boolean isExpanded) {
    }

//    private boolean isModified(int[][] initStates) {
//        boolean isModified = false;
//        for (int[] arr$ : initStates) {
//            for (int state : r0[r3]) {
//                if (state == -1) {
//                    isModified = true;
//                    break;
//                }
//            }
//        }
//        return isModified;
//    }

    public int getItemViewType(int flatListPos) {
        int retValue;
        PositionMetadata metadata = getUnflattenedPos(flatListPos);
        AmigoExpandableListPosition pos = metadata.position;
        if (this.mExpandableListAdapter instanceof HeterogeneousExpandableList) {
            HeterogeneousExpandableList adapter = (HeterogeneousExpandableList) this.mExpandableListAdapter;
            if (pos.type == 2) {
                retValue = adapter.getGroupType(pos.groupPos);
            } else {
                retValue = adapter.getGroupTypeCount() + adapter.getChildType(pos.groupPos, pos.childPos);
            }
        } else if (pos.type == 2) {
            retValue = 0;
        } else {
            retValue = 1;
        }
        metadata.recycle();
        return retValue;
    }

    public int getViewTypeCount() {
        if (!(this.mExpandableListAdapter instanceof HeterogeneousExpandableList)) {
            return 2;
        }
        HeterogeneousExpandableList adapter = (HeterogeneousExpandableList) this.mExpandableListAdapter;
        return adapter.getGroupTypeCount() + adapter.getChildTypeCount();
    }

    public boolean hasStableIds() {
        return this.mExpandableListAdapter.hasStableIds();
    }

    private void refreshExpGroupMetadataList(boolean forceChildrenCountRefresh, boolean syncGroupPositions) {
        int i;
        ArrayList<GroupMetadata> egml = this.mExpGroupMetadataList;
        int egmlSize = egml.size();
        int curFlPos = 0;
        this.mTotalExpChildrenCount = 0;
        if (syncGroupPositions) {
            boolean positionsChanged = false;
            for (i = egmlSize - 1; i >= 0; i--) {
                GroupMetadata curGm;
                curGm = (GroupMetadata) egml.get(i);
                int newGPos = findGroupPosition(curGm.gId, curGm.gPos);
                if (newGPos != curGm.gPos) {
                    if (newGPos == -1) {
                        egml.remove(i);
                        egmlSize--;
                    }
                    curGm.gPos = newGPos;
                    if (!positionsChanged) {
                        positionsChanged = true;
                    }
                }
            }
            if (positionsChanged) {
                Collections.sort(egml);
            }
        }
        int lastGPos = 0;
        for (i = 0; i < egmlSize; i++) {
            int gChildrenCount;
            GroupMetadata curGm = (GroupMetadata) egml.get(i);
            if (curGm.lastChildFlPos == -1 || forceChildrenCountRefresh) {
                gChildrenCount = this.mExpandableListAdapter.getChildrenCount(curGm.gPos);
            } else {
                gChildrenCount = curGm.lastChildFlPos - curGm.flPos;
            }
            this.mTotalExpChildrenCount += gChildrenCount;
            curFlPos += curGm.gPos - lastGPos;
            lastGPos = curGm.gPos;
            curGm.flPos = curFlPos;
            curFlPos += gChildrenCount;
            curGm.lastChildFlPos = curFlPos;
        }
    }

    boolean collapseGroup(int groupPos) {
        AmigoExpandableListPosition elGroupPos = AmigoExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        if (pm == null) {
            return false;
        }
        boolean retValue = collapseGroup(pm);
        pm.recycle();
        return retValue;
    }

    boolean collapseGroup(PositionMetadata posMetadata) {
        if (posMetadata.groupMetadata == null) {
            return false;
        }
        this.mExpGroupMetadataList.remove(posMetadata.groupMetadata);
        refreshExpGroupMetadataList(false, false);
        notifyDataSetChanged();
        this.mExpandableListAdapter.onGroupCollapsed(posMetadata.groupMetadata.gPos);
        return true;
    }

    boolean expandGroup(int groupPos) {
        AmigoExpandableListPosition elGroupPos = AmigoExpandableListPosition.obtain(2, groupPos, -1, -1);
        PositionMetadata pm = getFlattenedPos(elGroupPos);
        elGroupPos.recycle();
        boolean retValue = expandGroup(pm);
        pm.recycle();
        return retValue;
    }

    boolean expandGroup(PositionMetadata posMetadata) {
        if (posMetadata.position.groupPos < 0) {
            throw new RuntimeException("Need group");
        } else if (this.mMaxExpGroupCount == 0 || posMetadata.groupMetadata != null) {
            return false;
        } else {
            if (this.mExpGroupMetadataList.size() >= this.mMaxExpGroupCount) {
                GroupMetadata collapsedGm = (GroupMetadata) this.mExpGroupMetadataList.get(0);
                int collapsedIndex = this.mExpGroupMetadataList.indexOf(collapsedGm);
                collapseGroup(collapsedGm.gPos);
                if (posMetadata.groupInsertIndex > collapsedIndex) {
                    posMetadata.groupInsertIndex--;
                }
            }
            GroupMetadata expandedGm = GroupMetadata.obtain(-1, -1, posMetadata.position.groupPos, this.mExpandableListAdapter.getGroupId(posMetadata.position.groupPos));
            this.mExpGroupMetadataList.add(posMetadata.groupInsertIndex, expandedGm);
            refreshExpGroupMetadataList(false, false);
            notifyDataSetChanged();
            this.mExpandableListAdapter.onGroupExpanded(expandedGm.gPos);
            return true;
        }
    }

    public boolean isGroupExpanded(int groupPosition) {
        for (int i = this.mExpGroupMetadataList.size() - 1; i >= 0; i--) {
            if (((GroupMetadata) this.mExpGroupMetadataList.get(i)).gPos == groupPosition) {
                return true;
            }
        }
        return false;
    }

    public void setMaxExpGroupCount(int maxExpGroupCount) {
        this.mMaxExpGroupCount = maxExpGroupCount;
    }

    ExpandableListAdapter getAdapter() {
        return this.mExpandableListAdapter;
    }

    public Filter getFilter() {
        ExpandableListAdapter adapter = getAdapter();
        if (adapter instanceof Filterable) {
            return ((Filterable) adapter).getFilter();
        }
        return null;
    }

    ArrayList<GroupMetadata> getExpandedGroupMetadataList() {
        return this.mExpGroupMetadataList;
    }

    void setExpandedGroupMetadataList(ArrayList<GroupMetadata> expandedGroupMetadataList) {
        if (expandedGroupMetadataList != null && this.mExpandableListAdapter != null) {
            int numGroups = this.mExpandableListAdapter.getGroupCount();
            int i = expandedGroupMetadataList.size() - 1;
            while (i >= 0) {
                if (((GroupMetadata) expandedGroupMetadataList.get(i)).gPos < numGroups) {
                    i--;
                } else {
                    return;
                }
            }
            this.mExpGroupMetadataList = expandedGroupMetadataList;
            refreshExpGroupMetadataList(true, false);
        }
    }

    public boolean isEmpty() {
        ExpandableListAdapter adapter = getAdapter();
        return adapter != null ? adapter.isEmpty() : true;
    }

    int findGroupPosition(long groupIdToMatch, int seedGroupPosition) {
        int count = this.mExpandableListAdapter.getGroupCount();
        if (count == 0) {
            return -1;
        }
        if (groupIdToMatch == Long.MIN_VALUE) {
            return -1;
        }
        seedGroupPosition = Math.min(count - 1, Math.max(0, seedGroupPosition));
        long endTime = SystemClock.uptimeMillis() + 100;
        int first = seedGroupPosition;
        int last = seedGroupPosition;
        boolean next = false;
        ExpandableListAdapter adapter = getAdapter();
        if (adapter == null) {
            return -1;
        }
        while (SystemClock.uptimeMillis() <= endTime) {
            if (adapter.getGroupId(seedGroupPosition) != groupIdToMatch) {
                boolean hitLast = last == count + -1;
                boolean hitFirst = first == 0;
                if (hitLast && hitFirst) {
                    break;
                } else if (hitFirst || (next && !hitLast)) {
                    last++;
                    seedGroupPosition = last;
                    next = false;
                } else if (hitLast || !(next || hitFirst)) {
                    first--;
                    seedGroupPosition = first;
                    next = true;
                }
            } else {
                return seedGroupPosition;
            }
        }
        return -1;
    }

    public AmigoExpandableListConnector(Context context, ExpandableListAdapter expandableListAdapter) {
        setExpandableListAdapter(expandableListAdapter);
        getFrameListBackground(context);
        this.mChildItemHeight = (int) context.getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_expandablelistview_child_item"));
    }

    private Animator createExpandAnimaForView(final View view, int pos) {
        int height;
        LayoutParams ly = view.getLayoutParams();
//        Log.v("AmigoExpandListConnector","mChildItemHeight=" + this.mChildItemHeight);
        if (ly == null) {
            ly = new AbsListView.LayoutParams(-1, -2);
        }
        final LayoutParams lp = ly;
        this.mItemHeight = lp.height;
//        Log.v("AmigoExpandListConnector", "open  pos = " + pos);
        if (pos < 7) {
            height = this.mChildItemHeight - this.mHeight[pos];
        } else {
            height = 1;
        }
//        Log.v("AmigoExpandListConnector", "heght = " + height);
        ValueAnimator animator = ValueAnimator.ofInt(height, this.mChildItemHeight);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator arg0) {
            }

            public void onAnimationRepeat(Animator arg0) {
            }

            public void onAnimationEnd(Animator arg0) {
//                Log.v("AmigoExpandListConnector", "createExpandAnimaForView onAnimationEnd");
                view.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
                lp.height = AmigoExpandableListConnector.this.mItemHeight;
                view.setLayoutParams(lp);
            }

            public void onAnimationCancel(Animator arg0) {
            }
        });
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = ((Integer) valueAnimator.getAnimatedValue()).intValue();
//                Log.v("AmigoExpandListConnector", "createExpandAnimaForView onAnimationUpdate lp.height = " + lp.height);
                if (lp.height < AmigoExpandableListConnector.this.mChildItemHeight - 8) {
                    view.setAlpha(0.0f);
                } else {
                    view.setAlpha(IPhotoView.DEFAULT_MIN_SCALE);
                }
                view.setLayoutParams(lp);
            }
        });
        return animator;
    }

    void setExpandAnimFlg(boolean anim) {
        this.mIsExpandGroup = anim;
    }

    void setGroupPos(int pos) {
        this.mGroupPos = pos;
    }

    void setConnectorAnimatorEnabled(boolean enable) {
    }

    private void setChildViewBackground(View view, boolean isLastChild, boolean isFirstChild) {
        if (isFirstChild && isLastChild) {
            view.setBackgroundResource(this.mChildViewBackgroudRes[1]);
        } else if (isFirstChild) {
            view.setBackgroundResource(this.mChildViewBackgroudRes[2]);
        } else if (isLastChild) {
            view.setBackgroundResource(this.mChildViewBackgroudRes[4]);
        } else {
            view.setBackgroundResource(this.mChildViewBackgroudRes[3]);
        }
    }

    private void getFrameListBackground(Context context) {
        this.mChildViewBackgroudRes = new int[5];
        this.mChildViewBackgroudRes[0] = 0;
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(context, "amigoframeListBackground"), outValue, true);
        this.mChildViewBackgroudRes[1] = outValue.resourceId;
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(context, "amigoframeListTopBackground"), outValue, true);
        this.mChildViewBackgroudRes[2] = outValue.resourceId;
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(context, "amigoframeListMiddleBackground"), outValue, true);
        this.mChildViewBackgroudRes[3] = outValue.resourceId;
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(context, "amigoframeListBottomBackground"), outValue, true);
        this.mChildViewBackgroudRes[4] = outValue.resourceId;
    }
}
