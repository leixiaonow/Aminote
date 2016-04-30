package com.gionee.note.common;

import android.content.Context;

public class StatisticsModule {
    public static void init(Context context) {
        if (PlatformUtil.isGioneeDevice()) {
            YouJuUtils.youjuInit(context);
        }
    }

    public static void onResume(Context context) {
        if (PlatformUtil.isGioneeDevice()) {
            YouJuUtils.youjuResume(context);
        } else {
            BaiduStatistics.onResume(context);
        }
    }

    public static void onPause(Context context) {
        if (PlatformUtil.isGioneeDevice()) {
            YouJuUtils.youjuPause(context);
        } else {
            BaiduStatistics.onPause(context);
        }
    }

    public static void onEvent(Context context, int eventResId) {
        onEvent(context, context.getResources().getString(eventResId));
    }

    public static void onEvent(Context context, String eventId) {
        if (PlatformUtil.isGioneeDevice()) {
            YouJuUtils.youjuEvent(context, eventId);
        } else {
            BaiduStatistics.onEvent(context, eventId);
        }
    }

    public static void onEvent(Context context, String eventId, String lable) {
        if (PlatformUtil.isGioneeDevice()) {
            YouJuUtils.youjuEventWithLabel(context, eventId, lable);
        } else {
            BaiduStatistics.onEvent(context, eventId, lable);
        }
    }
}
