package com.gionee.feedback.config;

import android.os.Environment;
import com.gionee.feedback.utils.Log;
import java.io.File;

public class EnvConfig {
    private static final String FEEDBACK_FILE_NAME = ".feedback";
    private static final String FEEDBACK_TEST_FILE_NAME;
    private static final String TEST_FILE_NAME = "test888888";
    private static final boolean isTest;

    static {
        StringBuilder testFilePath = new StringBuilder(FEEDBACK_FILE_NAME);
        testFilePath.append(File.separator);
        testFilePath.append(TEST_FILE_NAME);
        FEEDBACK_TEST_FILE_NAME = testFilePath.toString();
        StringBuilder filePath = new StringBuilder(Environment.getExternalStoragePublicDirectory(FEEDBACK_TEST_FILE_NAME).toString());
        Log.d("EnvConfig", "filePath = " + filePath.toString());
        isTest = new File(filePath.toString()).exists();
    }

    private EnvConfig() {
    }

    public static boolean isTestEnv() {
        return isTest;
    }
}
