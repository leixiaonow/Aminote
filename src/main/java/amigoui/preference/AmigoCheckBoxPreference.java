package amigoui.preference;

import com.gionee.aminote.R;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

public class AmigoCheckBoxPreference extends AmigoTwoStatePreference {
    public AmigoCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoCheckBoxPreference, defStyle, 0);
            setSummaryOn(a.getString(R.styleable.AmigoCheckBoxPreference_amigosummaryOn));
            setSummaryOff(a.getString(R.styleable.AmigoCheckBoxPreference_amigosummaryOff));
            setDisableDependentsState(a.getBoolean(R.styleable.AmigoCheckBoxPreference_amigodisableDependentsState, false));
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
        setWidgetLayoutResource(AmigoWidgetResource.getIdentifierByLayout(context, "amigo_preference_widget_checkbox"));
    }

    public AmigoCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842895);
    }

    public AmigoCheckBoxPreference(Context context) {
        this(context, null);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        View checkboxView = view.findViewById(16908289);
        if (checkboxView != null && (checkboxView instanceof Checkable)) {
            ((Checkable) checkboxView).setChecked(this.mChecked);
            sendAccessibilityEvent(checkboxView);
        }
        syncSummaryView(view);
    }
}
