package amigoui.reflection;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;

public class AmigoRippleDrawable {
    private static Class<?> mClasz;

    static {
        mClasz = null;
        try {
            if (VERSION.SDK_INT > 20) {
                mClasz = Class.forName("android.graphics.drawable.RippleDrawable");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean instanceofRippleDrawable(Object obj) {
        if (mClasz != null) {
            return mClasz.isAssignableFrom(obj.getClass());
        }
        return false;
    }

    public static void setColor(Drawable selector, ColorStateList colorStateList) {
        try {
            mClasz.getMethod("setColor", new Class[]{ColorStateList.class}).invoke(selector, new Object[]{colorStateList});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
