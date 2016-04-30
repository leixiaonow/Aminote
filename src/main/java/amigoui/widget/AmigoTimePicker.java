package amigoui.widget;

import com.gionee.aminote.R;
import amigoui.widget.AmigoNumberPicker.OnValueChangeListener;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class AmigoTimePicker extends FrameLayout {
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int HOURS_IN_HALF_DAY = 12;
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        public void onTimeChanged(AmigoTimePicker view, int hourOfDay, int minute) {
        }
    };
    private final Button mAmPmButton;
    private final AmigoNumberPicker mAmPmSpinner;
    private final EditText mAmPmSpinnerInput;
    private final String[] mAmPmStrings;
    private Context mContext;
    private Locale mCurrentLocale;
    private final TextView mDivider;
    private int mHorizontalPadding;
    private final AmigoNumberPicker mHourSpinner;
    private final EditText mHourSpinnerInput;
    private boolean mIs24HourView;
    private boolean mIsAm;
    private boolean mIsEnabled;
    private final AmigoNumberPicker mMinuteSpinner;
    private final EditText mMinuteSpinnerInput;
    private OnTimeChangedListener mOnTimeChangedListener;
    private final LinearLayout mSpinners;
    private Calendar mTempCalendar;
    private int mVerticalPadding;

    public interface OnTimeChangedListener {
        void onTimeChanged(AmigoTimePicker amigoTimePicker, int i, int i2);
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            this.mHour = hour;
            this.mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mHour = in.readInt();
            this.mMinute = in.readInt();
        }

        public int getHour() {
            return this.mHour;
        }

        public int getMinute() {
            return this.mMinute;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mHour);
            dest.writeInt(this.mMinute);
        }
    }

    public AmigoTimePicker(Context context) {
        this(context, null);
    }

    public AmigoTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, AmigoWidgetResource.getIdentifierByAttr(context, "amigotimePickerStyle"));
    }

    public AmigoTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDivider = null;
        this.mIsEnabled = DEFAULT_ENABLED_STATE;
        this.mContext = context;
        setCurrentLocale(Locale.getDefault());
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.AmigoTimePicker, defStyle, 0);
        int layoutResourceId = attributesArray.getResourceId(R.styleable.AmigoTimePicker_amigointernalLayout, R.layout.amigo_time_picker);
        attributesArray.recycle();
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResourceId, this, DEFAULT_ENABLED_STATE);
        this.mSpinners = (LinearLayout) findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_time_picker"));
        this.mVerticalPadding = (int) getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_datepicker_vertical"));
        this.mHorizontalPadding = (int) getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(context, "amigo_datepicker_horizontal"));
        if (getResources().getConfiguration().orientation == 2) {
            this.mSpinners.setPadding(this.mHorizontalPadding, this.mVerticalPadding, this.mHorizontalPadding, this.mVerticalPadding);
        } else {
            this.mSpinners.setPadding(this.mVerticalPadding, this.mVerticalPadding, this.mVerticalPadding, this.mVerticalPadding);
        }
        this.mHourSpinner = (AmigoNumberPicker) findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_hour"));
        this.mHourSpinner.setSelectionSrc(getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_numberpicker_selection_left")));
        this.mHourSpinner.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(AmigoNumberPicker spinner, int oldVal, int newVal) {
                AmigoTimePicker.this.updateInputState();
                if (!AmigoTimePicker.this.is24HourView() && ((oldVal == 11 && newVal == 12) || (oldVal == 12 && newVal == 11))) {
                    AmigoTimePicker.this.mIsAm = !AmigoTimePicker.this.mIsAm ? AmigoTimePicker.DEFAULT_ENABLED_STATE : false;
                    AmigoTimePicker.this.updateAmPmControl();
                }
                AmigoTimePicker.this.onTimeChanged();
            }
        });
        this.mHourSpinnerInput = (EditText) this.mHourSpinner.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_numberpicker_input"));
        this.mHourSpinnerInput.setImeOptions(5);
        this.mMinuteSpinner = (AmigoNumberPicker) findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_minute"));
        this.mMinuteSpinner.setMinValue(0);
        this.mMinuteSpinner.setMaxValue(59);
        this.mMinuteSpinner.setOnLongPressUpdateInterval(100);
        this.mMinuteSpinner.setFormatter(AmigoNumberPicker.TWO_DIGIT_FORMATTER);
        this.mMinuteSpinner.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(AmigoNumberPicker spinner, int oldVal, int newVal) {
                boolean z = AmigoTimePicker.DEFAULT_ENABLED_STATE;
                AmigoTimePicker.this.updateInputState();
                int minValue = AmigoTimePicker.this.mMinuteSpinner.getMinValue();
                int maxValue = AmigoTimePicker.this.mMinuteSpinner.getMaxValue();
                int newHour;
                AmigoTimePicker amigoTimePicker;
                if (oldVal == maxValue && newVal == minValue) {
                    newHour = AmigoTimePicker.this.mHourSpinner.getValue() + 1;
                    if (!AmigoTimePicker.this.is24HourView() && newHour == 12) {
                        amigoTimePicker = AmigoTimePicker.this;
                        if (AmigoTimePicker.this.mIsAm) {
                            z = false;
                        }
                        amigoTimePicker.mIsAm = z;
                        AmigoTimePicker.this.updateAmPmControl();
                    }
                    AmigoTimePicker.this.mHourSpinner.setValue(newHour);
                } else if (oldVal == minValue && newVal == maxValue) {
                    newHour = AmigoTimePicker.this.mHourSpinner.getValue() - 1;
                    if (!AmigoTimePicker.this.is24HourView() && newHour == 11) {
                        amigoTimePicker = AmigoTimePicker.this;
                        if (AmigoTimePicker.this.mIsAm) {
                            z = false;
                        }
                        amigoTimePicker.mIsAm = z;
                        AmigoTimePicker.this.updateAmPmControl();
                    }
                    AmigoTimePicker.this.mHourSpinner.setValue(newHour);
                }
                AmigoTimePicker.this.onTimeChanged();
            }
        });
        this.mMinuteSpinnerInput = (EditText) this.mMinuteSpinner.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_numberpicker_input"));
        this.mMinuteSpinnerInput.setImeOptions(5);
        this.mAmPmStrings = new DateFormatSymbols().getAmPmStrings();
        View amPmView = findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_amPm"));
        if (amPmView instanceof Button) {
            this.mAmPmSpinner = null;
            this.mAmPmSpinnerInput = null;
            this.mAmPmButton = (Button) amPmView;
            this.mAmPmButton.setOnClickListener(new OnClickListener() {
                public void onClick(View button) {
                    button.requestFocus();
                    AmigoTimePicker.this.mIsAm = !AmigoTimePicker.this.mIsAm ? AmigoTimePicker.DEFAULT_ENABLED_STATE : false;
                    AmigoTimePicker.this.updateAmPmControl();
                    AmigoTimePicker.this.onTimeChanged();
                }
            });
        } else {
            this.mAmPmButton = null;
            this.mAmPmSpinner = (AmigoNumberPicker) amPmView;
            this.mAmPmSpinner.setMinValue(0);
            this.mAmPmSpinner.setMaxValue(1);
            this.mAmPmSpinner.setDisplayedValues(this.mAmPmStrings);
            this.mAmPmSpinner.setSelectionSrc(getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_numberpicker_selection_right")));
            this.mAmPmSpinner.setOnValueChangedListener(new OnValueChangeListener() {
                public void onValueChange(AmigoNumberPicker picker, int oldVal, int newVal) {
                    AmigoTimePicker.this.updateInputState();
                    picker.requestFocus();
                    AmigoTimePicker.this.mIsAm = !AmigoTimePicker.this.mIsAm ? AmigoTimePicker.DEFAULT_ENABLED_STATE : false;
                    AmigoTimePicker.this.updateAmPmControl();
                    AmigoTimePicker.this.onTimeChanged();
                }
            });
            this.mAmPmSpinnerInput = (EditText) this.mAmPmSpinner.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_numberpicker_input"));
            this.mAmPmSpinnerInput.setImeOptions(6);
        }
        updateHourControl();
        updateAmPmControl();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);
        setCurrentHour(this.mTempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(this.mTempCalendar.get(Calendar.MINUTE));
        if (!isEnabled()) {
            setEnabled(false);
        }
        setContentDescriptions();
    }

    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled != enabled) {
            super.setEnabled(enabled);
            this.mMinuteSpinner.setEnabled(enabled);
            if (this.mDivider != null) {
                this.mDivider.setEnabled(enabled);
            }
            this.mHourSpinner.setEnabled(enabled);
            if (this.mAmPmSpinner != null) {
                this.mAmPmSpinner.setEnabled(enabled);
            } else {
                this.mAmPmButton.setEnabled(enabled);
            }
            this.mIsEnabled = enabled;
        }
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mSpinners != null) {
            if (getResources().getConfiguration().orientation == 2) {
                this.mSpinners.setPadding(this.mHorizontalPadding, this.mVerticalPadding, this.mHorizontalPadding, this.mVerticalPadding);
            } else {
                this.mSpinners.setPadding(this.mVerticalPadding, this.mVerticalPadding, this.mVerticalPadding, this.mVerticalPadding);
            }
        }
        setCurrentLocale(newConfig.locale);
    }

    private void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            this.mCurrentLocale = locale;
            this.mTempCalendar = Calendar.getInstance(locale);
        }
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getCurrentHour().intValue(), getCurrentMinute().intValue());
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(Integer.valueOf(ss.getHour()));
        setCurrentMinute(Integer.valueOf(ss.getMinute()));
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mOnTimeChangedListener = onTimeChangedListener;
    }

    public Integer getCurrentHour() {
        int currentHour = this.mHourSpinner.getValue();
        if (is24HourView()) {
            return Integer.valueOf(currentHour);
        }
        if (this.mIsAm) {
            return Integer.valueOf(currentHour % 12);
        }
        return Integer.valueOf((currentHour % 12) + 12);
    }

    public void setCurrentHour(Integer currentHour) {
        if (currentHour != null && currentHour != getCurrentHour()) {
            if (!is24HourView()) {
                if (currentHour.intValue() >= 12) {
                    this.mIsAm = false;
                    if (currentHour.intValue() > 12) {
                        currentHour = Integer.valueOf(currentHour.intValue() - 12);
                    }
                } else {
                    this.mIsAm = DEFAULT_ENABLED_STATE;
                    if (currentHour.intValue() == 0) {
                        currentHour = Integer.valueOf(12);
                    }
                }
                updateAmPmControl();
            }
            this.mHourSpinner.setValue(currentHour.intValue());
            onTimeChanged();
        }
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (this.mIs24HourView != is24HourView.booleanValue()) {
            this.mIs24HourView = is24HourView.booleanValue();
            int currentHour = getCurrentHour().intValue();
            updateHourControl();
            setCurrentHour(Integer.valueOf(currentHour));
            updateAmPmControl();
        }
    }

    public boolean is24HourView() {
        return this.mIs24HourView;
    }

    public Integer getCurrentMinute() {
        return Integer.valueOf(this.mMinuteSpinner.getValue());
    }

    public void setCurrentMinute(Integer currentMinute) {
        if (currentMinute != getCurrentMinute()) {
            this.mMinuteSpinner.setValue(currentMinute.intValue());
            onTimeChanged();
        }
    }

    public int getBaseline() {
        return this.mHourSpinner.getBaseline();
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return DEFAULT_ENABLED_STATE;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        int flags;
        super.onPopulateAccessibilityEvent(event);
        if (this.mIs24HourView) {
            flags = 1 | 128;
        } else {
            flags = 1 | 64;
        }
        this.mTempCalendar.set(Calendar.HOUR_OF_DAY, getCurrentHour());
        this.mTempCalendar.set(Calendar.MINUTE, getCurrentMinute());
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mTempCalendar.getTimeInMillis(), flags));
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AmigoTimePicker.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AmigoTimePicker.class.getName());
    }

    private void updateHourControl() {
        if (is24HourView()) {
            this.mHourSpinner.setMinValue(0);
            this.mHourSpinner.setMaxValue(23);
            this.mHourSpinner.setFormatter(AmigoNumberPicker.TWO_DIGIT_FORMATTER);
            return;
        }
        this.mHourSpinner.setMinValue(1);
        this.mHourSpinner.setMaxValue(12);
        this.mHourSpinner.setFormatter(null);
    }

    private void updateAmPmControl() {
        if (is24HourView()) {
            if (this.mAmPmSpinner != null) {
                this.mAmPmSpinner.setVisibility(GONE);
            } else {
                this.mAmPmButton.setVisibility(GONE);
            }
            if (this.mMinuteSpinner != null) {
                this.mMinuteSpinner.setSelectionSrc(getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_numberpicker_selection_right")));
            }
        } else {
            int index = this.mIsAm ? 0 : 1;
            if (this.mAmPmSpinner != null) {
                this.mAmPmSpinner.setValue(index);
                this.mAmPmSpinner.setVisibility(VISIBLE);
            } else {
                this.mAmPmButton.setText(this.mAmPmStrings[index]);
                this.mAmPmButton.setVisibility(VISIBLE);
            }
            if (this.mMinuteSpinner != null) {
                this.mMinuteSpinner.setSelectionSrc(getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_numberpicker_selection_center")));
            }
        }
        sendAccessibilityEvent(4);
    }

    private void onTimeChanged() {
        sendAccessibilityEvent(4);
        if (this.mOnTimeChangedListener != null) {
            this.mOnTimeChangedListener.onTimeChanged(this, getCurrentHour().intValue(), getCurrentMinute().intValue());
        }
    }

    private void setContentDescriptions() {
    }

    private void trySetContentDescription(View root, int viewId, int contDescResId) {
        View target = root.findViewById(viewId);
        if (target != null) {
            target.setContentDescription(this.mContext.getString(contDescResId));
        }
    }

    private void updateInputState() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }
        if (inputMethodManager.isActive(this.mHourSpinnerInput)) {
            this.mHourSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mMinuteSpinnerInput)) {
            this.mMinuteSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mAmPmSpinnerInput)) {
            this.mAmPmSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }
}
