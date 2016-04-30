package amigoui.widget;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

class AmigoTextViewEditToolbar extends AmigoTextViewToolbar {
    private static final int ID_COPY = android.R.id.copy;
    private static final int ID_CUT = android.R.id.cut;
    private static final int ID_SELECT_ALL = android.R.id.selectAll;
    private static final int ID_START_SELECTING_TEXT = android.R.id.startSelectingText;
    private static final int ID_SWITCH_INPUT_METHOD = android.R.id.switchInputMethod;
    static final String LOG_TAG = "GN_FW_GNTextViewEditToolbar";
    private final int ID_COPY_STR = AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_copy");
    private final int ID_CUT_STR = AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_cut");
    private final int ID_SELECT_ALL_STR = AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_selectAll");
    private final int ID_START_SELECTING_TEXT_STR = AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_select");
    private final int ID_SWITCH_INPUT_METHOD_STR = AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_inputMethod");
    private TextView mItemCopy;
    private TextView mItemCut;
    private TextView mItemInputMethod;
    private TextView mItemSelectAll;
    private TextView mItemStartSelect;
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (AmigoTextViewEditToolbar.this.isShowing()) {
                AmigoTextViewEditToolbar.this.onItemAction(v.getId());
                switch (v.getId()) {
                    case AmigoTextViewEditToolbar.ID_SELECT_ALL /*16908319*/:
                    case AmigoTextViewEditToolbar.ID_START_SELECTING_TEXT /*16908328*/:
                        AmigoTextViewEditToolbar.this.hide();
                        AmigoTextViewEditToolbar.this.show();
                        return;
                    default:
                        AmigoTextViewEditToolbar.this.hide();
                }
            }
        }
    };

    AmigoTextViewEditToolbar(AmigoEditText hostView) {
        super(hostView);
        initToolbarItem();
    }

    protected void initToolbarItem() {
        super.initToolbarItem();
        this.mItemSelectAll = initToolbarItem(ID_SELECT_ALL, this.ID_SELECT_ALL_STR);
        this.mItemStartSelect = initToolbarItem(ID_START_SELECTING_TEXT, this.ID_START_SELECTING_TEXT_STR);
        this.mItemCopy = initToolbarItem(ID_COPY, this.ID_COPY_STR);
        this.mItemCut = initToolbarItem(ID_CUT, this.ID_CUT_STR);
        this.mItemInputMethod = initToolbarItem(ID_SWITCH_INPUT_METHOD, this.ID_SWITCH_INPUT_METHOD_STR);
    }

    protected OnClickListener getOnClickListener() {
        return this.mOnClickListener;
    }

    protected void updateToolbarItemsEx() {
    }

    protected void updateToolbarItems() {
        this.mToolbarGroup.removeAllViews();
        boolean passwordTransformed = this.mEditText.getTransformationMethod() instanceof PasswordTransformationMethod;
        CharSequence text = this.mEditText.getText();
        boolean hasClip = ((ClipboardManager) this.mEditText.getContext().getSystemService(Context.CLIPBOARD_SERVICE)).hasPrimaryClip();
        if (this.mEditText.hasSelection()) {
            if (!passwordTransformed && text.length() > 0 && this.mEditText.getKeyListener() != null) {
                this.mToolbarGroup.addView(this.mItemCut);
            }
            if (!passwordTransformed && text.length() > 0) {
                this.mToolbarGroup.addView(this.mItemCopy);
            }
            if ((text != null) && this.mEditText.getKeyListener() != null && this.mEditText.getSelectionStart() >= 0 && this.mEditText.getSelectionEnd() >= 0 && hasClip) {
                this.mToolbarGroup.addView(this.mItemPaste);
                return;
            }
            return;
        }
        if (text.length() > 0 && this.mEditText.isSelectionToolEnabled()) {
            if (!passwordTransformed) {
                this.mToolbarGroup.addView(this.mItemStartSelect);
            }
            this.mToolbarGroup.addView(this.mItemSelectAll);
        }
        if (this.mEditText.getKeyListener() != null && this.mEditText.getSelectionStart() >= 0 && this.mEditText.getSelectionEnd() >= 0 && hasClip) {
            this.mToolbarGroup.addView(this.mItemPaste);
        }
        if (this.mEditText.isImSwitcherEnabled() && this.mEditText.isInputMethodTarget()) {
            this.mToolbarGroup.addView(this.mItemInputMethod);
        }
    }

    private boolean onItemAction(int id) {
        CharSequence text = this.mEditText.getText();
        int min = 0;
        int max = text.length();
        if (this.mEditText.isFocused()) {
            int selStart = this.mEditText.getSelectionStart();
            int selEnd = this.mEditText.getSelectionEnd();
            min = Math.max(0, Math.min(selStart, selEnd));
            max = Math.max(0, Math.max(selStart, selEnd));
        }
        ClipboardManager clip = (ClipboardManager) this.mEditText.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        switch (id) {
            case ID_SELECT_ALL /*16908319*/:
                Selection.setSelection((Spannable) text, 0, text.length());
                this.mEditText.mStart = 0;
                this.mEditText.mEnd = text.length();
                this.mEditText.startTextSelectionMode();
                return true;
            case ID_CUT /*16908320*/:
                int end = this.mEditText.getSelectionStart();
                clip.setText(text.subSequence(min, max));
                ((Editable) text).delete(min, max);
                this.mEditText.stopTextSelectionMode();
                int maxlength = this.mEditText.getText().length();
                if (end > maxlength) {
                    end = maxlength;
                }
                Selection.setSelection(this.mEditText.getText(), end);
                return true;
            case ID_COPY /*16908321*/:
                clip.setText(text.subSequence(min, max));
                this.mEditText.stopTextSelectionMode();
                return true;
            case android.R.id.paste:
                CharSequence paste = clip.getText();
                if (paste != null && paste.length() > 0) {
                    Selection.setSelection((Spannable) text, max);
                    ((Editable) text).replace(min, max, paste);
                    this.mEditText.stopTextSelectionMode();
                }
                return true;
            case ID_SWITCH_INPUT_METHOD /*16908324*/:
                if (!(this.mEditText instanceof AmigoExtractEditText)) {
                    InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showInputMethodPicker();
                    }
                }
                return true;
            case ID_START_SELECTING_TEXT /*16908328*/:
                this.mEditText.startTextSelectionMode();
                return true;
            default:
                return false;
        }
    }
}
