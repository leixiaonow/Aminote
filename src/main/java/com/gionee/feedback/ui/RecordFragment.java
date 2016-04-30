package com.gionee.feedback.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.ReplyInfo;
import com.gionee.feedback.utils.Log;
import com.gionee.res.Layout;
import com.gionee.res.ResourceNotFoundException;
import com.gionee.res.Text;
import com.gionee.res.Widget;
import java.util.ArrayList;
import java.util.List;

@TargetApi(11)
public class RecordFragment extends BaseFragment {
    private static final String TAG = "RecordFragment";
    private ActionMode mActionMode;
    private TextView mEmptyView;
    protected List<FeedbackInfo> mFeedbackInfos;
    private MultiChoiceModeListener mMultiChoiceModeListener = new MultiChoiceModeListener() {
        OnClickListener mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (RecordFragment.this.mRecordAdapter.getCheckedItemCount() == RecordFragment.this.mRecordAdapter.getCount()) {
                    AnonymousClass2.this.deselectAll();
                } else {
                    AnonymousClass2.this.selectAll();
                }
            }
        };
        Button mutilButton;
        TextView mutilTitle;

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            RecordFragment.this.mRecordAdapter.setCheckedState(position, checked);
            String format = RecordFragment.this.getResources().getString(Text.gn_fb_string_mutil_select_more.getIdentifier(RecordFragment.this.mActivity));
            int checkedItemCount = RecordFragment.this.mRecordListView.getCheckedItemCount();
            String title = String.format(format, new Object[]{Integer.valueOf(checkedItemCount)});
            this.mutilTitle.setText(title);
            Log.d(RecordFragment.TAG, "title = " + title + "  checkCount = " + checkedItemCount + "  count = " + RecordFragment.this.mRecordAdapter.getCount());
            if (checkedItemCount == RecordFragment.this.mRecordAdapter.getCount()) {
                this.mutilButton.setText(Text.gn_fb_string_mutil_cancel_selectall.getIdentifier(RecordFragment.this.mActivity));
            } else {
                this.mutilButton.setText(Text.gn_fb_string_mutil_selectall.getIdentifier(RecordFragment.this.mActivity));
            }
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            RecordFragment.this.mActionMode = mode;
            View view = LayoutInflater.from(RecordFragment.this.mActivity).inflate(Layout.gn_fb_layout_mode_customview.getIdentifier(RecordFragment.this.mActivity), null);
            this.mutilTitle = (TextView) RecordFragment.this.getView(view, Widget.gn_fb_id_mutil_selecttitle.getIdentifier(RecordFragment.this.mActivity));
            this.mutilButton = (Button) RecordFragment.this.getView(view, Widget.gn_fb_id_mutil_selectbutton.getIdentifier(RecordFragment.this.mActivity));
            this.mutilButton.setOnClickListener(this.mOnClickListener);
            mode.setCustomView(view);
            mode.getMenuInflater().inflate(com.gionee.res.Menu.gn_fb_menu_multi_choice.getIdentifier(RecordFragment.this.mActivity), menu);
            RecordFragment.this.mRecordAdapter.setActionMode(true);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d(RecordFragment.TAG, "onActionItemClicked" + item.getTitle());
            RecordFragment.this.deleteCheckedItems(mode);
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            RecordFragment.this.mRecordAdapter.setActionMode(false);
            RecordFragment.this.mActionMode = null;
        }

        private void selectAll() {
            int count = RecordFragment.this.mRecordAdapter.getCount();
            for (int i = 0; i < count; i++) {
                RecordFragment.this.mRecordListView.setItemChecked(i, true);
                RecordFragment.this.mRecordAdapter.setCheckedState(i, true);
            }
        }

        private void deselectAll() {
            RecordFragment.this.mRecordAdapter.setActionMode(false);
            RecordFragment.this.mActionMode.finish();
        }
    };
    private RecordAdapter mRecordAdapter;
    private ListView mRecordListView;
    private View mTimeLineView;

    public static RecordFragment getInstance(State record) {
        RecordFragment recordFragment = new RecordFragment();
        recordFragment.mState = record;
        return recordFragment;
    }

    protected View creatView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(Layout.gn_fb_layout_record.getIdentifier(getActivity()), container, false);
    }

    protected void initData() {
        this.mFeedbackInfos = new ArrayList();
    }

    protected void initView() {
        try {
            this.mEmptyView = (TextView) getView(Widget.gn_fb_id_recordempty.getIdentifier(this.mActivity));
            this.mTimeLineView = getView(Widget.gn_fb_id_timeline.getIdentifier(this.mActivity));
            this.mRecordListView = (ListView) getView(Widget.gn_fb_id_recordlayout.getIdentifier(this.mActivity));
            this.mRecordAdapter = new RecordAdapter(this.mActivity, this.mFeedbackInfos);
            this.mRecordListView.setChoiceMode(3);
            this.mRecordListView.setMultiChoiceModeListener(this.mMultiChoiceModeListener);
            this.mRecordListView.setAdapter(this.mRecordAdapter);
            this.mRecordListView.setSelection(getFirstUnreadPosition(this.mFeedbackInfos));
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int getFirstUnreadPosition(List<FeedbackInfo> infos) {
        int size = infos.size();
        for (int i = 0; i < size; i++) {
            for (ReplyInfo replyInfo : ((FeedbackInfo) infos.get(i)).getReplyInfos()) {
                if (!replyInfo.isReaded()) {
                    return i;
                }
            }
        }
        return 0;
    }

    protected void onBackPressed() {
        this.mDataManager.readAllReplies(this.mFeedbackInfos);
        super.onBackPressed();
    }

    protected void reset() {
        if (this.mActionMode != null) {
            this.mRecordAdapter.setActionMode(false);
            this.mActionMode.finish();
        }
    }

    protected void updateView() {
        if (this.mRecordAdapter != null) {
            this.mRecordAdapter.update(this.mFeedbackInfos);
            if (this.mFeedbackInfos.size() == 0) {
                this.mEmptyView.setVisibility(0);
                this.mTimeLineView.setVisibility(8);
                return;
            }
            this.mEmptyView.setVisibility(8);
            this.mTimeLineView.setVisibility(0);
        }
    }

    protected void updateDatas(List<FeedbackInfo> feedbackInfos) {
        super.updateDatas(feedbackInfos);
        this.mFeedbackInfos = feedbackInfos;
    }

    private void deleteCheckedItems(ActionMode actionMode) {
        Log.d(TAG, "deleteCheckedItems()");
        new Builder(this.mActivity).setTitle(Text.gn_fb_string_delete_message_title.getIdentifier(this.mActivity)).setMessage(Text.gn_fb_string_delete_message_content.getIdentifier(this.mActivity)).setNegativeButton(17039360, null).setPositiveButton(17039370, handOkDelete(actionMode)).create().show();
    }

    private DialogInterface.OnClickListener handOkDelete(final ActionMode actionMode) {
        return new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int count = RecordFragment.this.mRecordAdapter.getCount();
                FeedbackInfo[] deleteInfos = new FeedbackInfo[count];
                int j = 0;
                int i = 0;
                while (j < count) {
                    int i2;
                    FeedbackInfo item = RecordFragment.this.mRecordAdapter.getItem(j);
                    if (item.isChecked()) {
                        i2 = i + 1;
                        deleteInfos[i] = item;
                    } else {
                        i2 = i;
                    }
                    j++;
                    i = i2;
                }
                RecordFragment.this.mDataManager.deleteFeedbackInfos(deleteInfos);
                actionMode.finish();
            }
        };
    }
}
