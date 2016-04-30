package com.gionee.note.app.span;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public interface AbstractClickSpan {
    boolean isClickValid(TextView textView, MotionEvent motionEvent, int i);

    void onClick(View view);
}
