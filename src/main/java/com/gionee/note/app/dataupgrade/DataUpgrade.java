package com.gionee.note.app.dataupgrade;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.util.SparseArray;
import com.gionee.note.app.BuiltInNote;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.LabelManager;
import com.gionee.note.app.LabelManager.LabelHolder;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.reminder.ReminderManager;
import com.gionee.note.app.span.SoundImageSpan;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.provider.NoteContract.NoteContent;
import com.gionee.note.provider.NoteProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class DataUpgrade {
    private static final int CODE_OK = 0;
    private static final int CODE_OLD_DB_QUERY_COUNT_ZERO = -1;
    private static final boolean DEBUG = false;
    private static final int FAIL_CODE_INSERTOLDTONEWDB_ERROR = 5;
    private static final int FAIL_CODE_NEW_DB_NULL = 6;
    private static final int FAIL_CODE_OLD_DB_NULL = 2;
    private static final int FAIL_CODE_OLD_DB_QUERY_CURSOR_NULL = 4;
    private static final int FAIL_CODE_OLD_DB_QUERY_ERROR = 3;
    private static final int FAIL_CODE_OPEN_OLD_DB_ERROR = 1;
    public static final int MAX_LABEL_LENGTH = 12;
    public static final int MAX_TITLE_LENGTH = 15;
    public static final String NO = "no";
    private static final String OLD_ALARM_TIME = "atime";
    private static final String OLD_CONTENT = "content";
    private static final String OLD_DB_NAME = "Notes";
    private static final String OLD_ID = "_id";
    private static final String OLD_IS_FOLDER = "isfolder";
    private static final String OLD_MEDIA_FILE_NAME = "mediaFileName";
    private static final String OLD_MEDIA_TABLE_NAME = "MediaItems";
    private static final String OLD_NOTE_ID = "noteId";
    private static final String OLD_NOTE_TITLE = "nodeTitle";
    private static final String OLD_PARENT_FOLDER = "parentfile";
    private static final String OLD_TABLE_NAME = "items";
    private static final String OLD_UPDATE_DATE = "cdate";
    private static final String OLD_UPDATE_TIME = "ctime";
    public static final String PREFIX = "<gionee_media:0:";
    public static final String SPLIT = ":";
    public static final String SUFFIX = "/>>";
    private static final String TAG = "DataUpgrade";
    public static final String YES = "yes";
    private volatile int mFailCode;
    private volatile boolean mIsUpgradeFinish;
    private volatile int mOldDbVersion;
    private volatile int mProgress;
    private volatile boolean mUpgradeFail;
    private volatile int mUpgradeFailCount;
    private volatile int mUpgradeSuccessCount;
    private volatile int mUpgradeTotalCount;

    public DataUpgrade() {
        start();
    }

    public String toString() {
        return "mOldDbVersion = " + this.mOldDbVersion + ",mIsUpgradeFinish = " + this.mIsUpgradeFinish + ",mProgress = " + this.mProgress;
    }

    public boolean isUpgradeFail() {
        return this.mUpgradeFail;
    }

    public boolean isUpgradeFinish() {
        return this.mIsUpgradeFinish;
    }

    public int getUpgradeTotalCount() {
        return this.mUpgradeTotalCount;
    }

    public int getUpgradeSuccessCount() {
        return this.mUpgradeSuccessCount;
    }

    public int getUpgradeFailCount() {
        return this.mUpgradeFailCount;
    }

    public int getFailCode() {
        return this.mFailCode;
    }

    private void updateUpgradeTotalCount(int upgradeTotalCount) {
        this.mUpgradeTotalCount = upgradeTotalCount;
    }

    private void updateUpgradeFailCount(int upgradeFailCount) {
        this.mUpgradeFailCount = upgradeFailCount;
    }

    private void updateUpgradeSuccessCount(int upgradeSuccessCount) {
        this.mUpgradeSuccessCount = upgradeSuccessCount;
    }

    private void upgradeFail(int failCode) {
        this.mFailCode = failCode;
        this.mUpgradeFail = true;
    }

    private void upgradeFinish() {
        updateProgress(100);
        this.mIsUpgradeFinish = true;
        NoteAppImpl.getContext().notifyDbInitComplete();
    }

    private void setOldDBVersion(int version) {
        this.mOldDbVersion = version;
        Log.d(TAG, "oldVersion = " + version);
    }

    private void start() {
        final NoteAppImpl appImpl = NoteAppImpl.getContext();
        appImpl.getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                DataUpgrade.this.updateProgress(1);
                ArrayList<OldNoteFolderData> oldNoteFolderDatas = new ArrayList();
                ArrayList<OldNoteItemData> oldNoteItemDatas = new ArrayList();
                int code = DataUpgrade.this.getNoteData(appImpl, oldNoteFolderDatas, oldNoteItemDatas);
                if (code > 0) {
                    DataUpgrade.this.upgradeFail(code);
                } else if (code == -1) {
                    Log.d(DataUpgrade.TAG, "CODE_OLD_DB_QUERY_COUNT_ZERO");
                    DataUpgrade.this.upgradeFinish();
                    BuiltInNote.insertBuildInNoteSync();
                    DataUpgrade.this.delOldDBFile(appImpl);
                } else {
                    BuiltInNote.setIsFirstLaunch(appImpl, false);
                    DataUpgrade.this.updateProgress(3);
                    if (oldNoteFolderDatas.size() > 0) {
                        DataUpgrade.this.converFolderToLabel(appImpl, oldNoteFolderDatas);
                        DataUpgrade.this.updateDataLabel(oldNoteFolderDatas, oldNoteItemDatas);
                    }
                    DataUpgrade.this.updateProgress(5);
                    SparseArray<ArrayList<String>> mediaArray = new SparseArray();
                    code = DataUpgrade.this.getMediaData(appImpl, mediaArray);
                    if (code > 0) {
                        DataUpgrade.this.upgradeFail(code);
                    } else {
                        DataUpgrade.this.updateProgress(7);
                        if (mediaArray.size() != 0) {
                            DataUpgrade.this.updateNoteMedia(mediaArray, oldNoteItemDatas);
                        }
                        DataUpgrade.this.updateOverplusNoteMedia(oldNoteItemDatas);
                        DataUpgrade.this.updateProgress(9);
                        DataUpgrade.this.updateNoteJsonContent(oldNoteItemDatas);
                        DataUpgrade.this.updateProgress(11);
                        code = DataUpgrade.this.insertOldToNewDB(appImpl, oldNoteItemDatas);
                        if (code > 0) {
                            DataUpgrade.this.upgradeFail(code);
                        } else {
                            appImpl.getContentResolver().notifyChange(NoteContent.CONTENT_URI, null);
                            ReminderManager.scheduleReminder(appImpl);
                            DataUpgrade.this.updateProgress(99);
                            DataUpgrade.this.delOldDBFile(appImpl);
                            DataUpgrade.this.upgradeFinish();
                        }
                    }
                }
                return null;
            }
        });
    }

    private int getNoteData(NoteAppImpl appImpl, ArrayList<OldNoteFolderData> oldNoteFolderDatas, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        SQLiteDatabase oldDb = null;
        try {
            oldDb = SQLiteDatabase.openDatabase(appImpl.getDatabasePath(OLD_DB_NAME).getPath(), null, 0);
            if (oldDb == null) {
                Log.w(TAG, "oldDb = null");
                NoteUtils.closeSilently(oldDb);
                return 2;
            }
            setOldDBVersion(oldDb.getVersion());
            Cursor noteDataCursor = null;
            noteDataCursor = oldDb.query(OLD_TABLE_NAME, new String[]{OLD_ID, "content", OLD_UPDATE_DATE, OLD_UPDATE_TIME, OLD_ALARM_TIME, OLD_IS_FOLDER, OLD_PARENT_FOLDER, OLD_NOTE_TITLE}, null, null, null, null, null);
            int i;
            if (noteDataCursor == null) {
                i = 4;
                NoteUtils.closeSilently(oldDb);
                return i;
            } else if (noteDataCursor.getCount() == 0) {
                NoteUtils.closeSilently(noteDataCursor);
                NoteUtils.closeSilently(oldDb);
                return -1;
            } else {
                while (noteDataCursor.moveToNext()) {
                    try {
                        try {
                            int id = noteDataCursor.getInt(0);
                            String content = noteDataCursor.getString(1);
                            String cdate = noteDataCursor.getString(2);
                            String ctime = noteDataCursor.getString(3);
                            long atime = noteDataCursor.getLong(4);
                            String isfolder = noteDataCursor.getString(5);
                            String parentfile = noteDataCursor.getString(6);
                            String nodeTitle = noteDataCursor.getString(7);
                            if (YES.equals(isfolder)) {
                                oldNoteFolderDatas.add(new OldNoteFolderData(id, nodeTitle));
                            } else {
                                oldNoteItemDatas.add(new OldNoteItemData(id, content, cdate, ctime, atime, parentfile, nodeTitle));
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "error", e);
                        }
                    } catch (Exception e2) {
                        Log.w(TAG, "error", e2);
                        i = 3;
                        NoteUtils.closeSilently(oldDb);
                        return i;
                    } finally {
                        NoteUtils.closeSilently(noteDataCursor);
                    }
                }
                NoteUtils.closeSilently(noteDataCursor);
                NoteUtils.closeSilently(oldDb);
                return 0;
            }
        } catch (SQLiteException e3) {
            try {
                Log.w(TAG, "error", e3);
                return 1;
            } finally {
                NoteUtils.closeSilently(oldDb);
            }
        }
    }

    private boolean delOldDBFile(NoteAppImpl appImpl) {
        File oldDBFile = appImpl.getDatabasePath(OLD_DB_NAME);
        if (oldDBFile.exists()) {
            return SQLiteDatabase.deleteDatabase(oldDBFile);
        }
        return false;
    }

    private void updateProgress(int progress) {
        this.mProgress = progress;
    }

    public int getProgress() {
        return this.mProgress;
    }

    private void converFolderToLabel(NoteAppImpl appImpl, ArrayList<OldNoteFolderData> oldNoteFolderDatas) {
        LabelManager labelManager = appImpl.getLabelManager();
        ArrayList<LabelHolder> labels = getLabels(labelManager);
        Iterator i$ = oldNoteFolderDatas.iterator();
        while (i$.hasNext()) {
            OldNoteFolderData folderData = (OldNoteFolderData) i$.next();
            String labelName = folderData.getName().trim();
            if (labelName.length() > 12) {
                labelName = labelName.substring(0, 12);
            }
            int id = getLabelId(labels, labelName);
            if (id < 0) {
                id = labelManager.addLabel(labelName);
                labels = getLabels(labelManager);
            }
            folderData.setNewId(id);
        }
    }

    private void updateDataLabel(ArrayList<OldNoteFolderData> oldNoteFolderDatas, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        Iterator i$ = oldNoteItemDatas.iterator();
        while (i$.hasNext()) {
            OldNoteItemData noteItem = (OldNoteItemData) i$.next();
            OldNoteFolderData folderData = OldNoteFolderData.getNoteFolderData(noteItem.getFolderId(), oldNoteFolderDatas);
            if (folderData != null) {
                noteItem.setLabel(Integer.toString(folderData.getNewId()));
            }
        }
    }

    private int getMediaData(NoteAppImpl appImpl, SparseArray<ArrayList<String>> mediaArray) {
        int i;
        SQLiteDatabase oldDb = null;
        try {
            oldDb = SQLiteDatabase.openDatabase(appImpl.getDatabasePath(OLD_DB_NAME).getPath(), null, 0);
            if (oldDb == null) {
                Log.w(TAG, "oldDb = null");
                NoteUtils.closeSilently(oldDb);
                return 2;
            }
            Cursor noteMediaCursor = null;
            noteMediaCursor = oldDb.query(OLD_MEDIA_TABLE_NAME, new String[]{OLD_NOTE_ID, OLD_MEDIA_FILE_NAME}, null, null, null, null, null);
            if (noteMediaCursor == null) {
                i = 0;
                NoteUtils.closeSilently(oldDb);
                return i;
            }
            while (noteMediaCursor.moveToNext()) {
                try {
                    try {
                        int noteId = noteMediaCursor.getInt(0);
                        String fileName = noteMediaCursor.getString(1);
                        ArrayList<String> fileNames = (ArrayList) mediaArray.get(noteId);
                        if (fileNames == null) {
                            fileNames = new ArrayList();
                            mediaArray.put(noteId, fileNames);
                        }
                        fileNames.add(fileName);
                    } catch (Exception e) {
                        Log.w(TAG, "error", e);
                    }
                } catch (Exception e2) {
                    Log.w(TAG, "error", e2);
                    i = 0;
                    NoteUtils.closeSilently(oldDb);
                    return i;
                } finally {
                    NoteUtils.closeSilently(noteMediaCursor);
                }
            }
            NoteUtils.closeSilently(noteMediaCursor);
            NoteUtils.closeSilently(oldDb);
            return 0;
        } catch (SQLiteException e3) {
            try {
                Log.w(TAG, "error", e3);
                return 1;
            } finally {
                NoteUtils.closeSilently(oldDb);
            }
        }
    }

    private void updateNoteMedia(SparseArray<ArrayList<String>> mediaArray, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        int size = mediaArray.size();
        for (int i = 0; i < size; i++) {
            int id = mediaArray.keyAt(i);
            OldNoteItemData noteItemData = OldNoteItemData.getNoteItemData(id, oldNoteItemDatas);
            if (noteItemData != null) {
                noteItemData.resolveMedia((ArrayList) mediaArray.get(id));
            }
        }
    }

    private void updateOverplusNoteMedia(ArrayList<OldNoteItemData> oldNoteItemDatas) {
        Iterator i$ = oldNoteItemDatas.iterator();
        while (i$.hasNext()) {
            OldNoteItemData noteItemData = (OldNoteItemData) i$.next();
            if (noteItemData.getSubs() == null) {
                noteItemData.resolveMedia();
            }
        }
    }

    private void updateNoteJsonContent(ArrayList<OldNoteItemData> oldNoteItemDatas) {
        Iterator i$ = oldNoteItemDatas.iterator();
        while (i$.hasNext()) {
            OldNoteItemData noteItemData = (OldNoteItemData) i$.next();
            noteItemData.setJsonContent(recoveryJsonContent(noteItemData));
        }
    }

    private int insertOldToNewDB(NoteAppImpl appImpl, ArrayList<OldNoteItemData> oldNoteItemDatas) {
        SQLiteDatabase oldDb = null;
        SQLiteDatabase newDb = null;
        int i;
        try {
            oldDb = SQLiteDatabase.openDatabase(appImpl.getDatabasePath(OLD_DB_NAME).getPath(), null, 0);
            if (oldDb == null) {
                Log.w(TAG, "oldDb = null");
                i = 2;
                return i;
            }
            newDb = SQLiteDatabase.openOrCreateDatabase(appImpl.getDatabasePath(NoteProvider.DATABASE_NAME).getPath(), null);
            if (newDb == null) {
                Log.w(TAG, "oldDb = null");
                NoteUtils.closeSilently(oldDb);
                NoteUtils.closeSilently(newDb);
                return 6;
            }
            int upgradeTotalCount = oldNoteItemDatas.size();
            updateUpgradeTotalCount(upgradeTotalCount);
            int successCount = 0;
            int failCount = 0;
            int curProgress = getProgress();
            Iterator i$ = oldNoteItemDatas.iterator();
            while (i$.hasNext()) {
                OldNoteItemData noteItemData = (OldNoteItemData) i$.next();
                ContentValues values = new ContentValues();
                String title = noteItemData.getNoteTile();
                if (title.length() > 15) {
                    title = title.substring(0, 15);
                }
                values.put(NoteContent.COLUMN_TITLE, title);
                values.put("content", noteItemData.getJsonContent());
                values.put(NoteContent.COLUMN_DATE_CREATED, Long.valueOf(noteItemData.getCreateTime()));
                values.put(NoteContent.COLUMN_DATE_MODIFIED, Long.valueOf(noteItemData.getCreateTime()));
                values.put(NoteContent.COLUMN_REMINDER, Long.valueOf(noteItemData.getAlarmTime()));
                values.put(NoteContent.COLUMN_LABEL, noteItemData.getLabelId());
                if (newDb.insert(NoteContent.TABLE_NAME, null, values) > 0) {
                    successCount++;
                    oldDb.delete(OLD_TABLE_NAME, "_id = ?", new String[]{String.valueOf(noteItemData.getId())});
                } else {
                    failCount++;
                }
                updateProgress((((successCount + failCount) / upgradeTotalCount) * 87) + curProgress);
            }
            updateUpgradeFailCount(failCount);
            updateUpgradeSuccessCount(successCount);
            NoteUtils.closeSilently(oldDb);
            NoteUtils.closeSilently(newDb);
            return 0;
        } catch (SQLiteException e) {
            i = 5;
        } finally {
            NoteUtils.closeSilently(oldDb);
            NoteUtils.closeSilently(newDb);
        }
    }

    public static int getLabelId(ArrayList<LabelHolder> labels, String labelName) {
        Iterator i$ = labels.iterator();
        while (i$.hasNext()) {
            LabelHolder label = (LabelHolder) i$.next();
            if (label.mContent.equals(labelName.trim())) {
                return label.mId;
            }
        }
        return -1;
    }

    public static ArrayList<LabelHolder> getLabels(LabelManager labelManager) {
        ArrayList<LabelHolder> labels = labelManager.getLabelList();
        if (labels.size() == 0) {
            while (labels.size() == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                labels = labelManager.getLabelList();
            }
        }
        return labels;
    }

    private String recoveryJsonContent(OldNoteItemData noteItemData) {
        ArrayList<SubData> subDatas = noteItemData.getSubs();
        if (subDatas == null || subDatas.size() == 0) {
            return NoteUtils.createPlainTextJsonContent(noteItemData.getContent());
        }
        return createWithMediaTextJsonContent(subDatas);
    }

    private String createWithMediaTextJsonContent(ArrayList<SubData> subDatas) {
        StringBuilder builder = new StringBuilder();
        JSONArray jsonArray = new JSONArray();
        int i = 0;
        try {
            int size = subDatas.size();
            while (i < size) {
                SubData data = (SubData) subDatas.get(i);
                if (data.isMedia()) {
                    if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
                        builder.append(Constants.STR_NEW_LINE);
                    }
                    int start = builder.length();
                    int end = start + Constants.MEDIA_SOUND.length();
                    builder.append(Constants.MEDIA_SOUND);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DataConvert.SPAN_ITEM_START, start);
                    jsonObject.put(DataConvert.SPAN_ITEM_END, end);
                    jsonObject.put(DataConvert.SPAN_ITEM_FLAG, 33);
                    jsonObject.put(DataConvert.SPAN_ITEM_TYPE, SoundImageSpan.class.getName());
                    jsonObject.put(SoundImageSpan.ORIGIN_PATH, data.getMediaFilePath());
                    jsonObject.put(SoundImageSpan.SOUND_DURATION, data.getTime());
                    jsonArray.put(jsonObject);
                } else {
                    String content = data.getContent();
                    if (i > 0 && ((SubData) subDatas.get(i - 1)).isMedia() && content.charAt(0) != '\n') {
                        builder.append(Constants.STR_NEW_LINE);
                    }
                    builder.append(content);
                }
                i++;
            }
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object();
            jsonStringer.key(DataConvert.JSON_CONTENT_KEY).value(builder.toString());
            if (!(jsonArray == null || jsonArray.length() == 0)) {
                jsonStringer.key(DataConvert.JSON_SPANS_KEY).value(jsonArray);
            }
            jsonStringer.endObject();
            return jsonStringer.toString();
        } catch (JSONException e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }

    public static boolean isExistOldDB(Context context) {
        return context.getDatabasePath(OLD_DB_NAME).exists();
    }
}
