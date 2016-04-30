package com.gionee.note.app.dialog;

import amigoui.widget.AmigoDatePicker;
import amigoui.widget.AmigoDatePicker.OnDateChangedListener;
import amigoui.widget.AmigoTimePicker;
import amigoui.widget.AmigoTimePicker.OnTimeChangedListener;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.aminote.R;
import java.lang.reflect.Field;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

public class DateTimeDialog extends Dialog implements OnClickListener, OnTimeChangedListener {
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String IS_24_HOUR = "is24hour";
    private static final String MINUTE = "minute";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private Calendar mCalendar;
    private Context mContext;
    private Date mDate;
    private AmigoDatePicker mDatePicker;
    private TextView mDateTextView;
    private final OnDateTimeSetListener mDateTimeCallback;
    private int mDayOfMonth;
    private Drawable mDefaultDrawable;
    private int mDefaultTextColor;
    private DateFormatSymbols mDfSymbols = new DateFormatSymbols();
    private int mHourOfDay;
    private boolean mIs24HourView;
    private int mMinute;
    private int mMonthOfYear;
    private Drawable mSelectedDrawable;
    private int mSelectedTextColor;
    private AmigoTimePicker mTimePicker;
    private TextView mTimeTextView;
    private int mYear;

    public interface OnDateTimeSetListener {
        void onDataTimeDelete();

        void onDateTimeSet(Calendar calendar);
    }

    private class DateChangedListener implements OnDateChangedListener {
        private DateChangedListener() {
        }

        public void onDateChanged(AmigoDatePicker view, int year, int monthOfYear, int dayOfMonth) {
            DateTimeDialog.this.mYear = year;
            DateTimeDialog.this.mMonthOfYear = monthOfYear;
            DateTimeDialog.this.mDayOfMonth = dayOfMonth;
            DateTimeDialog.this.updateDate();
            DateTimeDialog.this.mDateTextView.setText(DateTimeDialog.this.formatDate());
        }
    }

    public DateTimeDialog(Context context, int theme, long time, OnDateTimeSetListener dateTimeCallBack) {
        super(context, theme);
        setCanceledOnTouchOutside(true);
        this.mContext = context;
        this.mDateTimeCallback = dateTimeCallBack;
        Calendar calendar = Calendar.getInstance();
        if (time != 0) {
            calendar.setTimeInMillis(time);
        }
        this.mYear = calendar.get(1);
        this.mMonthOfYear = calendar.get(2);
        this.mDayOfMonth = calendar.get(5);
        this.mHourOfDay = calendar.get(11);
        this.mMinute = calendar.get(12);
        this.mIs24HourView = DateFormat.is24HourFormat(context);
        this.mCalendar = calendar;
        this.mSelectedTextColor = ContextCompat.getColor(context, R.color.system_stress_color);
        this.mDefaultTextColor = ContextCompat.getColor(context, R.color.datetime_title_text_normal_color);
        this.mSelectedDrawable = ContextCompat.getDrawable(context, R.drawable.datetime_select_bg);
        this.mDefaultDrawable = ContextCompat.getDrawable(context, R.drawable.datetime_light_bg);
        View view = LayoutInflater.from(this.mContext).inflate(R.layout.date_time_picker_dialog, null);
        initTextView(view, time);
        setContentView(view);
        Window window = getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = -1;
        lp.height = -2;
        window.setGravity(80);
    }

    private void initTextView(View view, long time) {
        this.mDateTextView = (TextView) view.findViewById(R.id.reminder_date_text);
        this.mDate = this.mCalendar.getTime();
        this.mDateTextView.setText(formatDate());
        this.mDateTextView.setBackground(this.mSelectedDrawable);
        this.mDateTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DateTimeDialog.this.mDatePicker.setVisibility(0);
                DateTimeDialog.this.mTimePicker.setVisibility(8);
                DateTimeDialog.this.mDateTextView.setBackground(DateTimeDialog.this.mSelectedDrawable);
                DateTimeDialog.this.mTimeTextView.setBackground(DateTimeDialog.this.mDefaultDrawable);
                DateTimeDialog.this.mDateTextView.setTextColor(DateTimeDialog.this.mSelectedTextColor);
                DateTimeDialog.this.mTimeTextView.setTextColor(DateTimeDialog.this.mDefaultTextColor);
            }
        });
        this.mTimeTextView = (TextView) view.findViewById(R.id.reminder_time_text);
        this.mTimeTextView.setText(formatTime());
        this.mTimeTextView.setBackground(this.mDefaultDrawable);
        this.mTimeTextView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DateTimeDialog.this.mDatePicker.setVisibility(8);
                DateTimeDialog.this.mTimePicker.setVisibility(0);
                DateTimeDialog.this.mDateTextView.setBackground(DateTimeDialog.this.mDefaultDrawable);
                DateTimeDialog.this.mTimeTextView.setBackground(DateTimeDialog.this.mSelectedDrawable);
                DateTimeDialog.this.mDateTextView.setTextColor(DateTimeDialog.this.mDefaultTextColor);
                DateTimeDialog.this.mTimeTextView.setTextColor(DateTimeDialog.this.mSelectedTextColor);
            }
        });
        this.mDatePicker = (AmigoDatePicker) view.findViewById(R.id.reminder_datePicker);
        this.mDatePicker.init(this.mYear, this.mMonthOfYear, this.mDayOfMonth, new DateChangedListener());
        this.mCalendar.clear();
        this.mCalendar.set(this.mYear, this.mMonthOfYear, this.mDayOfMonth, this.mHourOfDay, this.mMinute);
        this.mTimePicker = (AmigoTimePicker) view.findViewById(R.id.reminder_timePicker);
        this.mTimePicker.setIs24HourView(Boolean.valueOf(this.mIs24HourView));
        this.mTimePicker.setCurrentHour(Integer.valueOf(this.mHourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(this.mMinute));
        this.mTimePicker.setOnTimeChangedListener(this);
        if (time != 0) {
            TextView deleteButton = (TextView) view.findViewById(R.id.reminder_delete);
            deleteButton.setVisibility(0);
            deleteButton.setOnClickListener(this);
        }
        TextView sureButton = (TextView) view.findViewById(R.id.reminder_sure);
        ((TextView) view.findViewById(R.id.reminder_cancel)).setOnClickListener(this);
        sureButton.setOnClickListener(this);
    }

    private void updateDate() {
        this.mCalendar.set(this.mYear, this.mMonthOfYear, this.mDayOfMonth, this.mHourOfDay, this.mMinute);
        this.mDate = this.mCalendar.getTime();
    }

    public void onTimeChanged(AmigoTimePicker view, int hourOfDay, int minute) {
        if (isSuitableTime(hourOfDay, minute)) {
            this.mHourOfDay = hourOfDay;
            this.mMinute = minute;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 60000);
            this.mHourOfDay = calendar.get(11);
            this.mMinute = calendar.get(12);
            this.mTimePicker.setCurrentHour(Integer.valueOf(this.mHourOfDay));
            this.mTimePicker.setCurrentMinute(Integer.valueOf(this.mMinute));
        }
        updateDate();
        this.mTimeTextView.setText(formatTime());
    }

    private String formatDate() {
        return DateFormat.getDateFormat(this.mContext).format(this.mDate);
    }

    private String formatTime() {
        return DateFormat.getTimeFormat(this.mContext).format(this.mDate);
    }

    private boolean isSuitableTime(int h, int m) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(c.getTimeInMillis() + 60000);
        int year = c.get(1);
        int month = c.get(2);
        int day = c.get(5);
        int hour = c.get(11);
        int minute = c.get(12);
        if (year != this.mYear || month != this.mMonthOfYear || day != this.mDayOfMonth) {
            return true;
        }
        if (h < hour) {
            return false;
        }
        if (h != hour || m >= minute) {
            return true;
        }
        return false;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reminder_cancel:
                unShowDialog();
                return;
            case R.id.reminder_delete:
                unShowDialog();
                if (this.mDateTimeCallback != null) {
                    this.mDateTimeCallback.onDataTimeDelete();
                    return;
                }
                return;
            case R.id.reminder_sure:
                if (this.mCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    try {
                        Field field = getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(this, Boolean.valueOf(false));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(this.mContext, this.mContext.getString(R.string.alert_time_early), 0).show();
                    return;
                }
                if (this.mDateTimeCallback != null) {
                    setDatePickByInput();
                    setTimePickByInput();
                    this.mCalendar.set(this.mYear, this.mMonthOfYear, this.mDayOfMonth, this.mHourOfDay, this.mMinute);
                    this.mDateTimeCallback.onDateTimeSet(this.mCalendar);
                }
                if (this.mDatePicker.getVisibility() == 0) {
                    this.mDatePicker.clearFocus();
                } else if (this.mTimePicker.getVisibility() == 0) {
                    this.mTimePicker.clearFocus();
                }
                unShowDialog();
                return;
            default:
                return;
        }
    }

    public void updateDate(Calendar c) {
        this.mDatePicker.updateDate(c.get(1), c.get(2), c.get(5));
        this.mTimePicker.setCurrentHour(Integer.valueOf(c.get(11)));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(c.get(12)));
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        this.mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        this.mTimePicker.setCurrentHour(Integer.valueOf(hourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minutOfHour));
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, this.mDatePicker.getYear());
        state.putInt(MONTH, this.mDatePicker.getMonth());
        state.putInt(DAY, this.mDatePicker.getDayOfMonth());
        state.putInt(HOUR, this.mTimePicker.getCurrentHour().intValue());
        state.putInt(MINUTE, this.mTimePicker.getCurrentMinute().intValue());
        state.putBoolean(IS_24_HOUR, this.mTimePicker.is24HourView());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mDatePicker.init(savedInstanceState.getInt(YEAR), savedInstanceState.getInt(MONTH), savedInstanceState.getInt(DAY), new DateChangedListener());
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        this.mTimePicker.setIs24HourView(Boolean.valueOf(savedInstanceState.getBoolean(IS_24_HOUR)));
        this.mTimePicker.setCurrentHour(Integer.valueOf(hour));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minute));
    }

    private void unShowDialog() {
        dismiss();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 4:
                unShowDialog();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hideInputMethod() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.mContext.getSystemService("input_method");
        if (getWindow() != null && getWindow().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void setDatePickByInput() {
        Integer dayValue = StringConvertInteger(getPropertyValueByReflect(this.mDatePicker, "mDaySpinnerInput"));
        if (dayValue != null) {
            this.mDayOfMonth = dayValue.intValue();
        }
        Integer monthValue = getMonthByInput(getPropertyValueByReflect(this.mDatePicker, "mMonthSpinnerInput"));
        if (monthValue != null) {
            this.mMonthOfYear = monthValue.intValue();
        }
        Integer yearValue = StringConvertInteger(getPropertyValueByReflect(this.mDatePicker, "mYearSpinnerInput"));
        if (yearValue != null) {
            this.mYear = yearValue.intValue();
        }
    }

    private void setTimePickByInput() {
        Integer minuteValue = StringConvertInteger(getPropertyValueByReflect(this.mTimePicker, "mMinuteSpinnerInput"));
        if (minuteValue != null) {
            this.mMinute = minuteValue.intValue();
        }
        Integer hourValue = StringConvertInteger(getPropertyValueByReflect(this.mTimePicker, "mHourSpinnerInput"));
        if (hourValue == null) {
            return;
        }
        if (this.mIs24HourView || !isPM(getPropertyValueByReflect(this.mTimePicker, "mAmPmSpinnerInput"))) {
            this.mHourOfDay = hourValue.intValue();
        } else {
            this.mHourOfDay = hourValue.intValue() + 12;
        }
    }

    private Integer StringConvertInteger(String value) {
        if (TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value)) {
            return null;
        }
        return Integer.valueOf(Integer.parseInt(value));
    }

    private Integer getMonthByInput(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        String[] shortMonth = this.mDfSymbols.getShortMonths();
        int index = 0;
        for (String mon : shortMonth) {
            index++;
            if (mon.equalsIgnoreCase(value)) {
                return Integer.valueOf(index - 1);
            }
        }
        index = 0;
        for (String mon2 : shortMonth) {
            index++;
            if (value.equals(mon2.substring(0, mon2.length() - 1))) {
                return Integer.valueOf(index - 1);
            }
        }
        return null;
    }

    private boolean isPM(String propertyValue) {
        if (TextUtils.isEmpty(propertyValue) || !this.mDfSymbols.getAmPmStrings()[1].equals(propertyValue)) {
            return false;
        }
        return true;
    }

    private String getPropertyValueByReflect(Object obj, String property) {
        String propertyValue = null;
        if (!TextUtils.isEmpty(property)) {
            try {
                Field field = obj.getClass().getDeclaredField(property);
                field.setAccessible(true);
                try {
                    EditText editText = (EditText) field.get(obj);
                    if (!TextUtils.isEmpty(editText.getText())) {
                        propertyValue = editText.getText().toString();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
            }
        }
        return propertyValue;
    }
}
