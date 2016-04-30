package com.gionee.note.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import uk.co.senab.photoview.IPhotoView;

public class ImageCache {
    private static int ONE_M = 1024;
    private static final String TAG = "ImageCache";
    private static ImageCache sImageCache;
    private int mMemCacheSize;
    private LruCache<String, Bitmap> mMemoryCache;

    public static ImageCache getInstance(Context context) {
        if (sImageCache == null) {
            sImageCache = new ImageCache(context);
        }
        return sImageCache;
    }

    private ImageCache(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metric);
        float density = metric.density;
        if (density >= IPhotoView.DEFAULT_MAX_SCALE) {
            this.mMemCacheSize = ONE_M * 16;
        } else if (density < 2.0f || density >= IPhotoView.DEFAULT_MAX_SCALE) {
            this.mMemCacheSize = ONE_M * 8;
        } else {
            this.mMemCacheSize = ONE_M * 12;
        }
        this.mMemoryCache = new LruCache<String, Bitmap>(this.mMemCacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                int bitmapSize = ImageCache.this.getBitmapSize(value) / 1024;
                return bitmapSize == 0 ? 1 : bitmapSize;
            }

            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    private int getBitmapSize(Bitmap bitmap) {
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public synchronized void addBitmapToCache(String data, Bitmap bitmap) {
        if (!(data == null || bitmap == null)) {
            if (this.mMemoryCache.get(data) == null) {
                this.mMemoryCache.put(data, bitmap);
            }
        }
    }

    public synchronized Bitmap getBitmapFromMemCache(String data) {
        return (Bitmap) this.mMemoryCache.get(data);
    }

    public int getMemCacheSize() {
        return this.mMemCacheSize;
    }

    void clearCache() {
        if (this.mMemoryCache != null) {
            this.mMemoryCache.evictAll();
        }
    }
}
