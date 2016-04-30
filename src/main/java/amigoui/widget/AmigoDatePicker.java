package amigoui.widget;

import com.gionee.aminote.R;
import amigoui.widget.AmigoNumberPicker.OnValueChangeListener;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Paint.Align;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class AmigoDatePicker extends FrameLayout {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = true;
    private static final boolean DEFAULT_ENABLED_STATE = true;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final boolean DEFAULT_SPINNERS_SHOWN = true;
    private static final int DEFAULT_START_YEAR = 1900;
    private static final String LOG_TAG = AmigoDatePicker.class.getSimpleName();
    private final CalendarView mCalendarView;
    private Context mContext;
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private final DateFormat mDateFormat;
    private final AmigoNumberPicker mDaySpinner;
    private final EditText mDaySpinnerInput;
    private int mHorizontalPadding;
    private boolean mIsEnabled;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private final AmigoNumberPicker mMonthSpinner;
    private final EditText mMonthSpinnerInput;
    private int mNumberOfMonths;
    private OnDateChangedListener mOnDateChangedListener;
    private String[] mShortMonths;
    private final LinearLayout mSpinners;
    private Calendar mTempDate;
    private int mVerticalPadding;
    private final AmigoNumberPicker mYearSpinner;
    private final EditText mYearSpinnerInput;

    public interface OnDateChangedListener {
        void onDateChanged(AmigoDatePicker amigoDatePicker, int i, int i2, int i3);
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
        private final int mDay;
        private final int mMonth;
        private final int mYear;

        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            this.mYear = year;
            this.mMonth = month;
            this.mDay = day;
        }

        private SavedState(Parcel in) {
            super(in);
            this.mYear = in.readInt();
            this.mMonth = in.readInt();
            this.mDay = in.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mYear);
            dest.writeInt(this.mMonth);
            dest.writeInt(this.mDay);
        }
    }

    public AmigoDatePicker(Context context) {
        this(context, null);
    }

    public AmigoDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, AmigoWidgetResource.getIdentifierByAttr(context, "amigodatePickerStyle"));
    }

    public AmigoDatePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDateFormat = new SimpleDateFormat(DATE_FORMAT);
        this.mIsEnabled = true;
        this.mContext = context;
        setCurrentLocale(Locale.getDefault());
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.AmigoDatePicker, defStyle, 0);
        boolean spinnersShown = attributesArray.getBoolean(R.styleable.AmigoDatePicker_amigospinnersShown, true);
        boolean calendarViewShown = attributesArray.getBoolean(R.styleable.AmigoDatePicker_amigocalendarViewShown, true);
        int startYear = attributesArray.getInt(R.styleable.AmigoDatePicker_amigostartYear, DEFAULT_START_YEAR);
        int endYear = attributesArray.getInt(R.styleable.AmigoDatePicker_amigoendYear, DEFAULT_END_YEAR);
        String minDate = attributesArray.getString(R.styleable.AmigoDatePicker_amigominDate);
        String maxDate = attributesArray.getString(R.styleable.AmigoDatePicker_amigomaxDate);
        int layoutResourceId = attributesArray.getResourceId(R.styleable.AmigoDatePicker_amigointernalLayout, AmigoWidgetResource.getIdentifierByLayout(context, "amigo_date_picker"));
        attributesArray.recycle();
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResourceId, this, true);
        OnValueChangeListener onChangeListener = new OnValueChangeListener() {
            public void onValueChange(AmigoNumberPicker picker, int oldVal, int newVal) {
                AmigoDatePicker.this.updateInputState();
                AmigoDatePicker.this.mTempDate.setTimeInMillis(AmigoDatePicker.this.mCurrentDate.getTimeInMillis());
                if (picker == AmigoDatePicker.this.mDaySpinner) {
                    int maxDayOfMonth = AmigoDatePicker.this.mTempDate.getActualMaximum(5);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        AmigoDatePicker.this.mTempDate.add(5, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        AmigoDatePicker.this.mTempDate.add(5, -1);
                    } else {
                        AmigoDatePicker.this.mTempDate.add(5, newVal - oldVal);
                    }
                } else if (picker == AmigoDatePicker.this.mMonthSpinner) {
                    if (oldVal == 11 && newVal == 0) {
                        AmigoDatePicker.this.mTempDate.add(2, 1);
                    } else if (oldVal == 0 && newVal == 11) {
                        AmigoDatePicker.this.mTempDate.add(2, -1);
                    } else {
                        AmigoDatePicker.this.mTempDate.add(2, newVal - oldVal);
                    }
                } else if (picker == AmigoDatePicker.this.mYearSpinner) {
                    AmigoDatePicker.this.mTempDate.set(1, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                AmigoDatePicker.this.setDate(AmigoDatePicker.this.mTempDate.get(1), AmigoDatePicker.this.mTempDate.get(2), AmigoDatePicker.this.mTempDate.get(5));
                AmigoDatePicker.this.updateSpinners();
                AmigoDatePicker.this.updateCalendarView();
                AmigoDatePicker.this.notifyDateChanged();
            }
        };
        this.mSpinners = (LinearLayout) findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_pickers"));
        this.mCalendarView = (CalendarView) findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_calendar_view"));
        this.mCalendarView.setOnDateChangeListener(new OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int year, int month, int monthDay) {
                AmigoDatePicker.this.setDate(year, month, monthDay);
                AmigoDatePicker.this.updateSpinners();
                AmigoDatePicker.this.notifyDateChanged();
            }
        });
        this.mDaySpinner = (AmigoNumberPicker) findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_day"));
        this.mDaySpinner.setFormatter(AmigoNumberPicker.TWO_DIGIT_FORMATTER);
        this.mDaySpinner.setOnLongPressUpdateInterval(100);
        this.mDaySpinner.setOnValueChangedListener(onChangeListener);
        this.mDaySpinner.setSelectionSrc(getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_numberpicker_selection_right")));
        this.mDaySpinnerInput = (EditText) this.mDaySpinner.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_numberpicker_input"));
        this.mMonthSpinner = (AmigoNumberPicker) findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_month"));
        this.mMonthSpinner.setMinValue(0);
        this.mMonthSpinner.setMaxValue(this.mNumberOfMonths - 1);
        this.mMonthSpinner.setDisplayedValues(this.mShortMonths);
        this.mMonthSpinner.setOnLongPressUpdateInterval(200);
        this.mMonthSpinner.setOnValueChangedListener(onChangeListener);
        this.mMonthSpinner.setSelectionSrc(getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_numberpicker_selection_center")));
        this.mMonthSpinnerInput = (EditText) this.mMonthSpinner.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_numberpicker_input"));
        this.mYearSpinner = (AmigoNumberPicker) findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_year"));
        this.mYearSpinner.setOnLongPressUpdateInterval(100);
        this.mYearSpinner.setOnValueChangedListener(onChangeListener);
        this.mYearSpinner.setSelectionSrc(getResources().getDrawable(AmigoWidgetResource.getIdentifierByDrawable(this.mContext, "amigo_numberpicker_selection_left")));
        this.mYearSpinnerInput = (EditText) this.mYearSpinner.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_numberpicker_input"));
        if (spinnersShown || calendarViewShown) {
            setSpinnersShown(spinnersShown);
            setCalendarViewShown(calendarViewShown);
        } else {
            setSpinnersShown(true);
        }
        this.mTempDate.clear();
        if (TextUtils.isEmpty(minDate)) {
            this.mTempDate.set(startYear, 0, 1);
        } else {
            if (!parseDate(minDate, this.mTempDate)) {
                this.mTempDate.set(startYear, 0, 1);
            }
        }
        setMinDate(this.mTempDate.getTimeInMillis());
        this.mTempDate.clear();
        if (TextUtils.isEmpty(maxDate)) {
            this.mTempDate.set(endYear, 11, 31);
        } else {
            if (!parseDate(maxDate, this.mTempDate)) {
                this.mTempDate.set(endYear, 11, 31);
            }
        }
        setMaxDate(this.mTempDate.getTimeInMillis());
        this.mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(this.mCurrentDate.get(1), this.mCurrentDate.get(2), this.mCurrentDate.get(5), null);
        reorderSpinners();
        setContentDescriptions();
        this.mVerticalPadding = (int) getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_datepicker_vertical"));
        this.mHorizontalPadding = (int) getResources().getDimension(AmigoWidgetResource.getIdentifierByDimen(this.mContext, "amigo_datepicker_horizontal"));
        if (getResources().getConfiguration().orientation == 2) {
            this.mSpinners.setPadding(this.mHorizontalPadding, this.mVerticalPadding, this.mHorizontalPadding, this.mVerticalPadding);
        } else {
            this.mSpinners.setPadding(this.mVerticalPadding, this.mVerticalPadding, this.mVerticalPadding, this.mVerticalPadding);
        }
    }

    public long getMinDate() {
        return this.mCalendarView.getMinDate();
    }

    public void setMinDate(long minDate) {
        this.mTempDate.setTimeInMillis(minDate);
        if (this.mTempDate.get(1) != this.mMinDate.get(1) || this.mTempDate.get(6) == this.mMinDate.get(6)) {
            this.mMinDate.setTimeInMillis(minDate);
            this.mCalendarView.setMinDate(minDate);
            if (this.mCurrentDate.before(this.mMinDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
    }

    public long getMaxDate() {
        return this.mCalendarView.getMaxDate();
    }

    public void setMaxDate(long maxDate) {
        this.mTempDate.setTimeInMillis(maxDate);
        if (this.mTempDate.get(1) != this.mMaxDate.get(1) || this.mTempDate.get(6) == this.mMaxDate.get(6)) {
            this.mMaxDate.setTimeInMillis(maxDate);
            this.mCalendarView.setMaxDate(maxDate);
            if (this.mCurrentDate.after(this.mMaxDate)) {
                this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.mIsEnabled != enabled) {
            super.setEnabled(enabled);
            this.mDaySpinner.setEnabled(enabled);
            this.mMonthSpinner.setEnabled(enabled);
            this.mYearSpinner.setEnabled(enabled);
            this.mCalendarView.setEnabled(enabled);
            this.mIsEnabled = enabled;
        }
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);
        event.getText().add(DateUtils.formatDateTime(this.mContext, this.mCurrentDate.getTimeInMillis(), 20));
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AmigoDatePicker.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AmigoDatePicker.class.getName());
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

    public boolean getCalendarViewShown() {
        return this.mCalendarView.isShown();
    }

    public CalendarView getCalendarView() {
        return this.mCalendarView;
    }

    public void setCalendarViewShown(boolean shown) {
        this.mCalendarView.setVisibility(shown ? 0 : 8);
    }

    public boolean getSpinnersShown() {
        return this.mSpinners.isShown();
    }

    public void setSpinnersShown(boolean shown) {
        this.mSpinners.setVisibility(shown ? 0 : 8);
    }

    private void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            this.mCurrentLocale = locale;
            this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
            this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
            this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
            this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
            this.mNumberOfMonths = this.mTempDate.getActualMaximum(2) + 1;
            this.mShortMonths = new String[this.mNumberOfMonths];
            for (int i = 0; i < this.mNumberOfMonths; i++) {
                this.mShortMonths[i] = DateUtils.getMonthString(i + 0, 20);
            }
        }
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        }
        long currentTimeMillis = oldCalendar.getTimeInMillis();
        Calendar newCalendar = Calendar.getInstance(locale);
        newCalendar.setTimeInMillis(currentTimeMillis);
        return newCalendar;
    }

    private void reorderSpinners() {
        this.mSpinners.removeAllViews();
        char[] order = android.text.format.DateFormat.getDateFormatOrder(getContext());
        int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case 'M':
                    this.mSpinners.addView(this.mMonthSpinner);
                    setImeOptions(this.mMonthSpinner, spinnerCount, i);
                    break;
                case 'd':
                    this.mSpinners.addView(this.mDaySpinner);
                    setImeOptions(this.mDaySpinner, spinnerCount, i);
                    break;
                case 'y':
                    this.mSpinners.addView(this.mYearSpinner);
                    setImeOptions(this.mYearSpinner, spinnerCount, i);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private void setAlign(AmigoNumberPicker picker, int i) {
        if (i == 0) {
            picker.setAlign(Align.RIGHT);
        }
        if (i == 2) {
            picker.setAlign(Align.LEFT);
        }
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        if (isNewDate(year, month, dayOfMonth)) {
            setDate(year, month, dayOfMonth);
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        }
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getYear(), getMonth(), getDayOfMonth());
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
        updateCalendarView();
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        this.mOnDateChangedListener = onDateChangedListener;
    }

    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(this.mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return (this.mCurrentDate.get(1) == year && this.mCurrentDate.get(2) == dayOfMonth && this.mCurrentDate.get(5) == month) ? false : true;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        this.mCurrentDate.set(year, month, dayOfMonth);
        if (this.mCurrentDate.before(this.mMinDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMinDate.getTimeInMillis());
        } else if (this.mCurrentDate.after(this.mMaxDate)) {
            this.mCurrentDate.setTimeInMillis(this.mMaxDate.getTimeInMillis());
        }
    }

    private void updateSpinners() {
        if (this.mCurrentDate.equals(this.mMinDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.get(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.get(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.getActualMaximum(2));
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else if (this.mCurrentDate.equals(this.mMaxDate)) {
            this.mDaySpinner.setMinValue(this.mCurrentDate.getActualMinimum(5));
            this.mDaySpinner.setMaxValue(this.mCurrentDate.get(5));
            this.mDaySpinner.setWrapSelectorWheel(false);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(this.mCurrentDate.getActualMinimum(2));
            this.mMonthSpinner.setMaxValue(this.mCurrentDate.get(2));
            this.mMonthSpinner.setWrapSelectorWheel(false);
        } else {
            this.mDaySpinner.setMinValue(1);
            this.mDaySpinner.setMaxValue(this.mCurrentDate.getActualMaximum(5));
            this.mDaySpinner.setWrapSelectorWheel(true);
            this.mMonthSpinner.setDisplayedValues(null);
            this.mMonthSpinner.setMinValue(0);
            this.mMonthSpinner.setMaxValue(11);
            this.mMonthSpinner.setWrapSelectorWheel(true);
        }
        this.mMonthSpinner.setDisplayedValues((String[]) Arrays.copyOfRange(this.mShortMonths, this.mMonthSpinner.getMinValue(), this.mMonthSpinner.getMaxValue() + 1));
        this.mYearSpinner.setMinValue(this.mMinDate.get(1));
        this.mYearSpinner.setMaxValue(this.mMaxDate.get(1));
        this.mYearSpinner.setWrapSelectorWheel(true);
        this.mYearSpinner.setValue(this.mCurrentDate.get(1));
        this.mMonthSpinner.setValue(this.mCurrentDate.get(2));
        this.mDaySpinner.setValue(this.mCurrentDate.get(5));
    }

    private void updateCalendarView() {
        this.mCalendarView.setDate(this.mCurrentDate.getTimeInMillis(), false, false);
    }

    public int getYear() {
        return this.mCurrentDate.get(Calendar.YEAR);
    }

    public int getMonth() {
        return this.mCurrentDate.get(Calendar.MONTH);
    }

    public int getDayOfMonth() {
        return this.mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    private void notifyDateChanged() {
        sendAccessibilityEvent(4);
        if (this.mOnDateChangedListener != null) {
            this.mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    private void setImeOptions(AmigoNumberPicker spinner, int spinnerCount, int spinnerIndex) {
        int imeOptions;
        if (spinnerIndex < spinnerCount - 1) {
            imeOptions = 5;
        } else {
            imeOptions = 6;
        }
        ((TextView) spinner.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_numberpicker_input"))).setImeOptions(imeOptions);
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
        if (inputMethodManager.isActive(this.mYearSpinnerInput)) {
            this.mYearSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mMonthSpinnerInput)) {
            this.mMonthSpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        } else if (inputMethodManager.isActive(this.mDaySpinnerInput)) {
            this.mDaySpinnerInput.clearFocus();
            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }
}
