package amigoui.preference;

import com.gionee.aminote.R;
import amigoui.widget.AmigoSwitch;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AmigoSwitchPreference extends AmigoTwoStatePreference {
    private final Listener mListener;
    private CharSequence mSwitchOff;
    private CharSequence mSwitchOn;

    private class Listener implements OnCheckedChangeListener {
        private Listener() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (AmigoSwitchPreference.this.callChangeListener(Boolean.valueOf(isChecked))) {
                AmigoSwitchPreference.this.setChecked(isChecked);
            } else {
                buttonView.setChecked(!isChecked);
            }
        }
    }

    public AmigoSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mListener = new Listener();
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoSwitchPreference, defStyle, 0);
            setSummaryOn(a.getString(R.styleable.AmigoSwitchPreference_amigosummaryOn));
            setSummaryOff(a.getString(R.styleable.AmigoSwitchPreference_amigosummaryOff));
            setSwitchTextOn(a.getString(R.styleable.AmigoSwitchPreference_amigoswitchTextOn));
            setSwitchTextOff(a.getString(R.styleable.AmigoSwitchPreference_amigoswitchTextOff));
            setDisableDependentsState(a.getBoolean(R.styleable.AmigoSwitchPreference_amigodisableDependentsState, false));
            a.recycle();
            return;
        }
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16843247:
                    setSummaryOn(NativePreferenceManager.getAttributeStringValue(context, attrs, i));
                    break;
                case 16843248:
                    setSummaryOff(NativePreferenceManager.getAttributeStringValue(context, attrs, i));
                    break;
                case 16843249:
                    setDisableDependentsState(attrs.getAttributeBooleanValue(i, false));
                    break;
                default:
                    break;
            }
        }
        setSwitchTextOn(AmigoWidgetResource.getIdentifierByString(context, "amigo_capital_on"));
        setSwitchTextOff(AmigoWidgetResource.getIdentifierByString(context, "amigo_capital_off"));
        setWidgetLayoutResource(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_preference_widget_switch"));
    }

    public AmigoSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16843629);
    }

    public AmigoSwitchPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        View checkableView = view.findViewById(AmigoWidgetResource.getIdentifierById(view.getContext(), "amigo_switchWidget"));
        if (checkableView != null && (checkableView instanceof Checkable)) {
            if (checkableView instanceof AmigoSwitch) {
                ((AmigoSwitch) checkableView).setOnCheckedChangeListener(null);
            }
            ((Checkable) checkableView).setChecked(this.mChecked);
            if (checkableView instanceof AmigoSwitch) {
                AmigoSwitch switchView = (AmigoSwitch) checkableView;
                switchView.setTextOn(this.mSwitchOn);
                switchView.setTextOff(this.mSwitchOff);
                switchView.setOnCheckedChangeListener(this.mListener);
            }
        }
        syncSummaryView(view);
    }

    public void setSwitchTextOn(CharSequence onText) {
        this.mSwitchOn = onText;
        notifyChanged();
    }

    public void setSwitchTextOff(CharSequence offText) {
        this.mSwitchOff = offText;
        notifyChanged();
    }

    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    public CharSequence getSwitchTextOn() {
        return this.mSwitchOn;
    }

    public CharSequence getSwitchTextOff() {
        return this.mSwitchOff;
    }
}
