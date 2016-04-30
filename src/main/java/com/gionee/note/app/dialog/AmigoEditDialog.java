package com.gionee.note.app.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import com.gionee.aminote.R;
import com.gionee.note.app.utils.InputTextNumLimitHelp;

public class AmigoEditDialog implements OnClickListener {
    private Activity mActivity;
    private ConfirmListener mConfirmListener;
    private View mContent;
    private Dialog mDialog;
    private EditText mInputText;
    private InputTextNumLimitHelp mInputTextNumLimitHelp;

    public interface ConfirmListener {
        void onConfirm(String str);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amigo_confirm_dialog_id_cancel:
                this.mDialog.dismiss();
                return;
            case R.id.amigo_confirm_dialog_id_ok:
                Editable editable = this.mInputText.getText();
                if (editable != null) {
                    this.mConfirmListener.onConfirm(editable.toString());
                }
                this.mDialog.dismiss();
                return;
            default:
                return;
        }
    }

    public AmigoEditDialog(Activity activity, ConfirmListener confirmListener) {
        this.mActivity = activity;
        initView();
        intDialog(confirmListener);
        showInput();
    }

    public void show() {
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
    }

    public void setInputText(String inputText) {
        this.mInputText.setText(inputText);
        if (!TextUtils.isEmpty(inputText)) {
            this.mInputText.setSelection(inputText.length());
        }
    }

    public void dismiss() {
        if (this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    private void initView() {
        View content = this.mActivity.getLayoutInflater().inflate(R.layout.signature_input_layout, null);
        content.findViewById(R.id.amigo_confirm_dialog_id_ok).setOnClickListener(this);
        content.findViewById(R.id.amigo_confirm_dialog_id_cancel).setOnClickListener(this);
        this.mInputText = (EditText) content.findViewById(R.id.input_text);
        this.mInputTextNumLimitHelp = new InputTextNumLimitHelp(this.mInputText, 20, 10, 30);
        this.mContent = content;
    }

    private void intDialog(ConfirmListener confirmListener) {
        this.mConfirmListener = confirmListener;
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

    private void showInput() {
        this.mInputText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    AmigoEditDialog.this.mDialog.getWindow().setSoftInputMode(5);
                }
            }
        });
    }

    public void destroy() {
        if (this.mInputTextNumLimitHelp != null) {
            this.mInputTextNumLimitHelp.unRegisterWatcher();
        }
    }
}
