package com.gionee.note.app.effect;

import android.util.Log;
import java.util.Calendar;

public class EffectUtil {
    public static final int EFFECT_NORMAL = 0;
    public static final int EFFECT_OLD = 1;
    public static final int EFFECT_VERY_OLD = 2;
    public static final int EFFECT_VERY_VERY_OLD = 3;
    private static final long ONE_DAY_TIME_MILLIS = 86400000;
    private static final String TAG = "EffectUtil";
    private static final long THREE_DAY_TIME_MILLIS = 259200000;
    private TimeSpan mNormalTimeSpan;
    private TimeSpan mOldTimeSpan;
    private TimeSpan mVeryOldTimeSpan;

    private static class TimeSpan {
        private long mEndTime;
        private long mStartTime;

        public TimeSpan(long startT, long endT) {
            this.mStartTime = startT;
            this.mEndTime = endT;
        }

        public boolean isContain(long time) {
            return time >= this.mStartTime && time <= this.mEndTime;
        }

        public boolean isLessThanStart(long time) {
            return time < this.mStartTime;
        }

        public boolean isMoreThanEnd(long time) {
            return time > this.mEndTime;
        }
    }

    public EffectUtil(long timeInMillis) {
        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTimeInMillis(timeInMillis);
        printCalendar(curCalendar);
        int year = curCalendar.get(1);
        int month = curCalendar.get(2);
        int day = curCalendar.get(5);
        Calendar curStartCalendar = Calendar.getInstance();
        curStartCalendar.set(year, month, day, 0, 0, 0);
        initTimeSpans(curStartCalendar.getTimeInMillis());
    }

    private void initTimeSpans(long curStartTimeMillis) {
        long normalEndTimeMillis = curStartTimeMillis + ONE_DAY_TIME_MILLIS;
        long normalStartTimeMillis = normalEndTimeMillis - THREE_DAY_TIME_MILLIS;
        this.mNormalTimeSpan = new TimeSpan(normalStartTimeMillis, normalEndTimeMillis);
        long oldEndTimeMillis = normalStartTimeMillis;
        long oldStartTimeMillis = oldEndTimeMillis - THREE_DAY_TIME_MILLIS;
        this.mOldTimeSpan = new TimeSpan(oldStartTimeMillis, oldEndTimeMillis);
        long veryOldEndTimeMillis = oldStartTimeMillis;
        this.mVeryOldTimeSpan = new TimeSpan(veryOldEndTimeMillis - THREE_DAY_TIME_MILLIS, veryOldEndTimeMillis);
    }

    public int getEffect(long time) {
        if (this.mNormalTimeSpan.isMoreThanEnd(time) || this.mNormalTimeSpan.isContain(time)) {
            return 0;
        }
        if (this.mOldTimeSpan.isContain(time)) {
            return 1;
        }
        if (this.mVeryOldTimeSpan.isContain(time)) {
            return 2;
        }
        return 3;
    }

    private static void printCalendar(Calendar calendar) {
        int year = calendar.get(1);
        int month = calendar.get(2);
        int day = calendar.get(5);
        int hour = calendar.get(11);
        int mimute = calendar.get(12);
        Log.d(TAG, "year = " + year + ",month = " + month + ",day = " + day + ",hour = " + hour + ",mimute = " + mimute + ",second = " + calendar.get(13) + ",timeInMillis = " + calendar.getTimeInMillis());
    }
}
