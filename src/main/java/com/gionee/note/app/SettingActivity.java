package com.gionee.note.app;

import amigoui.app.AmigoProgressDialog;
import amigoui.widget.AmigoSwitch;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.appupgrade.common.FactoryAppUpgrade;
import com.gionee.appupgrade.common.GnAppUpgradeImple.State;
import com.gionee.appupgrade.common.IGnAppUpgrade;
import com.gionee.appupgrade.common.IGnAppUpgrade.CallBack;
import com.gionee.appupgrade.common.IGnAppUpgrade.Error;
import com.gionee.feedback.FeedbackApi;
import com.gionee.note.app.dialog.AmigoConfirmDialog;
import com.gionee.note.app.dialog.AmigoDeterminateProgressDialog;
import com.gionee.note.app.dialog.AmigoEditDialog;
import com.gionee.note.app.dialog.AmigoEditDialog.ConfirmListener;
import com.gionee.note.app.view.StandardActivity;
import com.gionee.note.app.view.StandardActivity.StandardAListener;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.StatisticsModule;
import com.gionee.note.feedback.NewFeedbackActivity;
import com.gionee.note.provider.NoteShareDataManager;
import java.util.regex.Pattern;

public class SettingActivity extends StandardActivity implements StandardAListener {
    private static final String FORMAT = "%3d%%";
    private static final int REQUEST_READ_PHONE_STATE_AND_START_APP_UPGRADE = 2;
    private static final int REQUEST_READ_PHONE_STATE_AND_START_NEW_FEEDBACK = 1;
    private static final String TAG = "SettingActivity";
    private IGnAppUpgrade mAppUpgrade;
    private CallBack mCallback = new CallBack() {
        public void onOperationStateChange(int GnAppUpgradeStatus, String PackageName) {
            switch (GnAppUpgradeStatus) {
                case 1:
                    SettingActivity.this.dismissCheckNewApkProgress();
                    SettingActivity.this.mState = State.READY_TO_DOWNLOAD;
                    SettingActivity.this.showNewApkDetailsDialog();
                    return;
                case 2:
                    SettingActivity.this.dismissCheckNewApkProgress();
                    SettingActivity.this.mState = State.INITIAL;
                    SettingActivity.this.showToast(R.string.no_version);
                    return;
                case 3:
                    SettingActivity.this.mState = State.DOWNLOAD_COMPLETE;
                    SettingActivity.this.dismissCheckNewApkProgress();
                    SettingActivity.this.dismissDownLoadDialog();
                    SettingActivity.this.showInstallDialog();
                    return;
                default:
                    return;
            }
        }

        public void onError(int GnAppUpgradeError, String PackageName) {
            switch (GnAppUpgradeError) {
                case 100:
                    if (SettingActivity.this.mState == State.CHECKING) {
                        SettingActivity.this.dismissCheckNewApkProgress();
                    } else if (SettingActivity.this.mState == State.DOWNLOADING) {
                        SettingActivity.this.dismissDownLoadDialog();
                    }
                    SettingActivity.this.showToast(R.string.network_error);
                    return;
                case 101:
                    SettingActivity.this.dismissDownLoadDialog();
                    SettingActivity.this.showToast(R.string.no_sdcard);
                    return;
                case 102:
                case Error.ERROR_EMMC_NOSPACE /*110*/:
                    SettingActivity.this.dismissDownLoadDialog();
                    SettingActivity.this.showToast(R.string.no_space);
                    return;
                case Error.NOTIFY_REMOTE_FILE_NOTFOUND /*103*/:
                    SettingActivity.this.dismissDownLoadDialog();
                    SettingActivity.this.showToast(R.string.server_error);
                    return;
                case Error.ERROR_UPGRADING /*104*/:
                    SettingActivity.this.showToast(R.string.upgrading);
                    return;
                case Error.ERROR_LOCAL_FILE_NOT_FOUND /*105*/:
                    SettingActivity.this.showToast(R.string.file_not_found);
                    return;
                case Error.ERROR_LOCAL_FILE_VERIFY_ERROR /*106*/:
                    SettingActivity.this.showToast(R.string.local_file_verify_failed);
                    return;
                case Error.ERROR_PATCH_FILE_ERROR /*107*/:
                    SettingActivity.this.dismissDownLoadDialog();
                    SettingActivity.this.showToast(R.string.server_error);
                    return;
                case Error.ERROR_LOW_MEMORY /*108*/:
                    SettingActivity.this.dismissDownLoadDialog();
                    SettingActivity.this.showToast(R.string.low_memory);
                    return;
                case Error.ERROR_VERIFY_FILE_ERROR /*109*/:
                    SettingActivity.this.dismissDownLoadDialog();
                    SettingActivity.this.showToast(R.string.verify_failed);
                    return;
                default:
                    return;
            }
        }

        public void onDownLoading(int totalSize, int downloadSize, String PackageName) {
            if (SettingActivity.this.mDownloadProgressDialog != null) {
                SettingActivity.this.mDownloadProgressDialog.setProgress(SettingActivity.this.getDownloadProgress(downloadSize, totalSize), SettingActivity.FORMAT);
            }
        }
    };
    private AmigoProgressDialog mCheckNewApkProgressDialog;
    private ConfirmListener mConfirmListener = new ConfirmListener() {
        public void onConfirm(String inputText) {
            String signature = "";
            if (!SettingActivity.this.isSpaceText(inputText)) {
                signature = inputText;
            }
            SettingActivity.this.setSignature(signature);
            NoteShareDataManager.saveSignature(SettingActivity.this, signature);
        }
    };
    private AmigoDeterminateProgressDialog mDownloadProgressDialog;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.setting_list_signature:
                    SettingActivity.this.showSignatureDialog();
                    return;
                case R.id.setting_list_update:
                    SettingActivity.this.startSafeAppUpgrade();
                    return;
                case R.id.setting_list_feedback:
                    SettingActivity.this.startSafeUserFeedback();
                    return;
                case R.id.setting_list_aboutus:
                    SettingActivity.this.startAboutUs();
                    return;
                default:
                    return;
            }
        }
    };
    private TextView mSignature;
    private AmigoEditDialog mSignatureInputDialog;
    private State mState = State.INITIAL;

    private int getDownloadProgress(int downloadSize, int totalSize) {
        if (totalSize == 0) {
            return 100;
        }
        return (downloadSize * 100) / totalSize;
    }

    private void showToast(int StrId) {
        Toast.makeText(this, getString(StrId), 0).show();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAppUpgrade = FactoryAppUpgrade.getGnAppUpgrade();
        this.mAppUpgrade.initial(this.mCallback, getApplicationContext(), getPackageName());
        setViewLayout();
        initView();
    }

    private void setViewLayout() {
        setTitle((int) R.string.setting_title);
        setStandardAListener(this);
        setNoteContentView(R.layout.setting_content_layout);
    }

    private void initView() {
        findViewById(R.id.setting_list_signature).setOnClickListener(this.mOnClickListener);
        findViewById(R.id.setting_list_feedback).setOnClickListener(this.mOnClickListener);
        findViewById(R.id.setting_list_aboutus).setOnClickListener(this.mOnClickListener);
        findViewById(R.id.setting_list_update).setOnClickListener(this.mOnClickListener);
        setNoteRootViewBackgroundColor(ContextCompat.getColor(this, R.color.abstract_note_activity_root_bg_color));
        this.mSignature = (TextView) findViewById(R.id.setting_signature_text);
        setSignature(NoteShareDataManager.getSignatureText(this));
        AmigoSwitch amigoSwitch = (AmigoSwitch) findViewById(R.id.ai_setting_switch);
        amigoSwitch.setChecked(NoteShareDataManager.isAISwitchOpen(this));
        amigoSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                NoteShareDataManager.setAISwitchValue(SettingActivity.this, isChecked);
            }
        });
    }

    private void showNewApkDetailsDialog() {
        if (validActivityEnv()) {
            createNewApkDetailsDialog().show();
        }
    }

    private void showDownLoadDialog() {
        if (validActivityEnv()) {
            if (this.mDownloadProgressDialog == null) {
                this.mDownloadProgressDialog = createDownLoadDialog();
            }
            this.mDownloadProgressDialog.setProgress(0);
            this.mDownloadProgressDialog.show();
        }
    }

    private AmigoDeterminateProgressDialog createDownLoadDialog() {
        AmigoDeterminateProgressDialog downLoadDialog = new AmigoDeterminateProgressDialog(this, false);
        downLoadDialog.setMax(100);
        downLoadDialog.setMessage((int) R.string.down_load_dialog_msg);
        return downLoadDialog;
    }

    private void dismissDownLoadDialog() {
        if (this.mDownloadProgressDialog != null && validActivityEnv() && this.mDownloadProgressDialog.isShowing()) {
            this.mDownloadProgressDialog.dismiss();
        }
    }

    private AmigoConfirmDialog createNewApkDetailsDialog() {
        AmigoConfirmDialog confirmDialog = new AmigoConfirmDialog(this);
        StringBuffer msg = new StringBuffer();
        msg.append(getString(R.string.dialog_update_compelete)).append(this.mAppUpgrade.getNewVersionNum());
        msg.append("\n\n");
        msg.append(this.mAppUpgrade.getReleaseNote());
        msg.append(getString(R.string.dialog_update_or_not));
        confirmDialog.setTitle((int) R.string.version_update);
        confirmDialog.setMessage(msg.toString());
        confirmDialog.setOKButtonAlias((int) R.string.dialog_update);
        confirmDialog.setCancelButtonAlias((int) R.string.dialog_negative_text);
        confirmDialog.setOnClickListener(new AmigoConfirmDialog.OnClickListener() {
            public void onClick(int which) {
                if (which == -1) {
                    SettingActivity.this.mState = State.DOWNLOADING;
                    SettingActivity.this.showDownLoadDialog();
                    new Thread(SettingActivity.this.mAppUpgrade.downLoadApk()).start();
                }
            }
        });
        return confirmDialog;
    }

    private void showInstallDialog() {
        if (validActivityEnv()) {
            createInstallDialog().show();
        }
    }

    private AmigoConfirmDialog createInstallDialog() {
        AmigoConfirmDialog confirmDialog = new AmigoConfirmDialog(this);
        confirmDialog.setTitle((int) R.string.dialog_update);
        confirmDialog.setMessage((int) R.string.dialog_install_or_not);
        confirmDialog.setOKButtonAlias((int) R.string.dialog_install);
        confirmDialog.setCancelButtonAlias((int) R.string.dialog_negative_text);
        confirmDialog.setOnClickListener(new AmigoConfirmDialog.OnClickListener() {
            public void onClick(int which) {
                if (which == -1) {
                    new Thread(SettingActivity.this.mAppUpgrade.installApk(SettingActivity.this, -1)).start();
                }
            }
        });
        return confirmDialog;
    }

    protected void onResume() {
        super.onResume();
        StatisticsModule.onResume(this);
    }

    protected void onPause() {
        super.onPause();
        StatisticsModule.onPause(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mAppUpgrade = null;
        FactoryAppUpgrade.destoryGnAppUpgrade();
    }

    private void startAboutUs() {
        try {
            Intent intent = new Intent();
            intent.setClass(this, AboutUsActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void startSafeUserFeedback() {
        if (NoteUtils.checkPhoneStatePermission()) {
            startUserFeedback();
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_PHONE_STATE"}, 1);
    }

    private void startUserFeedback() {
        try {
            FeedbackApi.createFeedbackApi(this);
            Intent intent = new Intent(this, NewFeedbackActivity.class);
            intent.addFlags(335544320);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void showSignatureDialog() {
        if (validActivityEnv()) {
            if (this.mSignatureInputDialog == null) {
                this.mSignatureInputDialog = new AmigoEditDialog(this, this.mConfirmListener);
            }
            this.mSignatureInputDialog.setInputText(this.mSignature.getText().toString());
            this.mSignatureInputDialog.show();
        }
    }

    private void startSafeAppUpgrade() {
        if (NoteUtils.checkPhoneStatePermission()) {
            this.mState = State.CHECKING;
            showCheckNewApkProgress();
            new Thread(this.mAppUpgrade.checkApkVersion(false, false)).start();
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_PHONE_STATE"}, 2);
    }

    private void showCheckNewApkProgress() {
        if (validActivityEnv()) {
            if (this.mCheckNewApkProgressDialog == null) {
                this.mCheckNewApkProgressDialog = AmigoProgressDialog.show(this, null, getString(R.string.progress_checking), true, true);
            }
            this.mCheckNewApkProgressDialog.show();
        }
    }

    private void dismissCheckNewApkProgress() {
        if (this.mCheckNewApkProgressDialog != null && validActivityEnv() && this.mCheckNewApkProgressDialog.isShowing()) {
            this.mCheckNewApkProgressDialog.dismiss();
        }
    }

    private boolean validActivityEnv() {
        return (isFinishing() || isDestroyed()) ? false : true;
    }

    private boolean isSpaceText(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        int len = text.length();
        StringBuilder builder = new StringBuilder();
        builder.append("\\s").append("{").append(len).append("}");
        return Pattern.compile(builder.toString()).matcher(text).matches();
    }

    private void setSignature(String signature) {
        if (TextUtils.isEmpty(signature)) {
            this.mSignature.setVisibility(8);
        } else {
            this.mSignature.setVisibility(0);
        }
        this.mSignature.setText(signature);
    }

    public void onClickHomeBack() {
        finish();
    }

    public void onClickRightView() {
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    showRemind();
                    return;
                } else {
                    startUserFeedback();
                    return;
                }
            case 2:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    showRemind();
                    return;
                }
                this.mState = State.CHECKING;
                showCheckNewApkProgress();
                new Thread(this.mAppUpgrade.checkApkVersion(false, false)).start();
                return;
            default:
                return;
        }
    }

    private void showRemind() {
        Toast.makeText(this, R.string.authorization_failed, 0).show();
    }
}
