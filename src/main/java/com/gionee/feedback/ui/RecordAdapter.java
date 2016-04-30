package com.gionee.feedback.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.utils.Log;
import java.util.List;

public class RecordAdapter extends BaseAdapter {
    private static final String TAG = "RecordAdapter";
    private boolean mActionMode = false;
    private Context mContext;
    private List<FeedbackInfo> mFeedbackInfos;

    public RecordAdapter(Context context, List<FeedbackInfo> infos) {
        this.mContext = context;
        this.mFeedbackInfos = infos;
    }

    public void update(List<FeedbackInfo> infos) {
        Log.d(TAG, "------update infos from db-----");
        for (FeedbackInfo dbInfo : infos) {
            for (FeedbackInfo mrInfo : this.mFeedbackInfos) {
                if (dbInfo.getID() == mrInfo.getID()) {
                    dbInfo.setChecked(mrInfo.isChecked());
                }
            }
        }
        this.mFeedbackInfos = infos;
        notifyDataSetChanged();
    }

    public void setActionMode(boolean flag) {
        this.mActionMode = flag;
        if (!flag) {
            for (FeedbackInfo info : this.mFeedbackInfos) {
                info.setChecked(false);
            }
        }
        notifyDataSetChanged();
    }

    public void setCheckedState(int position, boolean checked) {
        getItem(position).setChecked(checked);
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mFeedbackInfos != null ? this.mFeedbackInfos.size() : 0;
    }

    public FeedbackInfo getItem(int position) {
        return this.mFeedbackInfos != null ? (FeedbackInfo) this.mFeedbackInfos.get(position) : null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public FeedBackInfoItem getView(int position, View convertView, ViewGroup parent) {
        FeedBackInfoItem view;
        Log.d(TAG, "position=" + position);
        FeedbackInfo info = (FeedbackInfo) this.mFeedbackInfos.get(position);
        if (convertView == null) {
            view = new FeedBackInfoItem(this.mContext);
        } else {
            view = (FeedBackInfoItem) convertView;
        }
        view.updateView(info, this.mActionMode);
        return view;
    }

    public int getCheckedItemCount() {
        int checkedCount = 0;
        for (FeedbackInfo info : this.mFeedbackInfos) {
            if (info.isChecked()) {
                checkedCount++;
            }
        }
        return checkedCount;
    }
}
