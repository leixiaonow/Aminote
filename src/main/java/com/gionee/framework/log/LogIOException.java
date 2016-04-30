package com.gionee.framework.log;

public final class LogIOException extends RuntimeException {
    private String mMessage;

    public LogIOException(String detailMessage) {
        super(detailMessage);
        this.mMessage = detailMessage;
    }

    public String getMessage() {
        return this.mMessage == null ? "write file error!!!" : this.mMessage;
    }
}
