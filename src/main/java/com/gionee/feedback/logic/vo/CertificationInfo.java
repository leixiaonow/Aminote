package com.gionee.feedback.logic.vo;

public class CertificationInfo {
    private String mAccessToken;
    private int mEffectiveTime;

    public String getAccessToken() {
        return this.mAccessToken;
    }

    public void setAccessToken(String mAccessToken) {
        this.mAccessToken = mAccessToken;
    }

    public int getEffectiveTime() {
        return this.mEffectiveTime;
    }

    public void setEffectiveTime(int mEffectiveTime) {
        this.mEffectiveTime = mEffectiveTime;
    }
}
