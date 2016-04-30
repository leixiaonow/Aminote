package com.gionee.feedback.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.gionee.res.ResourceNotFoundException;

@SuppressLint({"ViewConstructor"})
public class BadgeView extends TextView {
    private static final float CROP = 0.4f;
    private Context mContext;
    private int mHeight;
    private int mIconHeight;
    private int mIconWidth;
    private int mWidth;

    public BadgeView(Context context, View target) {
        this(context, null, 16842884, target);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyle, View target) {
        super(context, attrs, defStyle);
        init(context, target);
    }

    private void init(Context context, View target) {
        this.mContext = context;
        try {
            Drawable drawable = getResources().getDrawable(com.gionee.res.Drawable.gn_fb_drawable_subscript.getIdentifier(this.mContext));
            this.mWidth = drawable.getIntrinsicWidth();
            this.mHeight = drawable.getIntrinsicHeight();
            setBackground(drawable);
            Drawable iconDrawable = getResources().getDrawable(com.gionee.res.Drawable.gn_fb_drawable_historymenu.getIdentifier(this.mContext));
            this.mIconHeight = iconDrawable.getIntrinsicHeight();
            this.mIconWidth = iconDrawable.getIntrinsicWidth();
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
        setGravity(17);
        setTextColor(-1);
        applyTo(target);
        gone();
    }

    private void applyTo(View target) {
        LayoutParams lp = target.getLayoutParams();
        ViewGroup group = (ViewGroup) target.getParent();
        RelativeLayout container = new RelativeLayout(this.mContext);
        int index = group.indexOfChild(target);
        group.removeView(target);
        group.addView(container, index, lp);
        RelativeLayout.LayoutParams layoutParamsOfTarget = new RelativeLayout.LayoutParams(lp.width, lp.height);
        layoutParamsOfTarget.setMargins(0, (int) (((float) this.mHeight) * CROP), (int) (((float) this.mWidth) * CROP), 0);
        layoutParamsOfTarget.addRule(11);
        target.setLayoutParams(layoutParamsOfTarget);
        container.addView(target);
        RelativeLayout.LayoutParams layoutParamsOfThis = new RelativeLayout.LayoutParams(this.mWidth, this.mHeight);
        layoutParamsOfThis.setMargins(0, ((lp.height - this.mIconHeight) - (this.mHeight / 2)) / 2, ((lp.width - this.mIconWidth) - (this.mWidth / 2)) / 2, 0);
        layoutParamsOfThis.addRule(11);
        layoutParamsOfThis.addRule(10);
        setLayoutParams(layoutParamsOfThis);
        container.addView(this);
        group.invalidate();
    }

    public void setShowCount(int count) {
        if (count <= 0) {
            gone();
            return;
        }
        if (count > 9) {
            setText("N");
        } else {
            setText(String.valueOf(count));
        }
        show();
    }

    public void show() {
        setVisibility(0);
    }

    public void gone() {
        setVisibility(8);
    }
}
