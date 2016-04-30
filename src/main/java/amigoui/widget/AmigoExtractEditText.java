package amigoui.widget;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.util.AttributeSet;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputMethodManager;

public class AmigoExtractEditText extends AmigoEditText {
    private InputMethodService mIME;
    private int mSettingExtractedText;

    public AmigoExtractEditText(Context context) {
        super(context, null);
    }

    public AmigoExtractEditText(Context context, AttributeSet attrs) {
        super(context, attrs, 16842862);
    }

    public AmigoExtractEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void setIME(InputMethodService ime) {
        this.mIME = ime;
    }

    public void startInternalChanges() {
        this.mSettingExtractedText++;
    }

    public void finishInternalChanges() {
        this.mSettingExtractedText--;
    }

    public void setExtractedText(ExtractedText text) {
        try {
            this.mSettingExtractedText++;
            super.setExtractedText(text);
        } finally {
            this.mSettingExtractedText--;
        }
    }

    protected void onSelectionChanged(int selStart, int selEnd) {
        if (this.mSettingExtractedText == 0 && this.mIME != null && selStart >= 0 && selEnd >= 0) {
            this.mIME.onExtractedSelectionChanged(selStart, selEnd);
        }
    }

    public boolean performClick() {
        if (super.performClick() || this.mIME == null) {
            return false;
        }
        this.mIME.onExtractedTextClicked();
        return true;
    }

    public boolean onTextContextMenuItem(int id) {
        if (this.mIME == null || !this.mIME.onExtractTextContextMenuItem(id)) {
            return super.onTextContextMenuItem(id);
        }
        return true;
    }

    public boolean isInputMethodTarget() {
        return true;
    }

    public boolean hasVerticalScrollBar() {
        return computeVerticalScrollRange() > computeVerticalScrollExtent();
    }

    public boolean hasWindowFocus() {
        return isEnabled();
    }

    public boolean isFocused() {
        return isEnabled();
    }

    public boolean hasFocus() {
        return isEnabled();
    }

    protected void viewClicked(InputMethodManager imm) {
        if (this.mIME != null) {
            this.mIME.onViewClicked(false);
        }
    }

    protected void deleteText_internal(int start, int end) {
    }

    protected void replaceText_internal(int start, int end, CharSequence text) {
    }

    protected void setSpan_internal(Object span, int start, int end, int flags) {
    }

    protected void setCursorPosition_internal(int start, int end) {
        this.mIME.onExtractedSelectionChanged(start, end);
    }
}
