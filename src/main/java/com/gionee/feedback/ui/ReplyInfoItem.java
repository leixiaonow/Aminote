package com.gionee.feedback.ui;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.gionee.feedback.logic.vo.ReplyInfo;
import com.gionee.feedback.utils.Log;
import com.gionee.feedback.utils.Utils;
import com.gionee.note.common.Constants;
import com.gionee.res.Color;
import com.gionee.res.ResourceNotFoundException;
import com.gionee.res.Text;
import com.gionee.res.Widget;

public class ReplyInfoItem extends RelativeLayout {
    private Context mContext;
    private View mLineView;
    private ImageView mNewFeedbackView;
    private ExpandableTextView mReplyContentView;
    private TextView mReplyDateView;
    private Resources mResources = getResources();

    public ReplyInfoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            this.mReplyContentView = (ExpandableTextView) findViewById(Widget.gn_fb_id_replycontent.getIdentifier(this.mContext));
            this.mReplyContentView.setExpandedLines(6);
            this.mReplyDateView = (TextView) findViewById(Widget.gn_fb_id_replydate.getIdentifier(this.mContext));
            this.mLineView = findViewById(Widget.gn_fb_id_replyviewline.getIdentifier(this.mContext));
            this.mNewFeedbackView = (ImageView) findViewById(Widget.gn_fb_id_new_feedback.getIdentifier(this.mContext));
            this.mNewFeedbackView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    try {
                        ((FeedBackActivity) ReplyInfoItem.this.mContext).showState(State.SEND);
                    } catch (ClassCastException e) {
                        Log.d("xxxx", e.getMessage());
                    }
                }
            });
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void updateView(ReplyInfo info, int position, int replySize) {
        if (position == 0) {
            this.mLineView.setVisibility(8);
        }
        if (position == replySize - 1) {
            this.mNewFeedbackView.setVisibility(0);
        } else {
            this.mNewFeedbackView.setVisibility(8);
        }
        this.mReplyContentView.setText(getContentString(info));
        this.mReplyContentView.setTextColor(this.mResources.getColor(Color.gn_fb_color_orange.getIdentifier(this.mContext)));
        this.mReplyDateView.setText(Utils.getFormatTime(info.getReplyTime(), this.mContext));
    }

    private SpannableStringBuilder getContentString(ReplyInfo info) {
        StringBuilder replyContent = new StringBuilder();
        replyContent.append(info.getReplyContent());
        String person = String.format(this.mContext.getString(Text.gn_fb_string_feedback_person_title.getIdentifier(this.mContext)), new Object[]{info.getReplyPerson()});
        if (!TextUtils.isEmpty(person)) {
            replyContent.append("\n\n");
            replyContent.append(person);
        }
        SpannableStringBuilder spannable = new SpannableStringBuilder(replyContent.toString());
        CharacterStyle size = new AbsoluteSizeSpan(8, true);
        int start = replyContent.toString().lastIndexOf(Constants.STR_NEW_LINE);
        spannable.setSpan(size, start, start + 1, 33);
        return spannable;
    }
}
