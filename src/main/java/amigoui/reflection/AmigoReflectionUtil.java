package amigoui.reflection;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.view.Window;

public class AmigoReflectionUtil {
    private static final int DEFAULT_ACCENT_COLOR = -28672;

    public static void setStatusBarColor(Window window, int color) {
        if (VERSION.SDK_INT > 20) {
            try {
                Window.class.getMethod("setStatusBarColor", new Class[]{Integer.TYPE}).invoke(window, new Object[]{Integer.valueOf(color)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getDisableColor(ColorStateList colorStateList) {
        if (VERSION.SDK_INT <= 20) {
            return DEFAULT_ACCENT_COLOR;
        }
        int disableColorIndex = -1;
        int[][] stateItems = getStates(colorStateList);
        for (int i = 0; i < stateItems.length; i++) {
            for (int state : stateItems[i]) {
                if (state == -16842910) {
                    disableColorIndex = i;
                    break;
                }
            }
        }
        if (disableColorIndex < 0 || disableColorIndex >= getColors(colorStateList).length) {
            return DEFAULT_ACCENT_COLOR;
        }
        return getColors(colorStateList)[disableColorIndex];
    }

    public static int[][] getStates(ColorStateList colorStateList) {
        if (VERSION.SDK_INT > 20) {
            try {
                return (int[][]) ColorStateList.class.getMethod("getStates", new Class[0]).invoke(colorStateList, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (int[][]) null;
    }

    public static int[] getColors(ColorStateList colorStateList) {
        if (VERSION.SDK_INT > 20) {
            try {
                return (int[]) ColorStateList.class.getMethod("getColors", new Class[0]).invoke(colorStateList, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void setTint(Drawable drawable, int color) {
        if (VERSION.SDK_INT > 20) {
            try {
                Drawable.class.getMethod("setTint", new Class[]{Integer.TYPE}).invoke(drawable, color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
