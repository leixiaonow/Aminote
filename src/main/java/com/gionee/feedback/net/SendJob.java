package com.gionee.feedback.net;

import android.content.Context;
import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.exception.FeedBackNetException;
import com.gionee.feedback.exception.FeedBackParserException;
import com.gionee.feedback.logic.vo.Message;
import com.gionee.feedback.logic.vo.ResultCode;
import com.gionee.feedback.net.Job.Callback;
import com.gionee.feedback.net.parser.SendParser;
import com.gionee.feedback.utils.Log;

public class SendJob extends Job {
    private static final String TAG = "SendJob";
    private IAppData mAppData;
    private Context mContext;
    private Message mMessage;

    public SendJob(Context context, Callback callback, Message message, IAppData appData) throws FeedBackException {
        super(callback);
        this.mContext = context;
        this.mMessage = message;
        this.mAppData = appData;
    }

    public Long run() {
        if (HttpUtils.isNetworkAvailable(this.mContext)) {
            long feedbackId;
            try {
                feedbackId = new SendParser().parser(HttpUtils.sendMessage(this.mMessage, this.mContext, this.mAppData)).longValue();
            } catch (FeedBackException e) {
                feedbackId = -3;
                if (e instanceof FeedBackNetException) {
                    Log.e(TAG, "FeedBackNetException status = " + ((FeedBackNetException) e).getHttpStatus());
                    sendMessage(102, Integer.valueOf(ResultCode.CODE_NETWORK_UNAVAILABLE.value()));
                } else if (e instanceof FeedBackParserException) {
                    FeedBackParserException exception = (FeedBackParserException) e;
                    sendMessage(102, Integer.valueOf(ResultCode.CODE_PARSE_ERROR.value()));
                    Log.e(TAG, "FeedBackParserException obj = " + exception.getParserObj());
                }
            }
            return Long.valueOf(feedbackId);
        }
        sendMessage(102, Integer.valueOf(ResultCode.CODE_NETWORK_DISCONNECTED.value()));
        return Long.valueOf(-3);
    }
}
