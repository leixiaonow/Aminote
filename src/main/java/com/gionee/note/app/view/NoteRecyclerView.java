package com.gionee.note.app.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import com.gionee.note.app.RecyclerViewAdapter;

public class NoteRecyclerView extends RecyclerView {
    private RecyclerViewAdapter mAdapter;

    public NoteRecyclerView(Context context) {
        this(context, null);
    }

    public NoteRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initListener();
    }

    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        this.mAdapter = (RecyclerViewAdapter) adapter;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        notifyVisibleRangeChanged();
    }

    private void initListener() {
        addOnScrollListener(new OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                NoteRecyclerView.this.notifyVisibleRangeChanged();
            }
        });
    }

    private void notifyVisibleRangeChanged() {
        RecyclerViewAdapter adapter = this.mAdapter;
        if (adapter != null) {
            LayoutManager layoutManager = getLayoutManager();
            int childCount = layoutManager.getChildCount();
            int visibleStart = 0;
            int visibleEnd = 0;
            if (childCount > 0) {
                visibleStart = layoutManager.getPosition(layoutManager.getChildAt(0));
                visibleEnd = layoutManager.getPosition(layoutManager.getChildAt(childCount - 1)) + 1;
            }
            adapter.notifyVisibleRangeChanged(visibleStart, visibleEnd);
        }
    }
}
