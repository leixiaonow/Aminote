package com.gionee.framework.component;

import android.app.Application;
import com.gionee.framework.log.Logger;

public class BaseApplication extends Application {
    public static final int FLAG_OPEN_LOG_TO_FILE = 0;
    public static final int FLAG_OPEN_LOG_TO_LOGCAT = 1;
    public static final int MASK_LOG_FLAG = 1;
    private static final String TAG = "BaseApplication";
    static BaseApplication sApplication;

    public static void setsApplication(BaseApplication sApplication) {
        sApplication = sApplication;
    }

    public BaseApplication() {
        setsApplication(this);
    }

    public void onCreate() {
        super.onCreate();
    }

    public final void enableLog(int status) {
        Logger.enableLog(status);
    }
}
