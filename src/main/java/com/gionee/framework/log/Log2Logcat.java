package com.gionee.framework.log;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

final class Log2Logcat implements ILog {
    Log2Logcat() {
    }

    public void println(Message msg) {
        Bundle bundle = msg.getData();
        Log.d(bundle.getString("tag"), bundle.getString("log"));
    }

    public void printStack(Message msg) {
        Bundle bundle = msg.getData();
        Log.d(bundle.getString("tag"), bundle.getString("log"), msg.obj);
    }
}
