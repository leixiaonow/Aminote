package com.gionee.note.feedback;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.gionee.aminote.R;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import java.util.List;

public class FeedbackRecordAdapter extends BaseAdapter {
    private List<FeedbackInfo> mFeedbackInfos;
    private LayoutInflater mInflater;

    public FeedbackRecordAdapter(Context context, List<FeedbackInfo> infos) {
        this.mFeedbackInfos = infos;
        this.mInflater = LayoutInflater.from(context);
    }

    public void update(List<FeedbackInfo> infos) {
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
        FeedBackInfoItem view = (FeedBackInfoItem) convertView;
        FeedbackInfo info = (FeedbackInfo) this.mFeedbackInfos.get(position);
        if (view == null) {
            view = (FeedBackInfoItem) this.mInflater.inflate(R.layout.fb_feedback_record_item_ly, null);
        }
        view.updateView(info);
        return view;
    }
}
