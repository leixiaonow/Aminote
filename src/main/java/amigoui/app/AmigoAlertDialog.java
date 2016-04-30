package amigoui.app;

import amigoui.widget.AmigoButton;
import amigoui.widget.AmigoWidgetResource;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.amigoui.internal.app.AmigoAlertController;
import com.amigoui.internal.app.AmigoAlertController.AlertParams;

public class AmigoAlertDialog extends Dialog implements DialogInterface {
    public static final int THEME_AMIGO_DARK = 8;
    public static final int THEME_AMIGO_FULLSCREEN = 6;
    public static final int THEME_AMIGO_FULLSCREEN_NEW = 9;
    public static final int THEME_AMIGO_LIGHT = 7;
    public static final int THEME_AMIGO_STRONG_HINT = 10;
    public static final int THEME_DEVICE_DEFAULT_DARK = 4;
    public static final int THEME_DEVICE_DEFAULT_LIGHT = 5;
    public static final int THEME_HOLO_DARK = 2;
    public static final int THEME_HOLO_LIGHT = 3;
    public static final int THEME_TRADITIONAL = 1;
    private AmigoAlertController mAlert;
    private Context mContext;
    private OnWindowFocusChangeListener mOnWindowFocusChangeListener;

    public static class Builder {
        private final AlertParams P;
        private int mTheme;

        public Builder(Context context) {
            this(context, AmigoAlertDialog.resolveDialogTheme(context, 0));
        }

        public Builder(Context context, int theme) {
            this.P = new AlertParams(new ContextThemeWrapper(context, AmigoAlertDialog.resolveDialogTheme(context, theme)));
            this.mTheme = theme;
            if (theme == 6) {
                setCancelIcon(Boolean.valueOf(false));
            }
            if (theme == 10) {
                setCancelIcon(Boolean.valueOf(false));
                setIcon(AmigoWidgetResource.getIdentifierByDrawable(context, "amigo_strong_hint_dialog_info"));
                setTitle(AmigoWidgetResource.getIdentifierByString(context, "amigo_strong_warning"));
            }
        }

        public Context getContext() {
            return this.P.mContext;
        }

        public Builder setTitle(int titleId) {
            this.P.mTitle = this.P.mContext.getText(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.P.mTitle = title;
            return this;
        }

        public Builder setCustomTitle(View customTitleView) {
            this.P.mCustomTitleView = customTitleView;
            return this;
        }

        public Builder setMessage(int messageId) {
            this.P.mMessage = this.P.mContext.getText(messageId);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            this.P.mMessage = message;
            return this;
        }

        public Builder setIcon(int iconId) {
            this.P.mIconId = iconId;
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.P.mIcon = icon;
            return this;
        }

        public Builder setIconAttribute(int attrId) {
            TypedValue out = new TypedValue();
            this.P.mContext.getTheme().resolveAttribute(attrId, out, true);
            this.P.mIconId = out.resourceId;
            return this;
        }

        public Builder setPositiveButton(int textId, OnClickListener listener) {
            this.P.mPositiveButtonText = this.P.mContext.getText(textId);
            this.P.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener listener) {
            this.P.mPositiveButtonText = text;
            this.P.mPositiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(int textId, OnClickListener listener) {
            this.P.mNegativeButtonText = this.P.mContext.getText(textId);
            this.P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener listener) {
            this.P.mNegativeButtonText = text;
            this.P.mNegativeButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(int textId, OnClickListener listener) {
            this.P.mNeutralButtonText = this.P.mContext.getText(textId);
            this.P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(CharSequence text, OnClickListener listener) {
            this.P.mNeutralButtonText = text;
            this.P.mNeutralButtonListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.P.mCancelable = cancelable;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            this.P.mOnCancelListener = onCancelListener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            this.P.mOnDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            this.P.mOnKeyListener = onKeyListener;
            return this;
        }

        public Builder setItems(int itemsId, OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setItems(CharSequence[] items, OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setAdapter(ListAdapter adapter, OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setCursor(Cursor cursor, OnClickListener listener, String labelColumn) {
            this.P.mCursor = cursor;
            this.P.mLabelColumn = labelColumn;
            this.P.mOnClickListener = listener;
            return this;
        }

        public Builder setMultiChoiceItems(int itemsId, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            this.P.mItems = items;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mCheckedItems = checkedItems;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, OnMultiChoiceClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnCheckboxClickListener = listener;
            this.P.mIsCheckedColumn = isCheckedColumn;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsMultiChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(int itemsId, int checkedItem, OnClickListener listener) {
            this.P.mItems = this.P.mContext.getResources().getTextArray(itemsId);
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, OnClickListener listener) {
            this.P.mCursor = cursor;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mLabelColumn = labelColumn;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, OnClickListener listener) {
            this.P.mItems = items;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setSingleChoiceItems(ListAdapter adapter, int checkedItem, OnClickListener listener) {
            this.P.mAdapter = adapter;
            this.P.mOnClickListener = listener;
            this.P.mCheckedItem = checkedItem;
            this.P.mIsSingleChoice = true;
            return this;
        }

        public Builder setOnItemSelectedListener(OnItemSelectedListener listener) {
            this.P.mOnItemSelectedListener = listener;
            return this;
        }

        public Builder setView(View view) {
            this.P.mView = view;
            this.P.mViewSpacingSpecified = false;
            return this;
        }

        public Builder setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
            this.P.mView = view;
            this.P.mViewSpacingSpecified = true;
            this.P.mViewSpacingLeft = viewSpacingLeft;
            this.P.mViewSpacingTop = viewSpacingTop;
            this.P.mViewSpacingRight = viewSpacingRight;
            this.P.mViewSpacingBottom = viewSpacingBottom;
            return this;
        }

        public Builder setInverseBackgroundForced(boolean useInverseBackground) {
            this.P.mForceInverseBackground = useInverseBackground;
            return this;
        }

        public Builder setRecycleOnMeasureEnabled(boolean enabled) {
            this.P.mRecycleOnMeasure = enabled;
            return this;
        }

        public Builder setCancelIcon(Boolean hasCancelIcon) {
            this.P.mHasCancelIcon = hasCancelIcon.booleanValue();
            return this;
        }

        public Builder setCancelIcon(Boolean hasCancelIcon, Drawable cancelIcon) {
            this.P.mHasCancelIcon = hasCancelIcon.booleanValue();
            this.P.mCancelIcon = cancelIcon;
            return this;
        }

        public AmigoAlertDialog create() {
            AmigoAlertDialog dialog = new AmigoAlertDialog(this.P.mContext, this.mTheme, false);
            this.P.apply(dialog.mAlert);
            dialog.setTitle(this.P.mTitle);
            dialog.setCancelable(this.P.mCancelable);
            if (this.P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(this.P.mOnCancelListener);
            dialog.setOnDismissListener(this.P.mOnDismissListener);
            if (this.P.mOnKeyListener != null) {
                dialog.setOnKeyListener(this.P.mOnKeyListener);
            }
            return dialog;
        }

        public AmigoAlertDialog show() {
            AmigoAlertDialog dialog = create();
            dialog.show();
            return dialog;
        }

        public Builder setPositiveButton(int buttonStyle, int textId, OnClickListener listener) {
            this.P.mPositiveButtonStyle = buttonStyle;
            return setPositiveButton(textId, listener);
        }

        public Builder setPositiveButton(int buttonStyle, CharSequence text, OnClickListener listener) {
            this.P.mPositiveButtonStyle = buttonStyle;
            return setPositiveButton(text, listener);
        }

        public Builder setNeutralButton(int buttonStyle, int textId, OnClickListener listener) {
            this.P.mNeutralButtonStyle = buttonStyle;
            return setNeutralButton(textId, listener);
        }

        public Builder setNeutralButton(int buttonStyle, CharSequence text, OnClickListener listener) {
            this.P.mNeutralButtonStyle = buttonStyle;
            return setNeutralButton(text, listener);
        }
    }

    public interface OnWindowFocusChangeListener {
        void onWindowFocusChanged(boolean z);
    }

    protected AmigoAlertDialog(Context context) {
        this(context, resolveDialogTheme(context, 0), true);
    }

    protected AmigoAlertDialog(Context context, int theme) {
        this(context, theme, true);
    }

    AmigoAlertDialog(Context context, int theme, boolean createThemeContextWrapper) {
        super(context, resolveDialogTheme(context, theme));
        this.mAlert = new AmigoAlertController(getContext(), this, getWindow());
        this.mContext = getContext();
        if (theme == 9) {
            this.mAlert.setHasCancelIcon(false);
            getWindow().setGravity(17);
        } else {
            getWindow().setGravity(80);
        }
        this.mAlert.setGnWidget3Style(true);
    }

    protected AmigoAlertDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, resolveDialogTheme(context, 0));
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
        this.mAlert = new AmigoAlertController(context, this, getWindow());
    }

    static int resolveDialogTheme(Context context, int resid) {
        if (resid == 1 || resid == 2 || resid == 3 || resid == 4 || resid == 5 || resid == 6) {
            return AmigoWidgetResource.getIdentifierByStyle(context, "Theme.Amigo.Light.Dialog.Alert");
        }
        if (resid == 9) {
            return AmigoWidgetResource.getIdentifierByStyle(context, "Theme.Amigo.Dialog.Alert.FullScreen");
        }
        if (resid == 7 || resid == 8) {
            return AmigoWidgetResource.getIdentifierByStyle(context, "Theme.Amigo.Light.Dialog.Alert");
        }
        if (resid >= ViewCompat.MEASURED_STATE_TOO_SMALL) {
            return resid;
        }
        if (resid == 10) {
            return AmigoWidgetResource.getIdentifierByStyle(context, "Theme.Amigo.Light.Dialog.StrongHint");
        }
        TypedValue outValue = new TypedValue();
        if (context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(context, "amigoDialogOtherBtnTxtColor"), outValue, true)) {
            context.getTheme().resolveAttribute(16843529, outValue, true);
        } else {
            outValue.resourceId = AmigoWidgetResource.getIdentifierByStyle(context, "Theme.Amigo.Light.Dialog.Alert");
        }
        return outValue.resourceId;
    }

    public AmigoButton getButton(int whichButton) {
        return this.mAlert.getButton(whichButton);
    }

    public ListView getListView() {
        return this.mAlert.getListView();
    }

    public void setTitle(CharSequence title) {
        super.setTitle(title);
        this.mAlert.setTitle(title);
    }

    public void setCustomTitle(View customTitleView) {
        this.mAlert.setCustomTitle(customTitleView);
    }

    public void setMessage(CharSequence message) {
        this.mAlert.setMessage(message);
    }

    public void setView(View view) {
        this.mAlert.setView(view);
    }

    public void setView(View view, int viewSpacingLeft, int viewSpacingTop, int viewSpacingRight, int viewSpacingBottom) {
        this.mAlert.setView(view, viewSpacingLeft, viewSpacingTop, viewSpacingRight, viewSpacingBottom);
    }

    public void setButton(int whichButton, CharSequence text, Message msg) {
        this.mAlert.setButton(whichButton, text, null, msg);
    }

    public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
        this.mAlert.setButton(whichButton, text, listener, null);
    }

    @Deprecated
    public void setButton(CharSequence text, Message msg) {
        setButton(-1, text, msg);
    }

    @Deprecated
    public void setButton2(CharSequence text, Message msg) {
        setButton(-2, text, msg);
    }

    @Deprecated
    public void setButton3(CharSequence text, Message msg) {
        setButton(-3, text, msg);
    }

    @Deprecated
    public void setButton(CharSequence text, OnClickListener listener) {
        setButton(-1, text, listener);
    }

    @Deprecated
    public void setButton2(CharSequence text, OnClickListener listener) {
        setButton(-2, text, listener);
    }

    @Deprecated
    public void setButton3(CharSequence text, OnClickListener listener) {
        setButton(-3, text, listener);
    }

    public void setIcon(int resId) {
        this.mAlert.setIcon(resId);
    }

    public void setIcon(Drawable icon) {
        this.mAlert.setIcon(icon);
    }

    public void setIconAttribute(int attrId) {
        TypedValue out = new TypedValue();
        this.mContext.getTheme().resolveAttribute(attrId, out, true);
        this.mAlert.setIcon(out.resourceId);
    }

    public void setInverseBackgroundForced(boolean forceInverseBackground) {
        this.mAlert.setInverseBackgroundForced(forceInverseBackground);
    }

    protected void onCreate(Bundle savedInstanceState) {
        boolean z = true;
        super.onCreate(savedInstanceState);
        this.mAlert.setGnWidget3Style(true);
        AmigoAlertController amigoAlertController = this.mAlert;
        if (this.mContext.getThemeResId() != AmigoWidgetResource.getIdentifierByStyle(this.mContext, "Theme.Amigo.Light.Dialog.StrongHint")) {
            z = false;
        }
        amigoAlertController.setStrongHint(z);
        this.mAlert.installContent();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mAlert.onKeyUp(keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (this.mOnWindowFocusChangeListener != null) {
            this.mOnWindowFocusChangeListener.onWindowFocusChanged(hasFocus);
        }
    }

    public void setOnWindowFocusChangeListener(OnWindowFocusChangeListener l) {
        this.mOnWindowFocusChangeListener = l;
    }

    public OnWindowFocusChangeListener getOnWindowFocusChangeListener() {
        return this.mOnWindowFocusChangeListener;
    }

    public void setButton(int whichButton, int buttonStyle, CharSequence text, OnClickListener listener) {
        this.mAlert.setButtonStyle(whichButton, buttonStyle);
        setButton(whichButton, text, listener);
    }

    public void setCancelable(boolean flag) {
        if (!flag) {
            this.mAlert.setHasCancelIcon(false);
        }
        super.setCancelable(flag);
    }
}
