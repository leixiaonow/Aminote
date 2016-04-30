package amigoui.app;

import amigoui.widget.AmigoExpandableListView;
import amigoui.widget.AmigoExpandableListView.OnChildClickListener;
import amigoui.widget.AmigoExpandableListView.OnGroupCollapseListener;
import amigoui.widget.AmigoExpandableListView.OnGroupExpandListener;
import amigoui.widget.AmigoWidgetResource;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ExpandableListAdapter;

public class AmigoExpandableListActivity extends AmigoActivity implements OnCreateContextMenuListener, OnChildClickListener, OnGroupCollapseListener, OnGroupExpandListener {
    ExpandableListAdapter mAdapter;
    boolean mFinishedStart = false;
    AmigoExpandableListView mList;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    }

    public boolean onChildClick(AmigoExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
    }

    public void onGroupCollapse(int groupPosition) {
    }

    public void onGroupExpand(int groupPosition) {
    }

    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(16908292);
        this.mList = (AmigoExpandableListView) findViewById(16908298);
        if (this.mList == null) {
            throw new RuntimeException("Your content must have a AmigoExpandableListView whose id attribute is 'android.R.id.list'");
        }
        if (emptyView != null) {
            this.mList.setEmptyView(emptyView);
        }
        this.mList.setOnChildClickListener(this);
        this.mList.setOnGroupExpandListener(this);
        this.mList.setOnGroupCollapseListener(this);
        if (this.mFinishedStart) {
            setListAdapter(this.mAdapter);
        }
        this.mFinishedStart = true;
    }

    public void setListAdapter(ExpandableListAdapter adapter) {
        synchronized (this) {
            ensureList();
            this.mAdapter = adapter;
            this.mList.setAdapter(adapter);
        }
    }

    public AmigoExpandableListView getExpandableListView() {
        ensureList();
        return this.mList;
    }

    public ExpandableListAdapter getExpandableListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            setContentView(AmigoWidgetResource.getIdentifierByLayout(this, "amigo_expandable_list_content"));
        }
    }

    public long getSelectedId() {
        return this.mList.getSelectedId();
    }

    public long getSelectedPosition() {
        return this.mList.getSelectedPosition();
    }

    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        return this.mList.setSelectedChild(groupPosition, childPosition, shouldExpandGroup);
    }

    public void setSelectedGroup(int groupPosition) {
        this.mList.setSelectedGroup(groupPosition);
    }
}
