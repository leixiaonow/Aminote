package amigoui.widget;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
//import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class AmigoSmartPopupLayout extends AmigoSmartLayout {
    public static String USER_DEGREE = "user_popup_degree";
    private long TIME_INTERNAL = 2500;

    public AmigoSmartPopupLayout(Context context) {
        super(context);
    }

    public AmigoSmartPopupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        setVisibility(GONE);//8
        this.mGoneTime = System.currentTimeMillis();
        Log.d("test", "mGoneTime = " + this.mGoneTime);
        processUserDegree();
        return super.dispatchTouchEvent(ev);
    }

    protected void processUserDegree() {
        if (this.mGoneTime - this.mVisibleTime > this.TIME_INTERNAL) {
            this.mUserDegree -= (120 - this.mSmartLayoutDegree) / 6;
            if (this.mUserDegree < 0) {
                this.mUserDegree = 0;
            }
        } else {
            this.mUserDegree += this.mSmartLayoutDegree / 6;
            if (this.mUserDegree >= 100) {
                this.mUserDegree = 99;
            }
        }
        saveUserDegree(this.mUserDegree);
    }

    protected int getUserDegree() {
        int userDegree = 30;
        try {
//            userDegree = System.getInt(this.mContext.getContentResolver(), USER_DEGREE);
            userDegree = Settings.System.getInt(this.mContext.getContentResolver(), USER_DEGREE);
        } catch (SettingNotFoundException e) {
//            System.putInt(this.mContext.getContentResolver(), USER_DEGREE, 30);
            Settings.System.putInt(this.mContext.getContentResolver(), USER_DEGREE, 30);
            e.printStackTrace();
        }
        return userDegree;
    }

    protected boolean saveUserDegree(int userDegree) {
//        return System.putInt(this.mContext.getContentResolver(), USER_DEGREE, userDegree);
        return Settings.System.putInt(this.mContext.getContentResolver(), USER_DEGREE, userDegree);
    }
}
