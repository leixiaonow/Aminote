package com.gionee.feedback.exception;

public class FeedBackParserException extends FeedBackException {
    private Object mParserObj;

    public FeedBackParserException(Object obj) {
        super("parser " + obj.toString() + " exception !!!");
        this.mParserObj = obj;
    }

    public Object getParserObj() {
        return this.mParserObj;
    }
}
