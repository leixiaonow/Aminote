package com.gionee.feedback.logic.vo;

public class ErrorInfo {
    private int mErrorCode;
    private String mErrorMsg;

    public String getErrorMsg() {
        return this.mErrorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.mErrorMsg = errorMsg;
    }

    public int getErrorCode() {
        return this.mErrorCode;
    }

    public void setErrorCode(int errorCode) {
        this.mErrorCode = errorCode;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("mErrorCode = ");
        builder.append(this.mErrorCode);
        builder.append("; mErrorMsg = ");
        builder.append(this.mErrorMsg);
        return builder.toString();
    }
}
