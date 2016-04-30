package com.gionee.feedback.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.ReplyInfo;
import com.gionee.feedback.utils.Log;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.res.Drawable;
import com.gionee.res.Layout;
import com.gionee.res.ResourceNotFoundException;
import com.gionee.res.Text;
import com.gionee.res.Widget;
import java.util.List;

public class FeedBackInfoItem extends LinearLayout {
    private static final String TAG = "FeedBackInfoItem";
    private TextView mAttaciCountView;
    private CheckBox mCheckbox;
    private Context mContext;
    private FeedbackInfo mFeedbackInfo;
    private ExpandableTextView mMessageContentView;
    private ImageView mReadStateIcon;
    private LinearLayout mReplyListLayout;
    private TextView mSendDaTextView;

    public FeedBackInfoItem(Context context) {
        this(context, null);
    }

    public FeedBackInfoItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedBackInfoItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        try {
            View view = LayoutInflater.from(context).inflate(Layout.gn_fb_layout_feedbackitem.getIdentifier(context), this, true);
            this.mMessageContentView = (ExpandableTextView) getViewById(view, Widget.gn_fb_id_messagecontent.getIdentifier(this.mContext));
            this.mMessageContentView.setExpandedLines(4);
            this.mSendDaTextView = (TextView) getViewById(view, Widget.gn_fb_id_senddate.getIdentifier(this.mContext));
            this.mReadStateIcon = (ImageView) getViewById(view, Widget.gn_fb_id_feedbackitem_readstate_icon.getIdentifier(this.mContext));
            this.mReplyListLayout = (LinearLayout) getViewById(view, Widget.gn_fb_id_replylist.getIdentifier(this.mContext));
            this.mCheckbox = (CheckBox) getViewById(view, Widget.gn_fb_id_chechbox.getIdentifier(this.mContext));
            this.mAttaciCountView = (TextView) getViewById(view, Widget.gn_fb_id_attach_count.getIdentifier(this.mContext));
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateView(FeedbackInfo info, boolean actionMode) {
        int i;
        Log.d(TAG, "updateView info = " + info);
        this.mFeedbackInfo = info;
        this.mMessageContentView.setText(info.getContent());
        this.mSendDaTextView.setText(info.getSendTime());
        if (hasUnreadReply(info)) {
            this.mReadStateIcon.setImageResource(Drawable.gn_fb_drawable_time_unread.getIdentifier(this.mContext));
        } else {
            this.mReadStateIcon.setImageResource(Drawable.gn_fb_drawable_time_read.getIdentifier(this.mContext));
        }
        CheckBox checkBox = this.mCheckbox;
        if (actionMode) {
            i = 0;
        } else {
            i = 8;
        }
        checkBox.setVisibility(i);
        this.mCheckbox.setChecked(this.mFeedbackInfo.isChecked());
        List<String> attachs = info.getAttachTextArray();
        if (attachs == null || attachs.size() <= 0) {
            this.mAttaciCountView.setVisibility(8);
        } else {
            Log.d(TAG, "updateView attachs = " + attachs + attachs.size() + "  :" + this.mAttaciCountView.getText() + "  :" + this.mAttaciCountView.getVisibility());
            this.mAttaciCountView.setVisibility(0);
            String attachCount = getResources().getString(Text.gn_fb_string_attach_count.getIdentifier(this.mContext));
            this.mAttaciCountView.setText(String.format(attachCount, new Object[]{Integer.valueOf(attachs.size())}));
        }
        updateReplyLayout(info);
    }

    private boolean hasUnreadReply(FeedbackInfo info) {
        List<ReplyInfo> replyInfos = info.getReplyInfos();
        if (replyInfos == null || replyInfos.isEmpty()) {
            return false;
        }
        for (ReplyInfo replyInfo : replyInfos) {
            if (!replyInfo.isReaded()) {
                return true;
            }
        }
        return false;
    }

    private <T extends View> T getViewById(View view, int id) {
        return view.findViewById(id);
    }

    private void updateReplyLayout(FeedbackInfo info) {
        List<ReplyInfo> replyInfos = info.getReplyInfos();
        int count = this.mReplyListLayout.getChildCount();
        Log.d(TAG, info.getContentID() + "   count = " + count);
        if (replyInfos == null || replyInfos.isEmpty()) {
            this.mReplyListLayout.removeAllViews();
            this.mReplyListLayout.setVisibility(8);
            return;
        }
        this.mReplyListLayout.setVisibility(0);
        int size = replyInfos.size();
        if (size <= count) {
            whenInfoSizeLessThanViewCount(replyInfos, count, size);
        } else {
            whenViewCountLessThanInfoSize(replyInfos, count, size);
        }
    }

    private void whenViewCountLessThanInfoSize(List<ReplyInfo> replyInfos, int count, int size) {
        int index = 0;
        while (index < count) {
            ((ReplyInfoItem) this.mReplyListLayout.getChildAt(index)).updateView((ReplyInfo) replyInfos.get(index), index, replyInfos.size());
            index++;
        }
        while (index < size) {
            Log.d(TAG, "whenViewCountLessThanInfoSize:" + index + DataUpgrade.SPLIT + size);
            ReplyInfoItem item = newReplyItem();
            item.updateView((ReplyInfo) replyInfos.get(index), index, replyInfos.size());
            this.mReplyListLayout.addView(item);
            index++;
        }
    }

    private void whenInfoSizeLessThanViewCount(List<ReplyInfo> replyInfos, int count, int size) {
        int index = 0;
        while (index < size) {
            ((ReplyInfoItem) this.mReplyListLayout.getChildAt(index)).updateView((ReplyInfo) replyInfos.get(index), index, replyInfos.size());
            index++;
        }
        while (index < count) {
            this.mReplyListLayout.removeViewAt(index);
            index++;
            count = this.mReplyListLayout.getChildCount();
        }
    }

    public ReplyInfoItem newReplyItem() {
        try {
            return (ReplyInfoItem) LayoutInflater.from(getContext()).inflate(Layout.gn_fb_layout_replyitem.getIdentifier(getContext()), null);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
