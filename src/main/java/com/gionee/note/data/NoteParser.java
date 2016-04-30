package com.gionee.note.data;

import android.net.Uri;
import android.text.TextUtils;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.DataConvert;
import com.gionee.note.app.SlidingWindow.NoteEntry;
import com.gionee.note.app.span.PhotoImageSpan;
import com.gionee.note.common.Constants;
import com.gionee.note.common.NoteUtils;
import java.text.SimpleDateFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class NoteParser {
    public static final String DATE_FORMAT = "yyyy-M-d HH:mm";
    public static final String EMPTY_STRING = "";
    private static final String TAG = "NoteParser";

    public void parseNote(NoteEntry entry, NoteItem item) {
        if (item != null && entry != null) {
            entry.title = item.getTitle();
            getOriginContent(item.getContent(), entry);
            entry.content = parserText(entry.content);
            if (!TextUtils.isEmpty(entry.content) && entry.content.length() > 100) {
                entry.content = entry.content.substring(0, 100);
            }
            entry.reminder = item.getDateTimeReminder();
            entry.timeMillis = item.getDateTimeModified();
            entry.time = NoteUtils.formatDateTime(item.getDateTimeModified(), new SimpleDateFormat(DATE_FORMAT));
        }
    }

    public void parseNoteContent(String jsContent, NoteEntry entry) {
        getOriginContent(jsContent, entry);
        entry.content = replaceMediaString(entry.content);
        if (!TextUtils.isEmpty(entry.content) && entry.content.length() > 500) {
            entry.content = entry.content.substring(0, 500);
        }
    }

    public void getOriginContent(String json, NoteEntry entry) {
        if (json == null || json.length() == 0) {
            entry.mediaType = -1;
            return;
        }
        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(json).nextValue();
            entry.content = jsonObject.getString(DataConvert.JSON_CONTENT_KEY);
            getOriginMedia(jsonObject, entry, entry.content);
        } catch (Exception e) {
            Logger.printLog(TAG, "getOriginContent fail : " + e);
        }
    }

    private void getOriginMedia(JSONObject jsonObject, NoteEntry entry, String content) {
        try {
            JSONArray spans = jsonObject.optJSONArray(DataConvert.JSON_SPANS_KEY);
            if (spans == null) {
                entry.mediaType = -1;
                return;
            }
            int mediaType = -1;
            String thumbUri = null;
            String originUri = null;
            int position = content.length();
            int length = spans.length();
            String noteImageSpan = PhotoImageSpan.class.getName();
            for (int i = 0; i < length; i++) {
                JSONObject json = spans.getJSONObject(i);
                if (json.getString(DataConvert.SPAN_ITEM_TYPE).equals(noteImageSpan)) {
                    int start = json.getInt(DataConvert.SPAN_ITEM_START);
                    if (start < position) {
                        position = start;
                        mediaType = 0;
                        thumbUri = json.getString(PhotoImageSpan.THUMB_URI);
                        originUri = json.getString(PhotoImageSpan.ORIGIN_URI);
                    }
                }
            }
            entry.mediaType = mediaType;
            if (thumbUri != null) {
                entry.thumbnailUri = Uri.parse(thumbUri);
            }
            if (originUri != null) {
                entry.originUri = Uri.parse(originUri);
            }
        } catch (Exception e) {
            Logger.printLog(TAG, "getOriginMedia fail : " + e);
        }
    }

    public static String replaceMediaString(String origin) {
        if (origin == null) {
            return null;
        }
        return origin.replaceAll("<photo:gionee/>\n", "").replaceAll("<sound:gionee/>\n", "").replaceAll(Constants.MEDIA_BILL, "").replaceAll(Constants.MEDIA_PHOTO, "").replaceAll(Constants.MEDIA_SOUND, "");
    }

    public static String parserText(String origin) {
        if (origin == null) {
            return null;
        }
        origin = replaceMediaString(origin);
        for (String text : origin.split(Constants.STR_NEW_LINE)) {
            if (!TextUtils.isEmpty(text)) {
                return text;
            }
        }
        return origin;
    }
}
