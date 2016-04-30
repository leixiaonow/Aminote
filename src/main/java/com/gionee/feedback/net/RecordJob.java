package com.gionee.feedback.net;

import android.content.Context;
import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.exception.FeedBackNetException;
import com.gionee.feedback.exception.FeedBackParserException;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.ResultCode;
import com.gionee.feedback.net.Job.Callback;
import com.gionee.feedback.net.parser.RecordParser;
import com.gionee.feedback.utils.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordJob extends Job {
    private static final int PERIOD = 30000;
    private static final String TAG = "RecordJob";
    private volatile AtomicBoolean isLoop = new AtomicBoolean(false);
    private IAppData mAppData;
    private Context mContext;
    private String mImei;
    private final Object mObject = new Object();
    private String mPackageName;

    public RecordJob(Context context, Callback callback, boolean loop, IAppData appData) throws FeedBackException {
        super(callback);
        this.mImei = DataManager.getInstance(context).getImei();
        this.mPackageName = context.getPackageName();
        setAtomicBoolean(this.isLoop, loop);
        this.mContext = context;
        this.mAppData = appData;
    }

    private void setAtomicBoolean(AtomicBoolean isLoop, boolean loop) {
        do {
        } while (!isLoop.compareAndSet(isLoop.get(), loop));
    }

    public boolean isLoop() {
        return this.isLoop.get();
    }

    public List<FeedbackInfo> run() {
        List<FeedbackInfo> feedbackInfos;
        Object feedbackInfos2 = new ArrayList();
        while (HttpUtils.isNetworkAvailable(this.mContext)) {
            try {
                feedbackInfos = new RecordParser().parser(HttpUtils.queryUnread(this.mContext, this.mPackageName, this.mImei, this.mAppData));
            } catch (FeedBackException e) {
                if (e instanceof FeedBackNetException) {
                    int status = ((FeedBackNetException) e).getHttpStatus();
                    sendMessage(102, Integer.valueOf(status));
                    Log.e(TAG, "FeedBackNetException status = " + status);
                } else if (e instanceof FeedBackParserException) {
                    FeedBackParserException exception = (FeedBackParserException) e;
                    sendMessage(102, Integer.valueOf(ResultCode.CODE_PARSE_ERROR.value()));
                    Log.e(TAG, "FeedBackParserException obj = " + exception.getParserObj());
                }
            }
            Log.d(TAG, "RecordJob run wait start-----" + this.isLoop.get());
            if (this.isLoop.get()) {
                sendMessage(101, feedbackInfos2);
                try {
                    synchronized (this.mObject) {
                        this.mObject.wait(30000);
                    }
                    feedbackInfos2 = new ArrayList();
                } catch (InterruptedException e2) {
                    Log.e(TAG, e2.getMessage());
                }
            }
            Log.d(TAG, "RecordJob run end-----");
            if (!this.isLoop.get()) {
                return feedbackInfos;
            }
        }
        sendMessage(102, Integer.valueOf(ResultCode.CODE_NETWORK_DISCONNECTED.value()));
        setAtomicBoolean(this.isLoop, false);
        return feedbackInfos;
    }

    public void stopLoopRecord() {
        setAtomicBoolean(this.isLoop, false);
        synchronized (this.mObject) {
            this.mObject.notify();
        }
    }
}
