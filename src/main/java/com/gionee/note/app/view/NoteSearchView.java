package com.gionee.note.app.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

public class NoteSearchView extends LinearLayout implements OnClickListener {
    private static final boolean DEBUG = false;
    private static final String TAG = "NoteSearchView";
    private ImageView mClearMsgView;
    private EditText mInputMsgView;
    private CharSequence mOldQueryText;
    private OnEditorActionListener mOnEditorActionListener;
    private OnQueryTextListener mOnQueryChangeListener;
    private Runnable mShowImeRunnable;
    private TextWatcher mTextWatcher;

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String str);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.search_clear_btn) {
            onClearBtnClicked();
        }
    }

    public NoteSearchView(Context context) {
        this(context, null);
    }

    public NoteSearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) NoteSearchView.this.getContext().getSystemService("input_method");
                if (imm != null) {
                    imm.showSoftInput(NoteSearchView.this.mInputMsgView, 0);
                }
            }
        };
        this.mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int after) {
                NoteSearchView.this.onTextChanged(s);
            }

            public void afterTextChanged(Editable s) {
            }
        };
        this.mOnEditorActionListener = new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return true;
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInputMsgView = (EditText) findViewById(R.id.search_input_msg_edit_text);
        this.mInputMsgView.addTextChangedListener(this.mTextWatcher);
        this.mInputMsgView.setOnEditorActionListener(this.mOnEditorActionListener);
        this.mInputMsgView.requestFocus();
        setImeVisibility(true);
        this.mClearMsgView = (ImageView) findViewById(R.id.search_clear_btn);
        this.mClearMsgView.setOnClickListener(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mInputMsgView.removeTextChangedListener(this.mTextWatcher);
        this.mInputMsgView.setOnEditorActionListener(null);
        this.mTextWatcher = null;
        this.mOnEditorActionListener = null;
        this.mOnQueryChangeListener = null;
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        this.mOnQueryChangeListener = listener;
    }

    private void onTextChanged(CharSequence newText) {
        updateClearButton();
        if (!(this.mOnQueryChangeListener == null || TextUtils.equals(newText, this.mOldQueryText))) {
            this.mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        this.mOldQueryText = newText.toString();
    }

    private void updateClearButton() {
        boolean hasText;
        int i = 0;
        if (TextUtils.isEmpty(this.mInputMsgView.getText())) {
            hasText = false;
        } else {
            hasText = true;
        }
        boolean showClearBtn = hasText;
        ImageView imageView = this.mClearMsgView;
        if (!showClearBtn) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    private void onClearBtnClicked() {
        this.mInputMsgView.setText("");
        this.mInputMsgView.requestFocus();
        setImeVisibility(true);
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
