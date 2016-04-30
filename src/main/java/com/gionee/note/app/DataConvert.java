package com.gionee.note.app;

import android.app.Activity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import com.gionee.framework.log.Logger;
import com.gionee.note.app.span.BillItem;
import com.gionee.note.app.span.JsonableSpan;
import com.gionee.note.app.span.JsonableSpan.Applyer;
import com.gionee.note.app.span.PhotoImageSpan;
import com.gionee.note.app.view.NoteContentEditText;
import com.gionee.note.common.BadJsonableException;
import com.gionee.note.common.NoteUtils;
import com.gionee.note.data.NoteParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

public class DataConvert {
    public static final String JSON_CONTENT_KEY = "text";
    public static final String JSON_SPANS_KEY = "spans";
    public static final String SPAN_ITEM_END = "end";
    public static final String SPAN_ITEM_FLAG = "flag";
    public static final String SPAN_ITEM_START = "start";
    public static final String SPAN_ITEM_TYPE = "class";
    public static final String TAG = "DataConvert";

    public static void applySpanToEditableFromJson(Activity activity, String string, NoteContentEditText editText) {
        SpannableStringBuilder builder = (SpannableStringBuilder) editText.getText();
        try {
            JSONObject jsonObject = (JSONObject) new JSONTokener(string).nextValue();
            builder.append(jsonObject.getString(JSON_CONTENT_KEY));
            JSONArray spans = jsonObject.optJSONArray(JSON_SPANS_KEY);
            if (spans != null) {
                int length = spans.length();
                for (int i = 0; i < length; i++) {
                    JSONObject json = spans.getJSONObject(i);
                    Object span = getJsonableApplyer(json.getString(SPAN_ITEM_TYPE)).applyFromJson(json, builder, activity);
                    if (span instanceof PhotoImageSpan) {
                        ((PhotoImageSpan) span).setOnImageSpanChangeListener(editText);
                    } else if (span instanceof BillItem) {
                        ((BillItem) span).setOnImageSpanChangeListener(editText);
                    }
                }
            }
        } catch (JSONException e) {
            Logger.printLog(TAG, "applySpanToEditableFromJson fail : " + e + " ,,, editText ,,," + editText);
        }
    }

    public static String editableConvertToJson(Editable editable) {
        try {
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object();
            jsonStringer.key(JSON_CONTENT_KEY).value(editable.toString());
            JSONArray spans = convertSpansToJson(editable);
            if (!(spans == null || spans.length() == 0)) {
                jsonStringer.key(JSON_SPANS_KEY).value(spans);
            }
            jsonStringer.endObject();
            return jsonStringer.toString();
        } catch (Exception e) {
            Logger.printLog(TAG, "editableConvertToJson fail : " + e + " ,,,editable,,," + editable.toString());
            return null;
        }
    }

    private static Applyer getJsonableApplyer(String clzName) throws JSONException {
        try {
            Applyer applyer = (Applyer) Class.forName(clzName).getField("APPLYER").get(null);
            if (applyer != null) {
                return applyer;
            }
            throw new BadJsonableException("JsonableSpan protocol requires a JsonableSpan.Creator object called  CREATOR on class " + clzName);
        } catch (IllegalAccessException e) {
            Logger.printLog(TAG, "Illegal access when unmarshalling: " + clzName + " :" + e);
            throw new BadJsonableException("IllegalAccessException when unmarshalling: " + clzName);
        } catch (ClassNotFoundException e2) {
            Logger.printLog(TAG, "Class not found when unmarshalling: " + clzName + e2);
            throw new BadJsonableException("ClassNotFoundException when unmarshalling: " + clzName);
        } catch (ClassCastException e3) {
            throw new BadJsonableException("JsonableSpan protocol requires a JsonableSpan.Creator object called  CREATOR on class " + clzName);
        } catch (NoSuchFieldException e4) {
            throw new BadJsonableException("JsonableSpan protocol requires a JsonableSpan.Creator object called  CREATOR on class " + clzName);
        } catch (NullPointerException e5) {
            throw new BadJsonableException("JsonableSpan protocol requires the CREATOR object to be static on class " + clzName);
        }
    }

    private static JSONArray convertSpansToJson(Editable editable) {
        JsonableSpan[] jsonableSpans = (JsonableSpan[]) editable.getSpans(0, editable.length(), JsonableSpan.class);
        JSONArray jsonArray = new JSONArray();
        for (JsonableSpan jsonableSpan : jsonableSpans) {
            JSONObject jsonObject = new JSONObject();
            try {
                boolean z;
                jsonableSpan.writeToJson(jsonObject);
                if (jsonObject.getString(SPAN_ITEM_TYPE) != null) {
                    z = true;
                } else {
                    z = false;
                }
                NoteUtils.assertTrue(z, "jsonable should be put in SPAN_ITEM_TYPE field. ");
            } catch (JSONException e) {
                Logger.printLog(TAG, "convert to json error:" + e + " ,,,,editable = " + editable.toString());
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public static String getContent(String json) {
        try {
            return NoteParser.replaceMediaString(((JSONObject) new JSONTokener(json).nextValue()).getString(JSON_CONTENT_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
