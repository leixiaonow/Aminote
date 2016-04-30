package com.gionee.note.app;

import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoAlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.note.ai.AIActivity;
import com.gionee.note.ai.AITipView;
import com.gionee.note.ai.AITipView.AITipCallback;
import com.gionee.note.app.LabelManager.LabelDataChangeListener;
import com.gionee.note.app.LabelSelector.OnLabelChangedListener;
import com.gionee.note.app.NoteDelExecutor.NoteDelListener;
import com.gionee.note.app.attachment.AttachmentSelector;
import com.gionee.note.app.attachment.AttachmentSelector.OnSelectPicToAddListener;
import com.gionee.note.app.attachment.AttachmentSelector.OnTakePhotoListener;
import com.gionee.note.app.attachment.SoundRecorder.OnSoundRecorderCompleteListener;
import com.gionee.note.app.dialog.DateTimeDialog;
import com.gionee.note.app.dialog.DateTimeDialog.OnDateTimeSetListener;
import com.gionee.note.app.effect.DrawableManager;
import com.gionee.note.app.effect.EffectUtil;
import com.gionee.note.app.reminder.ReminderManager;
import com.gionee.note.app.span.JsonableSpan;
import com.gionee.note.app.view.NoteContentEditText;
import com.gionee.note.app.view.NoteTitleEditText;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.PlatformUtil;
import com.gionee.note.common.StatisticsModule;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.common.UpdateHelper;
import com.gionee.note.data.NoteInfo;
import com.gionee.note.provider.NoteContract.NoteContent;
import com.gionee.note.provider.NoteShareDataManager;
import com.gionee.note.widget.WidgetUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class NewNoteActivity extends AbstractNoteActivity implements OnClickListener, IYouJuCallback, NoteDbInitCompleteNotify {
    private static final String CAMERA_ACTION = "note.intent.action.from.camera";
    public static final String ENABLE_EDIT_MODE = "enable_edit_mode";
    private static final String KEY_CONTENT = "note_content";
    private static final String KEY_PIC_URI = "note_pic_uri";
    private static final String KEY_TITLE = "note_title";
    public static final String NOTE_ITEM_PATH = "path";
    private static final long NOTE_SAVE_DURATION = 10000;
    public static final int REQUEST_CUSTOM_LABEL = 3;
    public static final int REQUEST_PICK_IMAGE = 1;
    public static final int REQUEST_TAKE_PHOTO = 2;
    private static final String SAVE_INSTANCE_ID = "id";
    private static final String TAG = "NewNoteActivity";
    private AITipView mAITipView;
    private boolean mActive = false;
    private AttachmentSelector mAttachmentSelector;
    private Handler mBackgroundHandler;
    private NoteContentEditText mContentEditText;
    private int[] mCurDate;
    private NoteInfo mCurrNoteInfo = new NoteInfo();
    private DialogInterface.OnClickListener mDataFlowCancleListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            NewNoteActivity.this.stopSave();
            NewNoteActivity.this.exitNote();
            NewNoteActivity.this.finish();
        }
    };
    private DialogInterface.OnClickListener mDataFlowSureListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            NoteShareDataManager.setShowDataFlowHint(NewNoteActivity.this.getApplicationContext(), true);
        }
    };
    private ImageView mDelete;
    private boolean mIsEditMode;
    private boolean mIsOnResumed;
    private boolean mIsShowDateTimeDilaog;
    private ViewGroup mLabelContent;
    private LabelDataChangeListener mLabelDataChangeListener;
    private LabelManager mLabelManager;
    private LabelSelector mLabelSelector;
    private LinearLayout mLabelView;
    private Handler mMainHandler;
    private final long mModifiedTime = System.currentTimeMillis();
    private NoteDelExecutor mNoteDelExecutor;
    private boolean mNoteInfoInitSuccess;
    private long mOldModifyTimeMillis;
    private OnSelectPicToAddListener mOnSelectPicToAddListener = new OnSelectPicToAddListener() {
        public void onAdd(CopyOnWriteArrayList<String> selectPicUris) {
            final CopyOnWriteArrayList<String> lists = selectPicUris;
            NewNoteActivity.this.mBackgroundHandler.post(new Runnable() {
                public void run() {
                    NewNoteActivity.this.addImageFromAttach(lists);
                }
            });
        }
    };
    private NoteInfo mPreNoteInfo = new NoteInfo();
    private PreviewPictureMakeProxy mPreviewPictureMakeProxy;
    private ContentResolver mResolver;
    private Runnable mSaveRunnable = new Runnable() {
        public void run() {
            NewNoteActivity.this.saveNote();
            if (NewNoteActivity.this.mActive) {
                NewNoteActivity.this.mMainHandler.postDelayed(NewNoteActivity.this.mSaveRunnable, NewNoteActivity.NOTE_SAVE_DURATION);
            }
        }
    };
    private View mSelectBill;
    private View mSelectCamera;
    private View mSelectGallery;
    private View mSelectLabel;
    private View mSelectOnlineImage;
    private View mSelectRecord;
    private View mSelectReminder;
    private ImageView mShare;
    private Uri mTakePhotoOutputUri;
    private NoteTitleEditText mTitleEditText;

    private class AlarmSetListener implements OnDateTimeSetListener {
        private AlarmSetListener() {
        }

        public void onDateTimeSet(Calendar calendar) {
            NewNoteActivity.this.mCurrNoteInfo.mDateReminderInMs = calendar.getTimeInMillis();
            NewNoteActivity.this.mContentEditText.setReminderTime(NewNoteActivity.this.mCurrNoteInfo.mDateReminderInMs);
        }

        public void onDataTimeDelete() {
            NewNoteActivity.this.mCurrNoteInfo.mDateReminderInMs = 0;
            NewNoteActivity.this.mContentEditText.setReminderTime(NewNoteActivity.this.mCurrNoteInfo.mDateReminderInMs);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NoteAppImpl.getContext().registerNoteDbInitCompleteNotify(this);
        initContentView();
        initView();
        initHandler();
        initData(savedInstanceState);
        initListener();
        if (!NoteShareDataManager.getHasShowDataFlowHint(this)) {
            showDataFlowHint();
        }
    }

    protected void onNewIntent(Intent intent) {
        if (this.mIsOnResumed) {
            reset();
            String action = intent.getAction();
            if (CAMERA_ACTION.equals(action)) {
                initFromCamera(intent);
            } else if ("android.intent.action.SEND".equals(action)) {
                initFromShare(intent);
            } else {
                initNoteInfo(intent, null);
            }
            super.onNewIntent(intent);
        }
    }

    private void reset() {
        if (isShouldDelete()) {
            deleteEmptyNote();
        }
        if (this.mPreviewPictureMakeProxy != null) {
            this.mPreviewPictureMakeProxy.cancel();
        }
        this.mLabelContent.removeAllViews();
        this.mTitleEditText.getText().clear();
        this.mContentEditText.getText().clear();
    }

    private void initContentView() {
        setNoteTitleView(R.layout.new_note_activity_title_layout);
        setNoteContentView(R.layout.new_note_activity_content_layout);
        setNoteFooterView(R.layout.new_note_activity_footer_layout);
    }

    private void initListener() {
        this.mSelectBill.setOnClickListener(this);
        this.mSelectLabel.setOnClickListener(this);
        this.mSelectRecord.setOnClickListener(this);
        this.mSelectReminder.setOnClickListener(this);
        this.mSelectCamera.setOnClickListener(this);
        this.mSelectGallery.setOnClickListener(this);
        this.mSelectOnlineImage.setOnClickListener(this);
        this.mShare.setOnClickListener(this);
        this.mDelete.setOnClickListener(this);
        findViewById(R.id.new_note_activity_title_layout_back).setOnClickListener(this);
        this.mTitleEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    NewNoteActivity.this.setFootActionEnable(false);
                    NewNoteActivity.this.mTitleEditText.setHint(R.string.title_focus_hint);
                    return;
                }
                NewNoteActivity.this.mTitleEditText.setHint(R.string.title_hint);
            }
        });
        this.mContentEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    NewNoteActivity.this.setFootActionEnable(true);
                }
            }
        });
    }

    private void initView() {
        this.mLabelView = (LinearLayout) findViewById(R.id.new_note_label);
        this.mLabelContent = (ViewGroup) this.mLabelView.findViewById(R.id.new_note_label_content);
        this.mContentEditText = (NoteContentEditText) findViewById(R.id.new_note_content);
        this.mContentEditText.setMovementMethod(new EditMovementMethod(this));
        this.mTitleEditText = (NoteTitleEditText) findViewById(R.id.new_note_title);
        this.mTitleEditText.setMovementMethod(new EditMovementMethod(this));
        this.mSelectBill = findViewById(R.id.action_bill);
        this.mSelectLabel = findViewById(R.id.action_label);
        this.mSelectRecord = findViewById(R.id.action_recorde);
        this.mSelectReminder = findViewById(R.id.action_reminder);
        this.mSelectCamera = findViewById(R.id.action_camera);
        this.mSelectGallery = findViewById(R.id.action_gallery);
        this.mSelectOnlineImage = findViewById(R.id.action_online_image);
        this.mShare = (ImageView) findViewById(R.id.new_note_activity_title_layout_share);
        this.mDelete = (ImageView) findViewById(R.id.new_note_activity_title_layout_delete);
        tintImageViewDrawable(R.id.new_note_activity_title_layout_share, R.drawable.action_preview_enable, R.color.new_note_title_share_color);
        tintImageViewDrawable(R.id.new_note_activity_title_layout_delete, R.drawable.action_del_icon_enable, R.color.new_note_title_delete_color);
        tintImageViewDrawable(R.id.action_label, R.drawable.action_label_icon, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_bill, R.drawable.action_bill_icon, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_reminder, R.drawable.action_alert_icon, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_recorde, R.drawable.attachment_sound_recorder, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_gallery, R.drawable.attachment_select_image, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_camera, R.drawable.attachment_take_photos, R.color.action_bar_image_color);
        tintImageViewDrawable(R.id.action_online_image, R.drawable.attachment_online_image, R.color.action_bar_image_color);
        if (NoteShareDataManager.isAISwitchOpen(this)) {
            ((ViewStub) findViewById(R.id.ai_tip_view_stub)).inflate();
            AITipView aiTipView = (AITipView) findViewById(R.id.ai_tip_image);
            this.mAITipView = aiTipView;
            aiTipView.setAICallback(new AITipCallback() {
                public String requestContent() {
                    if (NewNoteActivity.this.mContentEditText != null) {
                        return NewNoteActivity.this.mContentEditText.getText().toString();
                    }
                    return null;
                }

                public void resultKeyWords(ArrayList<String> keywords) {
                    NewNoteActivity.this.startAIActivity(keywords);
                }
            });
        }
    }

    private void startAIActivity(ArrayList<String> keywords) {
        youjuStatistics(R.string.youju_click_ai_view);
        try {
            Intent intent = new Intent();
            intent.setClass(this, AIActivity.class);
            intent.putExtra(AIActivity.KEY_AMI_Recommend, keywords);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void tintImageViewDrawable(int imageViewId, int iconId, int colorsId) {
        Drawable tintIcon = DrawableCompat.wrap(ContextCompat.getDrawable(this, iconId));
        DrawableCompat.setTintList(tintIcon, ContextCompat.getColorStateList(this, colorsId));
        ((ImageView) findViewById(imageViewId)).setImageDrawable(tintIcon);
    }

    private void refreshLabel(ArrayList<Integer> ids) {
        ArrayList<Integer> currLabels = this.mCurrNoteInfo.mLabel;
        currLabels.clear();
        currLabels.addAll(ids);
    }

    private void showLabel() {
        this.mLabelContent.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        ArrayList<Integer> labels = this.mCurrNoteInfo.mLabel;
        ArrayList<Integer> invalidLabels = new ArrayList();
        Iterator i$ = labels.iterator();
        while (i$.hasNext()) {
            Integer id = (Integer) i$.next();
            String label = this.mLabelSelector.getLabelContentById(id.intValue());
            if (TextUtils.isEmpty(label)) {
                invalidLabels.add(id);
            } else {
                TextView view = (TextView) inflater.inflate(R.layout.edit_page_label_item, null);
                view.setText(label);
                this.mLabelContent.addView(view);
            }
        }
        if (invalidLabels.size() > 0) {
            labels.removeAll(invalidLabels);
        }
        if (this.mLabelContent.getChildCount() > 0) {
            this.mLabelView.setVisibility(0);
        } else {
            this.mLabelView.setVisibility(8);
        }
    }

    private int getSaveNoteId(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getInt("id", -1);
        }
        return -1;
    }

    private void initNoteInfo(NoteInfo noteInfo) {
        this.mPreNoteInfo.mId = noteInfo.mId;
        this.mCurrNoteInfo.mId = this.mPreNoteInfo.mId;
        this.mPreNoteInfo.mDateReminderInMs = noteInfo.mDateReminderInMs;
        this.mCurrNoteInfo.mDateReminderInMs = this.mPreNoteInfo.mDateReminderInMs;
        ArrayList<Integer> labels = noteInfo.mLabel;
        this.mPreNoteInfo.mLabel.clear();
        this.mCurrNoteInfo.mLabel.clear();
        this.mPreNoteInfo.mLabel.addAll(labels);
        this.mCurrNoteInfo.mLabel.addAll(labels);
        String title = noteInfo.mTitle;
        String jsonContent = noteInfo.mContent;
        if (!TextUtils.isEmpty(jsonContent)) {
            DataConvert.applySpanToEditableFromJson(this, jsonContent, this.mContentEditText);
        }
        this.mTitleEditText.getText().append(title);
        this.mContentEditText.setNoteTime(noteInfo.mDateModifiedInMs);
        this.mContentEditText.setHint(R.string.content_hint);
        this.mOldModifyTimeMillis = noteInfo.mDateModifiedInMs;
        this.mContentEditText.setReminderTime(this.mCurrNoteInfo.mDateReminderInMs);
        if (this.mCurrNoteInfo.mDateReminderInMs != 0) {
            ReminderManager.cancelReminder(this, (long) this.mCurrNoteInfo.mId);
        }
        showLabel();
    }

    private void initNoteInfoFromDB(final int id) {
        this.mBackgroundHandler.post(new Runnable() {
            public void run() {
                final NoteInfo noteInfo = NoteUtils.getNoteItemFromDB(id);
                NewNoteActivity.this.mMainHandler.post(new Runnable() {
                    public void run() {
                        NewNoteActivity.this.mNoteInfoInitSuccess = noteInfo != null;
                        if (NewNoteActivity.this.mNoteInfoInitSuccess) {
                            NewNoteActivity.this.initNoteInfo(noteInfo);
                            NewNoteActivity.this.initWatcher();
                            NewNoteActivity.this.initCurDate();
                            NewNoteActivity.this.updateActivityWindowBackground();
                            NewNoteActivity.this.checkSetEmptyState();
                            return;
                        }
                        Toast.makeText(NoteAppImpl.getContext(), R.string.file_save_toast_text, 0).show();
                        NewNoteActivity.this.insertEmptyNoteInfo();
                    }
                });
            }
        });
    }

    private void checkSetEmptyState() {
        boolean enable = !NoteUtils.isContentEmpty(this.mContentEditText.getText());
        this.mShare.setEnabled(enable);
        this.mDelete.setEnabled(enable);
    }

    private void initCurDate() {
        this.mCurDate = NoteUtils.getToady();
    }

    private void updateActivityWindowBackground() {
        Bitmap bgBitmap = DrawableManager.getEffectBitmap(this, new EffectUtil(System.currentTimeMillis()).getEffect(this.mOldModifyTimeMillis), NoteUtils.sScreenWidth, NoteUtils.sScreenHeight);
        Drawable bgDrawable = null;
        if (bgBitmap != null) {
            bgDrawable = new BitmapDrawable(getResources(), bgBitmap);
        }
        getWindow().setBackgroundDrawable(bgDrawable);
    }

    private void insertEmptyNoteInfo() {
        this.mBackgroundHandler.post(new Runnable() {
            public void run() {
                String title = "";
                String jsonContent = "";
                NewNoteActivity.this.mOldModifyTimeMillis = NewNoteActivity.this.mModifiedTime;
                NoteInfo info = new NoteInfo();
                info.mDateModifiedInMs = NewNoteActivity.this.mModifiedTime;
                final int id = NoteUtils.addNoteData("", "", NewNoteActivity.this.mResolver, info.mDateModifiedInMs, info.mDateReminderInMs, info.mLabel);
                NewNoteActivity.this.mMainHandler.post(new Runnable() {
                    public void run() {
                        NewNoteActivity.this.updateActivityWindowBackground();
                        NewNoteActivity.this.mPreNoteInfo.mId = id;
                        NewNoteActivity.this.mCurrNoteInfo.mId = id;
                        NewNoteActivity.this.mPreNoteInfo.mDateModifiedInMs = NewNoteActivity.this.mModifiedTime;
                        NewNoteActivity.this.mCurrNoteInfo.mDateModifiedInMs = NewNoteActivity.this.mModifiedTime;
                        NewNoteActivity.this.initCurDate();
                        NewNoteActivity.this.initWatcher();
                        NewNoteActivity.this.mContentEditText.setNoteTime(NewNoteActivity.this.mModifiedTime);
                        NewNoteActivity.this.checkSetEmptyState();
                    }
                });
            }
        });
    }

    private void initNoteInfo(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (CAMERA_ACTION.equals(action)) {
            initFromCamera(intent);
        } else if ("android.intent.action.SEND".equals(action)) {
            initFromShare(intent);
        } else {
            initNoteInfo(intent, savedInstanceState);
        }
    }

    private void initNoteInfo(Intent intent, Bundle savedInstanceState) {
        int id = NoteUtils.getIdFromPath(intent.getStringExtra(NOTE_ITEM_PATH));
        if (-1 == id) {
            id = getSaveNoteId(savedInstanceState);
            if (-1 != id) {
                initNoteInfoFromDB(id);
                return;
            } else {
                insertEmptyNoteInfo();
                return;
            }
        }
        initNoteInfoFromDB(id);
    }

    private void initFromCamera(Intent intent) {
        initNoteInfo(intent.getStringExtra(KEY_TITLE), intent.getStringExtra(KEY_CONTENT), Uri.parse(intent.getStringExtra(KEY_PIC_URI)));
    }

    private void initFromShare(Intent intent) {
        initNoteInfo(intent.getStringExtra("android.intent.extra.TITLE"), intent.getStringExtra("android.intent.extra.TEXT"), (Uri) intent.getParcelableExtra("android.intent.extra.STREAM"));
    }

    private void initNoteInfo(final String title, final String content, final Uri picUri) {
        this.mBackgroundHandler.post(new Runnable() {
            public void run() {
                NewNoteActivity.this.insertEmptyNoteInfo();
                NewNoteActivity.this.mMainHandler.post(new Runnable() {
                    public void run() {
                        NewNoteActivity.this.mTitleEditText.setText(title);
                        NewNoteActivity.this.mContentEditText.setText(content);
                        if (picUri != null) {
                            NewNoteActivity.this.asynAddImage(picUri);
                        }
                    }
                });
            }
        });
    }

    private void initData(Bundle savedInstanceState) {
        this.mAttachmentSelector = new AttachmentSelector(this, new OnSoundRecorderCompleteListener() {
            public void onRecorderComplete(String soundPath, int durationInSec) {
                if (NewNoteActivity.this.mContentEditText.isSelectPositionReachMaxSize()) {
                    Toast.makeText(NewNoteActivity.this, R.string.max_content_input_mum_limit, 0).show();
                } else {
                    NewNoteActivity.this.insertSoundRecorder(soundPath, durationInSec);
                }
            }
        }, new OnTakePhotoListener() {
            public void onStartTakePhoto(Uri outputUri) {
                NewNoteActivity.this.mTakePhotoOutputUri = outputUri;
            }
        }, this.mOnSelectPicToAddListener);
        this.mResolver = getContentResolver();
        this.mLabelSelector = new LabelSelector(this, new OnLabelChangedListener() {
            public void onLabelChanged(ArrayList<Integer> ids) {
                NewNoteActivity.this.refreshLabel(ids);
            }

            public void onUpdate() {
                NewNoteActivity.this.showLabel();
            }
        });
        this.mLabelSelector.setYouJuCb(this);
        this.mLabelManager = ((NoteAppImpl) getApplication()).getLabelManager();
        this.mLabelSelector.setLabels(this.mLabelManager.getLabelList());
        this.mLabelDataChangeListener = new LabelDataChangeListener() {
            public void onDataChange() {
                NewNoteActivity.this.mMainHandler.post(new Runnable() {
                    public void run() {
                        NewNoteActivity.this.mLabelSelector.setLabels(NewNoteActivity.this.mLabelManager.getLabelList());
                    }
                });
            }
        };
        this.mLabelManager.addLabelDataChangeListener(this.mLabelDataChangeListener);
        initEditMode(savedInstanceState);
        initNoteInfo(savedInstanceState);
        this.mNoteDelExecutor = new NoteDelExecutor(this);
    }

    private void addImageFromAttach(CopyOnWriteArrayList<String> lists) {
        Iterator i$ = lists.iterator();
        while (i$.hasNext()) {
            String picUri = (String) i$.next();
            if (!NoteUtils.checkEnoughFreeMemory() || this.mContentEditText.isSelectPositionReachMaxSize()) {
                this.mMainHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(NewNoteActivity.this, NewNoteActivity.this.getString(R.string.max_pic_input_mum_limit), 0).show();
                    }
                });
                return;
            }
            addImage(Uri.parse(picUri));
        }
    }

    private void initEditMode(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            this.mIsEditMode = getIntent().getBooleanExtra(ENABLE_EDIT_MODE, false);
            if (this.mIsEditMode) {
                this.mContentEditText.requestFocus();
                return;
            }
            this.mContentEditText.setShowSoftInputOnFocus(false);
            this.mContentEditText.setCursorVisible(false);
            getFooterView().setVisibility(8);
        }
    }

    private void initHandler() {
        this.mMainHandler = new Handler(getMainLooper());
        this.mBackgroundHandler = new Handler(NoteAppImpl.getContext().getSaveNoteDataLooper());
    }

    public void hideSoftInput() {
        if (this.mContentEditText.getShowSoftInputOnFocus()) {
            this.mContentEditText.setShowSoftInputOnFocus(false);
        }
    }

    public void showSoftInput() {
        if (!this.mContentEditText.getShowSoftInputOnFocus()) {
            this.mContentEditText.setShowSoftInputOnFocus(true);
        }
    }

    public void enterEditMode() {
        showSoftInput();
        if (!this.mIsEditMode) {
            this.mIsEditMode = true;
            getFooterView().setVisibility(0);
            this.mContentEditText.setCursorVisible(true);
        }
    }

    private void setFootActionEnable(boolean enable) {
        this.mSelectLabel.setEnabled(enable);
        this.mSelectBill.setEnabled(enable);
        this.mSelectReminder.setEnabled(enable);
        this.mSelectRecord.setEnabled(enable);
        this.mSelectGallery.setEnabled(enable);
        this.mSelectCamera.setEnabled(enable);
        this.mSelectOnlineImage.setEnabled(enable);
    }

    private void addImage(Uri imageUri) {
        Context context = getApplicationContext();
        Uri fileUri = NoteUtils.convertToFileUri(getApplicationContext(), imageUri);
        Bitmap bitmap = NoteUtils.getAddBitmapFromUri(context, fileUri);
        if (bitmap == null) {
            sendMagAddImageFail(getApplicationContext());
            return;
        }
        File thumbFile = NoteUtils.getSaveBitmapFile(context);
        Uri thumbnailUri = Uri.fromFile(thumbFile);
        NoteUtils.saveBitmap(bitmap, thumbFile);
        sendMsgAddImage(thumbnailUri, fileUri, bitmap);
    }

    private void sendMagAddImageFail(final Context context) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, R.string.add_image_fail, 0).show();
            }
        });
    }

    private void sendMsgAddImage(final Uri thumbnailUri, final Uri fileUri, final Bitmap bitmap) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                NewNoteActivity.this.mContentEditText.insertPhoto(thumbnailUri, fileUri, bitmap);
            }
        });
    }

    private void initWatcher() {
        this.mContentEditText.initWatcher(this.mShare, this.mDelete);
        this.mTitleEditText.initWatcher();
    }

    private void startSave() {
        this.mMainHandler.postDelayed(this.mSaveRunnable, NOTE_SAVE_DURATION);
    }

    private void stopSave() {
        this.mMainHandler.removeCallbacks(this.mSaveRunnable);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        WidgetUtil.updateAllWidgets();
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("id", this.mCurrNoteInfo.mId);
        this.mBackgroundHandler.post(new Runnable() {
            public void run() {
                NewNoteActivity.this.saveNote();
            }
        });
        super.onSaveInstanceState(outState);
    }

    protected void onResume() {
        super.onResume();
        if (this.mAITipView != null) {
            this.mAITipView.resume();
        }
        checkTimeChange();
        this.mActive = true;
        this.mIsOnResumed = true;
        startSave();
        this.mNoteDelExecutor.resume();
        StatisticsModule.onResume(this);
    }

    private void checkTimeChange() {
        if (this.mCurDate != null) {
            int[] newCurDate = NoteUtils.getToady();
            if (!NoteUtils.isSomeDay(newCurDate, this.mCurDate)) {
                this.mCurDate = newCurDate;
                updateActivityWindowBackground();
            }
        }
    }

    protected void onPause() {
        this.mActive = false;
        if (this.mAITipView != null) {
            this.mAITipView.pause();
        }
        stopSave();
        this.mNoteDelExecutor.pause();
        StatisticsModule.onPause(this);
        super.onPause();
    }

    protected void onDestroy() {
        clearSpans();
        this.mLabelManager.removeLabelDataChangeListener(this.mLabelDataChangeListener);
        this.mNoteDelExecutor.destroy();
        this.mMainHandler.removeCallbacksAndMessages(null);
        NoteAppImpl.getContext().unRegisterNoteDbInitCompleteNotify(this);
        if (this.mPreviewPictureMakeProxy != null) {
            this.mPreviewPictureMakeProxy.cancel();
        }
        super.onDestroy();
    }

    private void clearSpans() {
        JsonableSpan[] jsonableSpans = (JsonableSpan[]) this.mContentEditText.getText().getSpans(0, this.mContentEditText.length(), JsonableSpan.class);
        if (jsonableSpans != null) {
            for (JsonableSpan span : jsonableSpans) {
                span.recycle();
            }
        }
        this.mContentEditText.getText().clearSpans();
    }

    private void asynAddImage(final Uri uri) {
        NoteAppImpl.getContext().getThreadPool().submit(new Job<Object>() {
            public Object run(JobContext jc) {
                NewNoteActivity.this.addImage(uri);
                return null;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            this.mAttachmentSelector.dismissPicSelectDialog();
            switch (requestCode) {
                case 1:
                    Uri uri = data.getData();
                    if (uri == null) {
                        return;
                    }
                    if (!NoteUtils.checkEnoughFreeMemory() || this.mContentEditText.isSelectPositionReachMaxSize()) {
                        Toast.makeText(this, getString(R.string.max_pic_input_mum_limit), 0).show();
                        return;
                    } else {
                        asynAddImage(uri);
                        return;
                    }
                case 2:
                    if (this.mTakePhotoOutputUri == null) {
                        return;
                    }
                    if (!NoteUtils.checkEnoughFreeMemory() || this.mContentEditText.isSelectPositionReachMaxSize()) {
                        Toast.makeText(this, getString(R.string.max_pic_input_mum_limit), 0).show();
                        return;
                    }
                    asynAddImage(this.mTakePhotoOutputUri);
                    this.mTakePhotoOutputUri = null;
                    return;
                case 3:
                    if (!this.mContentEditText.isSelectPositionReachMaxSize()) {
                        this.mLabelSelector.updateLabelList(this.mCurrNoteInfo.mLabel);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void onBackPressed() {
        onBack();
        super.onBackPressed();
    }

    private void onBack() {
        stopSave();
        exitNote();
    }

    private boolean isNoteChanged() {
        UpdateHelper uh = new UpdateHelper();
        this.mPreNoteInfo.mId = uh.update(this.mPreNoteInfo.mId, this.mCurrNoteInfo.mId);
        this.mPreNoteInfo.mLabel = uh.update(this.mPreNoteInfo.mLabel, this.mCurrNoteInfo.mLabel);
        this.mPreNoteInfo.mDateReminderInMs = uh.update(this.mPreNoteInfo.mDateReminderInMs, this.mCurrNoteInfo.mDateReminderInMs);
        return uh.isUpdated() || this.mTitleEditText.getAndResetTextChanged() || this.mContentEditText.getAndResetTextChanged() || isContentChangeFromOut();
    }

    private boolean isContentChangeFromOut() {
        String action = getIntent().getAction();
        if (TextUtils.isEmpty(action)) {
            return false;
        }
        if (CAMERA_ACTION.equals(action) || "android.intent.action.SEND".equals(action)) {
            return true;
        }
        return false;
    }

    private void deleteEmptyNote() {
        final int noteId = this.mCurrNoteInfo.mId;
        if (noteId != -1) {
            this.mBackgroundHandler.post(new Runnable() {
                public void run() {
                    String[] selectionArgs = new String[]{String.valueOf(noteId)};
                    NewNoteActivity.this.mResolver.delete(NoteContent.CONTENT_URI, "_id=?", selectionArgs);
                    WidgetUtil.updateAllWidgets();
                }
            });
        }
    }

    private boolean isShouldDelete() {
        if (PlatformUtil.isGioneeDevice() && !NoteShareDataManager.getHasShowDataFlowHint(getApplicationContext())) {
            return true;
        }
        boolean labelEmpty = this.mLabelSelector.isLabelInvalid(this.mCurrNoteInfo.mLabel);
        boolean titleEmpty = TextUtils.isEmpty(this.mTitleEditText.getText().toString().trim());
        boolean contentEmpty = NoteUtils.isContentEmpty(this.mContentEditText.getText());
        boolean reminderEmpty;
        if (this.mCurrNoteInfo.mDateReminderInMs == 0) {
            reminderEmpty = true;
        } else {
            reminderEmpty = false;
        }
        if (labelEmpty && titleEmpty && contentEmpty && reminderEmpty) {
            return true;
        }
        return false;
    }

    private void saveNote() {
        if (isNoteChanged()) {
            final int noteId = this.mCurrNoteInfo.mId;
            final String title = this.mTitleEditText.getText().toString();
            Editable content = this.mContentEditText.getText();
            final String jsonContent = NoteUtils.isContentEmpty(content) ? "" : DataConvert.editableConvertToJson(content);
            final long modifiedTime = this.mModifiedTime;
            final long dateReminderInMs = this.mCurrNoteInfo.mDateReminderInMs;
            final ArrayList<Integer> label = new ArrayList(this.mCurrNoteInfo.mLabel);
            final ContentResolver resolver = this.mResolver;
            if (noteId != -1) {
                this.mBackgroundHandler.post(new Runnable() {
                    public void run() {
                        NoteUtils.updateNoteData(title, jsonContent, resolver, noteId, modifiedTime, dateReminderInMs, label);
                    }
                });
                return;
            }
            final String str = title;
            final String str2 = jsonContent;
            final ContentResolver contentResolver = resolver;
            final long j = modifiedTime;
            final long j2 = dateReminderInMs;
            final ArrayList<Integer> arrayList = label;
            this.mBackgroundHandler.post(new Runnable() {
                public void run() {
                    int id = NoteUtils.addNoteData(str, str2, contentResolver, j, j2, arrayList);
                    NewNoteActivity.this.mPreNoteInfo.mId = id;
                    NewNoteActivity.this.mCurrNoteInfo.mId = id;
                }
            });
        }
    }

    private void exitNote() {
        if (isShouldDelete()) {
            deleteEmptyNote();
            return;
        }
        saveNote();
        if (this.mCurrNoteInfo.mDateReminderInMs != 0) {
            this.mBackgroundHandler.post(new Runnable() {
                public void run() {
                    ReminderManager.setReminder(NoteAppImpl.getContext(), (long) NewNoteActivity.this.mCurrNoteInfo.mId, NewNoteActivity.this.mCurrNoteInfo.mDateReminderInMs);
                }
            });
        }
    }

    private void insertSoundRecorder(String soundPath, int durationInSec) {
        this.mContentEditText.insertSound(soundPath, durationInSec);
    }

    private void deleteNote() {
        if (this.mCurrNoteInfo.mId != -1) {
            this.mResolver.delete(NoteContent.CONTENT_URI, "_id=?", new String[]{String.valueOf(this.mCurrNoteInfo.mId)});
            WidgetUtil.updateAllWidgets();
        }
        NoteUtils.deleteOriginMediaFile(DataConvert.editableConvertToJson(this.mContentEditText.getText()));
    }

    private void selectDelete() {
        this.mNoteDelExecutor.startAction((long) this.mCurrNoteInfo.mId, new NoteDelListener() {
            public void onDelPrepare() {
                NewNoteActivity.this.stopSave();
            }

            public int onDelInvalidId() {
                NewNoteActivity.this.deleteNote();
                return 1;
            }

            public void onDelFinish(int success, int fail) {
                if (success > 0) {
                    WidgetUtil.updateAllWidgets();
                    if (VERSION.SDK_INT >= 21) {
                        NewNoteActivity.this.finishAndRemoveTask();
                    } else {
                        NewNoteActivity.this.finish();
                    }
                }
            }
        });
    }

    public Handler getBackgroundHandler() {
        return this.mBackgroundHandler;
    }

    public Handler getMainHandler() {
        return this.mMainHandler;
    }

    private void selectLabel() {
        hideSoftInput();
        this.mLabelSelector.selectLabel(this.mCurrNoteInfo.mLabel);
    }

    private void selectBill() {
        this.mContentEditText.toggleBillItem();
    }

    private void selectReminder() {
        hideSoftInput();
        if (!this.mIsShowDateTimeDilaog) {
            Dialog dialog = new DateTimeDialog(this, R.style.DialogTheme, this.mCurrNoteInfo.mDateReminderInMs, new AlarmSetListener());
            dialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    NewNoteActivity.this.mIsShowDateTimeDilaog = false;
                }
            });
            this.mIsShowDateTimeDilaog = true;
            dialog.show();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_bill:
                selectBill();
                youjuStatistics(R.string.youju_add_bill);
                return;
            case R.id.action_camera:
                youjuStatistics(R.string.youju_call_camera);
                this.mAttachmentSelector.gotoTakePhotos();
                return;
            case R.id.action_gallery:
                youjuStatistics(R.string.youju_add_picture);
                this.mAttachmentSelector.gotoSelectImage();
                return;
            case R.id.action_label:
                selectLabel();
                youjuStatistics(R.string.youju_click_tag);
                return;
            case R.id.action_online_image:
                youjuStatistics(R.string.youju_go_online_picture);
                gotoOnlineImage();
                return;
            case R.id.action_recorde:
                youjuStatistics(R.string.youju_add_record);
                this.mAttachmentSelector.gotoRecordSound();
                return;
            case R.id.action_reminder:
                selectReminder();
                youjuStatistics(R.string.youju_add_alarm);
                return;
            case R.id.new_note_activity_title_layout_back:
                onBack();
                finish();
                return;
            case R.id.new_note_activity_title_layout_delete:
                selectDelete();
                youjuStatistics(R.string.youju_edit_page_del);
                return;
            case R.id.new_note_activity_title_layout_share:
                youjuStatistics(R.string.youju_share);
                PreviewPictureMakeProxy proxy = new PreviewPictureMakeProxy(this);
                this.mPreviewPictureMakeProxy = proxy;
                proxy.createPreviewPicture(this.mTitleEditText, this.mContentEditText, new EffectUtil(System.currentTimeMillis()).getEffect(this.mOldModifyTimeMillis));
                return;
            default:
                return;
        }
    }

    private void gotoOnlineImage() {
        Intent intent = new Intent();
        intent.setClass(this, OnlineImageActivity.class);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.i(TAG, "ActivityNotFoundException e = " + e);
        }
    }

    public void onEvent(int eventId) {
        youjuStatistics(eventId);
    }

    private void youjuStatistics(int stringId) {
        StatisticsModule.onEvent((Context) this, getResources().getString(stringId));
    }

    public void onLabelEvent(int eventId, String label) {
        StatisticsModule.onEvent(this, getResources().getString(eventId), label);
    }

    public void onNoteDbInitComplete() {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                if (!NewNoteActivity.this.mNoteInfoInitSuccess) {
                    NewNoteActivity.this.initNoteInfoFromDB(NoteUtils.getIdFromPath(NewNoteActivity.this.getIntent().getStringExtra(NewNoteActivity.NOTE_ITEM_PATH)));
                }
            }
        });
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mTitleEditText != null) {
            if (this.mTitleEditText.isFocused()) {
                this.mTitleEditText.setHint(R.string.title_focus_hint);
            } else {
                this.mTitleEditText.setHint(R.string.title_hint);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    private void showDataFlowHint(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.data_flow_dialog, null);
        Builder builder = new Builder(context);
        builder.setTitle((int) R.string.alert_user_title_str);
        builder.setView(view);
        builder.setPositiveButton((int) R.string.alert_user_ok, this.mDataFlowSureListener);
        builder.setNegativeButton((int) R.string.alert_user_cancle, this.mDataFlowCancleListener);
        AmigoAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showDataFlowHint() {
        if (!NoteShareDataManager.getHasShowDataFlowHint(getApplicationContext()) && PlatformUtil.isGioneeDevice()) {
            showDataFlowHint(this);
        }
    }
}
