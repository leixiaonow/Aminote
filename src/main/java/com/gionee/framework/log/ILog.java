package com.gionee.framework.log;

import android.os.Message;

interface ILog {
    void printStack(Message message);

    void println(Message message);
}
