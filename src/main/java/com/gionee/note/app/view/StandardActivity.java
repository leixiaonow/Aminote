package com.gionee.note.app.view;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.note.app.AbstractNoteActivity;

public class StandardActivity extends AbstractNoteActivity implements OnClickListener {
    private StandardAListener mListener;

    public interface StandardAListener {
        void onClickHomeBack();

        void onClickRightView();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setStandardAListener(StandardAListener listener) {
        this.mListener = listener;
    }

    public void setTitle(int titleTextId) {
        setTitle(getString(titleTextId));
    }

    public void setTitle(String title) {
        setNoteTitleView(R.layout.setting_title_layout);
        ((TextView) findViewById(R.id.setting_title)).setText(title);
        findViewById(R.id.action_bar_setting_custom_back).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (StandardActivity.this.mListener != null) {
                    StandardActivity.this.mListener.onClickHomeBack();
                }
            }
        });
    }

    public void setTitleAndRightTextView(int titleTextId, int rightTextId) {
        setNoteTitleView(R.layout.extended_standard_title_ly_rigth_textview);
        TextView rightTitleText = (TextView) findViewById(R.id.extender_standard_title_rigth_id);
        ((TextView) findViewById(R.id.freya_title_title)).setText(getString(titleTextId));
        rightTitleText.setText(getString(rightTextId));
        findViewById(R.id.freya_title_home).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StandardActivity.this.mListener.onClickHomeBack();
            }
        });
        rightTitleText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StandardActivity.this.mListener.onClickRightView();
            }
        });
    }

    public void setTitleAndRightImageView(int iconId) {
        setNoteTitleView(R.layout.extended_standard_title_ly_rigth_imageview);
        findViewById(iconId).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StandardActivity.this.mListener.onClickRightView();
            }
        });
        findViewById(R.id.freya_title_home).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                StandardActivity.this.mListener.onClickHomeBack();
            }
        });
    }

    public void onClick(View v) {
    }
}
