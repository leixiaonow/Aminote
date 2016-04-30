package com.gionee.note.app.inputbackup;

import android.content.ContentValues;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.note.common.Constants;
import com.gionee.note.common.FileUtils;
import com.gionee.note.provider.NoteContract.NoteContent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

class InputNoteInfo {
    private static final boolean DEBUG = false;
    private static final int LENGTH_THREE = 3;
    private static final int LENGTH_TWO = 2;
    private static final String TAG = "InputNoteInfo";
    private ArrayList<File> mMediaFiles;
    private String mText;

    InputNoteInfo() {
    }

    public void setText(String text) {
        this.mText = text;
    }

    public void putMediaFilePath(File mediaFile) {
        if (this.mMediaFiles == null) {
            this.mMediaFiles = new ArrayList();
        }
        this.mMediaFiles.add(mediaFile);
    }

    public void writToTemp() throws Exception {
        if (this.mText != null && this.mText.trim().length() > 0) {
            String[] ss = this.mText.split(String.valueOf('«'), -1);
            if (ss == null) {
                return;
            }
            ContentValues values;
            if (ss.length == 2) {
                values = new ContentValues();
                values.put(NoteContent.COLUMN_TITLE, ss[0]);
                values.put("content", modifyMediaInfoAndCopyMediaToTemp(ss[1].replaceAll(String.valueOf('»'), Constants.STR_NEW_LINE)));
                writeToDB(values);
            } else if (ss.length == 3) {
                values = new ContentValues();
                values.put(NoteContent.COLUMN_LABEL, ss[0]);
                values.put(NoteContent.COLUMN_TITLE, ss[1]);
                values.put("content", modifyMediaInfoAndCopyMediaToTemp(ss[2].replaceAll(String.valueOf('»'), Constants.STR_NEW_LINE)));
                writeToDB(values);
            }
        }
    }

    private boolean writeToDB(ContentValues values) throws Exception {
        ImportDBHelp dbHelp = new ImportDBHelp();
        try {
            boolean insertSuccess = dbHelp.insert(values) > 0;
            if (insertSuccess) {
                return insertSuccess;
            }
            throw new ImportError();
        } finally {
            dbHelp.close();
        }
    }

    private String modifyMediaInfoAndCopyMediaToTemp(String content) throws Exception {
        ArrayList<File> mediaFiles = this.mMediaFiles;
        if (mediaFiles == null || mediaFiles.size() == 0) {
            return content;
        }
        String newContent = content;
        Iterator i$ = mediaFiles.iterator();
        while (i$.hasNext()) {
            File file = (File) i$.next();
            File dstFile = getNewFile();
            File parentFile = dstFile.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new ImportError();
            } else if (FileUtils.copyFile(file.getPath(), dstFile.getPath())) {
                newContent = newContent.replaceAll(DataUpgrade.PREFIX + file.getName(), DataUpgrade.PREFIX + dstFile.getName());
            } else {
                throw new ImportError();
            }
        }
        return newContent;
    }

    private File getNewFile() {
        File file = new File(ImportBackUp.sTempSaveFileMedia, System.currentTimeMillis() + ".mp3");
        return !file.exists() ? file : getNewFile();
    }
}
