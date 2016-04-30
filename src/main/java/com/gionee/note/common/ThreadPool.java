package com.gionee.note.common;

import android.util.Log;
import com.gionee.framework.log.Logger;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private static final int CORE_POOL_SIZE = 3;
    public static final JobContext JOB_CONTEXT_STUB = new JobContextStub();
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int MAX_POOL_SIZE = 6;
    public static final int MODE_CPU = 1;
    public static final int MODE_NETWORK = 2;
    public static final int MODE_NONE = 0;
    public static final int MODE_SINGLE = 3;
    private static final String TAG = "ThreadPool";
    ResourceCounter mCpuCounter;
    private final Executor mExecutor;
    ResourceCounter mNetworkCounter;
    ResourceCounter mSingleCounter;

    public interface CancelListener {
        void onCancel();
    }

    public interface Job<T> {
        T run(JobContext jobContext);
    }

    public interface JobContext {
        boolean isCancelled();

        void setCancelListener(CancelListener cancelListener);

        boolean setMode(int i);
    }

    private static class ResourceCounter {
        public int value;

        public ResourceCounter(int v) {
            this.value = v;
        }
    }

    private static class JobContextStub implements JobContext {
        private JobContextStub() {
        }

        public boolean isCancelled() {
            return false;
        }

        public void setCancelListener(CancelListener listener) {
        }

        public boolean setMode(int mode) {
            return true;
        }
    }

    private class Worker<T> implements Runnable, Future<T>, JobContext {
        private static final String TAG = "Worker";
        private CancelListener mCancelListener;
        private volatile boolean mIsCancelled;
        private boolean mIsDone;
        private Job<T> mJob;
        private FutureListener<T> mListener;
        private int mMode;
        private T mResult;
        private ResourceCounter mWaitOnResource;

        public Worker(Job<T> job, FutureListener<T> listener) {
            this.mJob = job;
            this.mListener = listener;
        }

        public void run() {
            Object result = null;
            if (setMode(1)) {
                try {
                    result = this.mJob.run(this);
                } catch (Throwable ex) {
                    Log.w(TAG, "Exception in running a job", ex);
                }
            }
            synchronized (this) {
                setMode(0);
                this.mResult = result;
                this.mIsDone = true;
                notifyAll();
            }
            if (this.mListener != null) {
                this.mListener.onFutureDone(this);
            }
        }

        public synchronized void cancel() {
            if (!this.mIsCancelled) {
                this.mIsCancelled = true;
                if (this.mWaitOnResource != null) {
                    synchronized (this.mWaitOnResource) {
                        this.mWaitOnResource.notifyAll();
                    }
                }
                if (this.mCancelListener != null) {
                    this.mCancelListener.onCancel();
                }
            }
        }

        public boolean isCancelled() {
            return this.mIsCancelled;
        }

        public synchronized boolean isDone() {
            return this.mIsDone;
        }

        public synchronized T get() {
            while (!this.mIsDone) {
                try {
                    wait();
                } catch (Exception ex) {
                    Logger.printLog(TAG, "ingore exception:" + ex);
                }
            }
            return this.mResult;
        }

        public void waitDone() {
            get();
        }

        public synchronized void setCancelListener(CancelListener listener) {
            this.mCancelListener = listener;
            if (this.mIsCancelled && this.mCancelListener != null) {
                this.mCancelListener.onCancel();
            }
        }

        public boolean setMode(int mode) {
            ResourceCounter rc = modeToCounter(this.mMode);
            if (rc != null) {
                releaseResource(rc);
            }
            this.mMode = 0;
            rc = modeToCounter(mode);
            if (rc != null) {
                if (!acquireResource(rc)) {
                    return false;
                }
                this.mMode = mode;
            }
            return true;
        }

        private ResourceCounter modeToCounter(int mode) {
            if (mode == 1) {
                return ThreadPool.this.mCpuCounter;
            }
            if (mode == 2) {
                return ThreadPool.this.mNetworkCounter;
            }
            if (mode == 3) {
                return ThreadPool.this.mSingleCounter;
            }
            return null;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private boolean acquireResource(com.gionee.note.common.ThreadPool.ResourceCounter r2) {
            /*
            r1 = this;
        L_0x0000:
            monitor-enter(r1);
            r0 = r1.mIsCancelled;	 Catch:{ all -> 0x0021 }
            if (r0 == 0) goto L_0x000b;
        L_0x0005:
            r0 = 0;
            r1.mWaitOnResource = r0;	 Catch:{ all -> 0x0021 }
            r0 = 0;
            monitor-exit(r1);	 Catch:{ all -> 0x0021 }
        L_0x000a:
            return r0;
        L_0x000b:
            r1.mWaitOnResource = r2;	 Catch:{ all -> 0x0021 }
            monitor-exit(r1);	 Catch:{ all -> 0x0021 }
            monitor-enter(r2);
            r0 = r2.value;	 Catch:{ all -> 0x0029 }
            if (r0 <= 0) goto L_0x0024;
        L_0x0013:
            r0 = r2.value;	 Catch:{ all -> 0x0029 }
            r0 = r0 + -1;
            r2.value = r0;	 Catch:{ all -> 0x0029 }
            monitor-exit(r2);	 Catch:{ all -> 0x0029 }
            monitor-enter(r1);
            r0 = 0;
            r1.mWaitOnResource = r0;	 Catch:{ all -> 0x002c }
            monitor-exit(r1);	 Catch:{ all -> 0x002c }
            r0 = 1;
            goto L_0x000a;
        L_0x0021:
            r0 = move-exception;
            monitor-exit(r1);	 Catch:{ all -> 0x0021 }
            throw r0;
        L_0x0024:
            r2.wait();	 Catch:{ InterruptedException -> 0x002f }
        L_0x0027:
            monitor-exit(r2);	 Catch:{ all -> 0x0029 }
            goto L_0x0000;
        L_0x0029:
            r0 = move-exception;
            monitor-exit(r2);	 Catch:{ all -> 0x0029 }
            throw r0;
        L_0x002c:
            r0 = move-exception;
            monitor-exit(r1);	 Catch:{ all -> 0x002c }
            throw r0;
        L_0x002f:
            r0 = move-exception;
            goto L_0x0027;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.gionee.note.common.ThreadPool.Worker.acquireResource(com.gionee.note.common.ThreadPool$ResourceCounter):boolean");
        }

        private void releaseResource(ResourceCounter counter) {
            synchronized (counter) {
                counter.value++;
                counter.notifyAll();
            }
        }
    }

    public ThreadPool() {
        this(3, 6);
    }

    public ThreadPool(int initPoolSize, int maxPoolSize) {
        this.mCpuCounter = new ResourceCounter(2);
        this.mNetworkCounter = new ResourceCounter(2);
        this.mSingleCounter = new ResourceCounter(1);
        this.mExecutor = new ThreadPoolExecutor(initPoolSize, maxPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue(), new PriorityThreadFactory("thread-pool", 10));
    }

    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        Worker<T> w = new Worker(job, listener);
        this.mExecutor.execute(w);
        return w;
    }

    public <T> Future<T> submit(Job<T> job) {
        return submit(job, null);
    }
}
