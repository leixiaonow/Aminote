package amigoui.changecolors;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public class ChameleonColorManager implements OnChangeColorListener, OnChangeColorListenerWithParams {
    private static final String TAG = "Chameleon";
    private static int sAccentColorG1;
    private static int sAccentColorG2;
    private static int sAppbarColorA1;
    private static int sBackgroudColorB1;
    private static int sButtonBackgroudColorB4;
    private static ChameleonColorManager sChameleonColorManager;
    private static int sContentColorForthlyOnAppbarT4;
    private static int sContentColorOnStatusbarS3;
    private static int sContentColorPrimaryOnAppbarT1;
    private static int sContentColorPrimaryOnBackgroudC1;
    private static int sContentColorSecondaryOnAppbarT2;
    private static int sContentColorSecondaryOnBackgroudC2;
    private static int sContentColorThirdlyOnAppbarT3;
    private static int sContentColorThirdlyOnBackgroudC3;
    private static int sEditTextBackgroudColorB3;
    private static boolean sIsNeedChangeColor = false;
    private static boolean sIsPowerSavingMode;
    private static int sPopupBackgroudColorB2;
    private static int sStatusbarBackgroudColorS1;
    private static int sSystemNavigationBackgroudColorS2;
    private static int sThemeType;
    private String CHAMELEON_ACTION = "amigo.intent.action.chameleon.CHANGE_COLOR";
    private ArrayList<Activity> mActivityList = new ArrayList();
    private ChangeColorReceiver mChangeColorReceiver;
    private Context mContext;
    private IntentFilter mIntentFilter;
    private ArrayList<OnChangeColorListener> mOnChangeColorListenerList = new ArrayList();
    private ArrayList<OnChangeColorListenerWithParams> mOnChangeColorListenerWithParamsList = new ArrayList();

    public static synchronized ChameleonColorManager getInstance() {
        ChameleonColorManager chameleonColorManager;
        synchronized (ChameleonColorManager.class) {
            if (sChameleonColorManager == null) {
                sChameleonColorManager = new ChameleonColorManager();
            }
            chameleonColorManager = sChameleonColorManager;
        }
        return chameleonColorManager;
    }

    public void register(Context context) {
        register(context, true);
    }

    public void register(Context context, boolean restart) {
        Log.v(TAG, context.getPackageName() + " Register Chameleon, restart = " + restart);
        this.mContext = context;
        this.mIntentFilter = new IntentFilter(this.CHAMELEON_ACTION);
        this.mChangeColorReceiver = new ChangeColorReceiver();
        this.mChangeColorReceiver.setRestart(restart);
        this.mChangeColorReceiver.setOnChangeColorListener(this);
        context.registerReceiver(this.mChangeColorReceiver, this.mIntentFilter);
        init();
    }

    public void unregister() {
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mChangeColorReceiver);
            this.mContext = null;
        }
    }

    public void onCreate(Activity activity) {
        this.mActivityList.add(activity);
    }

    public void onDestroy(Activity activity) {
        this.mActivityList.remove(activity);
    }

    public void addOnChangeColorListener(OnChangeColorListener onChangeColorListener) {
        this.mOnChangeColorListenerList.add(onChangeColorListener);
    }

    public void removeOnChangeColorListener(OnChangeColorListener onChangeColorListener) {
        this.mOnChangeColorListenerList.remove(onChangeColorListener);
    }

    public void addOnChangeColorListenerWithParams(OnChangeColorListenerWithParams onchangeColorListenerWithParams) {
        this.mOnChangeColorListenerWithParamsList.add(onchangeColorListenerWithParams);
    }

    public void removeOnChangeColorListenerWithParams(OnChangeColorListenerWithParams onChangeColorListenerWithParams) {
        this.mOnChangeColorListenerWithParamsList.remove(onChangeColorListenerWithParams);
    }

    public void onChangeColor() {
        Iterator i$ = this.mActivityList.iterator();
        while (i$.hasNext()) {
            Activity activity = (Activity) i$.next();
            if (activity != null && activity.getParent() == null) {
                Log.d(TAG, "Restart Activity  : " + activity.getComponentName().getClassName());
                activity.recreate();
            }
        }
        i$ = this.mOnChangeColorListenerList.iterator();
        while (i$.hasNext()) {
            OnChangeColorListener onChangeColorListerner = (OnChangeColorListener) i$.next();
            if (onChangeColorListerner != null) {
                onChangeColorListerner.onChangeColor();
            }
        }
    }

    public void onChangeColor(int changeColorType) {
        Iterator i$ = this.mActivityList.iterator();
        while (i$.hasNext()) {
            Activity activity = (Activity) i$.next();
            if (activity != null && activity.getParent() == null) {
                Log.d(TAG, "Restart Activity  : " + activity.getComponentName().getClassName());
                activity.recreate();
            }
        }
        i$ = this.mOnChangeColorListenerWithParamsList.iterator();
        while (i$.hasNext()) {
            OnChangeColorListenerWithParams onChangeColorListernerWithParams = (OnChangeColorListenerWithParams) i$.next();
            if (onChangeColorListernerWithParams != null) {
                onChangeColorListernerWithParams.onChangeColor(changeColorType);
            }
        }
    }

    public void init() {
        Cursor cursor = this.mContext.getContentResolver().query(Uri.parse("content://com.amigo.chameleon.provider/colorConfiguration"), null, null, null, null);
        if (cursor == null || !cursor.moveToFirst()) {
            Log.d(TAG, "No data in the database");
            sIsPowerSavingMode = false;
            sIsNeedChangeColor = false;
        } else {
            sAppbarColorA1 = getColorFromCursor(cursor, ColorConfigConstants.APPBAR_COLOR_A1, ColorConfigConstants.DEFAULT_APPBAR_COLOR_A1);
            sBackgroudColorB1 = getColorFromCursor(cursor, ColorConfigConstants.BACKGROUND_COLOR_B1, ColorConfigConstants.DEFAULT_BACKGROUND_COLOR_B1);
            sPopupBackgroudColorB2 = getColorFromCursor(cursor, ColorConfigConstants.POPUP_BACKGROUND_COLOR_B2, ColorConfigConstants.DEFAULT_POPUP_BACKGROUND_COLOR_B2);
            sEditTextBackgroudColorB3 = getColorFromCursor(cursor, ColorConfigConstants.EDIT_TEXT_BACKGROUND_COLOR_B3, ColorConfigConstants.DEFAULT_EDIT_TEXT_BACKGROUND_COLOR_B3);
            sButtonBackgroudColorB4 = getColorFromCursor(cursor, ColorConfigConstants.BUTTON_BACKGROUND_COLOR_B4, ColorConfigConstants.DEFAULT_BUTTON_BACKGROUND_COLOR_B4);
            sStatusbarBackgroudColorS1 = getColorFromCursor(cursor, ColorConfigConstants.STATUSBAR_BACKGROUND_COLOR_S1, ColorConfigConstants.DEFAULT_STATUSBAR_BACKGROUND_COLOR_S1);
            sSystemNavigationBackgroudColorS2 = getColorFromCursor(cursor, ColorConfigConstants.SYSTEM_NAVIGATION_BACKGROUND_COLOR_S2, ColorConfigConstants.DEFAULT_SYSTEM_NAVIGATION_BACKGROUND_COLOR_S2);
            sAccentColorG1 = getColorFromCursor(cursor, ColorConfigConstants.ACCENT_COLOR_G1, ColorConfigConstants.DEFAULT_ACCENT_COLOR_G1);
            sAccentColorG2 = getColorFromCursor(cursor, ColorConfigConstants.ACCENT_COLOR_G2, ColorConfigConstants.DEFAULT_ACCENT_COLOR_G2);
            sContentColorPrimaryOnAppbarT1 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_PRIMARY_ON_APPBAR_T1, ColorConfigConstants.DEFAULT_CONTENT_COLOR_PRIMARY_ON_APPBAR_T1);
            sContentColorSecondaryOnAppbarT2 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_SECONDARY_ON_APPBAR_T2, ColorConfigConstants.DEFAULT_CONTENT_COLOR_SECONDARY_ON_APPBAR_T2);
            sContentColorThirdlyOnAppbarT3 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_THIRDLY_ON_APPBAR_T3, ColorConfigConstants.DEFAULT_CONTENT_COLOR_THIRDLY_ON_APPBAR_T3);
            sContentColorForthlyOnAppbarT4 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_FORTHLY_ON_APPBAR_T4, ColorConfigConstants.DEFAULT_CONTENT_COLOR_FORTHLY_ON_APPBAR_T4);
            sContentColorPrimaryOnBackgroudC1 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_PRIMARY_ON_BACKGROUD_C1, ColorConfigConstants.DEFAULT_CONTENT_COLOR_PRIMARY_ON_BACKGROUD_C1);
            sContentColorSecondaryOnBackgroudC2 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_SECONDARY_ON_BACKGROUD_C2, ColorConfigConstants.DEFAULT_CONTENT_COLOR_SECONDARY_ON_BACKGROUD_C2);
            sContentColorThirdlyOnBackgroudC3 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_THIRDLY_ON_BACKGROUD_C3, ColorConfigConstants.DEFAULT_CONTENT_COLOR_THIRDLY_ON_BACKGROUD_C3);
            sContentColorOnStatusbarS3 = getColorFromCursor(cursor, ColorConfigConstants.CONTENT_COLOR_ON_STATUSBAR_S3, ColorConfigConstants.DEFAULT_CONTENT_COLOR_ON_STATUSBAR_S3);
            sThemeType = getColorFromCursor(cursor, ColorConfigConstants.THEME_TYPE, ColorConfigConstants.DEFAULT_THEME_TYPE);
            sIsPowerSavingMode = getColorFromCursor(cursor, ColorConfigConstants.ID, ColorConfigConstants.DEFAULT_ID) == ColorConfigConstants.POWER_SAVING_ID;
            sIsNeedChangeColor = true;
            Log.d(TAG, "G1=" + sAccentColorG1 + "; B1=" + sBackgroudColorB1);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private int getColorFromCursor(Cursor cursor, String columnName) {
        return getColorFromCursor(cursor, columnName, 0);
    }

    private int getColorFromCursor(Cursor cursor, String columnName, int defaultColor) {
        int color = defaultColor;
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            return cursor.getInt(index);
        }
        return color;
    }

    public static boolean isNeedChangeColor() {
        return sIsNeedChangeColor;
    }

    public static int getAppbarColor_A1() {
        return sAppbarColorA1;
    }

    public static int getBackgroudColor_B1() {
        return sBackgroudColorB1;
    }

    public static int getPopupBackgroudColor_B2() {
        return sPopupBackgroudColorB2;
    }

    public static int getEditTextBackgroudColor_B3() {
        return sEditTextBackgroudColorB3;
    }

    public static int getButtonBackgroudColor_B4() {
        return sButtonBackgroudColorB4;
    }

    public static int getStatusbarBackgroudColor_S1() {
        return sStatusbarBackgroudColorS1;
    }

    public static int getSystemNavigationBackgroudColor_S2() {
        return sSystemNavigationBackgroudColorS2;
    }

    public static int getAccentColor_G1() {
        return sAccentColorG1;
    }

    public static int getAccentColor_G2() {
        return sAccentColorG2;
    }

    public static int getContentColorPrimaryOnAppbar_T1() {
        return sContentColorPrimaryOnAppbarT1;
    }

    public static int getContentColorSecondaryOnAppbar_T2() {
        return sContentColorSecondaryOnAppbarT2;
    }

    public static int getContentColorThirdlyOnAppbar_T3() {
        return sContentColorThirdlyOnAppbarT3;
    }

    public static int getContentColorForthlyOnAppbar_T4() {
        return sContentColorForthlyOnAppbarT4;
    }

    public static int getContentColorPrimaryOnBackgroud_C1() {
        return sContentColorPrimaryOnBackgroudC1;
    }

    public static int getContentColorSecondaryOnBackgroud_C2() {
        return sContentColorSecondaryOnBackgroudC2;
    }

    public static int getContentColorThirdlyOnBackgroud_C3() {
        return sContentColorThirdlyOnBackgroudC3;
    }

    public static int getContentColorOnStatusbar_S3() {
        return sContentColorOnStatusbarS3;
    }

    public static int getThemeType() {
        return sThemeType;
    }

    public static boolean isPowerSavingMode() {
        return sIsPowerSavingMode;
    }
}
