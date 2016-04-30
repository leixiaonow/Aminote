package com.gionee.note.common;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import com.gionee.framework.log.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {
    private static final int NO_SPACE_ERROR = -1;
    private static final String TAG = "StorageUtils";

    public static List<String> getLocalRootPath(Context context) {
        try {
            String[] volumePaths = (String[]) StorageManager.class.getDeclaredMethod("getVolumePaths", new Class[0]).invoke((StorageManager) context.getSystemService("storage"), new Object[0]);
            List<String> arrayList = new ArrayList();
            for (String path : volumePaths) {
                arrayList.add(path);
            }
            return arrayList;
        } catch (Exception ex) {
            Logger.printLog(TAG, "error:" + ex);
            return null;
        }
    }

    public static File createOtherSdCardFile(List<String> rootPaths, String absolutePath) {
        int length = absolutePath.length();
        String curPrefix = null;
        String suffix = null;
        for (String root : rootPaths) {
            if (absolutePath.startsWith(root)) {
                curPrefix = root;
                suffix = absolutePath.substring(root.length(), length);
                break;
            }
        }
        if (!(curPrefix == null || suffix == null)) {
            for (String prefix : rootPaths) {
                if (!prefix.equals(curPrefix)) {
                    return new File(prefix + suffix);
                }
            }
        }
        return null;
    }

    public static long getFileAvailableBytes(List<String> rootPaths, File file) {
        String curFileSD = null;
        for (String root : rootPaths) {
            if (file.getAbsolutePath().startsWith(root)) {
                curFileSD = root;
                break;
            }
        }
        return getAvailableBytes(curFileSD);
    }

    public static long getAvailableBytes(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        try {
            long availableBlocks;
            long blockSize;
            StatFs stat = new StatFs(new File(path).getAbsolutePath());
            if (VERSION.SDK_INT >= 18) {
                availableBlocks = stat.getAvailableBlocksLong();
                blockSize = stat.getBlockSizeLong();
            } else {
                availableBlocks = (long) stat.getAvailableBlocks();
                blockSize = (long) stat.getBlockSize();
            }
            return availableBlocks * blockSize;
        } catch (Exception e) {
            Logger.printLog(TAG, "Fail to access external storage:" + e);
            return -1;
        }
    }

    public static File getAvailableFileDirectory(Context context, long size, File defaultFile) {
        List<String> rootPaths = getLocalRootPath(context);
        File destPathRoot = defaultFile;
        if (getFileAvailableBytes(rootPaths, destPathRoot) > size) {
            return destPathRoot;
        }
        destPathRoot = createOtherSdCardFile(rootPaths, destPathRoot.getAbsolutePath());
        if (destPathRoot == null || getFileAvailableBytes(rootPaths, destPathRoot) <= size) {
            return null;
        }
        return destPathRoot;
    }
}
