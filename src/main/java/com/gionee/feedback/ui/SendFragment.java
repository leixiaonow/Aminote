package com.gionee.feedback.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.gionee.feedback.db.IDraftProvider;
import com.gionee.feedback.db.ProviderFactory;
import com.gionee.feedback.logic.SendState;
import com.gionee.feedback.logic.vo.DraftInfo;
import com.gionee.feedback.logic.vo.Message;
import com.gionee.feedback.logic.vo.Message.Builder;
import com.gionee.feedback.logic.vo.Message.Callback;
import com.gionee.feedback.logic.vo.ResultCode;
import com.gionee.feedback.utils.BitmapUtils;
import com.gionee.feedback.utils.Log;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.res.Color;
import com.gionee.res.Drawable;
import com.gionee.res.Layout;
import com.gionee.res.ResourceNotFoundException;
import com.gionee.res.Text;
import com.gionee.res.Widget;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class SendFragment extends BaseFragment {
    private static final long MAX_ATTACHS_LENGTH = 26214400;
    private static final int MAX_CONTACT_LENGTH = 40;
    private static final int MAX_MESSAGE_LENGTH = 800;
    private static final int REQUEST_CODE_DELETE_ATTACH = 1002;
    private static final int REQUEST_CODE_IMAGE = 1001;
    private static final String TAG = "SendFragment";
    private boolean isContentChange = false;
    private AddAttachViewClickListener mAddAttachClickListener = new AddAttachViewClickListener() {
        public void onAddAttachViewClick() {
            Intent intent = new Intent("android.intent.action.GET_CONTENT");
            intent.setType("image/*");
            SendFragment.this.startActivityForResult(Intent.createChooser(intent, null), SendFragment.REQUEST_CODE_IMAGE);
        }
    };
    private AddAttachImageView mAddAttachImageView;
    private AttachViewClickListener mAttachClickListener = new AttachViewClickListener() {
        public void onAttachViewClick(int position) {
            Intent intent = new Intent(SendFragment.this.mActivity, DeleteAttachActivity.class);
            intent.putExtra(DeleteAttachActivity.SHOW_ITEM, position);
            SendFragment.this.startActivityForResult(intent, SendFragment.REQUEST_CODE_DELETE_ATTACH);
        }
    };
    private final List<String> mAttachs = new ArrayList(10);
    private EditText mContactEditText;
    private IDraftProvider mDraftProvider;
    private EditText mMessagEditText;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            try {
                if (id == Widget.gn_fb_id_sendButton.getIdentifier(SendFragment.this.mActivity)) {
                    sendMessage();
                } else if (id == Widget.gn_fb_id_backmenu.getIdentifier(SendFragment.this.mActivity)) {
                    onBackClick();
                } else if (id == Widget.gn_fb_id_historymenu.getIdentifier(SendFragment.this.mActivity)) {
                    SendFragment.this.gotoRecord();
                }
            } catch (ResourceNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void sendMessage() {
            String message = SendFragment.this.mMessagEditText.getText().toString().trim();
            String contact = SendFragment.this.mContactEditText.getText().toString().trim();
            if (checkMessage(message) && checkContact(contact)) {
                Message mess = new Builder().setMessage(message).setContact(contact).setCallback(SendFragment.this.mSendCallback).setAttachs(SendFragment.this.mAttachs).builder();
                SendFragment.this.mDataManager.sendMessage(mess);
                SendFragment.this.updateButtonState();
            }
        }

        private boolean checkContact(String contact) {
            if (contact.length() <= 40) {
                return true;
            }
            SendFragment.this.showToast(Text.gn_fb_string_contact_beyond_max.getIdentifier(SendFragment.this.mActivity));
            return false;
        }

        private boolean checkMessage(String message) {
            Log.d(SendFragment.TAG, "checkMessage ->" + message);
            if (TextUtils.isEmpty(message)) {
                SendFragment.this.showToast(Text.gn_fb_string_message_not_null.getIdentifier(SendFragment.this.mActivity));
                return false;
            } else if (message.length() <= SendFragment.MAX_MESSAGE_LENGTH) {
                return true;
            } else {
                SendFragment.this.showToast(Text.gn_fb_string_message_beyond_max.getIdentifier(SendFragment.this.mActivity));
                return false;
            }
        }

        private void onBackClick() {
            SendFragment.this.mActivity.finish();
        }
    };
    private LinearLayout mSendButton;
    private ExpendTextView mSendButtonText;
    protected Callback mSendCallback = new Callback() {
        public void onResult(ResultCode resultCode) {
            Log.d(SendFragment.TAG, "SendCallback = " + resultCode.value());
            if (SendFragment.this.isAttach) {
                if (resultCode == ResultCode.CODE_SEND_SUCESSFUL) {
                    SendFragment.this.clearSendMessage();
                    SendFragment.this.mDataManager.resetSendState();
                    SendFragment.this.gotoRecord();
                    SendFragment.this.showToast(Text.gn_fb_string_send_success.getIdentifier(SendFragment.this.mActivity));
                }
                SendFragment.this.updateView();
                SendFragment.this.showError(resultCode);
            }
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            boolean sendFailedSateChange = SendFragment.this.isSendFailedSateChange(editable);
            Log.d(SendFragment.TAG, "sendFailedSateChange:" + sendFailedSateChange);
            if (sendFailedSateChange) {
                SendFragment.this.mDataManager.resetSendState();
                SendFragment.this.updateButtonState();
            }
            if (SendFragment.this.isContentChange) {
                SendFragment.this.updateSendButtonEnable(editable.toString());
            }
        }
    };

    public static SendFragment getInstance(State send) {
        SendFragment sendFragment = new SendFragment();
        sendFragment.mState = send;
        return sendFragment;
    }

    protected View creatView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(Layout.gn_fb_layout_send.getIdentifier(getActivity()), container, false);
    }

    protected void initData() {
        this.mDraftProvider = ProviderFactory.draftProvider(this.mActivity);
    }

    protected void initView() {
        try {
            this.mMessagEditText = (EditText) getView(Widget.gn_fb_id_messageEditText.getIdentifier(this.mActivity));
            this.mMessagEditText.addTextChangedListener(this.mTextWatcher);
            this.mContactEditText = (EditText) getView(Widget.gn_fb_id_contactEditText.getIdentifier(this.mActivity));
            this.mSendButton = (LinearLayout) getView(Widget.gn_fb_id_sendButton.getIdentifier(this.mActivity));
            this.mSendButton.setOnClickListener(this.mOnClickListener);
            this.mSendButtonText = (ExpendTextView) getView(Widget.gn_fb_id_sendButton_expendtextView.getIdentifier(this.mActivity));
            this.mSendButtonText.setAnimation(true);
            this.mAddAttachImageView = (AddAttachImageView) getView(Widget.gn_fb_id_addAttachImabeView.getIdentifier(this.mActivity));
            this.mAddAttachImageView.setOnAddAttachViewClickListener(this.mAddAttachClickListener);
            this.mAddAttachImageView.setOnAttachViewClickListener(this.mAttachClickListener);
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
        refreshMessageView();
    }

    public void onPause() {
        DraftInfo draftInfo = (DraftInfo) this.mDraftProvider.queryHead();
        if (draftInfo != null) {
            saveDraft(draftInfo.getAttachTextArray());
        } else {
            saveDraft(new ArrayList());
        }
        super.onPause();
    }

    public void onDestroy() {
        this.mSendButtonText.onDestory();
        super.onDestroy();
    }

    private void gotoRecord() {
        this.mActivity.showState(State.RECORD);
    }

    protected void updateView() {
        super.updateView();
        updateButtonState();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_IMAGE /*1001*/:
                if (data != null) {
                    String path;
                    Uri uri = data.getData();
                    Log.d(TAG, "uri = " + uri + "  type = " + getMimeType(uri));
                    List<String> attachs = new ArrayList();
                    DraftInfo draftInfo = (DraftInfo) this.mDraftProvider.queryHead();
                    Log.d(TAG, "draftInfo = " + draftInfo);
                    long size = 0;
                    if (draftInfo != null) {
                        List<String> attachTextArray = draftInfo.getAttachTextArray();
                        if (!(attachTextArray == null || attachTextArray.isEmpty())) {
                            for (String attach : attachTextArray) {
                                path = getPath(Uri.parse(attach));
                                Log.d(TAG, "path = " + path);
                                if (!TextUtils.isEmpty(path)) {
                                    size += new File(path).length();
                                }
                            }
                            attachs.addAll(attachTextArray);
                        }
                    }
                    if (uri != null) {
                        path = getPath(uri);
                        Log.d(TAG, "path = " + path);
                        if (!TextUtils.isEmpty(path)) {
                            size += new File(path).length();
                            Log.d(TAG, "size = " + size);
                            if (size <= MAX_ATTACHS_LENGTH) {
                                attachs.add(uri.toString());
                                saveDraft(attachs);
                                return;
                            }
                            showToast(Text.gn_fb_string_attach_max_length.getIdentifier(this.mActivity));
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    public String getPath(Uri uri) {
        String path = "";
        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                }
                if (cursor != null) {
                    cursor.close();
                }
                return path;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (cursor == null) {
            return null;
        } else {
            cursor.close();
            return null;
        }
    }

    private String getMimeType(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.mActivity.getContentResolver().getType(uri));
    }

    private void refreshMessageView() {
        DraftInfo draftInfo = (DraftInfo) ProviderFactory.draftProvider(this.mActivity).queryHead();
        Log.d(TAG, "refreshMessageView: " + draftInfo);
        if (draftInfo != null) {
            String content = draftInfo.getContentText();
            String contact = draftInfo.getContactText();
            this.isContentChange = false;
            this.mMessagEditText.setText(content);
            this.mContactEditText.setText(contact);
            List<String> attachs = draftInfo.getAttachTextArray();
            this.mAttachs.clear();
            this.mAddAttachImageView.removeAttach();
            if (!(attachs == null || attachs.isEmpty())) {
                for (String uri : attachs) {
                    try {
                        Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromUri(this.mActivity, Uri.parse(uri));
                        this.mAttachs.add(uri);
                        this.mAddAttachImageView.addAttach(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError e2) {
                        e2.printStackTrace();
                    }
                }
            }
            updateSendButtonEnable(content);
            return;
        }
        this.mAttachs.clear();
        this.mAddAttachImageView.removeAttach();
    }

    private void saveDraft(List<String> attachs) {
        DraftInfo draftInfo = (DraftInfo) this.mDraftProvider.queryHead();
        Log.d(TAG, "draftInfo = " + draftInfo);
        Log.d(TAG, "mAttachs = " + this.mAttachs);
        if (draftInfo != null) {
            draftInfo.setContentText(this.mMessagEditText.getText().toString());
            draftInfo.setContactText(this.mContactEditText.getText().toString());
            draftInfo.setAttachTextArray(attachs);
            this.mDraftProvider.update(draftInfo);
            return;
        }
        draftInfo = new DraftInfo();
        draftInfo.setContentText(this.mMessagEditText.getText().toString());
        draftInfo.setContactText(this.mContactEditText.getText().toString());
        draftInfo.setAttachTextArray(attachs);
        this.mDraftProvider.insert(draftInfo);
    }

    private void enableEditMode(boolean enable) {
        this.mMessagEditText.setEnabled(enable);
        this.mContactEditText.setEnabled(enable);
        this.mAddAttachImageView.setEnabled(enable);
        updateSendButtonEnable(this.mMessagEditText.getText().toString());
    }

    private void updateButtonState() {
        SendState sendState = this.mDataManager.getCurSendState();
        Log.d(TAG, "sendState:" + sendState);
        int buttonTextID = Text.gn_fb_string_send.getIdentifier(this.mActivity);
        int buttonBgID = Drawable.gn_fb_drawable_sendbutton.getIdentifier(this.mActivity);
        switch (sendState) {
            case INITIAL:
            case SEND_SUCCESS:
                buttonTextID = Text.gn_fb_string_send.getIdentifier(this.mActivity);
                buttonBgID = Drawable.gn_fb_drawable_sendbutton.getIdentifier(this.mActivity);
                enableEditMode(true);
                break;
            case SENDING:
                buttonTextID = Text.gn_fb_string_sending.getIdentifier(this.mActivity);
                buttonBgID = Drawable.gn_fb_drawable_sendbutton.getIdentifier(this.mActivity);
                enableEditMode(false);
                break;
            case SEND_FAILED:
                buttonTextID = Text.gn_fb_string_send_failed.getIdentifier(this.mActivity);
                buttonBgID = Drawable.gn_fb_drawable_sendbutton_failed.getIdentifier(this.mActivity);
                enableEditMode(true);
                break;
        }
        if (isAdded()) {
            this.mSendButtonText.setExpendText(getString(buttonTextID));
            this.mSendButton.setBackgroundResource(buttonBgID);
        }
    }

    private void updateSendButtonEnable(String message) {
        if (!isAdded()) {
            return;
        }
        if (TextUtils.isEmpty(message)) {
            this.mSendButton.setEnabled(false);
            this.mSendButtonText.setTextColor(getResources().getColor(Color.gn_fb_color_sendbutton_disable_text.getIdentifier(this.mActivity)));
            return;
        }
        this.mSendButton.setEnabled(true);
        this.mSendButtonText.setTextColor(getResources().getColor(Color.gn_fb_color_sendbutton_enable_text.getIdentifier(this.mActivity)));
    }

    private void showError(ResultCode resultCode) {
        if (ResultCode.CODE_NETWORK_DISCONNECTED.value() == resultCode.value()) {
            showToast(Text.gn_fb_string_message_network_disconnected.getIdentifier(this.mActivity));
        } else if (ResultCode.CODE_NETWORK_UNAVAILABLE.value() == resultCode.value()) {
            showToast(Text.fb_fb_string_message_network_exception.getIdentifier(this.mActivity));
        }
    }

    private void clearSendMessage() {
        Log.d(TAG, "clearSendMessage---------");
        this.mMessagEditText.setText(null);
        this.mContactEditText.setText(null);
        refreshMessageView();
    }

    private boolean isSendFailedSateChange(Editable editable) {
        boolean isSendFailedState;
        if (this.mDataManager.getCurSendState() == SendState.SEND_FAILED) {
            isSendFailedState = true;
        } else {
            isSendFailedState = false;
        }
        String edit = editable.toString();
        DraftInfo draft = (DraftInfo) this.mDraftProvider.queryHead();
        if (draft != null) {
            Log.d(TAG, " isContentChange:" + this.isContentChange + DataUpgrade.SPLIT + edit.equals(draft.toString()));
            if (!edit.equals(draft.getContentText())) {
                this.isContentChange = true;
            }
        }
        return this.isContentChange && isSendFailedState;
    }
}
