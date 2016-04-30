package com.gionee.appupgrade.common.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;
import com.amigoui.internal.util.HanziToPinyin.Token;
import com.gionee.note.common.Constants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogUtils {
    public static final int BACKUP_RESOTRE_AUTO_NOTIFY_ID = 201275;
    private static final String FILE_NAME = "gn_app_upgrade_log.txt";
    private static final boolean FLAG = true;
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
    private static final String LOG_HEAD = "GAU";
    private static final String TAG = "LogUtils";
    public static final String TAG_NETWORK = "Network";
    public static boolean sIsSaveLog = false;

    private static String formatLog(String log, String type, String level) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(FORMATTER.format(Calendar.getInstance().getTime()));
        builder.append("][");
        builder.append(type);
        builder.append("][");
        builder.append(level);
        builder.append("]");
        builder.append(log);
        builder.append(Constants.STR_NEW_LINE);
        return builder.toString();
    }

    public static void log(String tag, String msg) {
        Log.i("GAU." + tag, "" + msg);
        if (sIsSaveLog) {
            try {
                saveToSDCard(formatLog(msg, tag, "D"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void logv(String tag, String msg) {
        Log.v("GAU." + tag, "" + msg);
        if (sIsSaveLog) {
            try {
                saveToSDCard(formatLog(msg, tag, "V"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void logd(String tag, String msg) {
        Log.d("GAU." + tag, "" + msg);
        if (sIsSaveLog) {
            try {
                saveToSDCard(formatLog(msg, tag, "d"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loge(String tag, String msg) {
        Log.e("GAU." + tag + ".error", "" + msg);
        if (sIsSaveLog) {
            try {
                saveToSDCard(formatLog(msg, tag, "E"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void loge(String tag, String msg, Exception e) {
        Log.e("GAU." + tag, "" + msg, e);
        if (sIsSaveLog) {
            try {
                saveToSDCard(formatLog(msg, tag, "E"));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void saveToSDCard(String content) throws Exception {
        if (Environment.getExternalStorageState().equals("mounted")) {
            try {
                File file = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.seek(file.length());
                raf.write(content.getBytes());
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFunctionName() {
        StringBuffer sb = new StringBuffer();
        sb.append("-> ");
        sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
        sb.append("()");
        sb.append("-> ");
        return sb.toString();
    }

    public static String getThreadName() {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append(Thread.currentThread().getName());
            sb.append("-> ");
            sb.append(Thread.currentThread().getStackTrace()[3].getMethodName());
            sb.append("()");
            sb.append(Token.SEPARATOR);
        } catch (Exception e) {
            loge(TAG, e.getMessage());
        }
        return sb.toString();
    }

    public static synchronized String createtFileName() {
        String format;
        synchronized (LogUtils.class) {
            format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date(System.currentTimeMillis()));
        }
        return format;
    }

    public static String addSeparatorToPath(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        }
        return path + File.separator;
    }

    public static String formatFileLength(long fileLength) {
        long mb = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID;
        if (fileLength >= mb * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
            return String.format("%.1f GB", new Object[]{Float.valueOf(((float) fileLength) / ((float) (mb * PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)))});
        } else if (fileLength >= mb) {
            return String.format(((float) fileLength) / ((float) mb) > 100.0f ? "%.0f MB" : "%.1f MB", new Object[]{Float.valueOf(((float) fileLength) / ((float) mb))});
        } else if (fileLength >= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
            return String.format(((float) fileLength) / ((float) 1024) > 100.0f ? "%.0f KB" : "%.1f KB", new Object[]{Float.valueOf(((float) fileLength) / ((float) 1024))});
        } else {
            return String.format("%d B", new Object[]{Long.valueOf(fileLength)});
        }
    }

    public static String formatFileLengthEndMB(long fileLength) {
        return new DecimalFormat("0.00").format((double) (Float.parseFloat(Long.toString(fileLength)) / 1048576.0f)) + "MB";
    }

    public static byte[] drawable2Bytes(Drawable drawable) {
        return bitmap2Bytes(drawable2Bitmap(drawable));
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != -1 ? Config.ARGB_8888 : Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    public static Drawable bytes2Drawable(byte[] bytes) {
        Bitmap bitmap = bytes2Bimap(bytes);
        bitmap.setDensity(160);
        return new BitmapDrawable(bitmap);
    }

    public static void cancelNotification(Context context, int notificationId) {
        try {
            ((NotificationManager) context.getSystemService("notification")).cancel(notificationId);
        } catch (Exception e) {
        }
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, 1).show();
    }

    public static Drawable fetchApplicationIcon(Context context, String packageName) {
        Drawable icon = null;
        try {
            icon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return icon;
    }
}
