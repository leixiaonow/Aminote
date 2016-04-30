package com.gionee.framework.log;

final class LogFactory {
    private static final Log2Logcat LOGCAT_OPERATOR = new Log2Logcat();
    private static final Log2File LOG_FILE_OPERATOR = new Log2File();
    private static ILog sDefaultClient;

    LogFactory() {
    }

    public static ILog getDefaultLogClient() {
        if (sDefaultClient == null) {
            sDefaultClient = LOGCAT_OPERATOR;
        }
        return sDefaultClient;
    }

    public static ILog getLogcatClient() {
        return LOGCAT_OPERATOR;
    }

    public static ILog getLogFileClient() {
        return LOG_FILE_OPERATOR;
    }

    public static void setDefaultLogClient(int client) {
        if ((client & 1) == 0) {
            sDefaultClient = LOG_FILE_OPERATOR;
        } else {
            sDefaultClient = LOGCAT_OPERATOR;
        }
    }
}
