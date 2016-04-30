package com.gionee.feedback.net;

import android.content.Context;
import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.exception.FeedBackNetException;
import com.gionee.feedback.exception.FeedBackParserException;
import com.gionee.feedback.logic.vo.CertificationInfo;
import com.gionee.feedback.net.Job.Callback;
import com.gionee.feedback.net.parser.CertificationParser;
import com.gionee.feedback.utils.Log;

public class CertificationJob extends Job {
    private static final String TAG = "CertificationJob";
    private IAppData mAppData;
    private Context mContext;

    public CertificationJob(Context context, Callback callback, IAppData iAppData) throws FeedBackException {
        super(callback);
        this.mContext = context;
        this.mAppData = iAppData;
    }

    public CertificationInfo run() {
        Log.d(TAG, "appid = " + this.mAppData.getAppKey());
        CertificationInfo info = null;
        try {
            info = new CertificationParser().parser(HttpUtils.login(this.mContext, this.mAppData));
        } catch (FeedBackException e) {
            if (e instanceof FeedBackNetException) {
                Log.e(TAG, "FeedBackNetException status = " + ((FeedBackNetException) e).getHttpStatus());
            } else if (e instanceof FeedBackParserException) {
                Log.e(TAG, "FeedBackParserException obj = " + ((FeedBackParserException) e).getParserObj());
            }
        }
        return info;
    }
}
