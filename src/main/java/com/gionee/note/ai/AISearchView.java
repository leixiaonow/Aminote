package com.gionee.note.ai;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.gionee.aminote.R;

public class AISearchView extends RelativeLayout implements OnClickListener {
    private EditText mInputMsgView;
    private OnQueryTextListener mListener;
    private OnEditorActionListener mOnEditorActionListener;
    private ImageView mSearchBtn;
    private TextWatcher mTextWatcher;

    public interface OnQueryTextListener {
        void onQueryText(String str);
    }

    public AISearchView(Context context) {
        this(context, null);
    }

    public AISearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AISearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void afterTextChanged(Editable s) {
                AISearchView.this.updateSearchBtnState();
            }
        };
        this.mOnEditorActionListener = new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                AISearchView.this.notifyListener();
                return true;
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mInputMsgView = (EditText) findViewById(R.id.recommend_search_edit);
        this.mInputMsgView.addTextChangedListener(this.mTextWatcher);
        this.mInputMsgView.setOnEditorActionListener(this.mOnEditorActionListener);
        this.mSearchBtn = (ImageView) findViewById(R.id.recommend_search_btn);
        this.mSearchBtn.setOnClickListener(this);
        this.mSearchBtn.setEnabled(false);
        Drawable tintIcon = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.note_main_activity_title_dw_search));
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(getContext(), R.color.ai_search_drawable_tint_color));
        this.mSearchBtn.setImageDrawable(tintIcon);
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        this.mListener = listener;
    }

    private void updateSearchBtnState() {
        this.mSearchBtn.setEnabled(!TextUtils.isEmpty(this.mInputMsgView.getText()));
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mInputMsgView.removeTextChangedListener(this.mTextWatcher);
        this.mInputMsgView.setOnEditorActionListener(null);
        this.mTextWatcher = null;
        this.mOnEditorActionListener = null;
        this.mListener = null;
    }

    private void notifyListener() {
        if (this.mListener != null && !TextUtils.isEmpty(this.mInputMsgView.getText())) {
            this.mListener.onQueryText(this.mInputMsgView.getText().toString());
        }
    }

    public void onClick(View v) {
        notifyListener();
    }
}
