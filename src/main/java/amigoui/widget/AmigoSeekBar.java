package amigoui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class AmigoSeekBar extends SeekBar {
    public AmigoSeekBar(Context context) {
        this(context, null);
    }

    public AmigoSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842875);
    }

    public AmigoSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmigoSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    private void changeColers() {
    }

    private boolean stateIsDisable(int[] state) {
        for (int i : state) {
            if (i == -16842910) {
                return true;
            }
        }
        return false;
    }
}
