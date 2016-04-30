package com.gionee.note.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.ListView;
import com.gionee.aminote.R;

public class LabelSelectorListView extends ListView {
    private int mMaxHeight;

    public LabelSelectorListView(Context context) {
        this(context, null);
    }

    public LabelSelectorListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public LabelSelectorListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mMaxHeight = context.getResources().getDimensionPixelSize(R.dimen.label_selector_dialog_list_max_height);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mMaxHeight, Integer.MIN_VALUE));
    }
}
