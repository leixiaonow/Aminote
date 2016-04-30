package amigoui.widget;

import amigoui.changecolors.ChameleonColorManager;
import amigoui.changecolors.ChangeColorUtil;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AmigoTextView extends TextView {
    private static final String TAG = "AmigoTextView";

    public AmigoTextView(Context context) {
        this(context, null);
    }

    public AmigoTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842884);
    }

    public AmigoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmigoTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        changeColor();
    }

    private void changeColor() {
        if (ChameleonColorManager.isNeedChangeColor()) {
            ChangeColorUtil.changeTextViewTextColor(this);
        }
    }
}
