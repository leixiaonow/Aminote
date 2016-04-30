package com.gionee.note.app.inputbackup;

import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import com.gionee.framework.utils.StringUtils;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.LabelManager;
import com.gionee.note.app.LabelManager.LabelHolder;
import com.gionee.note.app.NoteAppImpl;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import com.gionee.note.app.span.SoundImageSpan;
import com.gionee.note.common.Constants;
import com.gionee.note.common.FileUtils;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.StorageUtils;
import com.gionee.note.provider.NoteContract.NoteContent;
import com.gionee.note.provider.NoteShareDataManager;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class ImportBackUp {
    static final char CONTENT_SPLIT = '«';
    static final char ENTER_REPLACE = '»';
    static final int FAIL_CODE_INSERT_DB_ERROR = 4;
    static final int FAIL_CODE_NO_SPACE = 1;
    static final int FAIL_CODE_SAVE_TO_TEMP_ERROR = 2;
    static final int FAIL_CODE_WRITE_CONFIG_ERROR = 3;
    private static final File MAIN_ROOT_FILE = Environment.getExternalStorageDirectory();
    private static final String MEDIA_NAME_HEAD = "import_";
    private static final long MIN_LIMIT_SIZE = 20971520;
    private static final File OLD_BACKUP_FILE_0 = new File(MAIN_ROOT_FILE, "/amigo/AmigoNote/Memo");
    private static final File OLD_BACKUP_FILE_1 = new File(MAIN_ROOT_FILE, "/备份/便签");
    private static final File OLD_BACKUP_FILE_2 = new File(MAIN_ROOT_FILE, "/backup/Memo");
    static final String STR_ENTER = "\n";
    static final String SUFFIX_MP3 = ".mp3";
    static final String SUFFIX_TXT = ".txt";
    private static final String TAG = "ImportBackUp";
    private static final File TEMP_SAVE_FILE = new File(Constants.NOTE_MEDIA_PATH, "temp_import_save");
    static File sTempSaveFile;
    static File sTempSaveFileMedia;
    private ArrayList<File> mBackupFiles;
    private int mFailCode;
    private boolean mImportBackupDataFinish;
    private boolean mImportFail;
    private long mMinSize;
    private int mProgress;
    private boolean mRuning;

    private boolean insertOldDataToNewDB(long r14, java.lang.String r16, java.lang.String r17, java.lang.String r18) {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
*/
        /*
        r13 = this;
        r3 = 0;
        r10 = com.gionee.note.app.NoteAppImpl.getContext();	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = "note.db";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = r10.getDatabasePath(r11);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = r10.getPath();	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = 0;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r3 = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(r10, r11);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r7 = new android.content.ContentValues;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r7.<init>();	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r6 = r16;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = r6.length();	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = 15;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        if (r10 <= r11) goto L_0x002a;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
    L_0x0023:
        r10 = 0;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = 15;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r6 = r6.substring(r10, r11);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
    L_0x002a:
        r8 = java.lang.System.currentTimeMillis();	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = "title";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r7.put(r10, r6);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = "content";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r0 = r17;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r7.put(r10, r0);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = "date_created";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = java.lang.Long.valueOf(r8);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r7.put(r10, r11);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = "date_modified";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = java.lang.Long.valueOf(r8);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r7.put(r10, r11);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = "label";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r0 = r18;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r7.put(r10, r0);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = "note_item";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = 0;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r4 = r3.insert(r10, r11, r7);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = 0;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        if (r10 <= 0) goto L_0x0063;	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
    L_0x0060:
        r13.delOldDataFromTempDB(r14);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
    L_0x0063:
        r10 = 0;
        r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r10 <= 0) goto L_0x006e;
    L_0x0069:
        r10 = 1;
    L_0x006a:
        com.gionee.note.common.NoteUtils.closeSilently(r3);
    L_0x006d:
        return r10;
    L_0x006e:
        r10 = 0;
        goto L_0x006a;
    L_0x0070:
        r2 = move-exception;
        r10 = "ImportBackUp";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        r11 = "error";	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        android.util.Log.w(r10, r11, r2);	 Catch:{ SQLiteException -> 0x0070, all -> 0x007d }
        com.gionee.note.common.NoteUtils.closeSilently(r3);
        r10 = 0;
        goto L_0x006d;
    L_0x007d:
        r10 = move-exception;
        com.gionee.note.common.NoteUtils.closeSilently(r3);
        throw r10;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.note.app.inputbackup.ImportBackUp.insertOldDataToNewDB(long, java.lang.String, java.lang.String, java.lang.String):boolean");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void start() {
        /*
        r10 = this;
        r9 = 3;
        r8 = 2;
        r7 = 1;
        monitor-enter(r10);
        r3 = r10.mRuning;	 Catch:{ all -> 0x0059 }
        if (r3 == 0) goto L_0x0011;
    L_0x0008:
        r3 = "ImportBackUp";
        r4 = "runing-----------";
        android.util.Log.d(r3, r4);	 Catch:{ all -> 0x0059 }
        monitor-exit(r10);	 Catch:{ all -> 0x0059 }
    L_0x0010:
        return;
    L_0x0011:
        r3 = 1;
        r10.mRuning = r3;	 Catch:{ all -> 0x0059 }
        monitor-exit(r10);	 Catch:{ all -> 0x0059 }
        r3 = r10.mImportBackupDataFinish;
        if (r3 != 0) goto L_0x0010;
    L_0x0019:
        r10.resetStates();
        r10.setProgress(r7);
        r0 = r10.getImportToTempFinishValue();
        r3 = "ImportBackUp";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "isImportToTempFinish = ";
        r4 = r4.append(r5);
        r4 = r4.append(r0);
        r4 = r4.toString();
        android.util.Log.d(r3, r4);
        r2 = 0;
        if (r0 == 0) goto L_0x0042;
    L_0x003e:
        r2 = r10.getTempFilePath();
    L_0x0042:
        if (r2 != 0) goto L_0x005c;
    L_0x0044:
        r10.clearTempFile();
        r3 = com.gionee.note.app.NoteAppImpl.getContext();
        r4 = r10.mMinSize;
        r6 = TEMP_SAVE_FILE;
        r1 = com.gionee.note.common.StorageUtils.getAvailableFileDirectory(r3, r4, r6);
    L_0x0053:
        if (r1 != 0) goto L_0x0062;
    L_0x0055:
        r10.importFail(r7);
        goto L_0x0010;
    L_0x0059:
        r3 = move-exception;
        monitor-exit(r10);	 Catch:{ all -> 0x0059 }
        throw r3;
    L_0x005c:
        r1 = new java.io.File;
        r1.<init>(r2);
        goto L_0x0053;
    L_0x0062:
        sTempSaveFile = r1;
        r3 = new java.io.File;
        r4 = "/media";
        r3.<init>(r1, r4);
        sTempSaveFileMedia = r3;
        r10.setProgress(r8);
        if (r0 != 0) goto L_0x0098;
    L_0x0072:
        r3 = r10.mBackupFiles;
        r3 = r10.saveAllImportNoteInfoToTemp(r3);
        if (r3 != 0) goto L_0x007e;
    L_0x007a:
        r10.importFail(r8);
        goto L_0x0010;
    L_0x007e:
        r3 = r10.writeFinishImportToTemp();
        if (r3 != 0) goto L_0x008f;
    L_0x0084:
        r3 = "ImportBackUp";
        r4 = "write value fail KEY_BACKUP_DATA_TO_TEMP_FINISH";
        android.util.Log.d(r3, r4);
        r10.importFail(r9);
        goto L_0x0010;
    L_0x008f:
        r3 = sTempSaveFile;
        r3 = r3.getPath();
        r10.writeTempFilePath(r3);
    L_0x0098:
        r10.setProgress(r9);
        r10.importBackupData();
        goto L_0x0010;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.gionee.note.app.inputbackup.ImportBackUp.start():void");
    }

    private void resetStates() {
        this.mImportFail = false;
        this.mFailCode = 0;
        this.mProgress = 0;
    }

    private void clearTempFile() {
        FileUtils.deleteContents(TEMP_SAVE_FILE);
        File otherSDBackUpFile = StorageUtils.createOtherSdCardFile(StorageUtils.getLocalRootPath(NoteAppImpl.getContext()), TEMP_SAVE_FILE.getAbsolutePath());
        if (otherSDBackUpFile != null) {
            FileUtils.deleteContents(otherSDBackUpFile);
        }
    }

    private boolean getImportToTempFinishValue() {
        return NoteShareDataManager.getImportToTempFinishValue(NoteAppImpl.getContext());
    }

    private String getTempFilePath() {
        return NoteShareDataManager.getTempFilePath(NoteAppImpl.getContext());
    }

    private boolean writeFinishImportToTemp() {
        return NoteShareDataManager.writeFinishImportToTemp(NoteAppImpl.getContext());
    }

    private void writeTempFilePath(String filePath) {
        NoteShareDataManager.writeTempFilePath(NoteAppImpl.getContext(), filePath);
    }

    public static boolean writeFinishImport() {
        return NoteShareDataManager.writeFinishImport(NoteAppImpl.getContext());
    }

    private void setProgress(int progress) {
        this.mProgress = progress;
    }

    public int getProgress() {
        return this.mProgress;
    }

    private void importBackupFinish() {
        this.mImportBackupDataFinish = true;
        setProgress(100);
        runFinish();
    }

    private void runFinish() {
        synchronized (this) {
            this.mRuning = false;
        }
    }

    public void resetEnv() {
        synchronized (this) {
            if (this.mRuning) {
                Log.d(TAG, "runing-----------");
                return;
            }
            initBackupFilesAndMinSize();
            initImportBackupDataFinishMember();
        }
    }

    private void initImportBackupDataFinishMember() {
        this.mImportBackupDataFinish = NoteShareDataManager.getImportBackupDataFinish(NoteAppImpl.getContext());
    }

    private void importBackupData() {
        ArrayList<OldNoteInfo> oldNoteInfos = getOldNoteInfos();
        if (oldNoteInfos == null || oldNoteInfos.size() == 0) {
            importBackupFinish();
            return;
        }
        Log.d(TAG, "size = " + oldNoteInfos.size());
        resolveNoteInfoMedia(oldNoteInfos);
        updateNoteInfoLabelId(oldNoteInfos);
        try {
            insertOldDataToNewDB(oldNoteInfos);
            if (writeFinishImport()) {
                clearTempFile();
                NoteAppImpl.getContext().getContentResolver().notifyChange(NoteContent.CONTENT_URI, null);
                importBackupFinish();
                Log.d(TAG, "importBackupData finish");
                return;
            }
            Log.d(TAG, "write value fail KEY_IMPORT_BACKUP_DATA_FINISH");
            importFail(3);
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            importFail(4);
        }
    }

    private void insertOldDataToNewDB(ArrayList<OldNoteInfo> oldNoteInfos) throws Exception {
        int progress = getProgress();
        int size = oldNoteInfos.size();
        for (int i = 0; i < size; i++) {
            OldNoteInfo oldNoteInfo = (OldNoteInfo) oldNoteInfos.get(i);
            ArrayList subInfos = oldNoteInfo.getSubInfos();
            if (subInfos == null || subInfos.size() == 0) {
                if (!insertOldDataToNewDB(oldNoteInfo.getId(), oldNoteInfo.getTitle(), NoteUtils.createPlainTextJsonContent(oldNoteInfo.getContent()), oldNoteInfo.getLabelId())) {
                    throw new ImportError();
                }
            } else {
                insertOldDataToNewDB(subInfos, oldNoteInfo.getId(), oldNoteInfo.getTitle(), oldNoteInfo.getLabelId());
            }
            setProgress(((i * 86) / size) + progress);
        }
    }

    private void updateNoteInfoLabelId(ArrayList<OldNoteInfo> oldNoteInfos) {
        LabelManager labelManager = NoteAppImpl.getContext().getLabelManager();
        ArrayList<LabelHolder> labels = DataUpgrade.getLabels(labelManager);
        Iterator i$ = oldNoteInfos.iterator();
        while (i$.hasNext()) {
            OldNoteInfo oldNoteInfo = (OldNoteInfo) i$.next();
            String labelName = oldNoteInfo.getLabel();
            if (labelName != null && labelName.trim().length() > 0) {
                if (labelName.length() > 12) {
                    labelName = labelName.substring(0, 12);
                }
                int id = DataUpgrade.getLabelId(labels, labelName);
                if (id < 0) {
                    id = labelManager.addLabel(labelName);
                    labels = DataUpgrade.getLabels(labelManager);
                }
                oldNoteInfo.setLabelId(Integer.toString(id));
            }
        }
    }

    private void resolveNoteInfoMedia(ArrayList<OldNoteInfo> oldNoteInfos) {
        Iterator i$ = oldNoteInfos.iterator();
        while (i$.hasNext()) {
            ((OldNoteInfo) i$.next()).resolveMedia();
        }
    }

    private void insertOldDataToNewDB(ArrayList<SubInfo> subDatas, long oldId, String title, String labelId) throws Exception {
        StringBuilder builder = new StringBuilder();
        JSONArray jsonArray = new JSONArray();
        ArrayList<File> delFiles = new ArrayList();
        ArrayList<File> dstFiles = new ArrayList();
        int size = subDatas.size();
        for (int i = 0; i < size; i++) {
            SubInfo data = (SubInfo) subDatas.get(i);
            if (data.isMedia()) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
                    builder.append("\n");
                }
                int start = builder.length();
                int end = start + Constants.MEDIA_SOUND.length();
                builder.append(Constants.MEDIA_SOUND);
                putToJsonArray(jsonArray, data, start, end, getDstMediaFilePath(delFiles, dstFiles, data.getMediaFilePath()));
            } else {
                String content = data.getContent();
                if (i > 0) {
                    if (((SubInfo) subDatas.get(i - 1)).isMedia() && content.charAt(0) != '\n') {
                        builder.append("\n");
                    }
                }
                builder.append(content);
            }
        }
        JSONStringer jsonStringer = new JSONStringer();
        jsonStringer.object();
        jsonStringer.key(DataConvert.JSON_CONTENT_KEY).value(builder.toString());
        if (!(jsonArray == null || jsonArray.length() == 0)) {
            jsonStringer.key(DataConvert.JSON_SPANS_KEY).value(jsonArray);
        }
        jsonStringer.endObject();
        if (insertOldDataToNewDB(oldId, title, jsonStringer.toString(), labelId)) {
            deleteFiles(delFiles);
        } else {
            deleteFiles(dstFiles);
            throw new ImportError();
        }
    }

    private boolean deleteFiles(ArrayList<File> delFiles) {
        boolean deleteResult = true;
        for (File file : delFiles) {
            if (!file.delete()) {
                deleteResult = false;
            }
        }
        return deleteResult;
    }

    private void putToJsonArray(JSONArray jsonArray, SubInfo data, int start, int end, String mediaFilePath) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DataConvert.SPAN_ITEM_START, start);
        jsonObject.put(DataConvert.SPAN_ITEM_END, end);
        jsonObject.put(DataConvert.SPAN_ITEM_FLAG, 33);
        jsonObject.put(DataConvert.SPAN_ITEM_TYPE, SoundImageSpan.class.getName());
        jsonObject.put(SoundImageSpan.ORIGIN_PATH, mediaFilePath);
        jsonObject.put(SoundImageSpan.SOUND_DURATION, data.getTime());
        jsonArray.put(jsonObject);
    }

    private String getDstMediaFilePath(ArrayList<File> delFiles, ArrayList<File> dstFiles, String mediaFilePath) {
        String dstMediaFilePath = mediaFilePath;
        File srcFile = getScrFile(dstMediaFilePath);
        if (srcFile == null) {
            return dstMediaFilePath;
        }
        File dstFile = getNewFile(srcFile.length());
        File dstParentFile = dstFile.getParentFile();
        if (!(dstParentFile.exists() || dstParentFile.mkdirs())) {
            Log.i(TAG, "getDstMediaFilePath dstParentFile.mkdirs failure!!!!!");
        }
        if (!FileUtils.copyFile(srcFile.getPath(), dstFile.getPath())) {
            return dstMediaFilePath;
        }
        dstMediaFilePath = dstFile.getPath();
        delFiles.add(srcFile);
        dstFiles.add(dstFile);
        return dstMediaFilePath;
    }

    private File getScrFile(String name) {
        File rootFile = sTempSaveFileMedia;
        if (!rootFile.exists()) {
            return null;
        }
        File[] listFiles = rootFile.listFiles();
        if (listFiles == null || listFiles.length == 0) {
            return null;
        }
        for (File file : listFiles) {
            if (file.getPath().endsWith(name)) {
                return file;
            }
        }
        return null;
    }

    private File getNewFile(long size) {
        File file = new File(StorageUtils.getAvailableFileDirectory(NoteAppImpl.getContext(), size, Constants.NOTE_MEDIA_SOUND_PATH), MEDIA_NAME_HEAD + System.currentTimeMillis());
        return !file.exists() ? file : getNewFile(size);
    }

    private int delOldDataFromTempDB(long id) {
        ImportDBHelp dbHelp = new ImportDBHelp();
        try {
            int delete = dbHelp.delete("_id = ?", new String[]{String.valueOf(id)});
            return delete;
        } finally {
            dbHelp.close();
        }
    }

    private ArrayList<OldNoteInfo> getOldNoteInfos() {
        ArrayList<OldNoteInfo> arrayList = null;
        ImportDBHelp dbHelp = new ImportDBHelp();
        Cursor cursor = null;
        try {
            cursor = dbHelp.query(new String[]{"_id", NoteContent.COLUMN_TITLE, "content", NoteContent.COLUMN_LABEL}, null, null, null, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                NoteUtils.closeSilently(cursor);
                dbHelp.close();
            } else {
                arrayList = new ArrayList();
                while (cursor.moveToNext()) {
                    arrayList.add(new OldNoteInfo(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)));
                }
                NoteUtils.closeSilently(cursor);
                dbHelp.close();
            }
            return arrayList;
        } catch (Throwable th) {
            dbHelp.close();
        }
    }

    private boolean saveAllImportNoteInfoToTemp(ArrayList<File> backupFiles) {
        try {
            Iterator it = backupFiles.iterator();
            while (it.hasNext()) {
                File[] listFiles = ((File) it.next()).listFiles();
                if (listFiles != null && listFiles.length > 0) {
                    for (File f : listFiles) {
                        saveOneExportInputNoteInfoToTemp(f);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            return false;
        }
    }

    private void saveOneExportInputNoteInfoToTemp(File file) throws Exception {
        if (file != null && !file.isFile()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                for (File f : files) {
                    saveInputNoteInfoToTemp(f);
                }
            }
        }
    }

    private void saveInputNoteInfoToTemp(File file) throws Exception {
        if (file != null && !file.isFile()) {
            File[] files = file.listFiles();
            if (files != null && files.length != 0) {
                InputNoteInfo item = new InputNoteInfo();
                for (File f : files) {
                    String fileName = f.getName();
                    if (fileName.endsWith(SUFFIX_TXT)) {
                        item.setText(txtToString(f));
                    } else if (fileName.endsWith(SUFFIX_MP3)) {
                        item.putMediaFilePath(f);
                    }
                }
                item.writToTemp();
            }
        }
    }

    private String txtToString(File file) {
        Exception e;
        Throwable th;
        Closeable br = null;
        try {
            Closeable br2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), StringUtils.ENCODING_UTF8));
            try {
                String readLine = br2.readLine();
                NoteUtils.closeSilently(br2);
                br = br2;
                return readLine;
            } catch (Exception e2) {
                e = e2;
                br = br2;
                try {
                    Log.w(TAG, "error", e);
                    NoteUtils.closeSilently(br);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    NoteUtils.closeSilently(br);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                br = br2;
                NoteUtils.closeSilently(br);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.w(TAG, "error", e);
            NoteUtils.closeSilently(br);
            return null;
        }
    }

    private void importFail(int failCode) {
        this.mFailCode = failCode;
        this.mImportFail = true;
        runFinish();
        Log.d(TAG, "importFail failCode = " + failCode);
    }

    public boolean isImportFail() {
        return this.mImportFail;
    }

    public int getFailCode() {
        return this.mFailCode;
    }

    private void addBackup(File file) {
        if (this.mBackupFiles == null) {
            this.mBackupFiles = new ArrayList();
        }
        this.mBackupFiles.add(file);
    }

    private void initBackupFilesAndMinSize() {
        List<String> rootPaths = StorageUtils.getLocalRootPath(NoteAppImpl.getContext());
        this.mMinSize = ((MIN_LIMIT_SIZE + initBackupFilesAndMinSize(rootPaths, OLD_BACKUP_FILE_0)) + initBackupFilesAndMinSize(rootPaths, OLD_BACKUP_FILE_1)) + initBackupFilesAndMinSize(rootPaths, OLD_BACKUP_FILE_2);
    }

    private long initBackupFilesAndMinSize(List<String> rootPaths, File mainSDBackUpFile) {
        long totalSize = 0;
        long mainSDBackUpFileTotalSize = FileUtils.getFileTotalSize(mainSDBackUpFile);
        if (mainSDBackUpFileTotalSize > 0) {
            totalSize = 0 + mainSDBackUpFileTotalSize;
            addBackup(mainSDBackUpFile);
        }
        File otherSDBackUpFile_0 = StorageUtils.createOtherSdCardFile(rootPaths, mainSDBackUpFile.getAbsolutePath());
        long otherSDBackUpFileTotalSize = FileUtils.getFileTotalSize(otherSDBackUpFile_0);
        if (otherSDBackUpFileTotalSize <= 0) {
            return totalSize;
        }
        totalSize += otherSDBackUpFileTotalSize;
        addBackup(otherSDBackUpFile_0);
        return totalSize;
    }

    public boolean isNeedInputBackup() {
        return (this.mBackupFiles == null || this.mImportBackupDataFinish) ? false : true;
    }

    public long getMinSize() {
        return this.mMinSize;
    }
}
