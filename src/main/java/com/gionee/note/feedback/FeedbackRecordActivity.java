package com.gionee.note.feedback;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.feedback.db.DataChangeObserver;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.note.app.view.StandardActivity;
import com.gionee.note.app.view.StandardActivity.StandardAListener;
import java.util.ArrayList;
import java.util.List;

public class FeedbackRecordActivity extends StandardActivity implements StandardAListener {
    private DataChangeObserver mDataChangeObserver = new DataChangeObserver() {
        public void onDataChange(List<FeedbackInfo> infos) {
            FeedbackRecordActivity.this.updateData(infos);
            FeedbackRecordActivity.this.updateView();
        }
    };
    private DataManager mDataManager;
    private TextView mEmptyView;
    private List<FeedbackInfo> mFeedbackInfos;
    private FeedbackRecordAdapter mFeedbackRecordAdapter;
    private ListView mRecordListView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle((int) R.string.fb_feedback_record_activity_title);
        setStandardAListener(this);
        setNoteContentView(R.layout.fb_feedback_record_ly);
        this.mEmptyView = (TextView) findViewById(R.id.gn_fb_id_recordempty);
        this.mRecordListView = (ListView) findViewById(R.id.gn_fb_id_recordlayout);
        this.mFeedbackInfos = new ArrayList();
        this.mFeedbackRecordAdapter = new FeedbackRecordAdapter(this, this.mFeedbackInfos);
        this.mRecordListView.setAdapter(this.mFeedbackRecordAdapter);
        this.mDataManager = DataManager.getInstance(this);
        this.mDataManager.registerDataObserver(this.mDataChangeObserver);
    }

    protected void onResume() {
        super.onResume();
        this.mDataManager.loopGetRecord();
    }

    protected void onPause() {
        super.onPause();
        this.mDataManager.stopLoopRecord();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mDataManager.unregisteredDataObserver(this.mDataChangeObserver);
    }

    private void updateData(List<FeedbackInfo> feedbackInfos) {
        this.mFeedbackInfos = feedbackInfos;
    }

    private void updateView() {
        if (this.mFeedbackRecordAdapter != null) {
            this.mFeedbackRecordAdapter.update(this.mFeedbackInfos);
            if (this.mFeedbackInfos.size() == 0) {
                this.mEmptyView.setVisibility(0);
            } else {
                this.mEmptyView.setVisibility(8);
            }
        }
    }

    public void onClickHomeBack() {
        finish();
    }

    public void onClickRightView() {
    }
}
