package com.gionee.note.app.view;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.gionee.aminote.R;

public class PicSelectorItemView extends FrameLayout {
    private static final int CHECKBOX_ID = 2;
    private static final int IMAGEVIEW_ID = 1;
    private static final int SELECT_STATE_ID = 3;
    private ImageView mCheckBox;
    private int mCheckBoxMargin;
    private ImageView mImageView;

    public PicSelectorItemView(Context context) {
        super(context);
        initView(context);
    }

    public PicSelectorItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PicSelectorItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mCheckBoxMargin = context.getResources().getDimensionPixelOffset(R.dimen.attach_selector_checkbox_margin);
        this.mImageView = new ImageView(context);
        this.mImageView.setId(1);
        this.mImageView.setScaleType(ScaleType.FIT_XY);
        this.mImageView.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.attach_pic_selector_bg, null));
        LayoutParams imageViewParams = new LayoutParams(-2, -1);
        imageViewParams.width = context.getResources().getDimensionPixelOffset(R.dimen.attach_selector_pic_default_widht);
        imageViewParams.height = context.getResources().getDimensionPixelOffset(R.dimen.attach_selector_pic_height);
        addView(this.mImageView, imageViewParams);
        LayoutParams checkBoxParams = new LayoutParams(-2, -2);
        this.mCheckBox = new ImageView(context);
        this.mCheckBox.setId(2);
        addView(this.mCheckBox, checkBoxParams);
        LayoutParams selectStateParams = new LayoutParams(-1, -1);
        ImageView selectorStateView = new ImageView(context);
        selectorStateView.setId(3);
        selectorStateView.setImageDrawable(context.getResources().getDrawable(R.drawable.attach_pic_selector_bg));
        addView(selectorStateView, selectStateParams);
    }

    public ImageView getImageView() {
        return this.mImageView;
    }

    public ImageView getCheckBox() {
        return this.mCheckBox;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getId() == 1) {
                layoutImageView(child);
            } else if (child.getId() == 2) {
                layoutCheckBox(child);
            } else {
                layout(child);
            }
        }
    }

    private void layoutImageView(View imageVie) {
        imageVie.layout(0, 0, imageVie.getMeasuredWidth(), imageVie.getMeasuredHeight());
    }

    private void layoutCheckBox(View checkBox) {
        int left = (getMeasuredWidth() - checkBox.getMeasuredWidth()) - this.mCheckBoxMargin;
        checkBox.layout(left, this.mCheckBoxMargin, checkBox.getMeasuredWidth() + left, this.mCheckBoxMargin + checkBox.getMeasuredHeight());
    }

    private void layout(View selectorStateView) {
        selectorStateView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }
}
