package com.gionee.note.ai;

import android.content.Context;
import android.util.Log;
import com.gionee.note.common.NoteUtils;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

class HotWordUtils {
    private static final int CACHE_SIZE = 1024;
    private static final String DATA = "data";
    private static final String HTTP_URL = "http://m.haosou.com/mhtml/app_index/app_news.json";
    private static final String KEY_WORDS_DIR_NAME = "key_words";
    private static final long MIN_SPACE = 2700000;
    private static final String SEARCH_WORD = "search_word";
    private static final String TAG = "HotWordUtils";
    private static final String TITLE = "title";

    static class HotWord {
        private String mSearchWork;
        private String mTitle;

        public HotWord(String title, String searchWork) {
            this.mTitle = title;
            this.mSearchWork = searchWork;
        }

        public String getTitle() {
            return this.mTitle;
        }

        public String getSearchWord() {
            return this.mSearchWork;
        }
    }

    HotWordUtils() {
    }

    public synchronized List<HotWord> getHotWorks(Context context, boolean isOpenNetwork) {
        List<HotWord> list = null;
        synchronized (this) {
            File keyWordsDir = new File(context.getFilesDir(), "/key_words");
            File[] keyWords = keyWordsDir.listFiles();
            if (isOpenNetwork) {
                list = getHotWorks(keyWordsDir, keyWords);
            } else if (!(keyWords == null || keyWords.length == 0)) {
                try {
                    list = getHotWorks(readKeyWordData(keyWords[0]));
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }
        }
        return list;
    }

    private List<HotWord> getHotWorks(File keyWordsDir, File[] keyWords) {
        List<HotWord> data1 = getHotWords(keyWords);
        if (data1 != null) {
            return data1;
        }
        try {
            byte[] data = readKeyWordData(HTTP_URL);
            File saveFile = null;
            if (keyWordsDir.exists()) {
                saveFile = new File(keyWordsDir, Long.toString(System.currentTimeMillis()));
            } else if (keyWordsDir.mkdirs()) {
                saveFile = new File(keyWordsDir, Long.toString(System.currentTimeMillis()));
            }
            if (saveFile != null) {
                writeFile(saveFile, data);
            }
            return getHotWorks(data);
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }

    private List<HotWord> getHotWords(File[] keyWords) {
        if (keyWords == null) {
            return null;
        }
        if (keyWords.length > 1) {
            for (File file : keyWords) {
                file.delete();
            }
            return null;
        }
        File keyWordFile = keyWords[0];
        if (Math.abs(Long.parseLong(keyWordFile.getName()) - System.currentTimeMillis()) >= MIN_SPACE) {
            keyWordFile.delete();
            return null;
        }
        try {
            return getHotWorks(readKeyWordData(keyWordFile));
        } catch (Exception e) {
            Log.w(TAG, "error", e);
            return null;
        }
    }

    private void writeFile(File file, byte[] data) {
        FileNotFoundException e;
        Throwable th;
        IOException e2;
        Closeable fos = null;
        try {
            Closeable fos2 = new FileOutputStream(file);
            try {
                fos2.write(data);
                NoteUtils.closeSilently(fos2);
                fos = fos2;
            } catch (FileNotFoundException e3) {
                e = e3;
                fos = fos2;
                try {
                    e.printStackTrace();
                    NoteUtils.closeSilently(fos);
                } catch (Throwable th2) {
                    th = th2;
                    NoteUtils.closeSilently(fos);
                    throw th;
                }
            } catch (IOException e4) {
                e2 = e4;
                fos = fos2;
                e2.printStackTrace();
                NoteUtils.closeSilently(fos);
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                NoteUtils.closeSilently(fos);
                throw th;
            }
        } catch (FileNotFoundException e5) {
            e = e5;
            e.printStackTrace();
            NoteUtils.closeSilently(fos);
        } catch (IOException e6) {
            e2 = e6;
            e2.printStackTrace();
            NoteUtils.closeSilently(fos);
        }
    }

    private List<HotWord> getHotWorks(byte[] data) throws Exception {
        JSONArray array = ((JSONObject) new JSONTokener(new String(data)).nextValue()).optJSONArray(DATA);
        int length = array.length();
        ArrayList<HotWord> workList = new ArrayList();
        for (int i = 0; i < length; i++) {
            JSONObject item = array.getJSONObject(i);
            workList.add(new HotWord(item.getString("title"), item.getString(SEARCH_WORD)));
        }
        return workList;
    }

    private byte[] readKeyWordData(File file) {
        FileNotFoundException e;
        Throwable th;
        Closeable inStream = null;
        try {
            Closeable inStream2 = new FileInputStream(file);
            try {
                byte[] readKeyWordData = readKeyWordData((InputStream) inStream2);
                NoteUtils.closeSilently(inStream2);
                inStream = inStream2;
                return readKeyWordData;
            } catch (FileNotFoundException e2) {
                e = e2;
                inStream = inStream2;
                try {
                    Log.w(TAG, e);
                    NoteUtils.closeSilently(inStream);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    NoteUtils.closeSilently(inStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inStream = inStream2;
                NoteUtils.closeSilently(inStream);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            e = e3;
            Log.w(TAG, e);
            NoteUtils.closeSilently(inStream);
            return null;
        }
    }

    private byte[] readKeyWordData(String urlPath) {
        byte[] readKeyWordData;
        Closeable inStream = null;
        try {
            inStream = ((HttpURLConnection) new URL(urlPath).openConnection()).getInputStream();
            readKeyWordData = readKeyWordData((InputStream) inStream);
            return readKeyWordData;
        } catch (MalformedURLException e) {
            readKeyWordData = TAG;
            Log.w(readKeyWordData, e);
            return null;
        } catch (IOException e2) {
            readKeyWordData = TAG;
            Log.w(readKeyWordData, e2);
            return null;
        } finally {
            NoteUtils.closeSilently(inStream);
        }
    }

    private byte[] readKeyWordData(InputStream inStream) {
        byte[] toByteArray;
        try {
            byte[] data = new byte[1024];
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while (true) {
                int len = inStream.read(data);
                if (len == -1) {
                    break;
                }
                outStream.write(data, 0, len);
            }
            toByteArray = outStream.toByteArray();
            return toByteArray;
        } catch (IOException e) {
            toByteArray = TAG;
            Log.w(toByteArray, e);
            return null;
        } finally {
            NoteUtils.closeSilently((Closeable) inStream);
        }
    }
}
