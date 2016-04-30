package com.gionee.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.gionee.aminote.R;

public class AmigoDeterminateProgressDialog implements OnClickListener {
    private Activity mActivity;
    private View mContent;
    private Dialog mDialog;
    private TextView mMessageView;
    private OnCancelListener mOnCancelListener;
    private ProgressBar mProgressBar;
    private String mProgressNumberFormat;
    private TextView mProgressNumberView;

    public interface OnCancelListener {
        void onCancel();
    }

    public AmigoDeterminateProgressDialog(Activity activity) {
        this(activity, true);
    }

    public AmigoDeterminateProgressDialog(Activity activity, boolean isShowCancelButton) {
        this.mActivity = activity;
        initView(isShowCancelButton);
        initDialog();
    }

    private void setFormats(int length) {
        this.mProgressNumberFormat = "%1$-" + length + "d/%2$-" + length + "d";
    }

    private void initView(boolean isShowCancelButton) {
        View content = this.mActivity.getLayoutInflater().inflate(R.layout.amigo_determinate_progress_dialog_ly, null, false);
        content.findViewById(R.id.amigo_determinate_progress_dialog_id_cancel).setOnClickListener(this);
        if (isShowCancelButton) {
            content.findViewById(R.id.amigo_determinate_progress_dialog_id_cancel_ly).setVisibility(0);
        } else {
            content.findViewById(R.id.amigo_determinate_progress_dialog_id_cancel_ly).setVisibility(8);
        }
        this.mProgressNumberView = (TextView) content.findViewById(R.id.amigo_determinate_progress_dialog_id_progress);
        this.mMessageView = (TextView) content.findViewById(R.id.amigo_determinate_progress_dialog_id_message);
        this.mProgressBar = (ProgressBar) content.findViewById(R.id.amigo_determinate_progress_dialog_id_progressbar);
        this.mProgressBar.setIndeterminate(false);
        this.mContent = content;
    }

    private void initDialog() {
        Dialog dialog = new Dialog(this.mActivity, R.style.DialogTheme);
        dialog.setContentView(this.mContent);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = -1;
        lp.height = -2;
        window.setGravity(80);
        this.mDialog = dialog;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.mOnCancelListener = listener;
    }

    public void show() {
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
    }

    public void dismiss() {
        if (this.mDialog.isShowing() && !this.mActivity.isFinishing() && !this.mActivity.isDestroyed()) {
            this.mDialog.dismiss();
        }
    }

    public boolean isShowing() {
        return this.mDialog.isShowing();
    }

    public void setMax(int max) {
        this.mProgressBar.setMax(max);
        setFormats(Integer.toString(max).length());
        updateProgressNumber();
    }

    public int getMax() {
        return this.mProgressBar.getMax();
    }

    public void setProgress(int value) {
        this.mProgressBar.setProgress(value);
        updateProgressNumber();
    }

    public void setProgress(int value, String format) {
        this.mProgressBar.setProgress(value);
        updateProgressNumber(format);
    }

    public void setMessage(int resid) {
        this.mMessageView.setText(resid);
    }

    public void setMessage(String title) {
        this.mMessageView.setText(title);
    }

    public void incrementProgressBy(int diff) {
        this.mProgressBar.incrementProgressBy(diff);
    }

    private void updateProgressNumber() {
        int progress = this.mProgressBar.getProgress();
        int max = this.mProgressBar.getMax();
        String format = this.mProgressNumberFormat;
        this.mProgressNumberView.setText(String.format(format, new Object[]{Integer.valueOf(progress), Integer.valueOf(max)}));
    }

    private void updateProgressNumber(String format) {
        int progress = this.mProgressBar.getProgress();
        int max = this.mProgressBar.getMax();
        this.mProgressNumberView.setText(String.format(format, new Object[]{Integer.valueOf(progress), Integer.valueOf(max)}));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amigo_determinate_progress_dialog_id_cancel:
                if (this.mOnCancelListener != null) {
                    this.mOnCancelListener.onCancel();
                    return;
                }
                return;
            default:
                return;
        }
    }
}
