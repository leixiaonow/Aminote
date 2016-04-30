package com.gionee.feedback.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.gionee.res.Color;
import com.gionee.res.Dimen;
import com.gionee.res.Drawable;
import com.gionee.res.Text;

public class AddAttachImageView extends LinearLayout {
    private static final int MAX_ATTACH_COUNT = 5;
    private AddAttachViewClickListener mAddAttachViewClickListener;
    private AttachViewClickListener mAttachViewClickListener;
    private Context mContext;
    private int mItemWidth;
    private int mSpacing;

    interface AddAttachViewClickListener {
        void onAddAttachViewClick();
    }

    interface AttachViewClickListener {
        void onAttachViewClick(int i);
    }

    public AddAttachImageView(Context context) {
        this(context, null);
    }

    public AddAttachImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddAttachImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setOrientation(0);
        setGravity(16);
        Resources resources = getResources();
        this.mItemWidth = resources.getDimensionPixelSize(Dimen.gn_fb_dimen_attach_border_size.getIdentifier(this.mContext));
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            boolean isChange = false;

            public void onGlobalLayout() {
                int width = AddAttachImageView.this.getWidth();
                if (!this.isChange) {
                    AddAttachImageView.this.setOrientation(0);
                    AddAttachImageView.this.setGravity(16);
                    AddAttachImageView.this.mSpacing = (width - (AddAttachImageView.this.mItemWidth * 5)) / 4;
                    int childCount = AddAttachImageView.this.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = AddAttachImageView.this.getChildAt(i);
                        LayoutParams params = (LayoutParams) child.getLayoutParams();
                        params.rightMargin = AddAttachImageView.this.mSpacing;
                        child.setLayoutParams(params);
                    }
                    if (width > 0) {
                        this.isChange = true;
                    }
                }
            }
        });
        attachTextView();
        attachView(BitmapFactory.decodeResource(resources, Drawable.gn_fb_drawable_add_attach_bn.getIdentifier(context)));
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        int childCount = getChildCount();
        if (childCount == 1) {
            getChildAt(0).setVisibility(0);
        } else if (childCount == 2) {
            getChildAt(1).setVisibility(0);
            getChildAt(0).setVisibility(0);
        } else {
            getChildAt(childCount - 1).setVisibility(8);
            if (childCount == 7) {
                getChildAt(childCount - 2).setVisibility(8);
            } else {
                getChildAt(childCount - 2).setVisibility(0);
            }
        }
        child.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int count = AddAttachImageView.this.getChildCount();
                int pos = 0;
                while (pos < count) {
                    if (v == AddAttachImageView.this.getChildAt(pos) && count >= 2 && pos != count - 1) {
                        if (pos == count - 2) {
                            if (AddAttachImageView.this.mAddAttachViewClickListener != null) {
                                AddAttachImageView.this.mAddAttachViewClickListener.onAddAttachViewClick();
                            }
                        } else if (AddAttachImageView.this.mAttachViewClickListener != null) {
                            AddAttachImageView.this.mAttachViewClickListener.onAttachViewClick(pos);
                        }
                    }
                    pos++;
                }
            }
        });
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setEnabled(enabled);
        }
    }

    public void setOnAddAttachViewClickListener(AddAttachViewClickListener listener) {
        this.mAddAttachViewClickListener = listener;
    }

    public void setOnAttachViewClickListener(AttachViewClickListener listener) {
        this.mAttachViewClickListener = listener;
    }

    public void addAttach(Bitmap bitmap) {
        attachView(ThumbnailUtils.extractThumbnail(bitmap, this.mItemWidth, this.mItemWidth));
    }

    public void removeAttach() {
        if (getChildCount() > 2) {
            while (getChildCount() > 2) {
                ImageView v = (ImageView) getChildAt(0);
                BitmapDrawable drawable = (BitmapDrawable) v.getDrawable();
                if (drawable != null) {
                    Bitmap bitmap = drawable.getBitmap();
                    if (!(bitmap == null || bitmap.isRecycled())) {
                        bitmap.recycle();
                    }
                }
                removeView(v);
            }
        }
        getChildAt(0).setVisibility(0);
        getChildAt(1).setVisibility(0);
    }

    private void attachTextView() {
        TextView textView = new TextView(this.mContext);
        Resources res = getResources();
        textView.setText(res.getString(Text.gn_fb_string_attach_text.getIdentifier(this.mContext)));
        textView.setTextColor(res.getColor(Color.gn_fb_string_attach_text_textColor.getIdentifier(this.mContext)));
        addView(textView, 0);
    }

    private void attachView(Bitmap bitmap) {
        ImageView imageView = new ImageView(this.mContext);
        LayoutParams params = new LayoutParams(this.mItemWidth, this.mItemWidth);
        params.setMargins(0, 0, this.mSpacing, 0);
        imageView.setLayoutParams(params);
        imageView.setImageBitmap(bitmap);
        int index = getChildCount() - 2;
        if (index <= 0) {
            index = 0;
        }
        addView(imageView, index);
    }
}
