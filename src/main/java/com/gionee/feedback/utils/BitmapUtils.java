package com.gionee.feedback.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.util.DisplayMetrics;
import java.io.FileNotFoundException;

public final class BitmapUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "BitmapUtils";

    private BitmapUtils() {
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri) throws FileNotFoundException {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while (true) {
                if (halfHeight / inSampleSize <= reqHeight && halfWidth / inSampleSize <= reqWidth) {
                    break;
                }
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
