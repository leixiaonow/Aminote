package com.gionee.note.app;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import com.gionee.aminote.R;
import com.gionee.note.app.dialog.AmigoConfirmDialog;
import com.gionee.note.app.dialog.AmigoConfirmDialog.OnClickListener;
import com.gionee.note.app.dialog.AmigoDeterminateProgressDialog;
import com.gionee.note.app.dialog.AmigoDeterminateProgressDialog.OnCancelListener;
import com.gionee.note.common.Future;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.data.DataManager;
import com.gionee.note.data.LocalNoteItem;
import com.gionee.note.data.NoteItem;
import com.gionee.note.data.Path;
import java.util.ArrayList;
import java.util.Iterator;

public class NoteDelExecutor {
    private static final int MSG_TASK_COMPLETE = 1;
    private static final int MSG_TASK_UPDATE = 2;
    private static final String TAG = "NoteDelExecutor";
    private Activity mActivity;
    private AmigoConfirmDialog mConfirmDialog;
    private Future mCurTask;
    private DataManager mDataManager;
    private Handler mMainHandler;
    private AmigoDeterminateProgressDialog mProgressDialog;

    private class DelRunnable implements Runnable {
        private long[] mIds;
        private JobContext mJc;
        private NoteDelListener mListener;

        public DelRunnable(long[] ids, NoteDelListener listener, JobContext jc) {
            this.mIds = ids;
            this.mListener = listener;
            this.mJc = jc;
        }

        public void run() {
            int success = 0;
            int fail = 0;
            int index = 0;
            for (long id : this.mIds) {
                if (NoteDelExecutor.this.onDel(id, this.mListener) > 0) {
                    success++;
                } else {
                    fail++;
                }
                index++;
                NoteDelExecutor.this.mMainHandler.sendMessage(NoteDelExecutor.this.mMainHandler.obtainMessage(2, index, 0));
                if (this.mJc.isCancelled()) {
                    break;
                }
            }
            NoteDelExecutor.this.mMainHandler.sendMessageDelayed(NoteDelExecutor.this.mMainHandler.obtainMessage(1, success, fail, this.mListener), 10);
        }
    }

    public interface NoteDelListener {
        void onDelFinish(int i, int i2);

        int onDelInvalidId();

        void onDelPrepare();
    }

    public NoteDelExecutor(Activity activity) {
        NoteAppImpl app = (NoteAppImpl) activity.getApplication();
        this.mActivity = activity;
        this.mDataManager = app.getDataManager();
        this.mMainHandler = new Handler(activity.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        NoteDelExecutor.this.mCurTask = null;
                        if (NoteDelExecutor.this.mProgressDialog != null) {
                            NoteDelExecutor.this.mProgressDialog.dismiss();
                        }
                        int success = msg.arg1;
                        int fail = msg.arg2;
                        Object listener = msg.obj;
                        if (listener != null) {
                            ((NoteDelListener) listener).onDelFinish(success, fail);
                            return;
                        }
                        return;
                    case 2:
                        if (NoteDelExecutor.this.mProgressDialog != null) {
                            NoteDelExecutor.this.mProgressDialog.setProgress(msg.arg1);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void resume() {
    }

    public void pause() {
    }

    public void destroy() {
        this.mMainHandler.removeCallbacksAndMessages(null);
    }

    public void startAction(final long id, final NoteDelListener listener) {
        if (this.mCurTask == null) {
            showConfirmDialog(new OnClickListener() {
                public void onClick(int which) {
                    if (which == -1) {
                        NoteDelExecutor.this.executorDel(id, listener);
                    }
                }
            });
        }
    }

    public void startAction(final NoteSelectionManager selectionManager, final NoteDelListener listener) {
        if (this.mCurTask == null) {
            showConfirmDialog(new OnClickListener() {
                public void onClick(int which) {
                    if (which == -1) {
                        NoteDelExecutor.this.executorDel(selectionManager, listener);
                    }
                }
            });
        }
    }

    private void executorDel(long id, final NoteDelListener listener) {
        final long[] ids = new long[]{id};
        showProgressDialog(1);
        this.mCurTask = NoteAppImpl.getContext().getThreadPool().submit(new Job() {
            public Object run(JobContext jc) {
                new DelRunnable(ids, listener, jc).run();
                return null;
            }
        });
    }

    private void executorDel(final NoteSelectionManager selectionManager, final NoteDelListener listener) {
        showProgressDialog(selectionManager.getSelectedCount());
        this.mCurTask = NoteAppImpl.getContext().getThreadPool().submit(new Job() {
            public Object run(JobContext jc) {
                ArrayList<Path> paths = selectionManager.getSelected();
                if (paths == null || paths.size() == 0) {
                    NoteDelExecutor.this.mMainHandler.sendMessage(NoteDelExecutor.this.mMainHandler.obtainMessage(1, 0, 0, listener));
                } else {
                    long[] ids = new long[paths.size()];
                    int index = 0;
                    DataManager dataManager = ((NoteAppImpl) NoteDelExecutor.this.mActivity.getApplication()).getDataManager();
                    Iterator i$ = paths.iterator();
                    while (i$.hasNext()) {
                        ids[index] = (long) ((NoteItem) dataManager.getMediaObject((Path) i$.next())).getId();
                        index++;
                    }
                    new DelRunnable(ids, listener, jc).run();
                }
                return null;
            }
        });
    }

    private void showConfirmDialog(OnClickListener listener) {
        AmigoConfirmDialog confirmDialog = this.mConfirmDialog;
        if (confirmDialog == null) {
            confirmDialog = new AmigoConfirmDialog(this.mActivity);
            confirmDialog.setMessage((int) R.string.note_action_del_message);
            confirmDialog.setTitle((int) R.string.note_action_del_string);
            this.mConfirmDialog = confirmDialog;
        }
        confirmDialog.setOnClickListener(listener);
        confirmDialog.show();
    }

    private void showProgressDialog(int maxProgress) {
        AmigoDeterminateProgressDialog progressDialog = this.mProgressDialog;
        if (progressDialog == null) {
            progressDialog = new AmigoDeterminateProgressDialog(this.mActivity);
            progressDialog.setMessage((int) R.string.note_action_del_string);
            progressDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel() {
                    if (NoteDelExecutor.this.mCurTask != null) {
                        NoteDelExecutor.this.mCurTask.cancel();
                    }
                }
            });
            this.mProgressDialog = progressDialog;
        }
        progressDialog.setMax(maxProgress);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    private int onDel(long id, NoteDelListener listener) {
        if (id == -1) {
            return listener.onDelInvalidId();
        }
        try {
            this.mDataManager.delete(LocalNoteItem.ITEM_PATH.getChild(id));
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }
}
