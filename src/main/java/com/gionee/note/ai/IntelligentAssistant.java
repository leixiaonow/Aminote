package com.gionee.note.ai;

import android.content.Context;
import android.util.Log;
import com.gionee.note.ai.algorithm.DFA;
import com.gionee.note.common.NoteUtils;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class IntelligentAssistant {
    private static final int Buffered_SIZE = 128;
    private static final String KEY_WORD_DIR = "keyWords";
    private static final String TAG = "IntelligentAssistant";
    private DFA mDFA;

    public IntelligentAssistant(Context context) {
        initDictionary(context);
    }

    private void initDictionary(Context context) {
        DFA dfa = new DFA();
        try {
            for (String str : context.getAssets().list(KEY_WORD_DIR)) {
                readKeyWords(context, dfa, "keyWords/" + str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mDFA = dfa;
    }

    private void readKeyWords(Context context, DFA dfa, String assetPath) {
        IOException e;
        Throwable th;
        Closeable closeable = null;
        try {
            Closeable bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open(assetPath)), 128);
            while (true) {
                try {
                    String keyWord = bufferedReader.readLine();
                    if (keyWord != null) {
                        dfa.addKeyword(keyWord.trim());
                    } else {
                        NoteUtils.closeSilently(bufferedReader);
                        closeable = bufferedReader;
                        return;
                    }
                } catch (IOException e2) {
                    e = e2;
                    closeable = bufferedReader;
                } catch (Throwable th2) {
                    th = th2;
                    closeable = bufferedReader;
                }
            }
        } catch (IOException e3) {
            e = e3;
            try {
                Log.w(TAG, "ERROR", e);
                NoteUtils.closeSilently(closeable);
            } catch (Throwable th3) {
                th = th3;
                NoteUtils.closeSilently(closeable);
                throw th;
            }
        }
    }

    public ArrayList<String> getKeyWords(String content) {
        try {
            return this.mDFA.searchKeyword(content);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }
}
