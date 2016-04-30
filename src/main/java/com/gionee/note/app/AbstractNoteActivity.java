package com.gionee.note.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.gionee.aminote.R;
import com.gionee.note.common.NoteUtils;

public class AbstractNoteActivity extends Activity {
    protected FrameLayout mContentViewGroup;
    protected FrameLayout mFooterViewGroup;
    protected FrameLayout mHeadViewGroup;
    private View mRootView;
    protected FrameLayout mTitleViewGroup;

    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.abstract_note_activity_layout);
        initViewGroups();
        super.onCreate(savedInstanceState);
        if (NoteUtils.checkHasSmartBar()) {
            getWindow().getDecorView().setSystemUiVisibility(2);
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    private void initViewGroups() {
        int statusHeight = getStatusBarHeight(this);
        this.mHeadViewGroup = (FrameLayout) findViewById(R.id.abstract_note_activity_layout_head);
        this.mContentViewGroup = (FrameLayout) findViewById(R.id.abstract_note_activity_layout_content);
        this.mFooterViewGroup = (FrameLayout) findViewById(R.id.abstract_note_activity_layout_footer);
        FrameLayout titleViewGroup = (FrameLayout) findViewById(R.id.abstract_note_activity_layout_title);
        this.mTitleViewGroup = titleViewGroup;
        View root = findViewById(R.id.abstract_note_activity_layout_root);
        this.mRootView = root;
        root.setSystemUiVisibility(root.getSystemUiVisibility() | 1024);
        LayoutParams hflp = (LayoutParams) root.getLayoutParams();
        hflp.topMargin = -statusHeight;
        root.setLayoutParams(hflp);
        LayoutParams tflp = (LayoutParams) titleViewGroup.getLayoutParams();
        tflp.topMargin = statusHeight;
        titleViewGroup.setLayoutParams(tflp);
    }

    public void setNoteContentView(int layoutResID) {
        this.mContentViewGroup.removeAllViews();
        if (layoutResID <= 0) {
            this.mContentViewGroup.setVisibility(8);
            return;
        }
        getLayoutInflater().inflate(layoutResID, this.mContentViewGroup, true);
        this.mContentViewGroup.setVisibility(0);
    }

    public void setNoteTitleView(int layoutResID) {
        this.mTitleViewGroup.removeAllViews();
        if (layoutResID <= 0) {
            this.mTitleViewGroup.setVisibility(8);
            return;
        }
        getLayoutInflater().inflate(layoutResID, this.mTitleViewGroup, true);
        this.mTitleViewGroup.setVisibility(0);
    }

    public void setNoteFooterView(int layoutResID) {
        this.mFooterViewGroup.removeAllViews();
        if (layoutResID <= 0) {
            this.mFooterViewGroup.setVisibility(8);
            return;
        }
        getLayoutInflater().inflate(layoutResID, this.mFooterViewGroup, true);
        this.mFooterViewGroup.setVisibility(0);
    }

    public void setNoteRootViewBackgroundColor(int color) {
        this.mRootView.setBackgroundColor(color);
    }

    public static int getStatusBarHeight(Context context) {
        Resources rs = context.getResources();
        int id = rs.getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) {
            return rs.getDimensionPixelSize(id);
        }
        return 0;
    }

    public View getFooterView() {
        return this.mFooterViewGroup;
    }
}
