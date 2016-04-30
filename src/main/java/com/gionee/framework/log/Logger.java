package com.gionee.framework.log;

import android.os.HandlerThread;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings(justification = "seems no problem", value = {"UUF_UNUSED_FIELD"})
public final class Logger {
    private static final boolean DEBUG = false;
    public static final int FLAG_OPEN_LOG_TO_FILE = 0;
    public static final int FLAG_OPEN_LOG_TO_LOGCAT = 1;
    static final int FLAG_TO_PRINT_LOG = 0;
    static final int FLAG_TO_PRINT_STACK_TRANCE = 1;
    static final String LOG = "log";
    public static final int MASK_LOG_FLAG = 1;
    private static final boolean OPEN_LOG_DEVICE = false;
    static final String TAG = "tag";
    private static final String TAG_PREFIX = "aminote_";
    static final String THREAD_ID = "thread_id";
    static final String THREAD_NAME = "thread_name";
    static final String THROWABLE = "throwable";
    private static HandlerThread sHandlerThread;
    private static LogHandler sLogHandler;

    public static void printLog(String tag, String log) {
    }

    public static void printStackTrace(String tag, String logMsg, Throwable throwable) {
    }

    public static void enableLog(int status) {
    }
}
