package com.gionee.feedback.net;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.gionee.feedback.exception.FeedBackException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class Job {
    private final String TAG = getClass().getSimpleName();
    private InternalHandler mHandler;
    protected JobInfo mJobInfo;

    private class CallableImpl<T> implements Callable<T> {
        private CallableImpl() {
        }

        public T call() throws Exception {
            T result = Job.this.run();
            Job.this.sendMessage(101, result);
            return result;
        }
    }

    public interface Callback<T> {
        void onError(int i);

        void onResult(T t);
    }

    private class InternalHandler extends Handler {
        InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    Job.this.mJobInfo.mCallback.onResult(msg.obj);
                    return;
                case 102:
                    Job.this.mJobInfo.mCallback.onError(((Integer) msg.obj).intValue());
                    return;
                default:
                    Job.this.handleJobMessage(msg);
                    return;
            }
        }
    }

    protected class JobInfo {
        protected Callable mCallable;
        protected Callback mCallback;
        protected Future mFuture;

        protected JobInfo() {
        }
    }

    public abstract <T> T run();

    protected Job(Callback callback) throws FeedBackException {
        Looper looper = Looper.myLooper();
        if (looper == null) {
            throw new FeedBackException("current no looper");
        }
        this.mHandler = new InternalHandler(looper);
        this.mJobInfo = new JobInfo();
        this.mJobInfo.mCallable = new CallableImpl();
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        this.mJobInfo.mCallback = callback;
    }

    protected void sendMessage(int what, Object obj) {
        Message msg = this.mHandler.obtainMessage(what);
        msg.obj = obj;
        sendMessage(msg);
    }

    protected void sendMessage(Message message) {
        this.mHandler.sendMessage(message);
    }

    protected void sendMessage(int what) {
        sendMessage(this.mHandler.obtainMessage(what));
    }

    protected void handleJobMessage(Message msg) {
    }

    public void cancel() {
        JobInfo info = this.mJobInfo;
        if (info.mFuture != null) {
            info.mFuture.cancel(true);
        }
    }
}
