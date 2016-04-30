package amigoui.changecolors;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class ChangeColorReceiver extends BroadcastReceiver {
    private static final String TAG = "Chameleon";
    private OnChangeColorListener mOnChangeColorListener;
    private OnChangeColorListenerWithParams mOnChangeColorListenerWithParams;
    private boolean mRestart = true;

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "start -> ");
        if (this.mRestart) {
            restartApplication(context);
            return;
        }
        Log.d(TAG, "Restart Activitys");
        ChameleonColorManager.getInstance().init();
        ChameleonColorManager.getInstance();
        if (!ChameleonColorManager.isNeedChangeColor()) {
            clearDrawableCaches(context);
        }
        if (this.mOnChangeColorListener != null) {
            this.mOnChangeColorListener.onChangeColor();
        }
        if (this.mOnChangeColorListenerWithParams != null && intent != null) {
            this.mOnChangeColorListenerWithParams.onChangeColor(intent.getIntExtra(ColorConfigConstants.CHANGE_COLOR_TYPE, 0));
        }
    }

    private void clearDrawableCaches(Context context) {
        try {
            Class.forName("android.content.res.Resources").getMethod("amigoClearDrawableCaches", new Class[0]).invoke(context.getResources(), new Object[0]);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        }
    }

    public void setOnChangeColorListener(OnChangeColorListener changeColorListener) {
        this.mOnChangeColorListener = changeColorListener;
    }

    public void setOnChangeColorListenerWithParams(OnChangeColorListenerWithParams changeColorListenerWithParams) {
        this.mOnChangeColorListenerWithParams = changeColorListenerWithParams;
    }

    public void setRestart(boolean restart) {
        this.mRestart = restart;
    }

    private void restartApplication(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Log.d(TAG, "Restart Application : " + context.getPackageName());
        am.killBackgroundProcesses(context.getPackageName());
    }
}
