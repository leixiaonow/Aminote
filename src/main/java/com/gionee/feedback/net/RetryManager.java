package com.gionee.feedback.net;

public class RetryManager {
    private static final int RETRY_TIMES = 3;
    private static RetryManager instance = null;
    private Object mLock = new Object();
    private int mRetryTimes = 0;

    private RetryManager() {
    }

    public static synchronized RetryManager getInstance() {
        RetryManager retryManager;
        synchronized (RetryManager.class) {
            if (instance == null) {
                instance = new RetryManager();
            }
            retryManager = instance;
        }
        return retryManager;
    }

    public boolean isRetry() {
        if (getRetryTimes() >= 3) {
            return false;
        }
        setRetryTimes(getRetryTimes() + 1);
        return true;
    }

    private int getRetryTimes() {
        int i;
        synchronized (this.mLock) {
            i = this.mRetryTimes;
        }
        return i;
    }

    private void setRetryTimes(int retryTimes) {
        synchronized (this.mLock) {
            this.mRetryTimes = retryTimes;
        }
    }

    public void reset() {
        setRetryTimes(0);
    }
}
