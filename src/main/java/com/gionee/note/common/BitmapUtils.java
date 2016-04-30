package com.gionee.note.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.Config.EditPage;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import java.io.Closeable;
import java.io.FileOutputStream;
import uk.co.senab.photoview.IPhotoView;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    public static Bitmap resizeBitmapBySize(Bitmap bitmap, int targetWidth, int targetHeight, boolean recycle) {
        float scale = Math.max(((float) targetWidth) / ((float) bitmap.getWidth()), ((float) targetHeight) / ((float) bitmap.getHeight()));
        int width = Math.round(((float) bitmap.getWidth()) * scale);
        int height = Math.round(((float) bitmap.getHeight()) * scale);
        if (width == targetHeight && height == targetWidth) {
            return bitmap;
        }
        Bitmap target = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(target);
        canvas.translate(((float) (targetWidth - width)) / 2.0f, ((float) (targetHeight - height)) / 2.0f);
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap resizeAndCropCenter(Bitmap bitmap, int targetW, int targetH, boolean recycle, boolean isCropped) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == targetW && h == targetH) {
            return bitmap;
        }
        float scale;
        if (isCropped) {
            scale = Math.max(((float) targetW) / ((float) w), ((float) targetH) / ((float) h));
        } else {
            scale = ((float) targetH) / ((float) h);
            targetW = Math.round(((float) w) * scale);
        }
        Bitmap target = Bitmap.createBitmap(targetW, targetH, getConfig(bitmap));
        int width = Math.round(((float) w) * scale);
        int height = Math.round(((float) h) * scale);
        Canvas canvas = new Canvas(target);
        canvas.translate(((float) (targetW - width)) / 2.0f, ((float) (targetH - height)) / 2.0f);
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    public static Bitmap assembleSoundBitmap(Context context, int durationInSec) {
        EditPage page = EditPage.get(context);
        Drawable bg = context.getDrawable(R.drawable.edit_page_sound_bg);
        Bitmap bitmap = Bitmap.createBitmap(page.mSoundWidth, page.mSoundHeight, Config.ARGB_8888);
        bg.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Canvas canvas = new Canvas(bitmap);
        bg.draw(canvas);
        Paint paint = new Paint(4);
        paint.setAntiAlias(true);
        paint.setColor(page.mSoundPointColor);
        canvas.drawCircle((float) page.mSoundPointOffsetLeft, (float) page.mSoundPointOffsetRight, (float) page.mSoundPointRadius, paint);
        drawText(page, canvas, durationInSec);
        return bitmap;
    }

    private static void drawText(EditPage page, Canvas canvas, int durationInSec) {
        String text = NoteUtils.formatTime(durationInSec, DataUpgrade.SPLIT);
        TextPaint paint = getTextPaint(page.mSoundDurationSize, page.mSoundDurationColor, false);
        FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(text, (float) page.mSoundDurationOffsetLeft, ((((float) page.mSoundHeight) - (metrics.descent - metrics.ascent)) / 2.0f) - metrics.ascent, paint);
    }

    public static void compressToFile(Bitmap bitmap, String filePath) {
        Exception e;
        Throwable th;
        Closeable outputStream = null;
        try {
            Closeable outputStream2 = new FileOutputStream(filePath);
            try {
                bitmap.compress(CompressFormat.PNG, 100, outputStream2);
                NoteUtils.closeSilently(outputStream2);
                outputStream = outputStream2;
            } catch (Exception e2) {
                e = e2;
                outputStream = outputStream2;
                try {
                    Logger.printLog(TAG, "compressToFile fail : " + e.toString());
                    NoteUtils.closeSilently(outputStream);
                } catch (Throwable th2) {
                    th = th2;
                    NoteUtils.closeSilently(outputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outputStream = outputStream2;
                NoteUtils.closeSilently(outputStream);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Logger.printLog(TAG, "compressToFile fail : " + e.toString());
            NoteUtils.closeSilently(outputStream);
        }
    }

    private static Config getConfig(Bitmap bitmap) {
        Config config = bitmap.getConfig();
        if (config == null) {
            return Config.ARGB_8888;
        }
        return config;
    }

    public static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor((double) (IPhotoView.DEFAULT_MIN_SCALE / scale));
        if (initialSize <= 1) {
            return 1;
        }
        return initialSize <= 8 ? NoteUtils.prevPowerOf2(initialSize) : (initialSize / 8) * 8;
    }

    public static TextPaint getTextPaint(int textSize, int color, boolean isBold) {
        TextPaint paint = new TextPaint();
        paint.setTextSize((float) textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(1));
        }
        return paint;
    }

    public static Bitmap createSpecifyColorBitmap(Bitmap origin, int color) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        int[] previousPixels = new int[(width * height)];
        origin.getPixels(previousPixels, 0, width, 0, 0, width, height);
        color &= ViewCompat.MEASURED_SIZE_MASK;
        for (int i = 0; i < height; i++) {
            int lineStart = i * width;
            for (int j = 0; j < width; j++) {
                int pos = lineStart + j;
                previousPixels[pos] = previousPixels[pos] & ViewCompat.MEASURED_STATE_MASK;
                previousPixels[pos] = previousPixels[pos] | color;
            }
        }
        Bitmap result = origin.copy(Config.ARGB_8888, true);
        result.setPixels(previousPixels, 0, width, 0, 0, width, height);
        return result;
    }
}
