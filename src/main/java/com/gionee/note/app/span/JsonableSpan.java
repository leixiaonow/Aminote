package com.gionee.note.app.span;

import android.content.Context;
import android.text.SpannableStringBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public interface JsonableSpan {

    public interface Applyer<T> {
        T applyFromJson(JSONObject jSONObject, SpannableStringBuilder spannableStringBuilder, Context context) throws JSONException;
    }

    void recycle();

    void updateSpanEditableText(SpannableStringBuilder spannableStringBuilder);

    void writeToJson(JSONObject jSONObject) throws JSONException;
}
