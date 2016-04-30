package amigoui.app;

import amigoui.widget.AmigoTimePicker;
import amigoui.widget.AmigoTimePicker.OnTimeChangedListener;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

public class AmigoTimePickerDialog extends AmigoAlertDialog implements OnClickListener, OnTimeChangedListener {
    private static final String HOUR = "hour";
    private static final String IS_24_HOUR = "is24hour";
    private static final String MINUTE = "minute";
    private final OnTimeSetListener mCallback;
    int mInitialHourOfDay;
    int mInitialMinute;
    boolean mIs24HourView;
    private final AmigoTimePicker mTimePicker;

    public interface OnTimeSetListener {
        void onTimeSet(AmigoTimePicker amigoTimePicker, int i, int i2);
    }

    public AmigoTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        this(context, resolvedTheme(context, AmigoWidgetResource.getIdentifierByAttr(context, "amigodatePickerDialogStyle")), callBack, hourOfDay, minute, is24HourView);
    }

    private static int resolvedTheme(Context cxt, int resId) {
        TypedValue outValue = new TypedValue();
        cxt.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.resourceId;
    }

    private AmigoTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
        super(context, theme);
        this.mCallback = callBack;
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mIs24HourView = is24HourView;
        setIcon(0);
        Context themeContext = getContext();
        setButton(-1, themeContext.getText(AmigoWidgetResource.getIdentifierByString(context, "amigo_date_time_done")), (OnClickListener) this);
        setButton(-2, themeContext.getText(AmigoWidgetResource.getIdentifierByString(context, "amigo_cancel")), (OnClickListener) this);
        View view = ((LayoutInflater) themeContext.getSystemService("layout_inflater")).inflate(AmigoWidgetResource.getIdentifierByLayout(themeContext, "amigo_time_picker_dialog"), null);
        setView(view);
        this.mTimePicker = (AmigoTimePicker) view.findViewById(AmigoWidgetResource.getIdentifierById(context, "amigo_timePicker"));
        this.mTimePicker.setIs24HourView(Boolean.valueOf(this.mIs24HourView));
        this.mTimePicker.setCurrentHour(Integer.valueOf(this.mInitialHourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(this.mInitialMinute));
        this.mTimePicker.setOnTimeChangedListener(this);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            tryNotifyTimeSet();
        }
    }

    public void updateTime(int hourOfDay, int minutOfHour) {
        this.mTimePicker.setCurrentHour(Integer.valueOf(hourOfDay));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minutOfHour));
    }

    public void onTimeChanged(AmigoTimePicker view, int hourOfDay, int minute) {
    }

    private void tryNotifyTimeSet() {
        if (this.mCallback != null) {
            this.mTimePicker.clearFocus();
            this.mCallback.onTimeSet(this.mTimePicker, this.mTimePicker.getCurrentHour().intValue(), this.mTimePicker.getCurrentMinute().intValue());
        }
    }

    protected void onStop() {
        super.onStop();
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(HOUR, this.mTimePicker.getCurrentHour().intValue());
        state.putInt(MINUTE, this.mTimePicker.getCurrentMinute().intValue());
        state.putBoolean(IS_24_HOUR, this.mTimePicker.is24HourView());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int hour = savedInstanceState.getInt(HOUR);
        int minute = savedInstanceState.getInt(MINUTE);
        this.mTimePicker.setIs24HourView(Boolean.valueOf(savedInstanceState.getBoolean(IS_24_HOUR)));
        this.mTimePicker.setCurrentHour(Integer.valueOf(hour));
        this.mTimePicker.setCurrentMinute(Integer.valueOf(minute));
    }
}
