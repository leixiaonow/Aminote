package com.amigoui.internal.widget;

import com.gionee.aminote.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class AmigoSplitActionBarContainer extends FrameLayout {
    private Drawable mBackground;
    private int mContainerHeight;
    private Context mContext;

    public AmigoSplitActionBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoActionBar);
        this.mBackground = a.getDrawable(R.styleable.AmigoActionBar_amigobackground);
        this.mContainerHeight = a.getLayoutDimension(R.styleable.AmigoActionBar_amigoheight, 0);
        a.recycle();
        setBackgroundDrawable(this.mBackground);
    }
}
