package com.gionee.note.common;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.gionee.framework.log.Logger;
import java.lang.reflect.Method;

public class YouJuUtils {
    private static final String TAG = "YouJuHelper";
    private static Method sYouJuEvent = null;
    private static Method sYouJuEventWithLabel = null;
    private static Handler sYouJuHandler;
    private static Method sYouJuPause = null;
    private static Method sYouJuResume = null;

    private static void youjuCreateHandler() {
        HandlerThread youjuThread = new HandlerThread("youju-task", 10);
        youjuThread.start();
        sYouJuHandler = new Handler(youjuThread.getLooper());
    }

    private static void initYouJuMethod(Context context) {
        try {
            Class<?> youju = Class.forName("com.gionee.youju.statistics.sdk.YouJuAgent");
            sYouJuResume = ReflectionUtils.findMethod(youju, "onResume", Context.class);
            sYouJuPause = ReflectionUtils.findMethod(youju, "onPause", Context.class);
            sYouJuEvent = ReflectionUtils.findMethod(youju, "onEvent", Context.class, String.class);
            sYouJuEventWithLabel = ReflectionUtils.findMethod(youju, "onEvent", Context.class, String.class, String.class);
            Method youJuInit = ReflectionUtils.findMethod(youju, "init", Context.class);
            Method youJuReport = ReflectionUtils.findMethod(youju, "setReportUncaughtExceptions", Boolean.TYPE);
            if (youJuInit != null) {
                ReflectionUtils.invokeMethod(youJuInit, null, context);
            }
            if (youJuReport != null) {
                ReflectionUtils.invokeMethod(youJuReport, null, Boolean.FALSE);
            }
        } catch (Exception e) {
            Logger.printLog(TAG, "YouJuAgent is not found!");
        }
    }

    public static void youjuInit(Context context) {
        youjuCreateHandler();
        initYouJuMethod(context);
    }

    public static void youjuResume(final Context context) {
        sYouJuHandler.post(new Runnable() {
            public void run() {
                if (YouJuUtils.sYouJuResume != null) {
                    ReflectionUtils.invokeMethod(YouJuUtils.sYouJuResume, null, context);
                }
            }
        });
    }

    public static void youjuPause(final Context context) {
        sYouJuHandler.post(new Runnable() {
            public void run() {
                if (YouJuUtils.sYouJuPause != null) {
                    ReflectionUtils.invokeMethod(YouJuUtils.sYouJuPause, null, context);
                }
            }
        });
    }

    public static void youjuEvent(final Context context, final String eventId) {
        sYouJuHandler.post(new Runnable() {
            public void run() {
                if (YouJuUtils.sYouJuEvent != null) {
                    ReflectionUtils.invokeMethod(YouJuUtils.sYouJuEvent, null, context, eventId);
                }
            }
        });
    }

    public static void youjuEventWithLabel(final Context context, final String eventId, final String label) {
        sYouJuHandler.post(new Runnable() {
            public void run() {
                if (YouJuUtils.sYouJuEventWithLabel != null) {
                    ReflectionUtils.invokeMethod(YouJuUtils.sYouJuEventWithLabel, null, context, eventId, label);
                }
            }
        });
    }
}
