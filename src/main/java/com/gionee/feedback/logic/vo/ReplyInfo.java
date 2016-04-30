package com.gionee.feedback.logic.vo;

import java.io.Serializable;

public class ReplyInfo implements Serializable {
    private static final long serialVersionUID = 99;
    private long mContentID;
    private int mID;
    private boolean mIsRead;
    private String mReplyContent;
    private long mReplyID;
    private String mReplyPerson;
    private long mReplyTime;

    public int getID() {
        return this.mID;
    }

    public void setID(int id) {
        this.mID = id;
    }

    public long getContentID() {
        return this.mContentID;
    }

    public void setContentID(long contentID) {
        this.mContentID = contentID;
    }

    public long getReplyTime() {
        return this.mReplyTime;
    }

    public void setReplyTime(long replyTime) {
        this.mReplyTime = replyTime;
    }

    public long getReplyID() {
        return this.mReplyID;
    }

    public void setReplyID(long replyID) {
        this.mReplyID = replyID;
    }

    public String getReplyPerson() {
        return this.mReplyPerson;
    }

    public void setReplyPerson(String replyPerson) {
        this.mReplyPerson = replyPerson;
    }

    public String getReplyContent() {
        return this.mReplyContent;
    }

    public void setReplyContent(String replyContent) {
        this.mReplyContent = replyContent;
    }

    public boolean isReaded() {
        return this.mIsRead;
    }

    public void setReaded(boolean isReaded) {
        this.mIsRead = isReaded;
    }

    public String toString() {
        return "ReplyInfo [mContentID=" + this.mContentID + ", mID=" + this.mID + ", mReplyTime=" + this.mReplyTime + ", mReplyID=" + this.mReplyID + ", mReplyPerson=" + this.mReplyPerson + ", mReplyContent=" + this.mReplyContent + ", isRead=" + this.mIsRead + "]";
    }
}
