package amigoui.preference;

import amigoui.app.AmigoAlertDialog.Builder;
import com.gionee.aminote.R;
import amigoui.preference.AmigoPreference.BaseSavedState;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import java.util.HashSet;
import java.util.Set;

public class AmigoMultiSelectListPreference extends AmigoDialogPreference {
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private Set<String> mNewValues;
    private boolean mPreferenceChanged;
    private Set<String> mValues;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Set<String> values;

        public SavedState(Parcel source) {
            super(source);
            this.values = new HashSet();
            for (Object add : new String[0]) {
                this.values.add(add);
            }
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeStringArray((String[]) this.values.toArray(new String[0]));
        }
    }

    static /* synthetic */ boolean access$076(AmigoMultiSelectListPreference x0, int x1) {
        boolean z = (byte) (x0.mPreferenceChanged | x1);
        x0.mPreferenceChanged = z;
        return z;
    }

    public AmigoMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mValues = new HashSet();
        this.mNewValues = new HashSet();
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoMultiSelectListPreference, 0, 0);
            this.mEntries = a.getTextArray(R.styleable.AmigoMultiSelectListPreference_amigoentries);
            this.mEntryValues = a.getTextArray(R.styleable.AmigoMultiSelectListPreference_amigoentryValues);
            a.recycle();
            return;
        }
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16842930:
                    this.mEntries = NativePreferenceManager.getAttributeStringArrayValue(context, attrs, i);
                    break;
                case 16843256:
                    this.mEntryValues = NativePreferenceManager.getAttributeStringArrayValue(context, attrs, i);
                    break;
                default:
                    break;
            }
        }
    }

    public AmigoMultiSelectListPreference(Context context) {
        this(context, null);
    }

    public void setEntries(CharSequence[] entries) {
        this.mEntries = entries;
    }

    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public CharSequence[] getEntries() {
        return this.mEntries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        this.mEntryValues = entryValues;
    }

    public void setEntryValues(int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    public CharSequence[] getEntryValues() {
        return this.mEntryValues;
    }

    public void setValues(Set<String> values) {
        this.mValues.clear();
        this.mValues.addAll(values);
        persistStringSet(values);
    }

    public Set<String> getValues() {
        return this.mValues;
    }

    public int findIndexOfValue(String value) {
        if (!(value == null || this.mEntryValues == null)) {
            for (int i = this.mEntryValues.length - 1; i >= 0; i--) {
                if (this.mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        if (this.mEntries == null || this.mEntryValues == null) {
            throw new IllegalStateException("MultiSelectListPreference requires an entries array and an entryValues array.");
        }
        builder.setMultiChoiceItems(this.mEntries, getSelectedItems(), new OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    AmigoMultiSelectListPreference.access$076(AmigoMultiSelectListPreference.this, AmigoMultiSelectListPreference.this.mNewValues.add(AmigoMultiSelectListPreference.this.mEntryValues[which].toString()));
                } else {
                    AmigoMultiSelectListPreference.access$076(AmigoMultiSelectListPreference.this, AmigoMultiSelectListPreference.this.mNewValues.remove(AmigoMultiSelectListPreference.this.mEntryValues[which].toString()));
                }
            }
        });
        this.mNewValues.clear();
        this.mNewValues.addAll(this.mValues);
    }

    private boolean[] getSelectedItems() {
        CharSequence[] entries = this.mEntryValues;
        int entryCount = entries.length;
        Set<String> values = this.mValues;
        boolean[] result = new boolean[entryCount];
        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(entries[i].toString());
        }
        return result;
    }

    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && this.mPreferenceChanged) {
            Set<String> values = this.mNewValues;
            if (callChangeListener(values)) {
                setValues(values);
            }
        }
        this.mPreferenceChanged = false;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        Set<String> result = new HashSet();
        for (CharSequence charSequence : a.getTextArray(index)) {
            result.add(charSequence.toString());
        }
        return result;
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            defaultValue = getPersistedStringSet(this.mValues);
        } else {
            Set defaultValue2 = (Set) defaultValue;
        }
        setValues(defaultValue);
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.values = getValues();
        return myState;
    }
}
