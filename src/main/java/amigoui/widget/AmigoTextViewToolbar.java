package amigoui.widget;

import android.text.Layout;
import android.view.View.OnClickListener;
import android.widget.TextView;

abstract class AmigoTextViewToolbar extends AmigoViewToolbar {
    protected static final int ID_PASTE = android.R.id.paste;
    protected final int ID_PASTE_STR = AmigoWidgetResource.getIdentifierByString(this.mContext, "amigo_paste");
    protected AmigoEditText mEditText;
    protected TextView mItemPaste;
    private int mLineHeight;
    private int mScreenX;
    private int mScreenY;

    protected abstract OnClickListener getOnClickListener();

    AmigoTextViewToolbar(AmigoEditText hostView) {
        super(hostView);
        this.mEditText = hostView;
    }

    protected void initToolbarItem() {
        this.mItemPaste = initToolbarItem(ID_PASTE, this.ID_PASTE_STR);
    }

    void show() {
        if (!this.mShowing) {
            calculateScreenPosition();
            showInternal(this.mScreenX, this.mScreenY, this.mLineHeight, this.mEditText.getSelectionStart() != this.mEditText.getSelectionEnd());
        }
    }

    void move() {
        if (this.mShowing) {
            calculateScreenPosition();
            moveInternal(this.mScreenX, this.mScreenY, this.mLineHeight, this.mEditText.getSelectionStart() != this.mEditText.getSelectionEnd());
        }
    }

    private void calculateScreenPosition() {
        int[] location = new int[2];
        this.mEditText.getLocationOnScreen(location);
        int start = this.mEditText.getSelectionStart();
        int end = this.mEditText.getSelectionEnd();
        Layout layout = this.mEditText.getLayout();
        if (layout != null) {
            int line = layout.getLineForOffset(start);
            int top = layout.getLineTop(line);
            this.mLineHeight = layout.getLineBottom(line) - top;
            this.mScreenY = ((((this.mLineHeight / 2) + top) + location[1]) + this.mEditText.getTotalPaddingTop()) - this.mEditText.getScrollY();
            if (start == end) {
                this.mScreenX = ((Math.round(layout.getPrimaryHorizontal(start)) + location[0]) + this.mEditText.getTotalPaddingLeft()) - this.mEditText.getScrollX();
            } else {
                int right;
                int left = Math.round(layout.getPrimaryHorizontal(start));
                if (line == layout.getLineForOffset(end)) {
                    right = Math.round(layout.getPrimaryHorizontal(end));
                } else {
                    right = Math.round(layout.getLineRight(line));
                }
                this.mScreenX = ((((left + right) / 2) + location[0]) + this.mEditText.getTotalPaddingLeft()) - this.mEditText.getScrollX();
            }
            this.mScreenY = Math.max(location[1], this.mScreenY);
        }
    }

    protected TextView initToolbarItem(int id, int textResId) {
        TextView textView = new TextView(this.mContext);
        textView.setGravity(17);
        textView.setTextSize(16.0f);
        textView.setTextColor(this.mContext.getResources().getColor(AmigoWidgetResource.getIdentifierByColor(this.mContext, "amigo_editor_toolbar_text_color")));
        textView.setId(id);
        textView.setPadding(this.mToolbarItemPaddingLeftAndRight, 0, this.mToolbarItemPaddingLeftAndRight, 0);
        textView.setText(textResId);
        textView.setOnClickListener(getOnClickListener());
        return textView;
    }
}
