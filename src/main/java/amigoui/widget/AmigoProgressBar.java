package amigoui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class AmigoProgressBar extends ProgressBar {
    public AmigoProgressBar(Context context) {
        this(context, null);
    }

    public AmigoProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842871);
    }

    public AmigoProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmigoProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        changeColers();
    }

    private void changeColers() {
    }
}
