package amigoui.widget;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

//import android.provider.Settings.System;

public class AmigoSmartFullScreenLayout extends AmigoSmartLayout implements OnClickListener {
    public static String USER_DEGREE = "user_full_screen_guide_degree";
    private long TIME_INTERNAL = 3000;
    private LinearLayout mLayoutView;
    private LayoutParams mParams;
    private AmigoButton mPositiveBtn;
    private boolean mShowPositiveBtn = false;

    public AmigoSmartFullScreenLayout(Context context) {
        super(context);
        init();
    }

    public AmigoSmartFullScreenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOnClickListener(this);
        initPositiveBtn();
    }

    public void onClick(View v) {
        if (!this.mShowPositiveBtn) {
            onClickEvent(v);
        }
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
            Settings.System.putInt(this.mContext.getContentResolver(), USER_DEGREE, 30);
//            System.putInt(this.mContext.getContentResolver(), USER_DEGREE, 30);
            e.printStackTrace();
        }
        return userDegree;
    }

    protected boolean saveUserDegree(int userDegree) {
//        return System.putInt(this.mContext.getContentResolver(), USER_DEGREE, userDegree);
        return Settings.System.putInt(this.mContext.getContentResolver(), USER_DEGREE, userDegree);
    }

    public void setPositiveBtnVisible(boolean flg) {
        if (this.mLayoutView != null) {
            this.mShowPositiveBtn = flg;
            if (this.mShowPositiveBtn) {
                this.mLayoutView.setVisibility(VISIBLE);
            } else {
                this.mLayoutView.setVisibility(GONE);
            }
        }
    }

    private void initPositiveBtn() {
        this.mLayoutView = new LinearLayout(this.mContext);
        this.mPositiveBtn = new AmigoButton(this.mContext);
        this.mPositiveBtn.setText(AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_smart_full_btn_label"));
        this.mPositiveBtn.setButtonStyle(1);
        this.mPositiveBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AmigoSmartFullScreenLayout.this.onClickEvent(v);
            }
        });
        this.mLayoutView.setOrientation(LinearLayout.VERTICAL);
        LayoutParams buttonParams = new LayoutParams(-2, -2);
        buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
        this.mLayoutView.addView(this.mPositiveBtn, buttonParams);
        this.mParams = new LayoutParams(-1, -2);
        this.mParams.gravity = Gravity.BOTTOM;
        this.mParams.bottomMargin = this.mContext.getResources().getDimensionPixelSize(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_smart_full_btn_bottom_margin"));
        addView(this.mLayoutView, this.mParams);
        setPositiveBtnVisible(true);
    }

    public AmigoButton getOperateButton() {
        return this.mPositiveBtn;
    }

    private void onClickEvent(View v) {
        setVisibility(GONE);
        this.mGoneTime = System.currentTimeMillis();
        Log.d("test", "mGoneTime = " + this.mGoneTime);
        processUserDegree();
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (this.mLayoutView != null) {
            if (this.mShowPositiveBtn) {
                this.mLayoutView.setVisibility(VISIBLE);//0
            } else {
                this.mLayoutView.setVisibility(GONE);//8
            }
        }
    }
}
