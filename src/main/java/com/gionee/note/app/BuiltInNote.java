package com.gionee.note.app;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.Config.EditPage;
import com.gionee.note.app.span.BillItem;
import com.gionee.note.app.span.PhotoImageSpan;
import com.gionee.note.common.BitmapUtils;
import com.gionee.note.common.Constants;
import com.gionee.note.common.DecodeUtils;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.common.StorageUtils;
import com.gionee.note.common.ThreadPool.Job;
import com.gionee.note.common.ThreadPool.JobContext;
import com.gionee.note.data.NoteItem;
import com.gionee.note.provider.NoteContract;
import com.gionee.note.provider.NoteContract.NoteContent;
import com.gionee.note.provider.NoteShareDataManager;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class BuiltInNote {
    private static final String BILL_SPAN = ":bill:";
    private static final String CONFIG_FILE_NAME = "init_config.xml";
    private static final String ERROR_FILE = "error_show_fail.error";
    private static final String FROM_APP = "app";
    private static final String FROM_SYSTEM = "system";
    private static final String IMAGE_SPAN = ":image:";
    private static final String LABEL_SPLIT = "-";
    private static final String SPAN_ITEM_SPLIT = "-";
    private static final String SPAN_SPLIT = "::";
    private static final String TAG = "BuiltInNote";
    private static final String TYPE_BILL = "bill";
    private static final int TYPE_BILL_INT = 2;
    private static final String TYPE_IMAGE = "image";
    private static final int TYPE_IMAGE_INT = 1;

    private static class NoteInfo {
        public String mContentSpan;
        public String mContentText;
        public String mJsonContent;
        public String mLabels;
        public String mSource;
        public String mTime;
        public String mTitle;

        private NoteInfo() {
        }

        public void coverToJsonContent() {
            String contentText = this.mContentText;
            if (!TextUtils.isEmpty(contentText)) {
                contentText = contentText.replace(BuiltInNote.IMAGE_SPAN, Constants.MEDIA_PHOTO).replace(BuiltInNote.BILL_SPAN, Constants.MEDIA_BILL);
            }
            this.mJsonContent = getJsonContent(getSpanList(this.mContentSpan), contentText, this.mSource);
        }

        private ArrayList<NoteSpan> getSpanList(String contentSpan) {
            if (TextUtils.isEmpty(contentSpan)) {
                return null;
            }
            String[] spans = contentSpan.split(BuiltInNote.SPAN_SPLIT);
            ArrayList<NoteSpan> spanList = new ArrayList();
            for (String split : spans) {
                String[] spanContents = split.split("-");
                String type = spanContents[0];
                NoteSpan span = new NoteSpan();
                span.mStart = Integer.parseInt(spanContents[1]);
                span.mEnd = Integer.parseInt(spanContents[2]);
                if (type.equals(BuiltInNote.TYPE_IMAGE)) {
                    span.mType = 1;
                    span.mFileName = spanContents[3];
                } else if (type.equals(BuiltInNote.TYPE_BILL)) {
                    span.mType = 2;
                    span.mChecked = Boolean.parseBoolean(spanContents[3]);
                }
                spanList.add(span);
            }
            return spanList;
        }

        private String getJsonContent(ArrayList<NoteSpan> spanList, String contentText, String source) {
            JSONStringer jsonStringer = new JSONStringer();
            try {
                jsonStringer.object();
                jsonStringer.key(DataConvert.JSON_CONTENT_KEY).value(contentText);
                if (spanList != null && spanList.size() > 0) {
                    JSONArray jsonArray = getJsonArray(spanList, source);
                    if (jsonArray != null) {
                        jsonStringer.key(DataConvert.JSON_SPANS_KEY).value(jsonArray);
                    }
                }
                jsonStringer.endObject();
            } catch (JSONException e) {
                Log.w(BuiltInNote.TAG, "error", e);
            }
            return jsonStringer.toString();
        }

        private JSONArray getJsonArray(ArrayList<NoteSpan> spanList, String source) {
            JSONArray jsonArray = new JSONArray();
            Iterator i$ = spanList.iterator();
            while (i$.hasNext()) {
                NoteSpan noteSpan = (NoteSpan) i$.next();
                JSONObject jsonObject = null;
                if (noteSpan.mType == 2) {
                    jsonObject = getBillJsonObject(noteSpan);
                } else if (noteSpan.mType == 1) {
                    jsonObject = getImageJsonObject(source, noteSpan);
                }
                if (jsonObject != null) {
                    jsonArray.put(jsonObject);
                }
            }
            return jsonArray;
        }

        private JSONObject getBillJsonObject(NoteSpan noteSpan) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(DataConvert.SPAN_ITEM_START, noteSpan.mStart);
                jsonObject.put(DataConvert.SPAN_ITEM_END, noteSpan.mEnd);
                jsonObject.put(DataConvert.SPAN_ITEM_FLAG, 18);
                jsonObject.put(DataConvert.SPAN_ITEM_TYPE, BillItem.class.getName());
                jsonObject.put(BillItem.CHECKED_KEY, noteSpan.mChecked);
                return jsonObject;
            } catch (JSONException e) {
                Log.w(BuiltInNote.TAG, "error", e);
                return null;
            }
        }

        private JSONObject getImageJsonObject(String source, NoteSpan noteSpan) {
            Bitmap rawBitmap = null;
            try {
                if (source.equals(BuiltInNote.FROM_APP)) {
                    rawBitmap = DecodeUtils.decodeRawBitmap(NoteAppImpl.getContext(), noteSpan.mFileName);
                } else if (source.equals(BuiltInNote.FROM_SYSTEM)) {
                    rawBitmap = DecodeUtils.decodeSystemBitmap(noteSpan.mFileName);
                }
                Uri originUri = null;
                Uri thumbUri = null;
                if (rawBitmap != null) {
                    File file = BuiltInNote.makeOriginFile(NoteAppImpl.getContext());
                    if (file != null) {
                        originUri = Uri.fromFile(file);
                    }
                    thumbUri = BuiltInNote.convertToThumbnail(NoteAppImpl.getContext(), rawBitmap);
                    if (file != null) {
                        NoteUtils.saveBitmap(rawBitmap, file);
                        rawBitmap.recycle();
                    }
                }
                if (originUri != null) {
                    if (thumbUri == null) {
                        thumbUri = Uri.fromFile(new File(BuiltInNote.ERROR_FILE));
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DataConvert.SPAN_ITEM_START, noteSpan.mStart);
                    jsonObject.put(DataConvert.SPAN_ITEM_END, noteSpan.mEnd);
                    jsonObject.put(DataConvert.SPAN_ITEM_FLAG, 33);
                    jsonObject.put(DataConvert.SPAN_ITEM_TYPE, PhotoImageSpan.class.getName());
                    jsonObject.put(PhotoImageSpan.ORIGIN_URI, originUri.toString());
                    jsonObject.put(PhotoImageSpan.THUMB_URI, thumbUri.toString());
                    return jsonObject;
                }
            } catch (JSONException e) {
                Log.w(BuiltInNote.TAG, "error", e);
            }
            return null;
        }
    }

    private static class NoteSpan {
        public boolean mChecked;
        public int mEnd;
        public String mFileName;
        public int mStart;
        public int mType;

        private NoteSpan() {
        }
    }

    public static void insertBuildInNoteSync() {
        if (getIsFirstLaunch(NoteAppImpl.getContext())) {
            initNoteData();
            setIsFirstLaunch(NoteAppImpl.getContext(), false);
            NoteAppImpl.getContext().notifyDbInitComplete();
        }
    }

    public static void insertBuildInNoteAsync() {
        NoteAppImpl.getContext().getThreadPool().submit(new Job<Void>() {
            public Void run(JobContext jc) {
                if (BuiltInNote.getIsFirstLaunch(NoteAppImpl.getContext())) {
                    BuiltInNote.initNoteData();
                    BuiltInNote.setIsFirstLaunch(NoteAppImpl.getContext(), false);
                    NoteAppImpl.getContext().notifyDbInitComplete();
                }
                return null;
            }
        });
    }

    private static File makeOriginFile(Context context) {
        File fileDirectory = StorageUtils.getAvailableFileDirectory(context, 10485760, Constants.NOTE_MEDIA_PHOTO_PATH);
        if (fileDirectory == null) {
            fileDirectory = Constants.NOTE_MEDIA_PHOTO_PATH;
        }
        if (fileDirectory.exists() || fileDirectory.mkdirs()) {
            return NoteUtils.getSaveImageFile(fileDirectory);
        }
        return null;
    }

    private static Uri convertToThumbnail(Context context, Bitmap rawBitmap) {
        if (rawBitmap == null) {
            return null;
        }
        EditPage page = EditPage.get(context);
        int width = page.mImageWidth;
        int height = page.mImageHeight;
        Bitmap bitmap = BitmapUtils.resizeAndCropCenter(rawBitmap, width, height, false, true);
        if (bitmap == null) {
            return null;
        }
        File fileDirectory = StorageUtils.getAvailableFileDirectory(context, (long) ((width * height) * 4), Constants.NOTE_MEDIA_THUMBNAIL_PATH);
        if (fileDirectory == null) {
            fileDirectory = Constants.NOTE_MEDIA_THUMBNAIL_PATH;
        }
        if (!fileDirectory.exists() && !fileDirectory.mkdirs()) {
            return null;
        }
        File thumbFile = NoteUtils.getSaveImageFile(fileDirectory);
        Uri thumbnailUri = Uri.fromFile(thumbFile);
        NoteUtils.saveBitmap(bitmap, thumbFile);
        return thumbnailUri;
    }

    public static boolean getIsFirstLaunch(Context context) {
        return NoteShareDataManager.getIsFirstLaunch(context);
    }

    public static void setIsFirstLaunch(Context context, boolean first) {
        NoteShareDataManager.setIsFirstLaunch(context, first);
    }

    public static void initNoteData() {
        ArrayList<NoteInfo> noteInfoList = getNoteInfoList();
        if (noteInfoList != null && noteInfoList.size() != 0) {
            Iterator i$ = noteInfoList.iterator();
            while (i$.hasNext()) {
                ((NoteInfo) i$.next()).coverToJsonContent();
            }
            saveToDB(noteInfoList);
        }
    }

    private static ArrayList<NoteInfo> getNoteInfoList() {
        ArrayList<NoteInfo> arrayList = null;
        Closeable is = getInitConfigInputStream();
        if (is != null) {
            arrayList = new ArrayList();
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(is, null);
                int eventType = xpp.getEventType();
                HashMap<String, String> map = new HashMap();
                while (true) {
                    if (eventType == 2 && "note".equals(xpp.getName())) {
                        int count = xpp.getAttributeCount();
                        map.clear();
                        for (int x = 0; x < count; x++) {
                            map.put(xpp.getAttributeName(x), xpp.getAttributeValue(x));
                        }
                        NoteInfo noteInfo = new NoteInfo();
                        noteInfo.mTitle = (String) map.get(NoteContent.COLUMN_TITLE);
                        noteInfo.mContentText = (String) map.get("contentText");
                        noteInfo.mContentSpan = (String) map.get("contentSpan");
                        noteInfo.mLabels = (String) map.get("labels");
                        noteInfo.mTime = (String) map.get("time");
                        noteInfo.mSource = (String) map.get("source");
                        arrayList.add(noteInfo);
                    }
                    eventType = xpp.next();
                    if (eventType == 1) {
                        break;
                    }
                }
            } catch (XmlPullParserException e) {
                Log.w(TAG, "error", e);
            } catch (IOException e2) {
                Log.w(TAG, "error", e2);
            } catch (Exception e3) {
                Log.w(TAG, "error", e3);
            } finally {
                NoteUtils.closeSilently(is);
            }
        }
        return arrayList;
    }

    private static void saveToDB(ArrayList<NoteInfo> noteInfoList) {
        ArrayList<ContentProviderOperation> insertOps = new ArrayList();
        Uri uri = NoteContent.CONTENT_URI;
        Iterator i$ = noteInfoList.iterator();
        while (i$.hasNext()) {
            NoteInfo info = (NoteInfo) i$.next();
            String label = getLabel(info.mLabels);
            ContentValues values = new ContentValues();
            values.put(NoteContent.COLUMN_TITLE, info.mTitle);
            values.put("content", info.mJsonContent);
            values.put(NoteContent.COLUMN_DATE_CREATED, Long.valueOf(info.mTime));
            values.put(NoteContent.COLUMN_DATE_MODIFIED, Long.valueOf(info.mTime));
            if (!TextUtils.isEmpty(label)) {
                values.put(NoteContent.COLUMN_LABEL, label);
            }
            values.put(NoteContent.COLUMN_REMINDER, Integer.valueOf(0));
            insertOps.add(ContentProviderOperation.newInsert(uri).withValues(values).build());
        }
        try {
            NoteAppImpl.getContext().getContentResolver().applyBatch(NoteContract.AUTHORITY, insertOps);
        } catch (Exception e) {
            Logger.printLog(TAG, "insert buildidnote fail : " + e.toString());
        }
    }

    private static String getLabel(String labels) {
        if (TextUtils.isEmpty(labels)) {
            return null;
        }
        ArrayList<Integer> labelList = new ArrayList();
        LabelManager labelManager = NoteAppImpl.getContext().getLabelManager();
        for (String l : labels.split("-")) {
            labelList.add(Integer.valueOf(labelManager.getLabelId(l)));
        }
        return NoteItem.convertToStringLabel(labelList);
    }

    private static InputStream getInitConfigInputStream() {
        File file = new File("/system/etc/Amigo_Note/init_config.xml");
        InputStream is = null;
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                is = null;
            }
        }
        if (is != null) {
            return is;
        }
        try {
            is = NoteAppImpl.getContext().getAssets().open(CONFIG_FILE_NAME);
        } catch (IOException e2) {
            is = null;
        }
        return is;
    }
}
