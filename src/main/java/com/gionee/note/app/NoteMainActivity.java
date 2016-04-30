package com.gionee.note.app;

import amigoui.app.AmigoAlertDialog.Builder;
import amigoui.app.AmigoProgressDialog;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.note.app.inputbackup.ImportBackupManager;
import com.gionee.note.app.view.NoteSearchView;
import com.gionee.note.app.view.NoteSearchView.OnQueryTextListener;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.PlatformUtil;
import com.gionee.note.common.StatisticsModule;
import com.gionee.note.provider.NoteContract.NoteContent;
import com.gionee.note.provider.NoteShareDataManager;

public class NoteMainActivity extends AbstractNoteActivity implements OnClickListener, NoteDbInitCompleteNotify {
    private static final int COVER_APPEAR_TIME = 2000;
    private static final int COVER_DISAPPEAR = 0;
    private static final boolean DEBUG = false;
    private static final int DELETE_ACTION_MODE = 3;
    private static final int EDIT_ACTION_MODE = 2;
    private static final int MSG_UPDATE_PROGRESS = 1;
    private static final String NOTE_ACTION_FROM_SEARCH = "note.intent.action.search";
    private static final int REQUEST_PERMISSION_AND_INIT_DATA = 1;
    private static final String TAG = "NoteMainActivity";
    private ImageView mCover;
    private DialogInterface.OnClickListener mDataFlowCancelListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            NoteMainActivity.this.finish();
        }
    };
    private Dialog mDataFlowHintDialog;
    private DialogInterface.OnClickListener mDataFlowSureListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            NoteShareDataManager.setShowDataFlowHint(NoteMainActivity.this.getApplicationContext(), true);
        }
    };
    private DataUpgrade mDataUpgrade;
    private Drawable mDeleteIcon;
    private String mDeleteString;
    private Drawable mEditIcon;
    private int mEditOrDeleteActionMode = 2;
    private String mEditString;
    private TextView mFootCommonView;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    NoteMainActivity.this.disappearCover();
                    String[] permissions = NoteMainActivity.this.getPermissions();
                    if (permissions != null) {
                        ActivityCompat.requestPermissions(NoteMainActivity.this, permissions, 1);
                        return;
                    }
                    return;
                case 1:
                    NoteMainActivity.this.checkUpgradeProgress();
                    return;
                default:
                    return;
            }
        }
    };
    private ImportBackupManager mImportBackupManager;
    private NoteSelectionManager mNoteSelectionManager;
    private AmigoProgressDialog mProgressDialog;
    private RelativeLayout mRootView;
    private TextView mSelectionAllView;
    private TextView mSelectionTitleView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NoteAppImpl.getContext().registerNoteDbInitCompleteNotify(this);
        initFavor();
        initView();
        initData();
        setFootCommonViewState();
        initListener();
        initFragment(savedInstanceState);
        if (NOTE_ACTION_FROM_SEARCH.equals(getIntent().getAction())) {
            onNoteSearch();
        }
        if (!NoteShareDataManager.getIsFirstLaunch(this)) {
            showDataFlowHint();
        }
    }

    private void initFavor() {
        initImportBackupManager();
        String[] permissions = getPermissions();
        if (permissions != null) {
            ActivityCompat.requestPermissions(this, permissions, 1);
            return;
        }
        initDataUpgrade();
        if (!PlatformUtil.isGioneeDevice()) {
            startCover();
        }
    }

    private String[] getPermissions() {
        String phoneStatePermission = null;
        if (!NoteUtils.checkPhoneStatePermission()) {
            phoneStatePermission = "android.permission.READ_PHONE_STATE";
        }
        String storagePermission = null;
        if (!NoteUtils.checkExternalStoragePermission()) {
            storagePermission = "android.permission.READ_EXTERNAL_STORAGE";
        }
        if (phoneStatePermission != null && storagePermission != null) {
            return new String[]{phoneStatePermission, storagePermission};
        } else if (phoneStatePermission != null) {
            return new String[]{phoneStatePermission};
        } else if (storagePermission == null) {
            return null;
        } else {
            return new String[]{storagePermission};
        }
    }

    private void initImportBackupManager() {
        this.mImportBackupManager = new ImportBackupManager(this);
    }

    private void startCover() {
        this.mRootView = (RelativeLayout) findViewById(R.id.abstract_note_activity_layout_root);
        this.mCover = new ImageView(this);
        this.mCover.setClickable(true);
        this.mCover.setBackground(ContextCompat.getDrawable(this, R.drawable.cover));
        this.mRootView.addView(this.mCover, new LayoutParams(-1, -1));
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0), 2000);
    }

    private void disappearCover() {
        this.mRootView.removeView(this.mCover);
        this.mCover.setClickable(false);
        this.mCover = null;
    }

    private void setFootCommonViewState() {
        if (2 == this.mEditOrDeleteActionMode) {
            this.mFootCommonView.setCompoundDrawables(null, this.mEditIcon, null, null);
            this.mFootCommonView.setText(this.mEditString);
            return;
        }
        this.mFootCommonView.setCompoundDrawables(null, this.mDeleteIcon, null, null);
        this.mFootCommonView.setText(this.mDeleteString);
    }

    private void initView() {
        setNoteTitleView(R.layout.note_main_activity_title_layout);
        setNoteContentView(R.layout.note_main_activity_content_layout);
        setNoteFooterView(R.layout.note_main_activity_action_mode_footer_layout);
        setNoteRootViewBackgroundColor(ContextCompat.getColor(this, R.color.abstract_note_activity_root_bg_color));
        this.mFootCommonView = (TextView) findViewById(R.id.footer_edit_or_delete_action);
    }

    private void initData() {
        this.mEditString = getResources().getString(R.string.note_action_edit_string);
        this.mDeleteString = getResources().getString(R.string.note_action_del_string);
        Drawable editIcon = ContextCompat.getDrawable(this, R.drawable.note_main_activity_title_dw_edit);
        Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.note_main_del_icon);
        Drawable tintEditIcon = DrawableCompat.wrap(editIcon);
        DrawableCompat.setTintList(tintEditIcon, ContextCompat.getColorStateList(this, R.color.action_bar_image_color));
        tintEditIcon.setBounds(0, 0, tintEditIcon.getIntrinsicWidth(), tintEditIcon.getIntrinsicHeight());
        this.mEditIcon = tintEditIcon;
        Drawable tintDeleteIcon = DrawableCompat.wrap(deleteIcon);
        DrawableCompat.setTintList(tintDeleteIcon, ContextCompat.getColorStateList(this, R.color.action_bar_image_color));
        tintDeleteIcon.setBounds(0, 0, tintDeleteIcon.getIntrinsicWidth(), tintDeleteIcon.getIntrinsicHeight());
        this.mDeleteIcon = tintDeleteIcon;
    }

    private void initListener() {
        findViewById(R.id.note_main_activity_title_layout_search).setOnClickListener(this);
        this.mFootCommonView.setOnClickListener(this);
        findViewById(R.id.note_main_activity_title_layout_setting).setOnClickListener(this);
    }

    private void initDataUpgrade() {
        if (DataUpgrade.isExistOldDB(this)) {
            DataUpgrade dataUpgrade = new DataUpgrade();
            if (dataUpgrade.isUpgradeFinish()) {
                startImportBackupCheck();
                return;
            }
            this.mDataUpgrade = dataUpgrade;
            AmigoProgressDialog progressDialog = new AmigoProgressDialog(this);
            this.mProgressDialog = progressDialog;
            this.mProgressDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (!NoteShareDataManager.getHasShowDataFlowHint(NoteMainActivity.this.getApplicationContext())) {
                        NoteMainActivity.this.showDataFlowDialog(NoteMainActivity.this.getApplicationContext());
                    }
                }
            });
            progressDialog.setMessage(getString(R.string.date_upgrade_message));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            this.mHandler.sendEmptyMessageDelayed(1, 100);
            return;
        }
        BuiltInNote.insertBuildInNoteAsync();
        startImportBackupCheck();
    }

    private void checkUpgradeProgress() {
        DataUpgrade dataUpgrade = this.mDataUpgrade;
        if (dataUpgrade.isUpgradeFinish()) {
            dismissProgressDialog();
            startImportBackupCheck();
        } else if (dataUpgrade.isUpgradeFail()) {
            dismissProgressDialog();
            startImportBackupCheck();
            Toast.makeText(this, "error code = " + dataUpgrade.getFailCode() + ",total = " + dataUpgrade.getUpgradeTotalCount() + ",success = " + dataUpgrade.getUpgradeSuccessCount() + ",failCount = " + dataUpgrade.getUpgradeFailCount(), 0).show();
        } else {
            this.mHandler.sendEmptyMessageDelayed(1, 100);
        }
    }

    private void startImportBackupCheck() {
        this.mImportBackupManager.startCheck();
    }

    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.fragment_container, new NoteMainFragment(), "NoteMainFragment");
            ft.commit();
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
    }

    protected void onNewIntent(Intent intent) {
        if (PlatformUtil.isGioneeDevice()) {
            if (NOTE_ACTION_FROM_SEARCH.equals(intent.getAction())) {
                onNoteSearch();
            }
        }
        super.onNewIntent(intent);
    }

    protected void onResume() {
        super.onResume();
        boolean shouldDismissDataFlowHintDialog = this.mDataFlowHintDialog != null && this.mDataFlowHintDialog.isShowing() && NoteShareDataManager.getHasShowDataFlowHint(getApplicationContext());
        if (shouldDismissDataFlowHintDialog) {
            this.mDataFlowHintDialog.dismiss();
            return;
        }
        if (PlatformUtil.isGioneeDevice()) {
            this.mImportBackupManager.resume();
        }
        StatisticsModule.onResume(this);
        statistics(R.string.youju_mainactivity_start);
    }

    protected void onPause() {
        if (PlatformUtil.isGioneeDevice()) {
            this.mImportBackupManager.pause();
        }
        StatisticsModule.onPause(this);
        super.onPause();
    }

    protected void onDestroy() {
        if (this.mHandler != null) {
            this.mHandler.removeCallbacksAndMessages(null);
        }
        if (PlatformUtil.isGioneeDevice()) {
            this.mImportBackupManager.destroy();
        }
        dismissProgressDialog();
        NoteAppImpl.getContext().unRegisterNoteDbInitCompleteNotify(this);
        super.onDestroy();
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
    }

    public void onBackPressed() {
        boolean backSuccess = true;
        if (this.mNoteSelectionManager != null && this.mNoteSelectionManager.inSelectionMode()) {
            this.mNoteSelectionManager.leaveSelectionMode();
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            if (!(PlatformUtil.isGioneeDevice() && moveTaskToBack(true))) {
                backSuccess = false;
            }
            if (!backSuccess) {
                super.onBackPressed();
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.note_main_activity_search_title_layout_back:
                onNoteSearchBack();
                return;
            case R.id.note_main_activity_title_layout_search:
                statistics(R.string.youju_search);
                onNoteSearch();
                return;
            case R.id.footer_edit_or_delete_action:
                editOrDeleteActionResponse();
                return;
            case R.id.note_main_activity_title_layout_setting:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                return;
            default:
                return;
        }
    }

    private void editOrDeleteActionResponse() {
        if (2 == this.mEditOrDeleteActionMode) {
            editResponse();
        } else {
            deleteResponse();
        }
    }

    private void editResponse() {
        statistics(R.string.youju_new_note);
        produceNewNote();
    }

    private void deleteResponse() {
        NoteMainFragment fragment = (NoteMainFragment) getFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onDel();
        }
    }

    private void produceNewNote() {
        Intent intent = new Intent(this, NewNoteActivity.class);
        intent.putExtra(NewNoteActivity.ENABLE_EDIT_MODE, true);
        startActivityForResult(intent, 0);
    }

    private void onNoteSearch() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, new NoteSearchFragment(), "NoteSearchFragment");
        ft.addToBackStack(null);
        ft.commit();
        setNoteFooterView(-1);
    }

    protected void beginNoteSearch(OnQueryTextListener listener) {
        setNoteTitleView(R.layout.note_main_activity_search_title_layout);
        findViewById(R.id.note_main_activity_search_title_layout_back).setOnClickListener(this);
        NoteSearchView searchView = (NoteSearchView) findViewById(R.id.note_search_view);
        searchView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.searchview_zoom));
        searchView.setOnQueryTextListener(listener);
    }

    protected void endNoteSearch() {
        setNoteTitleView(R.layout.note_main_activity_title_layout);
        findViewById(R.id.note_main_activity_title_layout_search).setOnClickListener(this);
        setNoteFooterView(R.layout.note_main_activity_action_mode_footer_layout);
        this.mFootCommonView = (TextView) findViewById(R.id.footer_edit_or_delete_action);
        this.mFootCommonView.setOnClickListener(this);
        findViewById(R.id.note_main_activity_title_layout_setting).setOnClickListener(this);
        setFootCommonViewState();
    }

    private void onNoteSearchBack() {
        findViewById(R.id.note_search_view).clearFocus();
        getFragmentManager().popBackStack();
    }

    public void startSelectionMode(NoteSelectionManager noteSelectionManager, OnClickListener listener) {
        this.mEditOrDeleteActionMode = 3;
        setFootCommonViewState();
        this.mNoteSelectionManager = noteSelectionManager;
        setNoteTitleView(R.layout.note_main_activity_action_mode_title_layout);
        findViewById(R.id.note_main_activity_action_mode_title_layout_back).setOnClickListener(listener);
        this.mSelectionAllView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_select);
        this.mSelectionAllView.setOnClickListener(listener);
        this.mSelectionTitleView = (TextView) findViewById(R.id.note_main_activity_action_mode_title_layout_text);
    }

    public void finishSelectionMode() {
        this.mEditOrDeleteActionMode = 2;
        setFootCommonViewState();
        setNoteTitleView(R.layout.note_main_activity_title_layout);
        findViewById(R.id.note_main_activity_title_layout_search).setOnClickListener(this);
        findViewById(R.id.note_main_activity_title_layout_setting).setOnClickListener(this);
        this.mNoteSelectionManager = null;
        this.mSelectionTitleView = null;
        this.mSelectionAllView = null;
        this.mFootCommonView.setEnabled(true);
    }

    public void setSelectionModeTitle(String title) {
        if (this.mSelectionTitleView != null) {
            this.mSelectionTitleView.setText(title);
        }
    }

    public void updateSelectionViewsState() {
        if (this.mSelectionAllView != null && this.mNoteSelectionManager != null) {
            this.mSelectionAllView.setText(this.mNoteSelectionManager.inSelectAllMode() ? R.string.unselect_all : R.string.select_all);
            if (this.mNoteSelectionManager.getSelectedCount() == 0) {
                this.mFootCommonView.setEnabled(false);
            } else {
                this.mFootCommonView.setEnabled(true);
            }
        }
    }

    private void statistics(int stringId) {
        StatisticsModule.onEvent((Context) this, getResources().getString(stringId));
    }

    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            boolean isOk = true;
            for (int i : grantResults) {
                if (i != 0) {
                    isOk = false;
                    break;
                }
            }
            if (isOk) {
                getContentResolver().notifyChange(NoteContent.CONTENT_URI, null);
                if (PlatformUtil.isGioneeDevice()) {
                    initDataUpgrade();
                    return;
                } else {
                    BuiltInNote.insertBuildInNoteAsync();
                    return;
                }
            }
            showRemind();
            finish();
        }
    }

    private void showRemind() {
        Toast.makeText(this, R.string.authorization_failed, 0).show();
    }

    private void showDataFlowDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.data_flow_dialog, null);
        Builder builder = new Builder(context);
        builder.setTitle((int) R.string.alert_user_title_str);
        builder.setView(view);
        builder.setPositiveButton((int) R.string.alert_user_ok, this.mDataFlowSureListener);
        builder.setNegativeButton((int) R.string.alert_user_cancle, this.mDataFlowCancelListener);
        this.mDataFlowHintDialog = builder.create();
        this.mDataFlowHintDialog.setCanceledOnTouchOutside(false);
        this.mDataFlowHintDialog.show();
    }

    private void showDataFlowHint() {
        if (!NoteShareDataManager.getHasShowDataFlowHint(getApplicationContext()) && PlatformUtil.isGioneeDevice()) {
            showDataFlowDialog(this);
        }
    }

    public void onNoteDbInitComplete() {
        this.mHandler.post(new Runnable() {
            public void run() {
                NoteMainActivity.this.showDataFlowHint();
            }
        });
    }
}
