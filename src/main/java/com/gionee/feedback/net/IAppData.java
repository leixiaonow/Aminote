package com.gionee.feedback.net;

public interface IAppData {
    public static final long FEEDBACK_ID_DEFAULT = -1;
    public static final long FEEDBACK_ID_SENDING = -2;
    public static final long FEEDBACK_ID_SEND_FAILED = -3;

    String getAppKey();

    String getImei();
}
