package com.gionee.note.common;

import com.gionee.framework.log.Logger;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    private static String TAG = "FileUtils";

    public static boolean copyFile(String srcPath, String toPath) {
        Closeable fOs;
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        Exception e3;
        Logger.printLog(TAG, "begin to copy file");
        Closeable fIs = null;
        Closeable fOs2 = null;
        Closeable fCi = null;
        Closeable fCo = null;
        try {
            Closeable fIs2 = new FileInputStream(new File(srcPath));
            try {
                fOs = new FileOutputStream(new File(toPath));
            } catch (FileNotFoundException e4) {
                e = e4;
                fIs = fIs2;
                try {
                    Logger.printLog(TAG, "image file not found:" + e);
                    NoteUtils.closeSilently(fIs);
                    NoteUtils.closeSilently(fOs2);
                    NoteUtils.closeSilently(fCi);
                    NoteUtils.closeSilently(fCo);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    NoteUtils.closeSilently(fIs);
                    NoteUtils.closeSilently(fOs2);
                    NoteUtils.closeSilently(fCi);
                    NoteUtils.closeSilently(fCo);
                    throw th;
                }
            } catch (IOException e5) {
                e2 = e5;
                fIs = fIs2;
                Logger.printLog(TAG, "copy image throw IOException:" + e2);
                NoteUtils.closeSilently(fIs);
                NoteUtils.closeSilently(fOs2);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                return false;
            } catch (Exception e6) {
                e3 = e6;
                fIs = fIs2;
                Logger.printLog(TAG, "copy image throw other Exception:" + e3);
                NoteUtils.closeSilently(fIs);
                NoteUtils.closeSilently(fOs2);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                return false;
            } catch (Throwable th3) {
                th = th3;
                fIs = fIs2;
                NoteUtils.closeSilently(fIs);
                NoteUtils.closeSilently(fOs2);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                throw th;
            }
            try {
                fCi = fIs2.getChannel();
                fCo = fOs.getChannel();
                fCi.transferTo(0, fCi.size(), fCo);
                NoteUtils.closeSilently(fIs2);
                NoteUtils.closeSilently(fOs);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                fOs2 = fOs;
                fIs = fIs2;
                return true;
            } catch (FileNotFoundException e7) {
                e = e7;
                fOs2 = fOs;
                fIs = fIs2;
                Logger.printLog(TAG, "image file not found:" + e);
                NoteUtils.closeSilently(fIs);
                NoteUtils.closeSilently(fOs2);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                return false;
            } catch (IOException e8) {
                e2 = e8;
                fOs2 = fOs;
                fIs = fIs2;
                Logger.printLog(TAG, "copy image throw IOException:" + e2);
                NoteUtils.closeSilently(fIs);
                NoteUtils.closeSilently(fOs2);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                return false;
            } catch (Exception e9) {
                e3 = e9;
                fOs2 = fOs;
                fIs = fIs2;
                Logger.printLog(TAG, "copy image throw other Exception:" + e3);
                NoteUtils.closeSilently(fIs);
                NoteUtils.closeSilently(fOs2);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                return false;
            } catch (Throwable th4) {
                th = th4;
                fOs2 = fOs;
                fIs = fIs2;
                NoteUtils.closeSilently(fIs);
                NoteUtils.closeSilently(fOs2);
                NoteUtils.closeSilently(fCi);
                NoteUtils.closeSilently(fCo);
                throw th;
            }
        } catch (FileNotFoundException e10) {
            e = e10;
            Logger.printLog(TAG, "image file not found:" + e);
            NoteUtils.closeSilently(fIs);
            NoteUtils.closeSilently(fOs2);
            NoteUtils.closeSilently(fCi);
            NoteUtils.closeSilently(fCo);
            return false;
        } catch (IOException e11) {
            e2 = e11;
            Logger.printLog(TAG, "copy image throw IOException:" + e2);
            NoteUtils.closeSilently(fIs);
            NoteUtils.closeSilently(fOs2);
            NoteUtils.closeSilently(fCi);
            NoteUtils.closeSilently(fCo);
            return false;
        } catch (Exception e12) {
            e3 = e12;
            Logger.printLog(TAG, "copy image throw other Exception:" + e3);
            NoteUtils.closeSilently(fIs);
            NoteUtils.closeSilently(fOs2);
            NoteUtils.closeSilently(fCi);
            NoteUtils.closeSilently(fCo);
            return false;
        }
    }

    public static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }
                if (!file.delete()) {
                    Logger.printLog(TAG, "Failed to delete " + file);
                    success = false;
                }
            }
        }
        return success;
    }

    public static long getFileTotalSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }
        long total = 0;
        File[] files = file.listFiles();
        if (files == null) {
            return 0;
        }
        for (File f : files) {
            total += getFileTotalSize(f);
        }
        return total;
    }
}
