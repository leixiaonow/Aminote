package com.gionee.feedback.ui;

import amigoui.preference.AmigoPreference;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gionee.res.Layout;
import com.gionee.res.ResourceNotFoundException;
import com.gionee.res.Text;
import com.gionee.res.Widget;

public class ExpandableTextView extends LinearLayout {
    private boolean isExpanded;
    private boolean isPreDraw;
    private TextView mContentView;
    private Context mContext;
    private TextView mExpandableView;
    private int mExpandedLines;
    private OnClickListener mOnClickListener;
    private OnPreDrawListener mOnPreDrawListener;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.isExpanded = false;
        this.isPreDraw = false;
        this.mExpandedLines = 4;
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                ExpandableTextView.this.isExpanded = !ExpandableTextView.this.isExpanded;
                ExpandableTextView.this.mExpandableView.setText(ExpandableTextView.this.isExpanded ? Text.gn_fb_string_packup.getIdentifier(ExpandableTextView.this.mContext) : Text.gn_fb_string_expansion.getIdentifier(ExpandableTextView.this.mContext));
                ExpandableTextView.this.mContentView.postInvalidate();
                ExpandableTextView.this.isPreDraw = true;
            }
        };
        this.mOnPreDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                if (ExpandableTextView.this.isPreDraw) {
                    int lineCount = ExpandableTextView.this.mContentView.getLineCount();
                    if (lineCount > ExpandableTextView.this.mExpandedLines) {
                        ExpandableTextView.this.mExpandableView.setVisibility(0);
                        if (ExpandableTextView.this.isExpanded) {
                            ExpandableTextView.this.mContentView.setMaxLines(AmigoPreference.DEFAULT_ORDER);
                        } else {
                            ExpandableTextView.this.mContentView.setMaxLines(ExpandableTextView.this.mExpandedLines);
                        }
                    } else {
                        ExpandableTextView.this.mExpandableView.setVisibility(8);
                        ExpandableTextView.this.mContentView.setMaxLines(lineCount);
                    }
                    ExpandableTextView.this.isPreDraw = false;
                }
                return true;
            }
        };
        this.mContext = context;
        try {
            View view = LayoutInflater.from(context).inflate(Layout.gn_fb_layout_expandtextview.getIdentifier(context), this, true);
            this.mContentView = (TextView) getViewById(view, Widget.gn_fb_id_expandtextview_content.getIdentifier(this.mContext));
            this.mContentView.getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
            this.mExpandableView = (TextView) getViewById(view, Widget.gn_fb_id_expandtextview_expandable.getIdentifier(this.mContext));
            this.mExpandableView.setOnClickListener(this.mOnClickListener);
            this.mExpandableView.setText(Text.gn_fb_string_expansion.getIdentifier(this.mContext));
        } catch (ResourceNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setText(SpannableStringBuilder builder) {
        this.mContentView.setText(builder);
        this.isPreDraw = true;
    }

    public void setText(String text) {
        this.mContentView.setText(text);
        this.isPreDraw = true;
    }

    public void setExpandedLines(int lines) {
        this.mExpandedLines = lines;
    }

    public void setTextSize(float size) {
        this.mContentView.setTextSize(size);
        this.isPreDraw = true;
    }

    public void setTextColor(int color) {
        this.mContentView.setTextColor(color);
        this.isPreDraw = true;
    }

    private <T extends View> T getViewById(View view, int id) {
        return view.findViewById(id);
    }
}
