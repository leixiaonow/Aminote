package com.gionee.note.common;

import com.gionee.framework.log.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ErnestWorker {
    private static final String TAG = "ErnestWorker";
    private ConcurrentLinkedQueue<JobProvider> mJobProviders = new ConcurrentLinkedQueue();
    private ArrayList<WorkThread> mThreads = new ArrayList();
    private final Object mWorkLock = new Object();

    public interface FancyJob extends Runnable {
    }

    public interface JobProvider {
        FancyJob provide();
    }

    private class WorkThread extends Thread {
        volatile boolean mBProvidersAdded;
        volatile boolean mBQuit = false;

        public WorkThread(String name) {
            super(name);
            setPriority(10);
        }

        public void quit() {
            this.mBQuit = true;
            synchronized (ErnestWorker.this.mWorkLock) {
                ErnestWorker.this.mWorkLock.notifyAll();
            }
        }

        public void run() {
            while (!this.mBQuit) {
                Iterator<JobProvider> iter = ErnestWorker.this.mJobProviders.iterator();
                this.mBProvidersAdded = false;
                boolean jobProvided = false;
                while (iter.hasNext()) {
                    FancyJob job = ((JobProvider) iter.next()).provide();
                    if (job != null) {
                        job.run();
                        jobProvided = true;
                    }
                    if (this.mBQuit) {
                        return;
                    }
                }
                if (!jobProvided) {
                    synchronized (ErnestWorker.this.mWorkLock) {
                        if (!this.mBProvidersAdded) {
                            try {
                                ErnestWorker.this.mWorkLock.wait();
                            } catch (InterruptedException e) {
                                Logger.printLog(ErnestWorker.TAG, "unexpected interrupt: " + ErnestWorker.this.mWorkLock);
                            }
                        }
                    }
                }
            }
        }
    }

    public interface StreamlinedJob extends FancyJob {
        int getId();
    }

    public static class StreamlinedJobProvider implements JobProvider {
        final IntSortedSet mMarks = new IntSortedSet();
        final JobProvider mProvider;
        final ConcurrentLinkedQueue<StreamlinedJob> pendingWorkFinishers = new ConcurrentLinkedQueue();

        class MyFancyJob implements FancyJob {
            final StreamlinedJob mJob;

            MyFancyJob(StreamlinedJob job) {
                this.mJob = job;
            }

            public void run() {
                this.mJob.run();
                StreamlinedJobProvider.this.unMark(this.mJob);
            }
        }

        public StreamlinedJobProvider(JobProvider provider) {
            this.mProvider = provider;
        }

        public FancyJob provide() {
            StreamlinedJob job = (StreamlinedJob) this.pendingWorkFinishers.poll();
            if (job != null) {
                if (!queryAndMark(job)) {
                    return new MyFancyJob(job);
                }
                this.pendingWorkFinishers.offer(job);
            }
            StreamlinedJob newJob = (StreamlinedJob) this.mProvider.provide();
            if (newJob == null) {
                return null;
            }
            if (!queryAndMark(newJob)) {
                return new MyFancyJob(newJob);
            }
            this.pendingWorkFinishers.offer(newJob);
            return null;
        }

        private boolean queryAndMark(StreamlinedJob job) {
            boolean findAndInsert;
            synchronized (this.mMarks) {
                findAndInsert = this.mMarks.findAndInsert(job.getId());
            }
            return findAndInsert;
        }

        private void unMark(StreamlinedJob job) {
            synchronized (this.mMarks) {
                this.mMarks.delete(job.getId());
            }
        }
    }

    public ErnestWorker(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            this.mThreads.add(new WorkThread("WorkThread-" + i));
        }
    }

    public void startWorking() {
        Iterator i$ = this.mThreads.iterator();
        while (i$.hasNext()) {
            ((WorkThread) i$.next()).start();
        }
    }

    public void addJobProvider(JobProvider provider) {
        this.mJobProviders.add(provider);
        Iterator i$ = this.mThreads.iterator();
        while (i$.hasNext()) {
            ((WorkThread) i$.next()).mBProvidersAdded = true;
        }
        wakeup();
    }

    public void removeJobProvider(JobProvider provider) {
        this.mJobProviders.remove(provider);
    }

    public void wakeup() {
        synchronized (this.mWorkLock) {
            this.mWorkLock.notifyAll();
        }
    }
}
