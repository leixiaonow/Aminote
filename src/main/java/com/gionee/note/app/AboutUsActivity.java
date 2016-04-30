package com.gionee.note.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.gionee.aminote.R;
import com.gionee.note.app.utils.EditUtils;
import com.gionee.note.app.view.StandardActivity;
import com.gionee.note.app.view.StandardActivity.StandardAListener;
import com.gionee.note.common.PlatformUtil;

public class AboutUsActivity extends StandardActivity implements StandardAListener {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle((int) R.string.about_us);
        setStandardAListener(this);
        setNoteContentView(R.layout.aboutus_content_layout);
        initView();
    }

    private void initView() {
        ((TextView) findViewById(R.id.aboutus_version_text)).setText(versionData(R.string.aboutus_version_content_text));
        TextView noteText = (TextView) findViewById(R.id.aboutus_note_text);
        if (PlatformUtil.isGioneeDevice()) {
            noteText.setText(R.string.app_name);
        } else {
            noteText.setText(R.string.app_name_outside);
        }
        View copyRightView = findViewById(R.id.aboutus_copyright_text);
        if (PlatformUtil.isGioneeDevice()) {
            copyRightView.setVisibility(0);
        } else {
            copyRightView.setVisibility(8);
        }
    }

    private String versionData(int resId) {
        return getString(resId) + EditUtils.getVersionName(this);
    }

    public void onClickHomeBack() {
        finish();
    }

    public void onClickRightView() {
    }
}
