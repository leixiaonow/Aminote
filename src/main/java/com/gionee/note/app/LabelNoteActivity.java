package com.gionee.note.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.note.app.LabelManager.LabelDataChangeListener;
import com.gionee.note.app.RecyclerViewAdapter.OnTouchListener;
import com.gionee.note.data.DataManager;
import com.gionee.note.data.LabelNoteSet;
import com.gionee.note.data.LabelSource;
import com.gionee.note.data.Path;

public class LabelNoteActivity extends AbstractNoteActivity implements LoadingListener, OnTouchListener {
    public static final String SELECT_LABEL_ID = "label_id";
    public static final String SELECT_LABEL_NAME = "label_name";
    private RecyclerViewAdapter mAdapter;
    private LabelDataChangeListener mLabelDataChangeListener;
    private LabelManager mLabelManager;
    private String mLabelName;
    private LabelNoteSet mNoteSet;
    private RecyclerView mRecyclerView;
    private TextView mTipTextView;
    private View mTipView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNoteTitleView(R.layout.label_note_activity_title_layout);
        setNoteContentView(R.layout.label_note_activity_content_layout);
        setNoteRootViewBackgroundColor(getResources().getColor(R.color.abstract_note_activity_root_bg_color));
        initData();
        initView();
    }

    protected void onResume() {
        super.onResume();
        this.mAdapter.resume();
    }

    protected void onPause() {
        super.onPause();
        this.mAdapter.pause();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mAdapter.destroy();
        this.mLabelManager.removeLabelDataChangeListener(this.mLabelDataChangeListener);
    }

    private void initData() {
        NoteAppImpl app = (NoteAppImpl) getApplication();
        DataManager dataManager = app.getDataManager();
        int labelId = getIntent().getIntExtra(SELECT_LABEL_ID, -1);
        this.mLabelName = getIntent().getStringExtra(SELECT_LABEL_NAME);
        this.mNoteSet = (LabelNoteSet) dataManager.getMediaSet(LabelSource.LABEL_SET_PATH);
        this.mNoteSet.setLabel(labelId);
        LabelManager labelManager = app.getLabelManager();
        this.mLabelManager = labelManager;
        this.mLabelDataChangeListener = new LabelDataChangeListener() {
            public void onDataChange() {
                LabelNoteActivity.this.mNoteSet.setLabels(LabelNoteActivity.this.mLabelManager.getLabelList());
            }
        };
        labelManager.addLabelDataChangeListener(this.mLabelDataChangeListener);
        this.mNoteSet.setLabels(labelManager.getLabelList());
    }

    private void initView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.label_recycler_view);
        this.mRecyclerView = recyclerView;
        recyclerView.setLayoutManager(getLayoutManager());
        this.mAdapter = new RecyclerViewAdapter(this, this.mNoteSet, this, null);
        this.mAdapter.setOnTouchListener(this);
        recyclerView.setAdapter(this.mAdapter);
        ((TextView) findViewById(R.id.label_note_activity_title_layout_title)).setText(this.mLabelName);
        findViewById(R.id.label_note_activity_title_layout_back).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LabelNoteActivity.this.finish();
            }
        });
        this.mTipView = findViewById(R.id.note_tip_view);
        this.mTipTextView = (TextView) findViewById(R.id.note_tip_text_view);
    }

    private LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(getResources().getInteger(R.integer.home_note_item_column), 1);
    }

    public void onLoadingStarted() {
        if (this.mAdapter.getItemCount() == 0) {
            this.mRecyclerView.setVisibility(4);
            this.mTipView.setVisibility(0);
            this.mTipTextView.setText(R.string.note_tip_search);
            return;
        }
        this.mRecyclerView.setVisibility(0);
        this.mTipView.setVisibility(8);
    }

    public void onLoadingFinished(boolean loadingFailed) {
        if (this.mAdapter.getItemCount() == 0) {
            this.mRecyclerView.setVisibility(4);
            this.mTipView.setVisibility(0);
            this.mTipTextView.setText(R.string.note_tip_searchFinish);
            return;
        }
        this.mRecyclerView.setVisibility(0);
        this.mTipView.setVisibility(8);
    }

    public void onSingleClickTouch(Path path) {
        try {
            Intent intent = new Intent(this, NewNoteActivity.class);
            intent.putExtra(NewNoteActivity.NOTE_ITEM_PATH, path.toString());
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    public void onLongClickTouch(Path path) {
    }
}
