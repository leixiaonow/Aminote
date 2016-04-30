package com.gionee.note.app.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.gionee.aminote.R;
import com.gionee.note.app.utils.InputTextNumLimitHelp;
import com.gionee.note.app.utils.InputTextNumLimitHelp.TextChangedListener;
import com.gionee.note.common.NoteUtils;

public class NoteLabelAddView extends LinearLayout implements OnClickListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "NoteLabelAddView";
    private EditText mInputMsgView;
    private InputTextNumLimitHelp mInputTextNumLimitHelp;
    private ImageView mOkMsgView;
    private OnAddLabelListener mOnAddLabelListener;
    private OnEditorActionListener mOnEditorActionListener;
    private Runnable mShowImeRunnable;
    private TextChangedListener mTextChangedListener;

    public interface OnAddLabelListener {
        void onAddLabel(String str);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.label_custom_edit_button) {
            onOkBtnClicked();
        }
    }

    public NoteLabelAddView(Context context) {
        this(context, null);
    }

    public NoteLabelAddView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteLabelAddView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) NoteLabelAddView.this.getContext().getSystemService("input_method");
                if (imm != null) {
                    imm.showSoftInput(NoteLabelAddView.this.mInputMsgView, 0);
                }
            }
        };
        this.mOnEditorActionListener = new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 6) {
                    NoteLabelAddView.this.onOkBtnClicked();
                }
                return true;
            }
        };
        this.mTextChangedListener = new TextChangedListener() {
            public void onTextChange() {
                NoteLabelAddView.this.onTextChanged();
            }
        };
    }

    public void initWatcher(EditText editText) {
        this.mInputTextNumLimitHelp = new InputTextNumLimitHelp(editText, 30, 15, 30);
        this.mInputTextNumLimitHelp.setTextChangedListener(this.mTextChangedListener);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInputMsgView = (EditText) findViewById(R.id.label_custom_edit_text);
        initWatcher(this.mInputMsgView);
        this.mInputMsgView.setOnEditorActionListener(this.mOnEditorActionListener);
        this.mInputMsgView.requestFocus();
        setImeVisibility(true);
        this.mOkMsgView = (ImageView) findViewById(R.id.label_custom_edit_button);
        this.mOkMsgView.setEnabled(false);
        this.mOkMsgView.setOnClickListener(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mInputMsgView.setOnEditorActionListener(null);
        if (this.mInputTextNumLimitHelp != null) {
            this.mInputTextNumLimitHelp.unRegisterWatcher();
        }
        this.mInputTextNumLimitHelp = null;
        this.mOnAddLabelListener = null;
        this.mOnEditorActionListener = null;
    }

    public void setOnAddLabelListener(OnAddLabelListener listener) {
        this.mOnAddLabelListener = listener;
    }

    private void onTextChanged() {
        updateOkButton();
    }

    private void updateOkButton() {
        this.mOkMsgView.setEnabled(!TextUtils.isEmpty(this.mInputMsgView.getText().toString().trim()));
    }

    private void onOkBtnClicked() {
        String labelName = NoteUtils.lineSpaceFilter(this.mInputMsgView.getText().toString().trim());
        this.mInputMsgView.setText("");
        this.mInputMsgView.requestFocus();
        setImeVisibility(true);
        if (!TextUtils.isEmpty(labelName) && this.mOnAddLabelListener != null) {
            this.mOnAddLabelListener.onAddLabel(labelName);
        }
    }

    private void setImeVisibility(boolean visible) {
        if (visible) {
            post(this.mShowImeRunnable);
            return;
        }
        removeCallbacks(this.mShowImeRunnable);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService("input_method");
        if (imm != null) {
            imm.hideSoftInputFromWindow(this.mInputMsgView.getWindowToken(), 0);
        }
    }

    public void clearFocus() {
        this.mInputMsgView.clearFocus();
        setImeVisibility(false);
        super.clearFocus();
    }
}
