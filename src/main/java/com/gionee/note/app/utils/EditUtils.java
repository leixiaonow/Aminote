package com.gionee.note.app.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import com.gionee.note.app.span.BillImageSpan;
import com.gionee.note.app.span.PhotoImageSpan;
import com.gionee.note.app.span.SoundImageSpan;
import com.gionee.note.common.Constants;

public class EditUtils {
    private static final String TAG = "EditUtils";

    public static void insertPhotoImageSpan(SpannableStringBuilder text, PhotoImageSpan span, int start) {
        text.insert(start, Constants.MEDIA_PHOTO);
        text.setSpan(span, start, Constants.MEDIA_PHOTO.length() + start, 33);
    }

    public static void insertSoundImageSpan(SpannableStringBuilder text, SoundImageSpan span, int start) {
        text.insert(start, Constants.MEDIA_SOUND);
        text.setSpan(span, start, Constants.MEDIA_SOUND.length() + start, 33);
    }

    public static void insertBillImageSpan(SpannableStringBuilder text, BillImageSpan span, int start) {
        if (!text.toString().startsWith(Constants.MEDIA_BILL, start)) {
            text.insert(start, Constants.MEDIA_BILL);
        }
        text.setSpan(span, start, Constants.MEDIA_BILL.length() + start, 33);
    }

    public static int getCurParagraphStart(SpannableStringBuilder text, int currPosition) {
        if (text.length() <= 0) {
            return 0;
        }
        int index = TextUtils.lastIndexOf(text.subSequence(0, currPosition), '\n');
        if (index != -1) {
            return index + 1;
        }
        return 0;
    }

    public static int getCurParagraphEnd(SpannableStringBuilder text, int currPosition) {
        if (text.length() <= 0) {
            return 0;
        }
        int index = TextUtils.indexOf(text, '\n', currPosition);
        if (index == -1) {
            return text.length();
        }
        return index;
    }

    public static String getVersionName(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.w(TAG, "error", e);
        }
        return packageInfo != null ? packageInfo.versionName : "";
    }
}
