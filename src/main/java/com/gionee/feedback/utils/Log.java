package com.gionee.feedback.utils;

public class Log {
    private static final boolean DUBUG = true;
    private static final boolean LOGD = false;
    private static final boolean LOGE = true;
    private static final boolean LOGI = false;
    private static final boolean LOGV = false;
    private static final boolean LOGW = true;
    private static final String TAG = "FeedBackLog.";

    private Log() {
    }

    public static final void i(String tag, String mess) {
        android.util.Log.i(TAG + tag, mess);
    }

    public static final void d(String tag, String mess) {
        android.util.Log.d(TAG + tag, mess);
    }

    public static final void v(String tag, String mess) {
        android.util.Log.v(TAG + tag, mess);
    }

    public static final void w(String tag, String mess) {
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            android.util.Log.w(TAG + tag, "in(" + elements[3].getMethodName() + ") call by (" + elements[4].getMethodName() + ") " + mess);
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public static final void e(String tag, String mess) {
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            android.util.Log.e(TAG + tag, "in(" + elements[3].getMethodName() + ") call by (" + elements[4].getMethodName() + ") " + mess);
        } catch (IndexOutOfBoundsException e) {
        }
    }
}
