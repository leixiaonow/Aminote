package com.gionee.feedback.logic.vo;

import java.util.concurrent.atomic.AtomicInteger;

public class ResultCode {
    private static final AtomicInteger BASE_CODE = new AtomicInteger(100);
    public static final ResultCode CODE_NETWORK_DISCONNECTED = new ResultCode(nextCode());
    public static final ResultCode CODE_NETWORK_UNAVAILABLE = new ResultCode(nextCode());
    public static final ResultCode CODE_PARSE_ERROR = new ResultCode(nextCode());
    public static final ResultCode CODE_SEND_FAILED = new ResultCode(nextCode());
    public static final ResultCode CODE_SEND_SUCESSFUL = new ResultCode(nextCode());
    private int mCode;

    private ResultCode(int code) {
        this.mCode = code;
    }

    public int value() {
        return this.mCode;
    }

    public String toString() {
        return "ResultCode{mCode=" + this.mCode + '}';
    }

    private static int nextCode() {
        return BASE_CODE.addAndGet(1);
    }
}
