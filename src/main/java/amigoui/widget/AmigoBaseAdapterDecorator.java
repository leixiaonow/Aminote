package amigoui.widget;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public abstract class AmigoBaseAdapterDecorator extends BaseAdapter {
    protected final BaseAdapter mDecoratedBaseAdapter;
    private AbsListView mListView;

    public AmigoBaseAdapterDecorator(BaseAdapter baseAdapter) {
        this.mDecoratedBaseAdapter = baseAdapter;
    }

    @Deprecated
    public void setListView(AbsListView listView) {
        this.mListView = listView;
        if (this.mDecoratedBaseAdapter instanceof AmigoBaseAdapterDecorator) {
            ((AmigoBaseAdapterDecorator) this.mDecoratedBaseAdapter).setListView(listView);
        }
    }

    public void setAbsListView(AbsListView listView) {
        this.mListView = listView;
        if (this.mDecoratedBaseAdapter instanceof AmigoBaseAdapterDecorator) {
            ((AmigoBaseAdapterDecorator) this.mDecoratedBaseAdapter).setAbsListView(listView);
        }
    }

    @Deprecated
    public AbsListView getListView() {
        return this.mListView;
    }

    public AbsListView getAbsListView() {
        return this.mListView;
    }

    public int getCount() {
        return this.mDecoratedBaseAdapter.getCount();
    }

    public Object getItem(int position) {
        return this.mDecoratedBaseAdapter.getItem(position);
    }

    public long getItemId(int position) {
        return this.mDecoratedBaseAdapter.getItemId(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return this.mDecoratedBaseAdapter.getView(position, convertView, parent);
    }

    public boolean areAllItemsEnabled() {
        return this.mDecoratedBaseAdapter.areAllItemsEnabled();
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return this.mDecoratedBaseAdapter.getDropDownView(position, convertView, parent);
    }

    public int getItemViewType(int position) {
        return this.mDecoratedBaseAdapter.getItemViewType(position);
    }

    public int getViewTypeCount() {
        return this.mDecoratedBaseAdapter.getViewTypeCount();
    }

    public boolean hasStableIds() {
        return this.mDecoratedBaseAdapter.hasStableIds();
    }

    public boolean isEmpty() {
        return this.mDecoratedBaseAdapter.isEmpty();
    }

    public boolean isEnabled(int position) {
        return this.mDecoratedBaseAdapter.isEnabled(position);
    }

    public void notifyDataSetChanged() {
        this.mDecoratedBaseAdapter.notifyDataSetChanged();
    }

    public void notifyDataSetInvalidated() {
        this.mDecoratedBaseAdapter.notifyDataSetInvalidated();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.mDecoratedBaseAdapter.registerDataSetObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        this.mDecoratedBaseAdapter.unregisterDataSetObserver(observer);
    }

    public BaseAdapter getDecoratedBaseAdapter() {
        return this.mDecoratedBaseAdapter;
    }
}
