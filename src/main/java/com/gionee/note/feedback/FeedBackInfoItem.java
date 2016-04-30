package com.gionee.note.feedback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.ReplyInfo;
import java.util.List;

public class FeedBackInfoItem extends LinearLayout {
    private TextView mAttachCountView;
    private TextView mMessageContentView;
    private LinearLayout mReplyListLayout;
    private TextView mSendDateTextView;

    public FeedBackInfoItem(Context context) {
        this(context, null);
    }

    public FeedBackInfoItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedBackInfoItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private void initView() {
        this.mMessageContentView = (TextView) findViewById(R.id.fb_problem_content);
        this.mSendDateTextView = (TextView) findViewById(R.id.gn_fb_id_senddate);
        this.mReplyListLayout = (LinearLayout) findViewById(R.id.gn_fb_id_replylist);
        this.mAttachCountView = (TextView) findViewById(R.id.gn_fb_id_attach_count);
    }

    public void updateView(FeedbackInfo info) {
        this.mMessageContentView.setText(getResources().getString(R.string.fb_problem_format) + info.getContent().trim());
        this.mSendDateTextView.setText(info.getSendTime());
        List<String> attachs = info.getAttachTextArray();
        if (attachs == null || attachs.size() <= 0) {
            this.mAttachCountView.setVisibility(8);
        } else {
            this.mAttachCountView.setVisibility(0);
            String attachCount = getResources().getString(R.string.gn_fb_string_attach_count);
            this.mAttachCountView.setText(String.format(attachCount, new Object[]{Integer.valueOf(attachs.size())}));
        }
        updateReplyLayout(info);
    }

    private void updateReplyLayout(FeedbackInfo info) {
        List<ReplyInfo> replyInfos = info.getReplyInfos();
        if (replyInfos == null || replyInfos.isEmpty()) {
            this.mReplyListLayout.removeAllViews();
            this.mReplyListLayout.setVisibility(8);
            return;
        }
        this.mReplyListLayout.setVisibility(0);
        updateReplyListLayout(replyInfos, this.mReplyListLayout.getChildCount(), replyInfos.size());
    }

    private void updateReplyListLayout(List<ReplyInfo> replyInfos, int curChildCount, int replyListSize) {
        int index = 0;
        while (index < curChildCount) {
            ((ReplyInfoItem) this.mReplyListLayout.getChildAt(index)).updateView((ReplyInfo) replyInfos.get(index), index, replyInfos.size());
            index++;
        }
        if (replyListSize <= curChildCount) {
            while (index < curChildCount) {
                this.mReplyListLayout.removeViewAt(index);
                index++;
            }
            return;
        }
        while (index < replyListSize) {
            ReplyInfoItem item = newReplyItem();
            item.updateView((ReplyInfo) replyInfos.get(index), index, replyInfos.size());
            this.mReplyListLayout.addView(item);
            index++;
        }
    }

    private ReplyInfoItem newReplyItem() {
        return (ReplyInfoItem) LayoutInflater.from(getContext()).inflate(R.layout.fb_feedback_record_item_reply_item_ly, null);
    }
}
