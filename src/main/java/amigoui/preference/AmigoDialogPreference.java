package amigoui.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.gionee.aminote.R;

import amigoui.app.AmigoAlertDialog.Builder;
import amigoui.preference.AmigoPreferenceManager.OnActivityDestroyListener;
import amigoui.widget.AmigoWidgetResource;

public abstract class AmigoDialogPreference extends AmigoPreference implements OnClickListener, OnDismissListener, OnActivityDestroyListener {
    private Builder mBuilder;
    protected Context mContext;
    private Dialog mDialog;
    private Drawable mDialogIcon;
    private int mDialogLayoutResId;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private boolean mFullScreenStyle;
    private CharSequence mNegativeButtonText;
    private CharSequence mPositiveButtonText;
    private int mWhichButtonClicked;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle dialogBundle;
        boolean isDialogShowing;

        public SavedState(Parcel source) {
            super(source);
            boolean z = true;

            if (source.readInt() != 1) {
                z = false;
            }
            this.isDialogShowing = z;
            this.dialogBundle = source.readBundle();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isDialogShowing ? 1 : 0);
            dest.writeBundle(this.dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public AmigoDialogPreference(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        this.mDialogLayoutResId = AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_preference_dialog_edittext");
        this.mFullScreenStyle = false;
        this.mContext = context;
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoDialogPreference, defStyle, 0);
            this.mDialogTitle = a.getString(R.styleable.AmigoDialogPreference_amigodialogTitle);
            if (this.mDialogTitle == null) {
                this.mDialogTitle = getTitle();
            }
            this.mDialogMessage = a.getString(R.styleable.AmigoDialogPreference_amigodialogMessage);
            this.mDialogIcon = a.getDrawable(R.styleable.AmigoDialogPreference_amigodialogIcon);
            this.mPositiveButtonText = a.getString(R.styleable.AmigoDialogPreference_amigopositiveButtonText);
            this.mNegativeButtonText = a.getString(R.styleable.AmigoDialogPreference_amigonegativeButtonText);
            this.mDialogLayoutResId = a.getResourceId(R.styleable.AmigoDialogPreference_amigodialogLayout, this.mDialogLayoutResId);
            a.recycle();
            return;
        }
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16843250:
                    this.mDialogTitle = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    if (this.mDialogTitle != null) {
                        break;
                    }
                    this.mDialogTitle = getTitle();
                    break;
                case 16843251:
                    this.mDialogMessage = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    break;
                case 16843253:
                    this.mPositiveButtonText = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    break;
                case 16843254:
                    this.mNegativeButtonText = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    break;
                case 16843255:
                    this.mDialogLayoutResId = attrs.getAttributeIntValue(i, this.mDialogLayoutResId);
                    break;
                default:
                    break;
            }
        }
    }

    public AmigoDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoDialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842897);
    }

    public void setDialogTitle(CharSequence dialogTitle) {
        this.mDialogTitle = dialogTitle;
    }

    public void setDialogTitle(int dialogTitleResId) {
        setDialogTitle(getContext().getString(dialogTitleResId));
    }

    public CharSequence getDialogTitle() {
        return this.mDialogTitle;
    }

    public void setDialogMessage(CharSequence dialogMessage) {
        this.mDialogMessage = dialogMessage;
    }

    public void setDialogMessage(int dialogMessageResId) {
        setDialogMessage(getContext().getString(dialogMessageResId));
    }

    public CharSequence getDialogMessage() {
        return this.mDialogMessage;
    }

    public void setDialogIcon(Drawable dialogIcon) {
        this.mDialogIcon = dialogIcon;
    }

    public void setDialogIcon(int dialogIconRes) {
        this.mDialogIcon = getContext().getResources().getDrawable(dialogIconRes);
    }

    public Drawable getDialogIcon() {
        return this.mDialogIcon;
    }

    public void setPositiveButtonText(CharSequence positiveButtonText) {
        this.mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextResId) {
        setPositiveButtonText(getContext().getString(positiveButtonTextResId));
    }

    public CharSequence getPositiveButtonText() {
        return this.mPositiveButtonText;
    }

    public void setNegativeButtonText(CharSequence negativeButtonText) {
        this.mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextResId) {
        setNegativeButtonText(getContext().getString(negativeButtonTextResId));
    }

    public CharSequence getNegativeButtonText() {
        return this.mNegativeButtonText;
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        this.mDialogLayoutResId = dialogLayoutResId;
    }

    public int getDialogLayoutResource() {
        return this.mDialogLayoutResId;
    }

    protected void onPrepareDialogBuilder(Builder builder) {
    }

    protected void onClick() {
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            showDialog(null);
        }
    }

    protected void showDialog(Bundle state) {
        Context context = getContext();
        this.mWhichButtonClicked = -2;
        if (this.mFullScreenStyle) {
            this.mBuilder = new Builder(context).setTitle(this.mDialogTitle).setIcon(this.mDialogIcon).setPositiveButton(this.mPositiveButtonText, (OnClickListener) this).setNegativeButton(this.mNegativeButtonText, (OnClickListener) this);
        } else {
            this.mBuilder = new Builder(context).setTitle(this.mDialogTitle).setIcon(this.mDialogIcon).setPositiveButton(this.mPositiveButtonText, (OnClickListener) this).setNegativeButton(this.mNegativeButtonText, (OnClickListener) this);
        }
        View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            this.mBuilder.setView(contentView);
        } else {
            this.mBuilder.setMessage(this.mDialogMessage);
        }
        onPrepareDialogBuilder(this.mBuilder);
        getPreferenceManager().registerOnActivityDestroyListener(this);
        Dialog dialog = this.mBuilder.create();
        this.mDialog = dialog;
        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        if (needInputMethod()) {
            requestInputMethod(dialog);
        }
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    protected boolean needInputMethod() {
        return false;
    }

    private void requestInputMethod(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(5);
    }

    protected View onCreateDialogView() {
        if (this.mDialogLayoutResId == 0) {
            return null;
        }
        return LayoutInflater.from(this.mBuilder.getContext()).inflate(this.mDialogLayoutResId, null);
    }

    protected void onBindDialogView(View view) {
        View dialogMessageView = view.findViewById(16908299);
        if (dialogMessageView != null) {
            CharSequence message = getDialogMessage();
            int newVisibility = 8;
            if (!TextUtils.isEmpty(message)) {
                if (dialogMessageView instanceof TextView) {
                    ((TextView) dialogMessageView).setText(message);
                }
                newVisibility = 0;
            }
            if (dialogMessageView.getVisibility() != newVisibility) {
                dialogMessageView.setVisibility(newVisibility);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        this.mWhichButtonClicked = which;
    }

    public void onDismiss(DialogInterface dialog) {
        getPreferenceManager().unregisterOnActivityDestroyListener(this);
        this.mDialog = null;
        onDialogClosed(this.mWhichButtonClicked == -1);
    }

    protected void onDialogClosed(boolean positiveResult) {
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    public void onActivityDestroy() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            return superState;
        }
//        Parcelable myState = new SavedState(superState);
        SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = this.mDialog.onSaveInstanceState();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }

    protected void setFullScreenStyle(boolean fullScreenStyle) {
        this.mFullScreenStyle = fullScreenStyle;
    }
}
