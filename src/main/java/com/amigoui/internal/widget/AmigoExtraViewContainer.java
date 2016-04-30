package com.amigoui.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class AmigoExtraViewContainer extends RelativeLayout {
    private View mView;

    public AmigoExtraViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setExtraView(View view) {
        if (this.mView != null) {
            removeView(this.mView);
        }
        this.mView = view;
        if (view != null) {
            LayoutParams params = new LayoutParams(-2, -2);
            params.addRule(13);
            this.mView.setLayoutParams(params);
            addView(this.mView);
        }
    }

    public View getExtraView() {
        return this.mView;
    }
}
