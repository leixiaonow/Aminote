package com.gionee.framework.log;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

final class LogHandler extends Handler {
    public LogHandler(Looper looper) {
        super(looper);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == 0) {
            LogFactory.getDefaultLogClient().println(msg);
        } else if (msg.what == 1) {
            LogFactory.getDefaultLogClient().printStack(msg);
        }
    }
}
