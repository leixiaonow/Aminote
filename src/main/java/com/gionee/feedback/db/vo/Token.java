package com.gionee.feedback.db.vo;

public class Token {
    private long mId;
    private String mToken;

    public long getId() {
        return this.mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getToken() {
        return this.mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id = ");
        sb.append(getId());
        sb.append("  token = ");
        sb.append(getToken());
        return sb.toString();
    }
}
