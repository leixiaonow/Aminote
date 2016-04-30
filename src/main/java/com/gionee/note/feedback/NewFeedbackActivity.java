package com.gionee.note.feedback;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.feedback.db.IDraftProvider;
import com.gionee.feedback.db.ProviderFactory;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.logic.SendState;
import com.gionee.feedback.logic.vo.DraftInfo;
import com.gionee.feedback.logic.vo.Message.Builder;
import com.gionee.feedback.logic.vo.Message.Callback;
import com.gionee.feedback.logic.vo.ResultCode;
import com.gionee.feedback.utils.BitmapUtils;
import com.gionee.note.app.view.StandardActivity;
import com.gionee.note.app.view.StandardActivity.StandardAListener;
import com.gionee.note.common.NoteUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NewFeedbackActivity extends StandardActivity implements StandardAListener {
    private static final String ACTION_SELECT_ATTACH = "android.intent.action.GET_CONTENT";
    private static final boolean DEBUG = false;
    private static final long MAX_ATTACHS_LENGTH = 26214400;
    private static final int MAX_CONTACT_LENGTH = 40;
    private static final int MAX_MESSAGE_LENGTH = 800;
    private static final int MSG_LOAD_ATTACH = 3;
    private static final int MSG_LOAD_ATTACH_FAIL = 4;
    private static final int MSG_LOAD_ATTACH_FAIL_MAX = 5;
    private static final int MSG_LOAD_ATTACH_FINISH = 6;
    private static final int MSG_LOAD_DRAFT_FINISH = 2;
    private static final int MSG_LOAD_DRAFT_START = 1;
    private static final int MSG_QUIT = 8;
    private static final int MSG_SAVE_DRAFT = 7;
    private static final int REQUEST_CODE_DELETE_ATTACH = 1002;
    private static final int REQUEST_CODE_IMAGE = 1001;
    private static final String TAG = "NewFeedbackActivity";
    private AddAttachViewClickListener mAddAttachClickListener = new AddAttachViewClickListener() {
        public void onAddAttachViewClick() {
            try {
                Intent intent = new Intent(NewFeedbackActivity.ACTION_SELECT_ATTACH);
                intent.setType("image/*");
                NewFeedbackActivity.this.startActivityForResult(Intent.createChooser(intent, null), NewFeedbackActivity.REQUEST_CODE_IMAGE);
            } catch (ActivityNotFoundException e) {
                Log.w(NewFeedbackActivity.TAG, "error", e);
            }
        }
    };
    private AddAttachImageView mAddAttachImageView;
    private boolean mAddAttaching;
    private AttachViewClickListener mAttachClickListener = new AttachViewClickListener() {
        public void onAttachViewClick(int position) {
            Intent intent = new Intent(NewFeedbackActivity.this, AttachmentManagerActivity.class);
            intent.putExtra(AttachmentManagerActivity.SHOW_ATTACH_ITEM_INDEX, position);
            intent.putStringArrayListExtra(AttachmentManagerActivity.SHOW_ATTACH_PATHS, NewFeedbackActivity.this.mAttachs);
            NewFeedbackActivity.this.startActivityForResult(intent, NewFeedbackActivity.REQUEST_CODE_DELETE_ATTACH);
        }
    };
    private int mAttachItemWidth;
    private final ArrayList<String> mAttachs = new ArrayList(5);
    private EditText mContactEditText;
    private DataManager mDataManager;
    private IDraftProvider mDraftProvider;
    private boolean mLoadDraftFinish;
    private Handler mMainThreadHandler;
    private EditText mMessageEditText;
    private LinearLayout mSendButton;
    private TextView mSendButtonText;
    private Callback mSendCallback = new Callback() {
        public void onResult(ResultCode resultCode) {
            if (!NewFeedbackActivity.this.isLeaveActivity()) {
                if (resultCode == ResultCode.CODE_SEND_SUCESSFUL) {
                    NewFeedbackActivity.this.clearFeedback();
                    NewFeedbackActivity.this.mDataManager.resetSendState();
                    NewFeedbackActivity.this.gotoRecord();
                    NewFeedbackActivity.this.showToast(R.string.gn_fb_string_send_success);
                }
                NewFeedbackActivity.this.updateView();
                NewFeedbackActivity.this.showError(resultCode);
            }
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            if (NewFeedbackActivity.this.isSendFailedState()) {
                NewFeedbackActivity.this.mDataManager.resetSendState();
                NewFeedbackActivity.this.updateButtonState();
            }
            NewFeedbackActivity.this.updateSendButtonEnable(editable.toString());
        }
    };
    private HandlerThread mWorkThread;
    private Handler mWorkThreadHandler;

    private static class AddAttachEntry {
        public String mAddUri;
        public ArrayList<String> mAttachPaths;

        private AddAttachEntry() {
        }
    }

    private static class AttachEntry {
        public Bitmap mBitmap;
        public String mPath;

        private AttachEntry() {
        }
    }

    private static class CharacterFilter implements InputFilter {
        private CharacterFilter() {
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String input = source.toString();
            StringBuilder sb = null;
            for (int i = 0; i < input.length(); i++) {
                char codePoint = input.charAt(i);
                if (NoteUtils.isEmojiCharacter(codePoint)) {
                    if (sb == null) {
                        sb = new StringBuilder();
                        sb.append(input.substring(0, i));
                    }
                } else if (sb != null) {
                    sb.append(codePoint);
                }
            }
            if (sb == null) {
                return null;
            }
            return sb.toString();
        }
    }

    private static class DraftEntry {
        public ArrayList<String> mAttachPaths;
        public String mContact;
        public String mContent;

        private DraftEntry() {
        }
    }

    private static class FeedbackEntry {
        public ArrayList<Bitmap> mAttachBitmaps;
        public ArrayList<String> mAttachPaths;
        public String mContact;
        public String mContent;

        private FeedbackEntry() {
        }
    }

    private java.lang.String getPath(android.net.Uri r11) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:79)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
*/
        /*
        r10 = this;
        r3 = 0;
        r9 = "";
        r0 = 1;
        r2 = new java.lang.String[r0];
        r0 = 0;
        r1 = "_data";
        r2[r0] = r1;
        r0 = r10.getContentResolver();
        r1 = r11;
        r4 = r3;
        r5 = r3;
        r7 = r0.query(r1, r2, r3, r4, r5);
        if (r7 == 0) goto L_0x001e;
    L_0x0018:
        r0 = r7.getCount();	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        if (r0 != 0) goto L_0x0024;
    L_0x001e:
        if (r7 == 0) goto L_0x0023;
    L_0x0020:
        r7.close();
    L_0x0023:
        return r3;
    L_0x0024:
        r7.moveToFirst();	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        r0 = "_data";	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        r6 = r7.getColumnIndexOrThrow(r0);	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        r9 = r7.getString(r6);	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        if (r7 == 0) goto L_0x0036;
    L_0x0033:
        r7.close();
    L_0x0036:
        r3 = r9;
        goto L_0x0023;
    L_0x0038:
        r8 = move-exception;
        r0 = "NewFeedbackActivity";	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        r1 = "error";	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        android.util.Log.w(r0, r1, r8);	 Catch:{ Exception -> 0x0038, all -> 0x0046 }
        if (r7 == 0) goto L_0x0023;
    L_0x0042:
        r7.close();
        goto L_0x0023;
    L_0x0046:
        r0 = move-exception;
        if (r7 == 0) goto L_0x004c;
    L_0x0049:
        r7.close();
    L_0x004c:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.note.feedback.NewFeedbackActivity.getPath(android.net.Uri):java.lang.String");
    }

    public void onClickHomeBack() {
        finish();
    }

    public void onClickRightView() {
        gotoRecord();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleAndRightTextView(R.string.fb_new_feedback_activity_title, R.string.fb_feedback_record_activity_title);
        setStandardAListener(this);
        setNoteContentView(R.layout.fb_new_feedback_content_ly);
        initView();
        initData();
        startLoadDraft();
    }

    private void startLoadDraft() {
        this.mWorkThreadHandler.sendEmptyMessage(1);
    }

    private void initView() {
        this.mMessageEditText = (EditText) findViewById(R.id.gn_fb_id_messageEditText);
        this.mMessageEditText.addTextChangedListener(this.mTextWatcher);
        this.mMessageEditText.setFilters(new InputFilter[]{new CharacterFilter()});
        this.mContactEditText = (EditText) findViewById(R.id.gn_fb_id_contactEditText);
        this.mSendButton = (LinearLayout) findViewById(R.id.gn_fb_id_sendButton);
        this.mSendButtonText = (TextView) findViewById(R.id.gn_fb_id_sendButton_expendtextView);
        this.mSendButtonText.setOnClickListener(this);
        this.mAddAttachImageView = (AddAttachImageView) findViewById(R.id.gn_fb_id_addAttachImabeView);
        this.mAddAttachImageView.setOnAddAttachViewClickListener(this.mAddAttachClickListener);
        this.mAddAttachImageView.setOnAttachViewClickListener(this.mAttachClickListener);
    }

    private void initData() {
        this.mDataManager = DataManager.getInstance(this);
        this.mDraftProvider = ProviderFactory.draftProvider(this);
        this.mAttachItemWidth = getResources().getDimensionPixelSize(R.dimen.gn_fb_dimen_attach_border_size);
        this.mMainThreadHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 2:
                        NewFeedbackActivity.this.loadDraftFinish((FeedbackEntry) msg.obj);
                        return;
                    case 4:
                        NewFeedbackActivity.this.resetAddAttachState();
                        return;
                    case 5:
                        NewFeedbackActivity.this.resetAddAttachState();
                        NewFeedbackActivity.this.showToast(R.string.gn_fb_string_attach_max_length);
                        return;
                    case 6:
                        NewFeedbackActivity.this.resetAddAttachState();
                        NewFeedbackActivity.this.addAttachFinish((AttachEntry) msg.obj);
                        return;
                    case 8:
                        NewFeedbackActivity.this.mWorkThread.quit();
                        return;
                    default:
                        return;
                }
            }
        };
        HandlerThread workThread = new HandlerThread("Feedback Work Thread");
        workThread.start();
        this.mWorkThread = workThread;
        this.mWorkThreadHandler = new Handler(workThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        NewFeedbackActivity.this.loadDraftStart();
                        return;
                    case 3:
                        NewFeedbackActivity.this.loadAttach((AddAttachEntry) msg.obj);
                        return;
                    case 7:
                        NewFeedbackActivity.this.saveDraft((DraftEntry) msg.obj);
                        NewFeedbackActivity.this.mMainThreadHandler.sendEmptyMessage(8);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void loadDraftStart() {
        DraftInfo draftInfo = (DraftInfo) this.mDraftProvider.queryHead();
        FeedbackEntry entry = null;
        if (draftInfo != null) {
            entry = getFeedbackEntry(draftInfo);
        }
        this.mMainThreadHandler.sendMessage(this.mMainThreadHandler.obtainMessage(2, entry));
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gn_fb_id_sendButton_expendtextView:
                sendMessage();
                return;
            default:
                return;
        }
    }

    private void loadDraftFinish(FeedbackEntry entry) {
        this.mLoadDraftFinish = true;
        String content = null;
        if (entry != null) {
            content = entry.mContent;
            this.mMessageEditText.setText(content);
            this.mContactEditText.setText(entry.mContact);
            ArrayList<String> attachPaths = entry.mAttachPaths;
            ArrayList<Bitmap> attachBitmaps = entry.mAttachBitmaps;
            if (attachPaths != null) {
                this.mAttachs.addAll(attachPaths);
                int size = attachBitmaps.size();
                for (int i = 0; i < size; i++) {
                    this.mAddAttachImageView.addAttach((Bitmap) attachBitmaps.get(i));
                }
            }
        }
        updateSendButtonEnable(content);
    }

    private void addAttachFinish(AttachEntry entry) {
        if (entry != null) {
            this.mAttachs.add(entry.mPath);
            this.mAddAttachImageView.addAttach(entry.mBitmap);
        }
    }

    private void resetAddAttachState() {
        this.mAddAttaching = false;
    }

    private long getCurAttachsTotalSize(ArrayList<String> attachs) {
        long totalSize = 0;
        Iterator i$ = attachs.iterator();
        while (i$.hasNext()) {
            String path = getPath(Uri.parse((String) i$.next()));
            if (!TextUtils.isEmpty(path)) {
                totalSize += new File(path).length();
            }
        }
        return totalSize;
    }

    private void loadAttach(AddAttachEntry entry) {
        long size = 0 + getCurAttachsTotalSize(entry.mAttachPaths);
        String uri = entry.mAddUri;
        String path = NoteUtils.convertToFileUri(this, Uri.parse(uri)).toString();
        if (TextUtils.isEmpty(path)) {
            this.mMainThreadHandler.sendEmptyMessage(4);
        } else if (size + new File(path).length() > MAX_ATTACHS_LENGTH) {
            this.mMainThreadHandler.sendEmptyMessage(5);
        } else {
            AttachEntry attachEntry = null;
            if (uri != null) {
                Bitmap bitmap = getAttachItemBitmap(uri);
                if (bitmap != null) {
                    attachEntry = new AttachEntry();
                    attachEntry.mPath = uri;
                    attachEntry.mBitmap = bitmap;
                }
            }
            this.mMainThreadHandler.sendMessage(this.mMainThreadHandler.obtainMessage(6, attachEntry));
        }
    }

    private FeedbackEntry getFeedbackEntry(DraftInfo draftInfo) {
        String content = draftInfo.getContentText();
        String contact = draftInfo.getContactText();
        List<String> attachs = draftInfo.getAttachTextArray();
        FeedbackEntry entry = new FeedbackEntry();
        entry.mContent = content;
        entry.mContact = contact;
        if (!(attachs == null || attachs.isEmpty())) {
            ArrayList<String> attachPaths = new ArrayList();
            ArrayList<Bitmap> attachBitmaps = new ArrayList();
            for (String uri : attachs) {
                if (!TextUtils.isEmpty(uri)) {
                    Bitmap bitmap = getAttachItemBitmap(uri);
                    if (bitmap != null) {
                        attachPaths.add(uri);
                        attachBitmaps.add(bitmap);
                    }
                }
            }
            if (attachPaths.size() > 0) {
                entry.mAttachPaths = attachPaths;
                entry.mAttachBitmaps = attachBitmaps;
            }
        }
        return entry;
    }

    private Bitmap getAttachItemBitmap(String uri) {
        try {
            return ThumbnailUtils.extractThumbnail(BitmapUtils.decodeSampledBitmapFromUri(this, Uri.parse(uri)), this.mAttachItemWidth, this.mAttachItemWidth);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "error", e);
            return null;
        } catch (OutOfMemoryError e2) {
            Log.w(TAG, "error", e2);
            return null;
        }
    }

    private void sendMessage() {
        String message = this.mMessageEditText.getText().toString().trim();
        String contact = this.mContactEditText.getText().toString().trim();
        if (checkMessage(message) && checkContact(contact)) {
            com.gionee.feedback.logic.vo.Message mess = new Builder().setMessage(message).setContact(contact).setCallback(this.mSendCallback).setAttachs(this.mAttachs).builder();
            this.mDataManager.sendMessage(mess);
            updateButtonState();
        }
    }

    private boolean checkContact(String contact) {
        if (contact.length() <= 40) {
            return true;
        }
        showToast(R.string.gn_fb_string_contact_beyond_max);
        return false;
    }

    private boolean checkMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            showToast(R.string.gn_fb_string_message_not_null);
            return false;
        } else if (message.length() <= MAX_MESSAGE_LENGTH) {
            return true;
        } else {
            showToast(R.string.gn_fb_string_message_beyond_max);
            return false;
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        saveDraft();
        super.onDestroy();
        this.mMessageEditText.removeTextChangedListener(this.mTextWatcher);
    }

    private void saveDraft() {
        if (this.mLoadDraftFinish) {
            DraftEntry entry = new DraftEntry();
            entry.mContact = this.mContactEditText.getText().toString();
            entry.mContent = this.mMessageEditText.getText().toString();
            entry.mAttachPaths = new ArrayList(this.mAttachs);
            this.mWorkThreadHandler.sendMessage(this.mWorkThreadHandler.obtainMessage(7, entry));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    addAttachRequest(uri);
                }
            }
        } else if (requestCode == REQUEST_CODE_DELETE_ATTACH) {
            ArrayList<String> paths;
            if (data != null) {
                paths = data.getStringArrayListExtra(AttachmentManagerActivity.SHOW_ATTACH_PATHS);
            } else {
                paths = new ArrayList(1);
            }
            updateAddAttachImageView(paths);
        }
    }

    private void updateAddAttachImageView(ArrayList<String> paths) {
        ArrayList<String> attachs = this.mAttachs;
        ArrayList<View> delViews = new ArrayList();
        ArrayList<String> delAttachs = new ArrayList();
        int size = attachs.size();
        for (int i = 0; i < size; i++) {
            String path = (String) attachs.get(i);
            if (!isExist(path, paths)) {
                delAttachs.add(path);
                delViews.add(this.mAddAttachImageView.getChildAt(i));
            }
        }
        attachs.removeAll(delAttachs);
        Iterator i$ = delViews.iterator();
        while (i$.hasNext()) {
            this.mAddAttachImageView.removeAttach((ImageView) ((View) i$.next()));
        }
        delAttachs.clear();
        delViews.clear();
    }

    private boolean isExist(String path, ArrayList<String> list) {
        Iterator i$ = list.iterator();
        while (i$.hasNext()) {
            if (path.trim().equals(((String) i$.next()).trim())) {
                return true;
            }
        }
        return false;
    }

    private void addAttachRequest(Uri uri) {
        ArrayList<String> attachs = this.mAttachs;
        if (isExist(uri.toString(), attachs)) {
            showToast(R.string.fb_some_attach_toast_message);
            return;
        }
        this.mAddAttaching = true;
        AddAttachEntry entry = new AddAttachEntry();
        entry.mAddUri = uri.toString();
        entry.mAttachPaths = new ArrayList(attachs);
        this.mWorkThreadHandler.sendMessage(this.mWorkThreadHandler.obtainMessage(3, entry));
    }

    private void saveDraft(DraftEntry entry) {
        DraftInfo draftInfo = (DraftInfo) this.mDraftProvider.queryHead();
        if (draftInfo != null) {
            draftInfo.setContentText(entry.mContent);
            draftInfo.setContactText(entry.mContact);
            draftInfo.setAttachTextArray(entry.mAttachPaths);
            this.mDraftProvider.update(draftInfo);
            return;
        }
        draftInfo = new DraftInfo();
        draftInfo.setContentText(entry.mContent);
        draftInfo.setContactText(entry.mContact);
        draftInfo.setAttachTextArray(entry.mAttachPaths);
        this.mDraftProvider.insert(draftInfo);
    }

    private void enableEditMode(boolean enable) {
        this.mMessageEditText.setEnabled(enable);
        this.mContactEditText.setEnabled(enable);
        this.mAddAttachImageView.setEnabled(enable);
        updateSendButtonEnable(this.mMessageEditText.getText().toString());
    }

    private void updateButtonState() {
        SendState sendState = this.mDataManager.getCurSendState();
        int buttonTextID = R.string.gn_fb_string_send;
        int bgColorId = R.color.fb_new_feedback_send_btn_normal_bg_color;
        switch (sendState) {
            case INITIAL:
            case SEND_SUCCESS:
                buttonTextID = R.string.gn_fb_string_send;
                bgColorId = R.color.fb_new_feedback_send_btn_normal_bg_color;
                enableEditMode(true);
                break;
            case SENDING:
                buttonTextID = R.string.gn_fb_string_sending;
                bgColorId = R.color.fb_new_feedback_send_btn_normal_bg_color;
                enableEditMode(false);
                break;
            case SEND_FAILED:
                buttonTextID = R.string.gn_fb_string_send_failed;
                bgColorId = R.color.fb_new_feedback_send_btn_fail_bg_color;
                enableEditMode(true);
                break;
        }
        this.mSendButtonText.setText(buttonTextID);
        this.mSendButton.setBackgroundColor(getResources().getColor(bgColorId));
    }

    private void updateSendButtonEnable(String message) {
        if (TextUtils.isEmpty(message)) {
            this.mSendButton.setEnabled(false);
            this.mSendButton.setBackgroundColor(getResources().getColor(R.color.fb_new_feedback_send_btn_no_enable_bg_color));
            this.mSendButtonText.setTextColor(getResources().getColor(R.color.fb_new_feedback_send_btn_text_no_enable_color));
            return;
        }
        this.mSendButton.setEnabled(true);
        this.mSendButton.setBackgroundColor(getResources().getColor(R.color.fb_new_feedback_send_btn_normal_bg_color));
        this.mSendButtonText.setTextColor(getResources().getColor(R.color.fb_new_feedback_send_btn_text_color));
    }

    private void showError(ResultCode resultCode) {
        if (ResultCode.CODE_NETWORK_DISCONNECTED.value() == resultCode.value()) {
            showToast(R.string.gn_fb_string_message_network_disconnected);
        } else if (ResultCode.CODE_NETWORK_UNAVAILABLE.value() == resultCode.value()) {
            showToast(R.string.fb_fb_string_message_network_exception);
        }
    }

    private void clearFeedback() {
        this.mMessageEditText.setText(null);
        this.mContactEditText.setText(null);
        this.mAttachs.clear();
        this.mAddAttachImageView.removeAttach();
    }

    private boolean isSendFailedState() {
        return this.mDataManager.getCurSendState() == SendState.SEND_FAILED;
    }

    private boolean isLeaveActivity() {
        return isFinishing() || isDestroyed();
    }

    private void gotoRecord() {
        Intent intent = new Intent(this, FeedbackRecordActivity.class);
        intent.setFlags(335544320);
        startActivity(intent);
    }

    private void updateView() {
        updateButtonState();
    }

    private void showToast(int toast) {
        Toast.makeText(this, toast, 0).show();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!this.mLoadDraftFinish || this.mAddAttaching) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }
}
