package com.gionee.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Build.VERSION;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.gionee.aminote.R;

public class AmigoIndeterminateProgressDialog {
    private Activity mActivity;
    private View mContent;
    private Dialog mDialog;
    private TextView mMessageView;

    public AmigoIndeterminateProgressDialog(Activity activity) {
        this.mActivity = activity;
        initView();
        initDialog();
    }

    private void initView() {
        View content = this.mActivity.getLayoutInflater().inflate(R.layout.amigo_indeterminate_progress_dialog_ly, null, false);
        ProgressBar progressBar = (ProgressBar) content.findViewById(R.id.amigo_indeterminate_progress_dialog_id_progressbar);
        if (VERSION.SDK_INT >= 21) {
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(this.mActivity.getResources().getColor(R.color.system_stress_color)));
        }
        this.mMessageView = (TextView) content.findViewById(R.id.amigo_indeterminate_progress_dialog_id_message);
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

    public void setMessage(int resid) {
        this.mMessageView.setText(resid);
    }

    public void setMessage(String message) {
        this.mMessageView.setText(message);
    }

    public void show() {
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
    }

    public void hide() {
        if (this.mDialog.isShowing()) {
            this.mDialog.hide();
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
}
