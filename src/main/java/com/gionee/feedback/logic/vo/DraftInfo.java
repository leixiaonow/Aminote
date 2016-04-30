package com.gionee.feedback.logic.vo;

import android.text.TextUtils;
import com.gionee.feedback.utils.Log;
import java.util.ArrayList;
import java.util.List;

public class DraftInfo {
    private static final String SPLITE = ",";
    private static final String TAG = "DraftInfo";
    private List<String> mAttachs;
    private String mContactText;
    private String mContentText;
    private long mId;

    public void setId(long id) {
        this.mId = id;
    }

    public long getId() {
        return this.mId;
    }

    public void setContentText(String contentText) {
        this.mContentText = contentText;
    }

    public String getContentText() {
        return this.mContentText;
    }

    public void setContactText(String contactText) {
        this.mContactText = contactText;
    }

    public String getContactText() {
        return this.mContactText;
    }

    public void setAttachTexts(String text) {
        if (!TextUtils.isEmpty(text)) {
            String[] strs = text.split(",");
            this.mAttachs = new ArrayList();
            for (String str : strs) {
                this.mAttachs.add(str);
            }
        }
    }

    public void setAttachTextArray(List<String> attachTexts) {
        this.mAttachs = attachTexts;
    }

    public List<String> getAttachTextArray() {
        return this.mAttachs;
    }

    public String getAttachTexts() {
        if (this.mAttachs == null || this.mAttachs.size() == 0) {
            return null;
        }
        Log.d(TAG, "attachs = " + this.mAttachs);
        StringBuilder sb = new StringBuilder();
        int size = this.mAttachs.size();
        for (int i = 0; i < size; i++) {
            String attach = (String) this.mAttachs.get(i);
            if (!TextUtils.isEmpty(attach)) {
                sb.append(attach);
                if (i != size - 1) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("mContentText = ");
        builder.append(this.mContentText);
        builder.append(", mContactText = ");
        builder.append(this.mContactText);
        builder.append(" Attachs:");
        builder.append(getAttachTexts());
        return builder.toString();
    }
}
