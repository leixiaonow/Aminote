package com.gionee.feedback.exception;

public class FeedBackNetException extends FeedBackException {
    private int mHttpStatus = -1;

    public FeedBackNetException(int httpStatus) {
        super("net exception occur !!! status = " + httpStatus);
        this.mHttpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return this.mHttpStatus;
    }
}
