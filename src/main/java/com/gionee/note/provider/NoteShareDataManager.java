package com.gionee.note.provider;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class NoteShareDataManager {
    private static final String AI_SWITCH_OPEN = "ai_switch_open";
    private static final String FIRST_LAUNCH = "first_launch";
    private static final String IMPORT_BACKUP_DATA_CONFIG = "import_backup_data_config";
    private static final String IS_SHOW_DATA_FLOW_HINE = "show_data_flow_hint";
    private static final String KEY_BACKUP_DATA_TO_TEMP_FILE_PATH = "key_backup_data_to_temp_file_path";
    private static final String KEY_BACKUP_DATA_TO_TEMP_FINISH = "key_backup_data_to_temp_finish";
    private static final String KEY_IMPORT_BACKUP_DATA_FINISH = "key_import_backup_data_finish";
    private static final String LABEL_INIT = "label_init";
    private static final String NOTE_PREFERENCE = "note_preference";
    private static final String NOTE_SIGNATURE = "note_signature";

    public static String getSignatureText(Context context) {
        return context.getSharedPreferences(NOTE_PREFERENCE, 0).getString(NOTE_SIGNATURE, "");
    }

    public static void saveSignature(Context context, String signature) {
        context.getSharedPreferences(NOTE_PREFERENCE, 0).edit().putString(NOTE_SIGNATURE, signature).commit();
    }

    public static boolean getIsFirstLaunch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(FIRST_LAUNCH, true);
    }

    public static void setIsFirstLaunch(Context context, boolean first) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(FIRST_LAUNCH, first).commit();
    }

    public static boolean getImportToTempFinishValue(Context context) {
        return context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG, 0).getBoolean(KEY_BACKUP_DATA_TO_TEMP_FINISH, false);
    }

    public static String getTempFilePath(Context context) {
        return context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG, 0).getString(KEY_BACKUP_DATA_TO_TEMP_FILE_PATH, null);
    }

    public static boolean writeFinishImportToTemp(Context context) {
        Editor editor = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG, 0).edit();
        editor.putBoolean(KEY_BACKUP_DATA_TO_TEMP_FINISH, true);
        return editor.commit();
    }

    public static void writeTempFilePath(Context context, String filePath) {
        Editor editor = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG, 0).edit();
        editor.putString(KEY_BACKUP_DATA_TO_TEMP_FILE_PATH, filePath);
        editor.commit();
    }

    public static boolean writeFinishImport(Context context) {
        Editor editor = context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG, 0).edit();
        editor.putBoolean(KEY_IMPORT_BACKUP_DATA_FINISH, true);
        return editor.commit();
    }

    public static boolean getImportBackupDataFinish(Context context) {
        return context.getSharedPreferences(IMPORT_BACKUP_DATA_CONFIG, 0).getBoolean(KEY_IMPORT_BACKUP_DATA_FINISH, false);
    }

    public static boolean getIsInitLabel(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(LABEL_INIT, false);
    }

    public static void setIsInitLabel(Context context, boolean init) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(LABEL_INIT, init).commit();
    }

    public static boolean isAISwitchOpen(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AI_SWITCH_OPEN, true);
    }

    public static void setAISwitchValue(Context context, boolean isOpen) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(AI_SWITCH_OPEN, isOpen).commit();
    }

    public static void setShowDataFlowHint(Context context, boolean isShow) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(IS_SHOW_DATA_FLOW_HINE, isShow).commit();
    }

    public static boolean getHasShowDataFlowHint(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(IS_SHOW_DATA_FLOW_HINE, false);
    }
}
