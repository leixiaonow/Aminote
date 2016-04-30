package amigoui.app;

import amigoui.widget.AmigoListView;
import amigoui.widget.AmigoWidgetResource;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;

public class AmigoListActivity extends AmigoActivity {
    protected ListAdapter mAdapter;
    private boolean mFinishedStart = false;
    private Handler mHandler = new Handler();
    protected AmigoListView mList;
    private OnItemClickListener mOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            AmigoListActivity.this.onListItemClick((AmigoListView) parent, v, position, id);
        }
    };
    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            AmigoListActivity.this.mList.focusableViewAvailable(AmigoListActivity.this.mList);
        }
    };

    protected void onListItemClick(AmigoListView l, View v, int position, long id) {
    }

    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    protected void onDestroy() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        super.onDestroy();
    }

    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(16908292);
        this.mList = (AmigoListView) findViewById(16908298);
        if (this.mList == null) {
            throw new RuntimeException("Your content must have a AmigoListView whose id attribute is 'android.R.id.list'");
        }
        if (emptyView != null) {
            this.mList.setEmptyView(emptyView);
        }
        this.mList.setOnItemClickListener(this.mOnClickListener);
        if (this.mFinishedStart) {
            setListAdapter(this.mAdapter);
        }
        this.mHandler.post(this.mRequestFocus);
        this.mFinishedStart = true;
    }

    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureList();
            this.mAdapter = adapter;
            this.mList.setAdapter(adapter);
        }
    }

    public void setSelection(int position) {
        this.mList.setSelection(position);
    }

    public int getSelectedItemPosition() {
        return this.mList.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        return this.mList.getSelectedItemId();
    }

    public AmigoListView getListView() {
        ensureList();
        return this.mList;
    }

    public ListAdapter getListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            setContentView(AmigoWidgetResource.getIdentifierByLayout(this, "amigo_list_content_simple"));
        }
    }
}
