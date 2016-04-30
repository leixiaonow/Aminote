package com.gionee.note.common;

import android.os.Environment;
import java.io.File;

public class Constants {
    public static final char CHAR_NEW_LINE = '\n';
    public static final String MEDIA_BILL = "<bills:gionee/>";
    public static final String MEDIA_PHOTO = "<photo:gionee/>";
    public static final String MEDIA_SOUND = "<sound:gionee/>";
    public static final String NOTE = "/Notes";
    public static final File NOTE_MEDIA_IMAGE_TEMP_SHARE_PATH = new File(NOTE_MEDIA_PATH, "temp_share");
    public static final File NOTE_MEDIA_PATH;
    public static final File NOTE_MEDIA_PHOTO_PATH = new File(NOTE_MEDIA_PATH, "photo");
    public static final File NOTE_MEDIA_SOUND_PATH = new File(NOTE_MEDIA_PATH, "sound");
    public static final File NOTE_MEDIA_THUMBNAIL_PATH = new File(NOTE_MEDIA_PATH, "thumbnail");
    public static final int ONE_M = 1048576;
    public static final File ROOT_FILE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    public static final String STR_NEW_LINE = "\n";
    public static final String SYSTEM_ETC_DIR = "/system/etc/Amigo_Note/";

    static {
        if (PlatformUtil.isGioneeDevice()) {
            NOTE_MEDIA_PATH = new File(ROOT_FILE, "/.NoteMedia");
        } else {
            NOTE_MEDIA_PATH = new File(ROOT_FILE, "/.Media");
        }
    }
}
