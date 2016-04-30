package com.gionee.note.app;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;
import com.gionee.aminote.R;

public class NoteItemDecoration extends ItemDecoration {
    private int mColumnGap;

    public NoteItemDecoration(Context context) {
        this.mColumnGap = context.getResources().getDimensionPixelSize(R.dimen.home_note_item_gap);
    }

    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        outRect.set(this.mColumnGap, this.mColumnGap, this.mColumnGap, this.mColumnGap);
    }
}
