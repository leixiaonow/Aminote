package amigoui.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;

import amigoui.widget.AmigoDatePicker;
import amigoui.widget.AmigoDatePicker.OnDateChangedListener;
import amigoui.widget.AmigoWidgetResource;

public class AmigoDatePickerDialog extends AmigoAlertDialog implements OnClickListener, OnDateChangedListener {
    private static final String DAY = "day";
    private static final String MONTH = "month";
    private static final String YEAR = "year";
    private final Calendar mCalendar;
    private final OnDateSetListener mCallBack;
    private Context mContext;
    private final AmigoDatePicker mDatePicker;
    private boolean mTitleNeedsUpdate;

    public interface OnDateSetListener {
        void onDateSet(AmigoDatePicker amigoDatePicker, int i, int i2, int i3);
    }

    public AmigoDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        this(context, resolvedTheme(context, AmigoWidgetResource.getIdentifierByAttr(context, "amigodatePickerDialogStyle")), callBack, year, monthOfYear, dayOfMonth);
    }

    private static int resolvedTheme(Context cxt, int resId) {
        TypedValue outValue = new TypedValue();
        cxt.getTheme().resolveAttribute(resId, outValue, true);
        return outValue.resourceId;
    }

    private AmigoDatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, theme);
        this.mTitleNeedsUpdate = true;
        this.mContext = getContext();
        this.mCallBack = callBack;
        this.mCalendar = Calendar.getInstance();
        Context themeContext = getContext();
        setButton(-1, themeContext.getText(AmigoWidgetResource.getIdentifierByString(themeContext, "amigo_date_time_done")), (OnClickListener) this);
        setButton(-2, themeContext.getText(AmigoWidgetResource.getIdentifierByString(themeContext, "amigo_cancel")), (OnClickListener) this);
        setIcon(0);
        View view = ((LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_date_picker_dialog"), null);
        setView(view);
        this.mDatePicker = (AmigoDatePicker) view.findViewById(AmigoWidgetResource.getIdentifierById(getContext(), "amigo_datePicker"));
        this.mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        updateTitle(year, monthOfYear, dayOfMonth);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            tryNotifyDateSet();
        }
    }

    public void onDateChanged(AmigoDatePicker view, int year, int month, int day) {
        this.mDatePicker.init(year, month, day, this);
        updateTitle(year, month, day);
    }

    public AmigoDatePicker getDatePicker() {
        return this.mDatePicker;
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {
        this.mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
    }

    private void tryNotifyDateSet() {
        if (this.mCallBack != null) {
            this.mDatePicker.clearFocus();
            this.mCallBack.onDateSet(this.mDatePicker, this.mDatePicker.getYear(), this.mDatePicker.getMonth(), this.mDatePicker.getDayOfMonth());
        }
    }

    protected void onStop() {
        super.onStop();
    }

    private void updateTitle(int year, int month, int day) {
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, this.mDatePicker.getYear());
        state.putInt(MONTH, this.mDatePicker.getMonth());
        state.putInt(DAY, this.mDatePicker.getDayOfMonth());
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mDatePicker.init(savedInstanceState.getInt(YEAR), savedInstanceState.getInt(MONTH), savedInstanceState.getInt(DAY), this);
    }
}
