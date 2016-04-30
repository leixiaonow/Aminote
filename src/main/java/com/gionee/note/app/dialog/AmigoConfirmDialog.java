package com.gionee.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import com.gionee.aminote.R;

public class AmigoConfirmDialog implements android.view.View.OnClickListener {
    public static final int BUTTON_NEGATIVE = -2;
    public static final int BUTTON_POSITIVE = -1;
    private static final String TAG = "AmigoConfirmDialog";
    private Activity mActivity;
    private View mContent;
    private Dialog mDialog;
    private TextView mMessageView;
    private OnClickListener mOnClickListener;
    private TextView mTitleView;

    public interface OnClickListener {
        void onClick(int i);
    }

    public AmigoConfirmDialog(Activity activity) {
        this.mActivity = activity;
        initView();
        initDialog();
    }

    private void initView() {
        View content = this.mActivity.getLayoutInflater().inflate(R.layout.amigo_confirm_dialog_ly, null, false);
        ((TextView) content.findViewById(R.id.amigo_confirm_dialog_id_ok)).setOnClickListener(this);
        content.findViewById(R.id.amigo_confirm_dialog_id_cancel).setOnClickListener(this);
        this.mTitleView = (TextView) content.findViewById(R.id.amigo_confirm_dialog_id_title);
        this.mMessageView = (TextView) content.findViewById(R.id.amigo_confirm_dialog_id_message);
        this.mContent = content;
    }

    private void initDialog() {
        Dialog dialog = new Dialog(this.mActivity, R.style.DialogTheme);
        dialog.setContentView(this.mContent);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = -1;
        lp.height = -2;
        window.setGravity(80);
        this.mDialog = dialog;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amigo_confirm_dialog_id_cancel:
                if (this.mOnClickListener != null) {
                    this.mOnClickListener.onClick(-2);
                }
                this.mDialog.dismiss();
                return;
            case R.id.amigo_confirm_dialog_id_ok:
                if (this.mOnClickListener != null) {
                    this.mOnClickListener.onClick(-1);
                }
                this.mDialog.dismiss();
                return;
            default:
                return;
        }
    }

    public void setTitle(int resid) {
        this.mTitleView.setVisibility(0);
        this.mTitleView.setText(resid);
    }

    public void setMessage(int resid) {
        this.mMessageView.setText(resid);
    }

    public void setTitle(String title) {
        this.mTitleView.setVisibility(0);
        this.mTitleView.setText(title);
    }

    public void setMessage(String message) {
        this.mMessageView.setText(message);
    }

    public void setOKButtonAlias(int resid) {
        TextView okView = (TextView) this.mContent.findViewById(R.id.amigo_confirm_dialog_id_ok);
        if (resid == 0) {
            okView.setVisibility(8);
        } else {
            okView.setText(resid);
        }
    }

    public void setOKButtonAlias(String alias) {
        ((TextView) this.mContent.findViewById(R.id.amigo_confirm_dialog_id_ok)).setText(alias);
    }

    public void setCancelButtonAlias(int resid) {
        ((TextView) this.mContent.findViewById(R.id.amigo_confirm_dialog_id_cancel)).setText(resid);
    }

    public void setCancelButtonAlias(String alias) {
        ((TextView) this.mContent.findViewById(R.id.amigo_confirm_dialog_id_cancel)).setText(alias);
    }

    public void show() {
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
    }

    public void dismiss() {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    public void cancel() {
        if (this.mDialog != null) {
            this.mDialog.cancel();
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.mDialog.setOnCancelListener(listener);
    }
}
