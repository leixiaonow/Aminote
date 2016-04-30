package com.gionee.note.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import com.gionee.framework.log.Logger;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DecodeUtils {
    private static final String TAG = "DecodeUtils";

    public static Bitmap decodeRawBitmap(Context context, String rawFileName) {
        Bitmap bitmap = null;
        Closeable is = null;
        try {
            is = context.getAssets().open(rawFileName);
            Options options = new Options();
            options.inMutable = true;
            options.inPreferredConfig = Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(is, null, options);
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
        return bitmap;
    }

    public static Bitmap decodeSystemBitmap(String fileName) {
        Throwable e;
        Throwable th;
        Bitmap bitmap = null;
        File file = new File(Constants.SYSTEM_ETC_DIR + fileName);
        if (file.exists()) {
            Closeable is = null;
            try {
                Closeable is2 = new FileInputStream(file);
                try {
                    Options options = new Options();
                    options.inMutable = true;
                    options.inPreferredConfig = Config.ARGB_8888;
                    bitmap = BitmapFactory.decodeStream(is2, null, options);
                    NoteUtils.closeSilently(is2);
                } catch (Throwable th2) {
                    th = th2;
                    is = is2;
                    NoteUtils.closeSilently(is);
                    throw th;
                }
            } catch (Throwable th3) {
                e = th3;
                Logger.printLog(TAG, "decodeRawBitmap rawFileName fail" + e);
                NoteUtils.closeSilently(is);
                return bitmap;
            }
        }
        return bitmap;
    }

    public static Bitmap decodeThumbnail(Context context, Uri uri, int targetW, int targetH, int rotation, boolean isCropped) {
        Bitmap bitmap = null;
        int[] size = loadBitmapSize(context, uri);
        if (size != null) {
            Closeable is = null;
            try {
                is = context.getContentResolver().openInputStream(uri);
                bitmap = decodeThumbnail(is, size[0], size[1], targetW, targetH, rotation, isCropped);
            } catch (Throwable e) {
                Logger.printLog(TAG, e.toString());
            } finally {
                NoteUtils.closeSilently(is);
            }
        }
        return bitmap;
    }

    public static int decodeImageRotate(Uri uri) {
        if ("file".equals(uri.getScheme())) {
            return getExifOrientation(uri.getPath());
        }
        return 0;
    }

    public static int getExifOrientation(String filepath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            Logger.printLog(TAG, "cannot read exif" + ex);
        }
        if (exif == null) {
            return 0;
        }
        int orientation = exif.getAttributeInt("Orientation", -1);
        if (orientation == -1) {
            return 0;
        }
        switch (orientation) {
            case 3:
                return com.gionee.appupgrade.common.utils.Config.DOWNLOAD_TIMEOUT_CHECK_TIMES;
            case 6:
                return 90;
            case 8:
                return 270;
            default:
                return 0;
        }
    }

    public static Bitmap decodeThumbnail(InputStream is, int originW, int originH, int targetW, int targetH, int rotation, boolean isCropped) {
        float scale;
        if (rotation == 90 || rotation == 270) {
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
        if (rotation != 0) {
            result = rotate(result, rotation);
        }
        return BitmapUtils.resizeAndCropCenter(result, targetW, targetH, true, isCropped);
    }

    public static Bitmap rotate(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) rotation);
        Bitmap rotationBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotationBitmap;
    }

    public static int[] loadBitmapSize(Context context, Uri uri) {
        Closeable is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                return null;
            }
            Options options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            int[] size = new int[]{options.outWidth, options.outHeight};
            NoteUtils.closeSilently(is);
            return size;
        } catch (FileNotFoundException e) {
            Logger.printLog(TAG, "loadBitmapSize error:" + e);
            return null;
        } finally {
            NoteUtils.closeSilently(is);
        }
    }

    public static Bitmap decodeBitmap(String filePath) {
        Throwable e;
        Throwable th;
        Closeable fis = null;
        try {
            Closeable fis2 = new FileInputStream(filePath);
            try {
                Bitmap decodeBitmap = decodeBitmap((InputStream) fis2);
                NoteUtils.closeSilently(fis2);
                fis = fis2;
                return decodeBitmap;
            } catch (Throwable th2) {
                th = th2;
                fis = fis2;
                NoteUtils.closeSilently(fis);
                throw th;
            }
        } catch (Throwable th3) {
            e = th3;
            Logger.printLog(TAG, "decodeBitmap filePath error: " + e);
            NoteUtils.closeSilently(fis);
            return null;
        }
    }

    public static Bitmap decodeBitmap(Context context, Uri uri) {
        Closeable is = null;
        Bitmap decodeBitmap;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                decodeBitmap = decodeBitmap((InputStream) is);
                return decodeBitmap;
            }
            NoteUtils.closeSilently(is);
            return null;
        } catch (Throwable e) {
            decodeBitmap = TAG;
            Logger.printLog(decodeBitmap, "decodeBitmap error:" + e);
        } finally {
            NoteUtils.closeSilently(is);
        }
    }

    public static Bitmap decodeBitmap(InputStream is) {
        try {
            return BitmapFactory.decodeStream(is);
        } catch (Throwable e) {
            Logger.printLog(TAG, "decodeBitmap error111 :" + e);
            return null;
        }
    }
}
