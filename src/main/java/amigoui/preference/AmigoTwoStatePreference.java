package amigoui.preference;

import amigoui.preference.AmigoPreference.BaseSavedState;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

public abstract class AmigoTwoStatePreference extends AmigoPreference {
    boolean mChecked;
    private boolean mCheckedSet;
    private boolean mDisableDependentsState;
    private boolean mSendClickAccessibilityEvent;
    private CharSequence mSummaryOff;
    private CharSequence mSummaryOn;

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean checked;

        public SavedState(Parcel source) {
            boolean z = true;
            super(source);
            if (source.readInt() != 1) {
                z = false;
            }
            this.checked = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.checked ? 1 : 0);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public AmigoTwoStatePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AmigoTwoStatePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmigoTwoStatePreference(Context context) {
        this(context, null);
    }

    protected void onClick() {
        super.onClick();
        boolean newValue = !isChecked();
        this.mSendClickAccessibilityEvent = true;
        if (callChangeListener(Boolean.valueOf(newValue))) {
            setChecked(newValue);
        }
    }

    public void setChecked(boolean checked) {
        boolean changed = this.mChecked != checked;
        if (changed || !this.mCheckedSet) {
            this.mChecked = checked;
            this.mCheckedSet = true;
            persistBoolean(checked);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public boolean shouldDisableDependents() {
        boolean shouldDisable = this.mDisableDependentsState ? this.mChecked : !this.mChecked;
        if (shouldDisable || super.shouldDisableDependents()) {
            return true;
        }
        return false;
    }

    public void setSummaryOn(CharSequence summary) {
        this.mSummaryOn = summary;
        if (isChecked()) {
            notifyChanged();
        }
    }

    public void setSummaryOn(int summaryResId) {
        setSummaryOn(getContext().getString(summaryResId));
    }

    public CharSequence getSummaryOn() {
        return this.mSummaryOn;
    }

    public void setSummaryOff(CharSequence summary) {
        this.mSummaryOff = summary;
        if (!isChecked()) {
            notifyChanged();
        }
    }

    public void setSummaryOff(int summaryResId) {
        setSummaryOff(getContext().getString(summaryResId));
    }

    public CharSequence getSummaryOff() {
        return this.mSummaryOff;
    }

    public boolean getDisableDependentsState() {
        return this.mDisableDependentsState;
    }

    public void setDisableDependentsState(boolean disableDependentsState) {
        this.mDisableDependentsState = disableDependentsState;
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Boolean.valueOf(a.getBoolean(index, false));
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setChecked(restoreValue ? getPersistedBoolean(this.mChecked) : ((Boolean) defaultValue).booleanValue());
    }

    void sendAccessibilityEvent(View view) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        if (this.mSendClickAccessibilityEvent && accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.setEventType(1);
            view.onInitializeAccessibilityEvent(event);
            view.dispatchPopulateAccessibilityEvent(event);
            accessibilityManager.sendAccessibilityEvent(event);
        }
        this.mSendClickAccessibilityEvent = false;
    }

    void syncSummaryView(View view) {
        TextView summaryView = (TextView) view.findViewById(16908304);
        if (summaryView != null) {
            boolean useDefaultSummary = true;
            if (this.mChecked && this.mSummaryOn != null) {
                summaryView.setText(this.mSummaryOn);
                useDefaultSummary = false;
            } else if (!(this.mChecked || this.mSummaryOff == null)) {
                summaryView.setText(this.mSummaryOff);
                useDefaultSummary = false;
            }
            if (useDefaultSummary) {
                CharSequence summary = getSummary();
                if (summary != null && summary.length() > 0) {
                    summaryView.setText(summary);
                    useDefaultSummary = false;
                }
            }
            int newVisibility = 8;
            if (!useDefaultSummary) {
                newVisibility = 0;
            }
            if (newVisibility != summaryView.getVisibility()) {
                summaryView.setVisibility(newVisibility);
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.checked = isChecked();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setChecked(myState.checked);
    }
}
