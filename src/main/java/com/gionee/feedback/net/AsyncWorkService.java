package com.gionee.feedback.net;

import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.utils.Log;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncWorkService {
    private static final String TAG = "AsyncWorkService";
    private static final int THREAD_POOL_SIZE = 5;
    public ExecutorService mExecutorService = Executors.newFixedThreadPool(5, new DefaultThreadFactory());

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup mGroup;
        private final String mNamePrefix;
        private final AtomicInteger mThreadNumber = new AtomicInteger(1);

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            this.mGroup = s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            this.mNamePrefix = "pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.mGroup, r, this.mNamePrefix + this.mThreadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != 5) {
                t.setPriority(5);
            }
            t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable throwable) {
                    Log.e(AsyncWorkService.TAG, t.getName() + " throw exception " + throwable.getMessage());
                }
            });
            return t;
        }
    }

    public void submit(Job job) throws FeedBackException {
        RetryManager.getInstance().reset();
        Log.d(TAG, "ThreadPool isShutdow = " + this.mExecutorService.isShutdown());
        if (this.mExecutorService.isShutdown()) {
            throw new FeedBackException("ThreadPool is shutdown!!!!");
        }
        JobInfo info = job.mJobInfo;
        info.mFuture = this.mExecutorService.submit(info.mCallable);
    }

    public void shutdown() {
        this.mExecutorService.shutdown();
    }

    public boolean isShutDown() {
        return this.mExecutorService.isShutdown();
    }
}
