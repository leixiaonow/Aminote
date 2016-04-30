package com.gionee.note.photoview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class HackyViewPager extends ViewPager {
    private boolean mIsLocked = false;

    public HackyViewPager(Context context) {
        super(context);
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        if (!this.mIsLocked) {
            try {
                z = super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return !this.mIsLocked && super.onTouchEvent(event);
    }

    public void toggleLock() {
        this.mIsLocked = !this.mIsLocked;
    }

    public void setLocked(boolean isLocked) {
        this.mIsLocked = isLocked;
    }

    public boolean isLocked() {
        return this.mIsLocked;
    }
}
