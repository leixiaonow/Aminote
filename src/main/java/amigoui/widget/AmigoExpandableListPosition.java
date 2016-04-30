package amigoui.widget;

import java.util.ArrayList;

class AmigoExpandableListPosition {
    public static final int CHILD = 1;
    public static final int GROUP = 2;
    private static final int MAX_POOL_SIZE = 5;
    private static ArrayList<AmigoExpandableListPosition> sPool = new ArrayList<>(5);
    public int childPos;
    int flatListPos;
    public int groupPos;
    public int type;

    private void resetState() {
        this.groupPos = 0;
        this.childPos = 0;
        this.flatListPos = 0;
        this.type = 0;
    }

    private AmigoExpandableListPosition() {
    }

    long getPackedPosition() {
        if (this.type == 1) {
            return AmigoExpandableListView.getPackedPositionForChild(this.groupPos, this.childPos);
        }
        return AmigoExpandableListView.getPackedPositionForGroup(this.groupPos);
    }

    static AmigoExpandableListPosition obtainGroupPosition(int groupPosition) {
        return obtain(2, groupPosition, 0, 0);
    }

    static AmigoExpandableListPosition obtainChildPosition(int groupPosition, int childPosition) {
        return obtain(1, groupPosition, childPosition, 0);
    }

    static AmigoExpandableListPosition obtainPosition(long packedPosition) {
        if (packedPosition == AmigoExpandableListView.PACKED_POSITION_VALUE_NULL) {
            return null;
        }
        AmigoExpandableListPosition elp = getRecycledOrCreate();
        elp.groupPos = AmigoExpandableListView.getPackedPositionGroup(packedPosition);
        if (AmigoExpandableListView.getPackedPositionType(packedPosition) == 1) {
            elp.type = 1;
            elp.childPos = AmigoExpandableListView.getPackedPositionChild(packedPosition);
            return elp;
        }
        elp.type = 2;
        return elp;
    }

    static AmigoExpandableListPosition obtain(int type, int groupPos, int childPos, int flatListPos) {
        AmigoExpandableListPosition elp = getRecycledOrCreate();
        elp.type = type;
        elp.groupPos = groupPos;
        elp.childPos = childPos;
        elp.flatListPos = flatListPos;
        return elp;
    }

    private static AmigoExpandableListPosition getRecycledOrCreate() {
        AmigoExpandableListPosition elp;
        synchronized (sPool) {
            if (sPool.size() > 0) {
                elp = (AmigoExpandableListPosition) sPool.remove(0);
                elp.resetState();
            } else {
                elp = new AmigoExpandableListPosition();
            }
        }
        return elp;
    }

    public void recycle() {
        synchronized (sPool) {
            if (sPool.size() < 5) {
                sPool.add(this);
            }
        }
    }
}
