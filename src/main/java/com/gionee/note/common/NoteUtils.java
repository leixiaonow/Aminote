package com.gionee.note.common;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;
import com.gionee.aminote.R;
import com.gionee.appupgrade.common.NewVersion.VersionType;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.Config.EditPage;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.ImageCache;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.note.app.span.PhotoImageSpan;
import com.gionee.note.app.span.SoundImageSpan;
import com.gionee.note.common.ThumbnailDecodeProcess.ThumbnailDecodeMode;
import com.gionee.note.data.LocalNoteItem;
import com.gionee.note.data.LocalNoteSet;
import com.gionee.note.data.NoteInfo;
import com.gionee.note.data.NoteItem;
import com.gionee.note.data.Path;
import com.gionee.note.provider.NoteContract.NoteContent;
import com.gionee.note.widget.WidgetUtil;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

@SuppressWarnings(justification = "seems no problem", value = {"MS_CANNOT_BE_FINAL"})
public class NoteUtils {
    private static final boolean AT_BEFORE_23 = (VERSION.SDK_INT < 23);
    private static final boolean DEBUG = false;
    private static final String TAG = "NoteUtils";
    public static float sDensity;
    public static int sScreenHeight;
    public static int sScreenWidth;

    public static void initScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        sScreenWidth = Math.min(metrics.widthPixels, metrics.heightPixels);
        sScreenHeight = Math.max(metrics.widthPixels, metrics.heightPixels);
        sDensity = metrics.density;
    }

    public static Uri convertToFileUri(Context context, Uri uri) {
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            return uri;
        }
        String filePath;
        if (VERSION.SDK_INT < 19 || !DocumentsContract.isDocumentUri(context, uri)) {
            if (!"content".equals(scheme)) {
                return uri;
            }
            filePath = getDataColumn(context, uri, null, null);
            if (filePath != null) {
                return Uri.fromFile(new File(filePath));
            }
            return uri;
        } else if (!isMediaDocument(uri)) {
            return uri;
        } else {
            String type = DocumentsContract.getDocumentId(uri).split(DataUpgrade.SPLIT)[0];
            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String selection = "_id=?";
            filePath = getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
            if (filePath != null) {
                return Uri.fromFile(new File(filePath));
            }
            return uri;
        }
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                closeSilently(cursor);
                return string;
            }
            closeSilently(cursor);
            return null;
        } finally {
            closeSilently(cursor);
        }
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String formatDateTime(long timeInMs, SimpleDateFormat formatter) {
        return formatter.format(new Date(timeInMs));
    }

    public static String formatTime(int elapse, String connectChar) {
        int hour = elapse / 3600;
        int minute = (elapse % 3600) / 60;
        int second = elapse % 60;
        StringBuilder builder = new StringBuilder();
        appendFormat(builder, hour, true, connectChar);
        appendFormat(builder, minute, true, connectChar);
        appendFormat(builder, second, false, connectChar);
        return builder.toString();
    }

    private static void appendFormat(StringBuilder builder, int digital, boolean connect, String connectChar) {
        if (digital < 10) {
            builder.append(VersionType.NORMAL_VERSION);
        }
        builder.append(digital);
        if (connect) {
            builder.append(connectChar);
        }
    }

    public static void deleteOriginMediaFile(String json) {
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONArray spans = ((JSONObject) new JSONTokener(json).nextValue()).optJSONArray(DataConvert.JSON_SPANS_KEY);
                if (spans != null && spans.length() != 0) {
                    int length = spans.length();
                    String noteImageSpan = PhotoImageSpan.class.getName();
                    String soundImageSpan = SoundImageSpan.class.getName();
                    for (int i = 0; i < length; i++) {
                        JSONObject span = spans.getJSONObject(i);
                        String type = span.getString(DataConvert.SPAN_ITEM_TYPE);
                        if (type.equals(noteImageSpan)) {
                            deleteImageFile(Uri.parse(span.getString(PhotoImageSpan.ORIGIN_URI)), Uri.parse(span.getString(PhotoImageSpan.THUMB_URI)));
                        } else if (type.equals(soundImageSpan)) {
                            deleteSoundFile(span.getString(SoundImageSpan.ORIGIN_PATH));
                        }
                    }
                }
            } catch (Throwable e) {
                Logger.printLog(TAG, "getOriginData fail : " + e);
            }
        }
    }

    public static boolean deleteSoundFile(String originPath) {
        File originFile = new File(originPath);
        if (!originFile.exists() || originFile.delete()) {
            return true;
        }
        return false;
    }

    public static boolean deleteImageFile(Uri originUri, Uri thumbUri) {
        String originPath = originUri.getPath();
        String thumbPath = thumbUri.getPath();
        boolean deleteOriginSuccess = false;
        boolean deleteThumbSuccess = false;
        if (originPath.startsWith(Constants.NOTE_MEDIA_PHOTO_PATH.toString())) {
            File originFile = new File(originPath);
            if (originFile.exists()) {
                deleteOriginSuccess = originFile.delete();
            }
        }
        File thumbFile = new File(thumbPath);
        if (thumbFile.exists()) {
            deleteThumbSuccess = thumbFile.delete();
        }
        return deleteOriginSuccess && deleteThumbSuccess;
    }

    public static boolean saveBitmap(Bitmap bitmap, File file) {
        Throwable e;
        Throwable th;
        Closeable os = null;
        boolean success = false;
        try {
            Closeable os2 = new FileOutputStream(file);
            try {
                success = bitmap.compress(CompressFormat.JPEG, 90, os2);
                if (!success) {
                    file.delete();
                }
                closeSilently(os2);
                os = os2;
                return success;
            } catch (Throwable th2) {
                th = th2;
                os = os2;
                if (success) {
                    file.delete();
                }
                closeSilently(os);
                throw th;
            }
        } catch (Throwable th3) {
            e = th3;
            Logger.printLog(TAG, "error:" + e);
            if (success) {
                file.delete();
            }
            closeSilently(os);
            return false;
        }
    }

    public static boolean saveBitmap(Bitmap bitmap, File file, CompressFormat format, int quality) {
        Throwable e;
        Throwable th;
        Closeable os = null;
        boolean success = false;
        try {
            Closeable os2 = new FileOutputStream(file);
            try {
                success = bitmap.compress(format, quality, os2);
                if (!success) {
                    file.delete();
                }
                closeSilently(os2);
                os = os2;
                return success;
            } catch (Throwable th2) {
                th = th2;
                os = os2;
                if (success) {
                    file.delete();
                }
                closeSilently(os);
                throw th;
            }
        } catch (Throwable th3) {
            e = th3;
            Logger.printLog(TAG, "error:" + e);
            if (success) {
                file.delete();
            }
            closeSilently(os);
            return false;
        }
    }

    public static File getSaveImageFile(File fileDirectory) {
        File file = new File(fileDirectory, "/" + System.currentTimeMillis());
        if (!file.exists()) {
            return file;
        }
        Random random = new Random();
        do {
            file = new File(fileDirectory, "/" + (System.currentTimeMillis() + random.nextLong()));
        } while (file.exists());
        return file;
    }

    public static boolean fileNotFound(Context context, String filePath) {
        if (new File(filePath).exists()) {
            return false;
        }
        Toast.makeText(context, R.string.file_note_found, 0).show();
        return true;
    }

    public static String customName(Context context, String path) {
        StorageManager storageManager = (StorageManager) context.getSystemService("storage");
        if (storageManager == null) {
            return path;
        }
        try {
            Class<?> property = Class.forName("android.os.storage.StorageVolume");
            Object[] storageVolume = (Object[]) StorageManager.class.getDeclaredMethod("getVolumeList", new Class[0]).invoke(storageManager, new Object[0]);
            if (storageVolume == null) {
                return path;
            }
            int length = storageVolume.length;
            Method method = property.getDeclaredMethod("getPath", new Class[0]);
            for (int i = 0; i < length; i++) {
                String rootPath = (String) method.invoke(storageVolume[i], new Object[0]);
                if (path.startsWith(rootPath)) {
                    path = ((String) property.getDeclaredMethod("getDescription", new Class[]{Context.class}).invoke(storageVolume[i], new Object[]{context})) + path.substring(rootPath.length(), path.length());
                    break;
                }
            }
            return path;
        } catch (Exception e) {
            Logger.printLog(TAG, "customName fail:" + e);
        }
    }

    public static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            throw new AssertionError(msg);
        }
    }

    public static void flushSilently(Flushable f) {
        if (f != null) {
            try {
                f.flush();
            } catch (IOException t) {
                Logger.printLog(TAG, "flush fail :" + t);
            }
        }
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException t) {
                Logger.printLog(TAG, "close fail :" + t);
            }
        }
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        if (fd != null) {
            try {
                fd.close();
            } catch (Throwable t) {
                Logger.printLog(TAG, "fail to close:" + t);
            }
        }
    }

    public static void closeSilently(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable t) {
                Logger.printLog(TAG, "fail to close:" + t);
            }
        }
    }

    public static void closeSilently(SQLiteDatabase database) {
        if (database != null) {
            try {
                database.close();
            } catch (Throwable t) {
                Logger.printLog(TAG, "fail to close:" + t);
            }
        }
    }

    public static int prevPowerOf2(int n) {
        if (n > 0) {
            return Integer.highestOneBit(n);
        }
        throw new IllegalArgumentException();
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        return x < min ? min : x;
    }

    public static void fail(String message, Object... args) {
        if (args.length != 0) {
            message = String.format(message, args);
        }
        throw new AssertionError(message);
    }

    public static boolean equals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static String createPlainTextJsonContent(String content) {
        try {
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object();
            jsonStringer.key(DataConvert.JSON_CONTENT_KEY).value(content);
            jsonStringer.endObject();
            return jsonStringer.toString();
        } catch (JSONException e) {
            Logger.printLog(TAG, "error:" + e);
            return null;
        }
    }

    public static ArrayList<Integer> indexofs(String content, String subStr) {
        if (!content.contains(subStr)) {
            return null;
        }
        int subStrLength = subStr.length();
        int fromIndex = 0;
        ArrayList<Integer> indexs = null;
        while (true) {
            int index = content.indexOf(subStr, fromIndex);
            if (index < 0) {
                return indexs;
            }
            if (indexs == null) {
                indexs = new ArrayList(3);
            }
            indexs.add(Integer.valueOf(index));
            fromIndex = index + subStrLength;
        }
    }

    public static boolean checkHasSmartBar() {
        boolean hasSmartBar = false;
        try {
            hasSmartBar = ((Boolean) Class.forName("android.os.Build").getMethod("hasSmartBar", new Class[0]).invoke(null, new Object[0])).booleanValue();
            if (!false) {
                return hasSmartBar;
            }
            if (Build.DEVICE.equals("mx2")) {
                return true;
            }
            if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                return false;
            }
            return hasSmartBar;
        } catch (SecurityException e) {
            if (1 == null) {
                return hasSmartBar;
            }
            if (Build.DEVICE.equals("mx2")) {
                return true;
            }
            if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                return false;
            }
            return hasSmartBar;
        } catch (NoSuchMethodException e2) {
            if (1 == null) {
                return hasSmartBar;
            }
            if (Build.DEVICE.equals("mx2")) {
                return true;
            }
            if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                return false;
            }
            return hasSmartBar;
        } catch (ClassNotFoundException e3) {
            if (1 == null) {
                return hasSmartBar;
            }
            if (Build.DEVICE.equals("mx2")) {
                return true;
            }
            if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                return false;
            }
            return hasSmartBar;
        } catch (IllegalArgumentException e4) {
            if (1 == null) {
                return hasSmartBar;
            }
            if (Build.DEVICE.equals("mx2")) {
                return true;
            }
            if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                return false;
            }
            return hasSmartBar;
        } catch (IllegalAccessException e5) {
            if (1 == null) {
                return hasSmartBar;
            }
            if (Build.DEVICE.equals("mx2")) {
                return true;
            }
            if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                return false;
            }
            return hasSmartBar;
        } catch (InvocationTargetException e6) {
            if (1 == null) {
                return hasSmartBar;
            }
            if (Build.DEVICE.equals("mx2")) {
                return true;
            }
            if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                return false;
            }
            return hasSmartBar;
        } catch (Throwable th) {
            if (1 != null) {
                if (!Build.DEVICE.equals("mx2")) {
                    if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
                    }
                }
            }
        }
    }

    public static String lineSpaceFilter(String source) {
        return Pattern.compile("\\s*|\t|\r|\n").matcher(source).replaceAll("");
    }

    public static int getIdFromPath(String path) {
        int id = -1;
        String pathString = path;
        if (TextUtils.isEmpty(pathString)) {
            return -1;
        }
        String[] pathArray = Path.split(pathString);
        if (pathArray.length >= 1) {
            id = parseInt(pathArray[pathArray.length - 1]);
        }
        if (isExist(id)) {
            return id;
        }
        return -1;
    }

    private static boolean isExist(int id) {
        Cursor cursor = LocalNoteSet.getItemCursor(NoteAppImpl.getContext().getContentResolver(), NoteContent.CONTENT_URI, null, id);
        if (cursor != null) {
            try {
                if (cursor.getCount() != 0) {
                    return true;
                }
            } finally {
                closeSilently(cursor);
            }
        }
        closeSilently(cursor);
        return false;
    }

    private static int parseInt(String id) {
        int parseId = -1;
        try {
            parseId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
        }
        return parseId;
    }

    public static NoteInfo getNoteItemFromDB(int id) {
        Throwable th;
        NoteInfo item = null;
        Cursor cursor = LocalNoteSet.getItemCursor(NoteAppImpl.getContext().getContentResolver(), NoteContent.CONTENT_URI, LocalNoteSet.NOTE_PROJECTION, id);
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                NoteInfo item2 = new NoteInfo();
                try {
                    item2.mId = cursor.getInt(0);
                    item2.mTitle = cursor.getString(1);
                    item2.mContent = cursor.getString(2);
                    item2.mLabel = convertLabel(cursor.getString(3));
                    item2.mDateCreatedInMs = cursor.getLong(4);
                    item2.mDateModifiedInMs = cursor.getLong(5);
                    item2.mDateReminderInMs = cursor.getLong(6);
                    item = item2;
                } catch (Throwable th2) {
                    th = th2;
                    item = item2;
                    closeSilently(cursor);
                    throw th;
                }
            }
            closeSilently(cursor);
            return item;
        } catch (Throwable th3) {
            th = th3;
            closeSilently(cursor);
            throw th;
        }
    }

    public static ArrayList<Integer> convertLabel(String labels) {
        ArrayList<Integer> label = new ArrayList();
        label.clear();
        if (labels != null) {
            for (String temp : labels.split(LocalNoteItem.LABEL_SEPARATOR)) {
                label.add(Integer.valueOf(Integer.parseInt(temp)));
            }
        }
        return label;
    }

    public static int[] getToady() {
        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTimeInMillis(System.currentTimeMillis());
        int year = curCalendar.get(1);
        int month = curCalendar.get(2);
        int day = curCalendar.get(5);
        return new int[]{year, month, day};
    }

    public static boolean isSomeDay(int[] day1, int[] day2) {
        boolean z = true;
        if (day1[2] != day2[2] || day1[1] != day2[1]) {
            return false;
        }
        if (day1[0] != day2[0]) {
            z = false;
        }
        return z;
    }

    public static void updateNoteData(String title, String jsonContent, ContentResolver resolver, int noteId, long modifiedTime, long reminderInMs, ArrayList<Integer> label) {
        ContentValues values = new ContentValues();
        values.put(NoteContent.COLUMN_TITLE, title);
        values.put("content", jsonContent);
        values.put(NoteContent.COLUMN_DATE_MODIFIED, Long.valueOf(modifiedTime));
        values.put(NoteContent.COLUMN_REMINDER, Long.valueOf(reminderInMs));
        values.put(NoteContent.COLUMN_LABEL, NoteItem.convertToStringLabel(label));
        String[] selectionArgs = new String[]{String.valueOf(noteId)};
        resolver.update(NoteContent.CONTENT_URI, values, "_id=?", selectionArgs);
        WidgetUtil.updateWidget(title, jsonContent, noteId, modifiedTime, reminderInMs);
    }

    public static int addNoteData(String title, String jsonContent, ContentResolver resolver, long modifiedTime, long reminderInMs, ArrayList<Integer> label) {
        ContentValues values = new ContentValues();
        values.put(NoteContent.COLUMN_TITLE, title);
        values.put("content", jsonContent);
        values.put(NoteContent.COLUMN_DATE_MODIFIED, Long.valueOf(modifiedTime));
        values.put(NoteContent.COLUMN_REMINDER, Long.valueOf(reminderInMs));
        values.put(NoteContent.COLUMN_LABEL, NoteItem.convertToStringLabel(label));
        values.put(NoteContent.COLUMN_DATE_CREATED, Long.valueOf(modifiedTime));
        return (int) ContentUris.parseId(resolver.insert(NoteContent.CONTENT_URI, values));
    }

    public static boolean checkEnoughFreeMemory() {
        if (Runtime.getRuntime().maxMemory() - ((long) ((int) Runtime.getRuntime().totalMemory())) <= getRecommendFreeMemory()) {
            return false;
        }
        return true;
    }

    public static long getRecommendFreeMemory() {
        return ((long) (((double) Runtime.getRuntime().maxMemory()) * 0.2d)) - ((long) ImageCache.getInstance(NoteAppImpl.getContext()).getMemCacheSize());
    }

    public static Bitmap getAddBitmapFromUri(Context context, Uri uri) {
        EditPage config = EditPage.get(context);
        return new ThumbnailDecodeProcess(context, uri, config.mImageWidth, config.mImageHeight, ThumbnailDecodeMode.WIDTH_FIXED_HEIGHT_SCALE).getThumbnail();
    }

    public static File getSaveBitmapFile(Context context) {
        EditPage page = EditPage.get(context);
        File fileDirectory = StorageUtils.getAvailableFileDirectory(context, (long) ((page.mImageWidth * page.mImageHeight) * 4), Constants.NOTE_MEDIA_THUMBNAIL_PATH);
        if (fileDirectory == null) {
            fileDirectory = Constants.NOTE_MEDIA_THUMBNAIL_PATH;
        }
        if (fileDirectory.exists() || fileDirectory.mkdirs()) {
            return getSaveImageFile(fileDirectory);
        }
        return null;
    }

    public static boolean isContentEmpty(Editable editable) {
        String content = editable.toString().trim();
        if (TextUtils.isEmpty(content)) {
            return true;
        }
        for (String text : content.replaceAll(Constants.MEDIA_BILL, "").split(Constants.STR_NEW_LINE)) {
            if (!TextUtils.isEmpty(text)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == '\u0000' || codePoint == '\t' || codePoint == '\n' || codePoint == '\r' || ((codePoint >= ' ' && codePoint <= '퟿') || ((codePoint >= '' && codePoint <= '�') || (codePoint >= '\u0000' && codePoint <= '￿')))) ? false : true;
    }

    public static boolean checkExternalStoragePermission() {
        if (AT_BEFORE_23 || ContextCompat.checkSelfPermission(NoteAppImpl.getContext(), "android.permission.READ_EXTERNAL_STORAGE") == 0) {
            return true;
        }
        return false;
    }

    public static boolean checkRecordAudioPermission() {
        if (AT_BEFORE_23 || ContextCompat.checkSelfPermission(NoteAppImpl.getContext(), "android.permission.RECORD_AUDIO") == 0) {
            return true;
        }
        return false;
    }

    public static boolean checkPhoneStatePermission() {
        if (AT_BEFORE_23 || ContextCompat.checkSelfPermission(NoteAppImpl.getContext(), "android.permission.READ_PHONE_STATE") == 0) {
            return true;
        }
        return false;
    }
}
