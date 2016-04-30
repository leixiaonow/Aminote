package com.gionee.note.feedback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.feedback.logic.vo.ReplyInfo;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class ReplyInfoItem extends LinearLayout {
    private View mLineView;
    private TextView mReplyContentView;
    private TextView mReplyDateView;

    public ReplyInfoItem(Context context) {
        this(context, null);
    }

    public ReplyInfoItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReplyInfoItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mReplyContentView = (TextView) findViewById(R.id.gn_fb_id_replycontent);
        this.mReplyDateView = (TextView) findViewById(R.id.gn_fb_id_replydate);
        this.mLineView = findViewById(R.id.gn_fb_id_replyviewline);
    }

    public void updateView(ReplyInfo info, int position, int replySize) {
        if (position == 0) {
            this.mLineView.setVisibility(8);
        }
        this.mReplyContentView.setText(getContentString(info));
        this.mReplyDateView.setText(getFormatTime(info.getReplyTime(), getContext()));
    }

    private static String getFormatTime(long time, Context context) {
        return new SimpleDateFormat(context.getString(R.string.gn_fb_string_format)).format(new Date(time));
    }

    private String getContentString(ReplyInfo info) {
        return getResources().getString(R.string.fb_answer_format) + info.getReplyContent().trim();
    }
}
