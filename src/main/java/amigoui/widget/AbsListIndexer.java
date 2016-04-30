package amigoui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public abstract class AbsListIndexer extends View {
    public abstract void invalidateShowingLetterIndex();

    public abstract boolean isBusying();

    public abstract void setList(ListView listView, OnScrollListener onScrollListener);

    public AbsListIndexer(Context context) {
        super(context);
    }

    public AbsListIndexer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsListIndexer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected int toRawTextSize(float sp) {
        return (int) TypedValue.applyDimension(2, sp, getResources().getDisplayMetrics());
    }
}
