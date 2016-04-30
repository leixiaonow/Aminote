package amigoui.preference;

import amigoui.preference.AmigoPreference.BaseSavedState;
import amigoui.widget.AmigoEditText;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

public class AmigoEditTextPreference extends AmigoDialogPreference {
    private AmigoEditText mEditText;
    private int mEditTextMarginBootom;
    private int mEditTextMarginLeft;
    private String mText;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String text;

        public SavedState(Parcel source) {
            super(source);
            this.text = source.readString();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(this.text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public AmigoEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mEditText = new AmigoEditText(context, attrs);
//        this.mEditText.setId(16908291);
        this.mEditText.setId(android.R.id.edit);
        this.mEditText.setEnabled(true);
        this.mEditTextMarginBootom = (int) context.getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_edit_text_margin_bottom"));
        this.mEditTextMarginLeft = (int) context.getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_edit_text_margin_left"));
    }

    public AmigoEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoEditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextPreferenceStyle);
    }

    public AmigoEditTextPreference(Context context) {
        this(context, null);
    }

    public void setText(String text) {
        boolean wasBlocking = shouldDisableDependents();
        this.mText = text;
        persistString(text);
        boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public String getText() {
        return this.mText;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        AmigoEditText editText = this.mEditText;
        editText.setText(getText());
        View oldParent = (View) editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    protected void onAddEditTextToDialogView(View dialogView, AmigoEditText editText) {
        ViewGroup container = (ViewGroup) dialogView.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_edittext_container"));
        if (container != null) {
            LayoutParams params = new LayoutParams(-1, -2);
            if (getDialogMessage() == null || getDialogMessage().equals("")) {
                params.setMargins(this.mEditTextMarginLeft, this.mEditTextMarginBootom, this.mEditTextMarginLeft, this.mEditTextMarginBootom);
            } else {
                params.setMargins(this.mEditTextMarginLeft, 0, this.mEditTextMarginLeft, this.mEditTextMarginBootom);
            }
            container.addView(editText, params);
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String value = this.mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            defaultValue = getPersistedString(this.mText);
        } else {
            String defaultValue2 = (String) defaultValue;
        }
        setText(defaultValue);
    }

    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(this.mText) || super.shouldDisableDependents();
    }

    public AmigoEditText getEditText() {
        return this.mEditText;
    }

    protected boolean needInputMethod() {
        return true;
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.text = getText();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.text);
    }
}
