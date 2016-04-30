package com.gionee.note.app.inputbackup;

import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoAlertDialog.Builder;
import amigoui.widget.AmigoCheckBox;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;

public class ImportBackupManager {
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 2;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final String PROGRESS_FORMAT = "%3d%%";
    private static final int PROGRESS_MAX = 100;
    private static final String TAG = "ImportBackupManager";
    private Activity mActivity;
    private ImportBackUp mImportBackUp;
    private AmigoAlertDialog mImportDialog;
    private Handler mMainHandler;
    private ProgressBar mProgressBar;
    private AmigoAlertDialog mProgressDialog;
    private TextView mProgressMessage;

    public ImportBackupManager(Activity activity) {
        this.mActivity = activity;
        this.mMainHandler = new Handler(activity.getMainLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ImportBackupManager.this.setProgress(ImportBackupManager.this.mImportBackUp.getProgress());
                        if (ImportBackupManager.this.mImportBackUp.isImportFail()) {
                            ImportBackupManager.this.dismissProgressDialog();
                            ImportBackupManager.this.showFailToast(ImportBackupManager.this.mImportBackUp.getFailCode(), ImportBackupManager.this.mImportBackUp.getMinSize());
                            return;
                        } else if (ImportBackupManager.this.mImportBackUp.isNeedInputBackup()) {
                            sendEmptyMessageDelayed(1, 100);
                            return;
                        } else {
                            ImportBackupManager.this.setProgress(100);
                            sendEmptyMessageDelayed(2, 100);
                            return;
                        }
                    case 2:
                        ImportBackupManager.this.dismissProgressDialog();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void showFailToast(int failCode, long minSize) {
        Log.d(TAG, "showFailToast failCode = " + failCode);
        int minSizeMB = (int) (minSize / 1000000);
        Toast.makeText(this.mActivity, this.mActivity.getResources().getString(R.string.import_backup_fail_message, new Object[]{Integer.valueOf(minSizeMB)}), 1).show();
    }

    public void startCheck() {
        NoteAppImpl.getContext().getThreadPool().submit(new Job<Object>() {
            public Object run(JobContext jc) {
                ImportBackUp importBackUp = NoteAppImpl.getContext().getImportBackUp();
                if (importBackUp.isNeedInputBackup()) {
                    ImportBackupManager.this.mImportBackUp = importBackUp;
                    ImportBackupManager.this.mMainHandler.post(new Runnable() {
                        public void run() {
                            if (!ImportBackupManager.this.mActivity.isFinishing() && !ImportBackupManager.this.mActivity.isDestroyed()) {
                                if (ImportBackupManager.this.mImportDialog == null) {
                                    ImportBackupManager.this.mImportDialog = ImportBackupManager.this.createImportDialog();
                                }
                                ImportBackupManager.this.mImportDialog.show();
                            }
                        }
                    });
                }
                return null;
            }
        });
    }

    private AmigoAlertDialog createImportDialog() {
        Builder builder = new Builder(this.mActivity);
        View view = LayoutInflater.from(this.mActivity).inflate(R.layout.import_backup_config_dialog_content_ly, null);
        final AmigoCheckBox checkBox = (AmigoCheckBox) view.findViewById(R.id.import_dialog_checkBox_id);
        view.findViewById(R.id.amigo_confirm_dialog_id_cancel).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ImportBackupManager.this.dismissImportDialog();
                if (checkBox.isChecked()) {
                    ImportBackUp.writeFinishImport();
                }
            }
        });
        view.findViewById(R.id.amigo_confirm_dialog_id_ok).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ImportBackupManager.this.dismissImportDialog();
                ImportBackupManager.this.startInput();
            }
        });
        builder.setCancelable(true);
        builder.setView(view);
        return builder.create();
    }

    private AmigoAlertDialog createProgressDialog() {
        Builder builder = new Builder(this.mActivity);
        View view = LayoutInflater.from(this.mActivity).inflate(R.layout.import_backup_progress_dialog_ly, null);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.import_backup_dialog_progress_bar_id);
        TextView progressMessage = (TextView) view.findViewById(R.id.import_backup_dialog_progress_message_id);
        progressBar.setMax(100);
        this.mProgressBar = progressBar;
        this.mProgressMessage = progressMessage;
        builder.setView(view);
        AmigoAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    private void setProgress(int progress) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setProgress(progress);
        }
        if (this.mProgressMessage != null) {
            this.mProgressMessage.setText(String.format(PROGRESS_FORMAT, new Object[]{Integer.valueOf(progress)}));
        }
    }

    private void startInput() {
        if (this.mImportBackUp != null) {
            if (this.mProgressDialog == null) {
                this.mProgressDialog = createProgressDialog();
            }
            this.mProgressDialog.show();
            setProgress(this.mImportBackUp.getProgress());
            NoteAppImpl.getContext().getThreadPool().submit(new Job<Object>() {
                public Object run(JobContext jc) {
                    ImportBackupManager.this.mImportBackUp.start();
                    return null;
                }
            });
            this.mMainHandler.sendEmptyMessage(1);
        }
    }

    public void resume() {
    }

    public void pause() {
    }

    public void destroy() {
        this.mMainHandler.removeCallbacksAndMessages(null);
        dismissImportDialog();
        dismissProgressDialog();
    }

    private void dismissImportDialog() {
        if (this.mImportDialog != null && this.mImportDialog.isShowing()) {
            this.mImportDialog.dismiss();
        }
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
    }
}
