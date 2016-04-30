package amigoui.preference;

import amigoui.preference.AmigoPreference.BaseSavedState;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class AmigoSeekBarPreference extends AmigoPreference implements OnSeekBarChangeListener {
    private int mMax;
    private int mProgress;
    private boolean mTrackingTouch;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int max;
        int progress;

        public SavedState(Parcel source) {
            super(source);
            this.progress = source.readInt();
            this.max = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.progress);
            dest.writeInt(this.max);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public AmigoSeekBarPreference(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        setLayoutResource(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_preference_widget_seekbar"));
    }

    public AmigoSeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmigoSeekBarPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        SeekBar seekBar = (SeekBar) view.findViewById(AmigoWidgetResource.getIdentifierById(view.getContext(), "amigo_seekbar"));
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(this.mMax);
        seekBar.setProgress(this.mProgress);
        seekBar.setEnabled(isEnabled());
    }

    public CharSequence getSummary() {
        return null;
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setProgress(restoreValue ? getPersistedInt(this.mProgress) : ((Integer) defaultValue).intValue());
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 1) {
            if (keyCode == 81 || keyCode == 70) {
                setProgress(getProgress() + 1);
                return true;
            } else if (keyCode == 69) {
                setProgress(getProgress() - 1);
                return true;
            }
        }
        return false;
    }

    public void setMax(int max) {
        if (max != this.mMax) {
            this.mMax = max;
            notifyChanged();
        }
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    private void setProgress(int progress, boolean notifyChanged) {
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress < 0) {
            progress = 0;
        }
        if (progress != this.mProgress) {
            this.mProgress = progress;
            persistInt(progress);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getProgress() {
        return this.mProgress;
    }

    void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress == this.mProgress) {
            return;
        }
        if (callChangeListener(Integer.valueOf(progress))) {
            setProgress(progress, false);
        } else {
            seekBar.setProgress(this.mProgress);
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && !this.mTrackingTouch) {
            syncProgress(seekBar);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = false;
        if (seekBar.getProgress() != this.mProgress) {
            syncProgress(seekBar);
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.progress = this.mProgress;
        myState.max = this.mMax;
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state.getClass().equals(SavedState.class)) {
            SavedState myState = (SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            this.mProgress = myState.progress;
            this.mMax = myState.max;
            notifyChanged();
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
