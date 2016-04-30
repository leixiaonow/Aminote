package amigoui.widget;

import amigoui.changecolors.ColorConfigConstants;
import android.content.Context;
import android.util.Log;

public class AmigoWidgetResource {
    public static int getIdentifierById(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, ColorConfigConstants.ID, context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByDrawable(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "drawable", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByLayout(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "layout", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByAnim(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "anim", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByAttr(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "attr", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByBool(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "bool", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByColor(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "color", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByDimen(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "dimen", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByString(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "string", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }

    public static int getIdentifierByStyle(Context context, String idName) {
        if (context != null) {
            return context.getResources().getIdentifier(idName, "style", context.getPackageName());
        }
        Log.e("GnWidget", "context is null");
        return 0;
    }
}
