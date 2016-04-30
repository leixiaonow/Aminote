package amigoui.preference;

import android.content.Context;
import android.util.AttributeSet;

public class NativePreferenceManager {
    private static boolean mAnalyzeNativePreferenceXml = false;

    NativePreferenceManager() {
    }

    public static void setAnalyzeNativePreferenceXml(boolean isNativePreferenceXml) {
        mAnalyzeNativePreferenceXml = isNativePreferenceXml;
    }

    public static boolean getAnalyzeNativePreferenceXml() {
        return mAnalyzeNativePreferenceXml;
    }

    public static String getAttributeStringValue(Context context, AttributeSet attrs, int index) {
        String mAttrStr = attrs.getAttributeValue(index);
        if (mAttrStr.startsWith("@")) {
            return context.getResources().getString(Integer.valueOf(mAttrStr.substring(1)).intValue());
        }
        return mAttrStr;
    }

    public static String[] getAttributeStringArrayValue(Context context, AttributeSet attrs, int index) {
        String mAttrStr = attrs.getAttributeValue(index);
        if (mAttrStr.startsWith("@")) {
            return context.getResources().getStringArray(Integer.valueOf(mAttrStr.substring(1)).intValue());
        }
        return null;
    }
}
