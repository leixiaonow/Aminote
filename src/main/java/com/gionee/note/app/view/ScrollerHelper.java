package com.gionee.note.app.view;

import android.content.Context;
import android.widget.OverScroller;

public class ScrollerHelper {
    private OverScroller mScroller;

    public ScrollerHelper(Context context) {
        this.mScroller = new OverScroller(context);
    }

    public boolean advanceAnimation() {
        return this.mScroller.computeScrollOffset();
    }

    public boolean isFinished() {
        return this.mScroller.isFinished();
    }

    public void forceFinished() {
        this.mScroller.forceFinished(true);
    }

    public int getPosition() {
        return this.mScroller.getCurrX();
    }

    public void fling(int currX, int velocity, int min, int max, int overflingDistance) {
        this.mScroller.fling(currX, 0, velocity, 0, min, max, 0, 0, overflingDistance, 0);
    }

    public void springBack(int curPosition, int min, int max) {
        this.mScroller.springBack(curPosition, 0, min, max, 0, 0);
    }
}
