package com.gionee.feedback.logic.vo;

import android.text.TextUtils;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.note.data.LocalNoteItem;
import java.util.List;

public class Message {
    private List<String> mAttachs;
    private Callback mCallback;
    private String mContact;
    private String mEntity;
    private long mID;
    private String mMessage;

    public static final class Builder {
        private List<String> mAttachs;
        private Callback mCallback;
        private String mContact;
        private String mEntity = "";
        private long mID;
        private String mMessage;

        public Builder setMessage(String message) {
            this.mMessage = message;
            return this;
        }

        public Builder setContact(String contact) {
            this.mContact = contact;
            return this;
        }

        public Builder setAttachs(List<String> attachs) {
            this.mAttachs = attachs;
            return this;
        }

        public Builder setID(long id) {
            this.mID = id;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.mCallback = callback;
            return this;
        }

        public Builder setEntity(String key, String value) {
            StringBuilder sb = new StringBuilder(this.mEntity);
            if (!TextUtils.isEmpty(this.mEntity)) {
                sb.append(LocalNoteItem.LABEL_SEPARATOR);
            }
            sb.append(key);
            sb.append(DataUpgrade.SPLIT);
            sb.append(value);
            this.mEntity = sb.toString();
            return this;
        }

        public Message builder() {
            return new Message();
        }
    }

    public interface Callback {
        void onResult(ResultCode resultCode);
    }

    private Message(Builder builder) {
        this.mID = -1;
        this.mMessage = builder.mMessage;
        this.mContact = builder.mContact;
        this.mID = builder.mID;
        this.mCallback = builder.mCallback;
        this.mEntity = builder.mEntity;
        this.mAttachs = builder.mAttachs;
    }

    public long getID() {
        return this.mID;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public String getContact() {
        return this.mContact;
    }

    public Callback getCallback() {
        return this.mCallback;
    }

    public String getEntity() {
        return this.mEntity;
    }

    public List<String> getAttachs() {
        return this.mAttachs;
    }
}
