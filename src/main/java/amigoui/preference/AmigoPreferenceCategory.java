package amigoui.preference;

import android.content.Context;
import android.util.AttributeSet;

public class AmigoPreferenceCategory extends AmigoPreferenceGroup {
    private static final String TAG = "PreferenceCategory";

    public AmigoPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AmigoPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoPreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, 16842892);
    }

    public AmigoPreferenceCategory(Context context) {
        this(context, null);
    }

    protected boolean onPrepareAddPreference(AmigoPreference preference) {
        if (!(preference instanceof AmigoPreferenceCategory)) {
            return super.onPrepareAddPreference(preference);
        }
        throw new IllegalArgumentException("Cannot add a PreferenceCategory directly to a PreferenceCategory");
    }

    public boolean isEnabled() {
        return false;
    }
}
