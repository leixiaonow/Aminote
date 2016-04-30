package com.gionee.note.common;

import android.widget.TextView;

public class PlatformUtil {
    public static boolean isMtkPlatform() {
        if (isGioneeDevice()) {
            if (ReflectionUtils.findMethod(TextView.class, "setSmartFit", Boolean.TYPE) != null) {
            }
        }
        return true;
    }

    public static boolean isGioneeDevice() {
        return false;
    }
}
