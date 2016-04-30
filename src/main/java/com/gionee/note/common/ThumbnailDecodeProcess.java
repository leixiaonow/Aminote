package com.gionee.note.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import com.gionee.aminote.R;
import com.gionee.framework.log.Logger;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ThumbnailDecodeProcess {
    public static final String TAG = "ThumbnailDecodeProcess";
    private Context mContext;
    private int mHeight;
    private int mSizeThreshold;
    public ThumbnailDecodeMode mThumbnailDecodeMode;
    public Uri mUri;
    private int mWidth;

    public enum ThumbnailDecodeMode {
        CUT_WIDTH_AND_HEIGHT,
        WIDTH_FIXED_HEIGHT_SCALE,
        HEIGHT_FIXED_WIDTH_SCALE,
        WITH_AND_HEIGHT_SCALE
    }

    public ThumbnailDecodeProcess(Context context, Uri uri, int thumbnailWith, int thumbnailHeight, ThumbnailDecodeMode decodeMode) {
        this.mContext = context.getApplicationContext();
        this.mUri = uri;
        this.mWidth = thumbnailWith;
        this.mHeight = thumbnailHeight;
        this.mSizeThreshold = context.getResources().getDimensionPixelSize(R.dimen.attach_selector_pic_size_threshold);
        this.mThumbnailDecodeMode = decodeMode;
    }

    public Bitmap getThumbnail() {
        Closeable is = null;
        try {
            int[] size = DecodeUtils.loadBitmapSize(this.mContext, this.mUri);
            if (size == null) {
                return null;
            }
            is = this.mContext.getContentResolver().openInputStream(this.mUri);
            if (is == null) {
                NoteUtils.closeSilently(is);
                return null;
            }
            Bitmap decodeScaleThumbnail = decodeScaleThumbnail(is, size[0], size[1], this.mWidth, this.mHeight);
            NoteUtils.closeSilently(is);
            return decodeScaleThumbnail;
        } catch (FileNotFoundException e) {
            Logger.printLog(TAG, "decodeProcess FileNotFoundException:" + e);
            return null;
        } catch (Throwable e2) {
            Logger.printLog(TAG, "decodeProcess rawFileName fail" + e2);
            return null;
        } finally {
            NoteUtils.closeSilently(is);
        }
    }

    private Bitmap decodeScaleThumbnail(InputStream is, int originW, int originH, int targetW, int targetH) {
        float scale;
        int rotate = DecodeUtils.decodeImageRotate(this.mUri);
        if (rotate == 90 || rotate == 270) {
            scale = Math.max(((float) targetW) / ((float) originH), ((float) targetH) / ((float) originW));
        } else {
            scale = Math.max(((float) targetW) / ((float) originW), ((float) targetH) / ((float) originH));
        }
        Options options = new Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(scale);
        options.inMutable = true;
        Bitmap result = BitmapFactory.decodeStream(is, null, options);
        if (result == null) {
            return null;
        }
        if (rotate != 0) {
            result = DecodeUtils.rotate(result, rotate);
        }
        return resizeThumbnail(result, targetW, targetH);
    }

    private Bitmap resizeThumbnail(Bitmap bitmap, int targetW, int targetH) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == targetW && h == targetH) {
            return bitmap;
        }
        float scale = 0.0f;
        switch (this.mThumbnailDecodeMode) {
            case CUT_WIDTH_AND_HEIGHT:
                scale = Math.max(((float) targetW) / ((float) w), ((float) targetH) / ((float) h));
                break;
            case WIDTH_FIXED_HEIGHT_SCALE:
                scale = ((float) targetW) / ((float) w);
                targetH = Math.round(((float) h) * scale);
                break;
            case HEIGHT_FIXED_WIDTH_SCALE:
                float tempScale = ((float) targetH) / ((float) h);
                int tempTargetW = Math.round(((float) w) * tempScale);
                if (tempTargetW < this.mSizeThreshold) {
                    scale = Math.max(((float) targetW) / ((float) w), ((float) targetH) / ((float) h));
                    break;
                }
                scale = tempScale;
                targetW = tempTargetW;
                break;
        }
        Bitmap target = Bitmap.createBitmap(targetW, targetH, getConfig(bitmap));
        int width = Math.round(((float) w) * scale);
        int height = Math.round(((float) h) * scale);
        Canvas canvas = new Canvas(target);
        canvas.translate(((float) (targetW - width)) / 2.0f, ((float) (targetH - height)) / 2.0f);
        canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, new Paint(6));
        bitmap.recycle();
        return target;
    }

    private Config getConfig(Bitmap bitmap) {
        Config config = bitmap.getConfig();
        if (config == null) {
            return Config.ARGB_8888;
        }
        return config;
    }
}
