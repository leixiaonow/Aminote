package com.gionee.note.app.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import com.gionee.note.common.NoteUtils;
import java.io.Closeable;

public class DrawableManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "DrawableManager";
    private static Drawable sNoteCardNormalDrawable;
    private static Drawable sNoteCardOldDrawable;
    private static Drawable sNoteCardVeryOldDrawable;
    private static Drawable sNoteCardVeryVeryOldDrawable;

    public static Drawable getCardEffectDrawable(Context context, int effect, int w, int h) {
        if (effect == 0) {
            return getNoteCardNormalDrawable(context, w, h);
        }
        if (effect == 1) {
            return getNoteCardOldDrawable(context, w, h);
        }
        if (effect == 2) {
            return getNoteCardVeryOldDrawable(context, w, h);
        }
        if (effect == 3) {
            return getNoteCardVeryVeryOldDrawable(context, w, h);
        }
        throw new AssertionError();
    }

    public static Bitmap getWidgetEffectBitmap(Context context, int widgetType, int effect, int w, int h) {
        if (effect == 0) {
            return getNoteWidgetNormalBitmap(context, widgetType, w, h);
        }
        if (effect == 1) {
            return getNoteWidgetOldBitmap(context, widgetType, w, h);
        }
        if (effect == 2) {
            return getNoteWidgetVeryOldBitmap(context, widgetType, w, h);
        }
        if (effect == 3) {
            return getNoteWidgetVeryVeryOldBitmap(context, widgetType, w, h);
        }
        throw new AssertionError();
    }

    private static Drawable getNoteCardNormalDrawable(Context context, int w, int h) {
        if (sNoteCardNormalDrawable == null) {
            sNoteCardNormalDrawable = createDrawable(context, "note_card_normal.png", w, h);
        }
        return sNoteCardNormalDrawable;
    }

    private static Drawable getNoteCardOldDrawable(Context context, int w, int h) {
        if (sNoteCardOldDrawable == null) {
            sNoteCardOldDrawable = createDrawable(context, "note_card_old.png", w, h);
        }
        return sNoteCardOldDrawable;
    }

    private static Drawable getNoteCardVeryOldDrawable(Context context, int w, int h) {
        if (sNoteCardVeryOldDrawable == null) {
            sNoteCardVeryOldDrawable = createDrawable(context, "note_card_very_old.png", w, h);
        }
        return sNoteCardVeryOldDrawable;
    }

    private static Drawable getNoteCardVeryVeryOldDrawable(Context context, int w, int h) {
        if (sNoteCardVeryVeryOldDrawable == null) {
            sNoteCardVeryVeryOldDrawable = createDrawable(context, "note_card_vv_old.png", w, h);
        }
        return sNoteCardVeryVeryOldDrawable;
    }

    private static Bitmap getNoteWidgetNormalBitmap(Context context, int widgetType, int w, int h) {
        if (1 == widgetType) {
            return createBitmap(context, "note_widget_bg_2_2_normal.png", w, h);
        }
        if (2 == widgetType) {
            return createBitmap(context, "note_widget_bg_4_4_normal.png", w, h);
        }
        throw new AssertionError();
    }

    private static Bitmap getNoteWidgetOldBitmap(Context context, int widgetType, int w, int h) {
        if (1 == widgetType) {
            return createBitmap(context, "note_widget_bg_2_2_old.png", w, h);
        }
        if (2 == widgetType) {
            return createBitmap(context, "note_widget_bg_4_4_old.png", w, h);
        }
        throw new AssertionError();
    }

    private static Bitmap getNoteWidgetVeryOldBitmap(Context context, int widgetType, int w, int h) {
        if (1 == widgetType) {
            return createBitmap(context, "note_widget_bg_2_2_v_old.png", w, h);
        }
        if (2 == widgetType) {
            return createBitmap(context, "note_widget_bg_4_4_v_old.png", w, h);
        }
        throw new AssertionError();
    }

    private static Bitmap getNoteWidgetVeryVeryOldBitmap(Context context, int widgetType, int w, int h) {
        if (1 == widgetType) {
            return createBitmap(context, "note_widget_bg_2_2_vv_old.png", w, h);
        }
        if (2 == widgetType) {
            return createBitmap(context, "note_widget_bg_4_4_vv_old.png", w, h);
        }
        throw new AssertionError();
    }

    private static Drawable createDrawable(Context context, String name, int w, int h) {
        Bitmap bitmap = createBitmap(context, name, w, h);
        if (bitmap == null) {
            return null;
        }
        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.setBounds(0, 0, w, h);
        return drawable;
    }

    private static Bitmap createBitmap(Context context, String name, int w, int h) {
        int[] size = getBitmapOriginalSize(context, name);
        if (!isValidBitmapSize(size)) {
            return null;
        }
        Bitmap bitmap = decodeRawBitmap(context, name, size[0], size[0], w, h);
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() == w && bitmap.getHeight() == h) {
            return bitmap;
        }
        return createTargetBitmap(bitmap, w, h, true);
    }

    private static int[] getBitmapOriginalSize(Context context, String rawFileName) {
        Closeable is = null;
        try {
            is = context.getAssets().open(rawFileName);
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            int originalW = options.outWidth;
            int originalH = options.outHeight;
            int[] size = new int[]{originalW, originalH};
            return size;
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
            return null;
        } finally {
            NoteUtils.closeSilently(is);
        }
    }

    private static Bitmap decodeRawBitmap(Context context, String rawFileName, int ow, int oh, int tw, int th) {
        Closeable is = null;
        try {
            is = context.getAssets().open(rawFileName);
            Options options = new Options();
            options.inMutable = true;
            options.inSampleSize = calculationSampleSize(ow, oh, tw, th);
            options.inPreferredConfig = Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            if (bitmap == null) {
                return null;
            }
            NoteUtils.closeSilently(is);
            return bitmap;
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
            return null;
        } finally {
            NoteUtils.closeSilently(is);
        }
    }

    private static int calculationSampleSize(int originalW, int originalH, int tw, int th) {
        int inSampleSize = Math.min(originalW / tw, originalH / th);
        return inSampleSize > 1 ? inSampleSize : 1;
    }

    private static Bitmap createTargetBitmap(Bitmap bitmap, int tw, int th, boolean isRecycle) {
        try {
            Bitmap targetBitmap = Bitmap.createBitmap(tw, th, Config.ARGB_8888);
            new Canvas(targetBitmap).drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, tw, th), new Paint(6));
            if (!isRecycle) {
                return targetBitmap;
            }
            bitmap.recycle();
            return targetBitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public static Bitmap getEffectBitmap(Context context, int effect, int w, int h) {
        if (effect == 0) {
            return getNoteBgNormalBitmap(context, w, h);
        }
        if (effect == 1) {
            return getNoteBgOldBitmap(context, w, h);
        }
        if (effect == 2) {
            return getNoteBgVeryOldBitmap(context, w, h);
        }
        if (effect == 3) {
            return getNoteBgVeryVeryOldBitmap(context, w, h);
        }
        return null;
    }

    private static Bitmap getNoteBgVeryVeryOldBitmap(Context context, int w, int h) {
        return getNoteBgBitmap(context, "note_bg_vv_old.png", "note_bg_vv_old_bottom.png", w, h);
    }

    private static Bitmap getNoteBgVeryOldBitmap(Context context, int w, int h) {
        return getNoteBgBitmap(context, "note_bg_very_old.png", "note_bg_very_old_bottom.png", w, h);
    }

    private static Bitmap getNoteBgOldBitmap(Context context, int w, int h) {
        return getNoteBgBitmap(context, "note_bg_old.png", "note_bg_old_bottom.png", w, h);
    }

    private static Bitmap getNoteBgBitmap(Context context, String normalName, String bottomName, int w, int h) {
        int[] oldBmpSize = getBitmapOriginalSize(context, normalName);
        if (!isValidBitmapSize(oldBmpSize)) {
            return null;
        }
        int[] oldBottomBmpSize = getBitmapOriginalSize(context, bottomName);
        if (!isValidBitmapSize(oldBottomBmpSize)) {
            return null;
        }
        Bitmap oldBmp = decodeRawBitmap(context, normalName, oldBmpSize[0], oldBmpSize[0], w, w);
        if (oldBmp == null) {
            return null;
        }
        Bitmap oldBottomBmp = decodeRawBitmap(context, bottomName, oldBottomBmpSize[0], oldBottomBmpSize[0], w, w);
        if (oldBottomBmp == null) {
            return null;
        }
        try {
            Bitmap dstBmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            Canvas canvas = new Canvas(dstBmp);
            Paint paint = new Paint(6);
            int oldBottomBmpW = oldBottomBmp.getWidth();
            int oldBottomBmpH = oldBottomBmp.getHeight();
            float dstBottomW = (float) w;
            float dstBottomH = ((float) oldBottomBmpH) * (dstBottomW / ((float) oldBottomBmpW));
            float dstBottomY = ((float) h) - dstBottomH;
            RectF bottomRectF = new RectF(0.0f, dstBottomY, 0.0f + dstBottomW, dstBottomY + dstBottomH);
            canvas.drawBitmap(oldBottomBmp, new Rect(0, 0, oldBottomBmpW, oldBottomBmpH), bottomRectF, paint);
            float leaveH = dstBottomY;
            int oldBmpW = oldBmp.getWidth();
            int oldBmpH = oldBmp.getHeight();
            float dstOldW = (float) w;
            float oldBmpScale = dstOldW / ((float) oldBmpW);
            float dstOldH = ((float) oldBmpH) * oldBmpScale;
            int drawCount = (int) (leaveH / dstOldH);
            Rect rect = new Rect(0, 0, oldBmpW, oldBmpH);
            RectF dstRectF = new RectF();
            for (int i = drawCount - 1; i >= 0; i--) {
                float top = leaveH - (((float) (drawCount - i)) * dstOldH);
                dstRectF.set(0.0f, top, dstOldW, top + dstOldH);
                canvas.drawBitmap(oldBmp, rect, dstRectF, paint);
            }
            float overPlus = leaveH - (((float) drawCount) * dstOldH);
            if (overPlus <= 0.0f) {
                return dstBmp;
            }
            dstRectF.set(0.0f, 0.0f, dstOldW, overPlus);
            rect = rect;
            rect.set(0, (int) (((float) oldBmpH) - (overPlus / oldBmpScale)), oldBmpW, oldBmpH);
            canvas.drawBitmap(oldBmp, rect, dstRectF, paint);
            return dstBmp;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private static boolean isValidBitmapSize(int[] bitmapSize) {
        boolean z = true;
        if (bitmapSize == null) {
            return false;
        }
        if (bitmapSize[0] == 0 || bitmapSize[1] == 0) {
            z = false;
        }
        return z;
    }

    private static Bitmap getNoteBgNormalBitmap(Context context, int w, int h) {
        try {
            Bitmap dstBmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            Canvas canvas = new Canvas(dstBmp);
            int color = context.getResources().getColor(R.color.new_note_activity_normal_bg_color);
            Paint paint = new Paint(6);
            paint.setColor(color);
            canvas.drawRect(new Rect(0, 0, w, h), paint);
            return dstBmp;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
}
