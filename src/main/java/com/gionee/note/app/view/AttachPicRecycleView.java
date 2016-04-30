package com.gionee.note.app.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import com.gionee.note.app.attachment.PicSelectorAdapter;

public class AttachPicRecycleView extends RecyclerView {
    public static final int SCROLL_LEFT = 0;
    public static final int SCROLL_RIGHT = 1;
    private PicSelectorAdapter mAdapter;
    private int mScrollDirection;

    public AttachPicRecycleView(Context context) {
        this(context, null);
        initListener();
    }

    public AttachPicRecycleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initListener();
    }

    public AttachPicRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initListener();
    }

    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        this.mAdapter = (PicSelectorAdapter) adapter;
    }

    private void initListener() {
        addOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == 0) {
                    AttachPicRecycleView.this.notifyVisibleRangeChanged();
                    AttachPicRecycleView.this.mAdapter.preLoadPic(AttachPicRecycleView.this.mScrollDirection);
                }
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dx >= 0) {
                    AttachPicRecycleView.this.mScrollDirection = 1;
                } else {
                    AttachPicRecycleView.this.mScrollDirection = 0;
                }
            }
        });
    }

    private void notifyVisibleRangeChanged() {
        if (this.mAdapter != null) {
            LayoutManager layoutManager = getLayoutManager();
            int childCount = layoutManager.getChildCount();
            int visibleStart = 0;
            int visibleEnd = 0;
            if (childCount > 0) {
                visibleStart = layoutManager.getPosition(layoutManager.getChildAt(0));
                visibleEnd = layoutManager.getPosition(layoutManager.getChildAt(childCount - 1)) + 1;
            }
            this.mAdapter.notifyVisibleRangeChanged(visibleStart, visibleEnd);
        }
    }
}
