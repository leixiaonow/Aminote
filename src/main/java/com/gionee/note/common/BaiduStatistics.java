package com.gionee.note.common;

import android.content.Context;
import com.baidu.mobstat.StatService;

public class BaiduStatistics {
    private static final String DEFAULT_LABLE_VALUE = "C";

    private BaiduStatistics() {
    }

    public static void onEvent(Context context, String eventId, String lable) {
        StatService.onEvent(context, eventId, lable);
    }

    public static void onEvent(Context context, String eventId) {
        onEvent(context, eventId, DEFAULT_LABLE_VALUE);
    }

    public static void onResume(Context context) {
        StatService.onResume(context);
    }

    public static void onPause(Context context) {
        StatService.onPause(context);
    }
}
