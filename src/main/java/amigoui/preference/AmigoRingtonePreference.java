package amigoui.preference;

import com.gionee.aminote.R;
import amigoui.preference.AmigoPreferenceManager.OnActivityResultListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;

public class AmigoRingtonePreference extends AmigoPreference implements OnActivityResultListener {
    private static final String TAG = "RingtonePreference";
    private int mRequestCode;
    private int mRingtoneType;
    private boolean mShowDefault;
    private boolean mShowSilent;

    public AmigoRingtonePreference(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoRingtonePreference, defStyle, 0);
            this.mRingtoneType = a.getInt(R.styleable.AmigoRingtonePreference_amigoringtoneType, 1);
            this.mShowDefault = a.getBoolean(R.styleable.AmigoRingtonePreference_amigoshowDefault, true);
            this.mShowSilent = a.getBoolean(R.styleable.AmigoRingtonePreference_amigoshowSilent, true);
            a.recycle();
            return;
        }
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16843257:
                    this.mRingtoneType = attrs.getAttributeIntValue(i, 1);
                    break;
                case 16843258:
                    this.mShowDefault = attrs.getAttributeBooleanValue(i, true);
                    break;
                case 16843259:
                    this.mShowSilent = attrs.getAttributeBooleanValue(i, true);
                    break;
                default:
                    break;
            }
        }
    }

    public AmigoRingtonePreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoRingtonePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842899);
    }

    public AmigoRingtonePreference(Context context) {
        this(context, null);
    }

    public int getRingtoneType() {
        return this.mRingtoneType;
    }

    public void setRingtoneType(int type) {
        this.mRingtoneType = type;
    }

    public boolean getShowDefault() {
        return this.mShowDefault;
    }

    public void setShowDefault(boolean showDefault) {
        this.mShowDefault = showDefault;
    }

    public boolean getShowSilent() {
        return this.mShowSilent;
    }

    public void setShowSilent(boolean showSilent) {
        this.mShowSilent = showSilent;
    }

    protected void onClick() {
        Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
        onPrepareRingtonePickerIntent(intent);
        AmigoPreferenceFragment owningFragment = getPreferenceManager().getFragment();
        if (owningFragment != null) {
            owningFragment.startActivityForResult(intent, this.mRequestCode);
        } else {
            getPreferenceManager().getActivity().startActivityForResult(intent, this.mRequestCode);
        }
    }

    protected void onPrepareRingtonePickerIntent(Intent ringtonePickerIntent) {
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", onRestoreRingtone());
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", this.mShowDefault);
        if (this.mShowDefault) {
            ringtonePickerIntent.putExtra("android.intent.extra.ringtone.DEFAULT_URI", RingtoneManager.getDefaultUri(getRingtoneType()));
        }
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", this.mShowSilent);
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.TYPE", this.mRingtoneType);
        ringtonePickerIntent.putExtra("android.intent.extra.ringtone.TITLE", getTitle());
    }

    protected void onSaveRingtone(Uri ringtoneUri) {
        persistString(ringtoneUri != null ? ringtoneUri.toString() : "");
    }

    protected Uri onRestoreRingtone() {
        String uriString = getPersistedString(null);
        if (TextUtils.isEmpty(uriString)) {
            return null;
        }
        return Uri.parse(uriString);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValueObj) {
        String defaultValue = (String) defaultValueObj;
        if (!restorePersistedValue && !TextUtils.isEmpty(defaultValue)) {
            onSaveRingtone(Uri.parse(defaultValue));
        }
    }

    protected void onAttachedToHierarchy(AmigoPreferenceManager preferenceManager) {
        super.onAttachedToHierarchy(preferenceManager);
        preferenceManager.registerOnActivityResultListener(this);
        this.mRequestCode = preferenceManager.getNextRequestCode();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != this.mRequestCode) {
            return false;
        }
        if (data != null) {
            Uri uri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
            if (callChangeListener(uri != null ? uri.toString() : "")) {
                onSaveRingtone(uri);
            }
        }
        return true;
    }
}
