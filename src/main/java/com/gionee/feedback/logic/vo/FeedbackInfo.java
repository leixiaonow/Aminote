package com.gionee.feedback.logic.vo;

import android.text.TextUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FeedbackInfo implements Serializable {
    private static final String SPLITE = ",";
    private static final long serialVersionUID = 100;
    private boolean isChecked;
    private final List<String> mAttachs = new ArrayList();
    private String mContent;
    private long mContentID;
    private long mID;
    private List<ReplyInfo> mReplyInfos;
    private String mSendTime;
    private String mUserContact;

    public long getID() {
        return this.mID;
    }

    public void setID(long id) {
        this.mID = id;
    }

    public long getContentID() {
        return this.mContentID;
    }

    public void setContentID(long contentID) {
        this.mContentID = contentID;
    }

    public String getSendTime() {
        return this.mSendTime;
    }

    public void setSendTime(String sendTime) {
        this.mSendTime = sendTime;
    }

    public String getUserContact() {
        return this.mUserContact;
    }

    public void setUserContact(String userContact) {
        this.mUserContact = userContact;
    }

    public List<ReplyInfo> getReplyInfos() {
        return this.mReplyInfos;
    }

    public void setReplyInfos(List<ReplyInfo> replyInfos) {
        this.mReplyInfos = replyInfos;
    }

    public String getContent() {
        return this.mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public synchronized void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public synchronized boolean isChecked() {
        return this.isChecked;
    }

    public void setAttachTexts(String text) {
        this.mAttachs.clear();
        for (String str : text.split(",")) {
            if (!TextUtils.isEmpty(str)) {
                this.mAttachs.add(str);
            }
        }
    }

    public void setAttachTextArray(List<String> attachTexts) {
        if (attachTexts != null) {
            this.mAttachs.clear();
            this.mAttachs.addAll(attachTexts);
        }
    }

    public List<String> getAttachTextArray() {
        return this.mAttachs;
    }

    public String getAttachTexts() {
        StringBuilder sb = new StringBuilder();
        int size = this.mAttachs.size();
        for (int i = 0; i < size; i++) {
            sb.append((String) this.mAttachs.get(i));
            if (i != size - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public String toString() {
        return "FeedbackInfo [mID=" + this.mID + ", mContentID=" + this.mContentID + ", mSendTime=" + this.mSendTime + ", mUserContact=" + this.mUserContact + ", mContent=" + this.mContent + ", mReplyInfos=" + this.mReplyInfos + ", isChecked=" + this.isChecked + ", mAttachs = " + this.mAttachs + "]";
    }
}
