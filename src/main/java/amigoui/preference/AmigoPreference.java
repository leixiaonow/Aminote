package amigoui.preference;

import com.gionee.aminote.R;
import amigoui.changecolors.ChameleonColorManager;
import amigoui.preference.AmigoPreferenceManager.OnPreferenceTreeClickListener;
import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.amigoui.internal.util.AmigoCharSequences;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AmigoPreference implements Comparable<AmigoPreference>, OnDependencyChangeListener {
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    private boolean mBaseMethodCalled;
    private Context mContext;
    private Object mDefaultValue;
    private String mDependencyKey;
    private boolean mDependencyMet;
    private List<AmigoPreference> mDependents;
    private boolean mEnabled;
    private Bundle mExtras;
    private String mFragment;
    private boolean mHasSpecifiedLayout;
    private Drawable mIcon;
    private int mIconResId;
    private long mId;
    private Intent mIntent;
    private String mKey;
    private int mLayoutResId;
    private OnPreferenceChangeInternalListener mListener;
    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private int mOrder;
    private boolean mParentDependencyMet;
    private boolean mPersistent;
    private AmigoPreferenceManager mPreferenceManager;
    private boolean mRequiresKey;
    private boolean mSelectable;
    private boolean mShouldDisableView;
    private boolean mShowDivider;
    private CharSequence mSummary;
    private CharSequence mTitle;
    private int mTitleRes;
    private int mWidgetLayoutResId;

    public static class BaseSavedState extends AbsSavedState {
        public static final Creator<BaseSavedState> CREATOR = new Creator<BaseSavedState>() {
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };

        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
    }

    interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(AmigoPreference amigoPreference);

        void onPreferenceHierarchyChange(AmigoPreference amigoPreference);
    }

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(AmigoPreference amigoPreference, Object obj);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(AmigoPreference amigoPreference);
    }

    public AmigoPreference(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        this.mOrder = DEFAULT_ORDER;
        this.mEnabled = true;
        this.mSelectable = true;
        this.mPersistent = true;
        this.mDependencyMet = true;
        this.mParentDependencyMet = true;
        this.mShowDivider = false;
        this.mShouldDisableView = true;
        this.mLayoutResId = AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_preference");
        this.mHasSpecifiedLayout = false;
        this.mContext = context;
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            AnalyzeAmigoPreferenceAttributeSet(context, attrs, defStyle, defStyleRes);
        } else {
            AnalyzeNativePreferenceAttributeSet(context, attrs, defStyle, defStyleRes);
        }
        if (!getClass().getName().startsWith(AmigoPreferenceManager.METADATA_KEY_PREFERENCES)) {
            this.mHasSpecifiedLayout = true;
        }
    }

    public AmigoPreference(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 16842894);
    }

    public AmigoPreference(Context context) {
        this(context, null);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public void setFragment(String fragment) {
        this.mFragment = fragment;
    }

    public String getFragment() {
        return this.mFragment;
    }

    public Bundle getExtras() {
        if (this.mExtras == null) {
            this.mExtras = new Bundle();
        }
        return this.mExtras;
    }

    public Bundle peekExtras() {
        return this.mExtras;
    }

    public void setLayoutResource(int layoutResId) {
        if (layoutResId != this.mLayoutResId) {
            this.mHasSpecifiedLayout = true;
        }
        this.mLayoutResId = layoutResId;
    }

    public int getLayoutResource() {
        return this.mLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        if (widgetLayoutResId != this.mWidgetLayoutResId) {
            this.mHasSpecifiedLayout = true;
        }
        this.mWidgetLayoutResId = widgetLayoutResId;
    }

    public int getWidgetLayoutResource() {
        return this.mWidgetLayoutResId;
    }

    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
        }
        onBindView(convertView);
        return convertView;
    }

    public void setShowDivider(boolean showDivider) {
        this.mShowDivider = showDivider;
    }

    protected View onCreateView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        View layout = layoutInflater.inflate(this.mLayoutResId, parent, false);
        ViewGroup widgetFrame = (ViewGroup) layout.findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (this.mWidgetLayoutResId != 0) {
                layoutInflater.inflate(this.mWidgetLayoutResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(8);
            }
        }
        LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        LayoutParams lParams = new LayoutParams(-1, -2);
        lParams.gravity = 80;
        ViewGroup dividerContainer = (ViewGroup) layout.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_preference_title_layout"));
        ImageView imgview = (ImageView) inflater.inflate(AmigoWidgetResource.getIdentifierByLayout(this.mContext, "amigo_divider"), dividerContainer, false);
        if (dividerContainer != null) {
            dividerContainer.addView(imgview, lParams);
        }
        if (ChameleonColorManager.isNeedChangeColor()) {
            imgview.setBackgroundColor(ChameleonColorManager.getContentColorThirdlyOnBackgroud_C3());
        }
        return layout;
    }

    protected void onBindView(View view) {
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        if (titleView != null) {
            CharSequence title = getTitle();
            if (TextUtils.isEmpty(title)) {
                titleView.setVisibility(8);
            } else {
                titleView.setText(title);
                titleView.setVisibility(0);
            }
        }
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
            CharSequence summary = getSummary();
            if (TextUtils.isEmpty(summary)) {
                summaryView.setVisibility(8);
            } else {
                summaryView.setText(summary);
                summaryView.setVisibility(0);
            }
        }
        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        if (imageView != null) {
            int i;
            if (!(this.mIconResId == 0 && this.mIcon == null)) {
                if (this.mIcon == null) {
                    this.mIcon = getContext().getResources().getDrawable(this.mIconResId);
                }
                if (this.mIcon != null) {
                    imageView.setImageDrawable(this.mIcon);
                }
            }
            if (this.mIcon != null) {
                i = 0;
            } else {
                i = 8;
            }
            imageView.setVisibility(i);
        }
        if (this.mShouldDisableView) {
            setEnabledStateOnViews(view, isEnabled());
        }
        ImageView dividerView = (ImageView) view.findViewById(AmigoWidgetResource.getIdentifierById(this.mContext, "amigo_divider"));
        if (dividerView != null) {
            if (this.mShowDivider) {
                dividerView.setVisibility(0);
            } else {
                dividerView.setVisibility(8);
            }
        }
    }

    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    public void setOrder(int order) {
        if (order != this.mOrder) {
            this.mOrder = order;
            notifyHierarchyChanged();
        }
    }

    public int getOrder() {
        return this.mOrder;
    }

    public void setTitle(CharSequence title) {
        if ((title == null && this.mTitle != null) || (title != null && !title.equals(this.mTitle))) {
            this.mTitleRes = 0;
            this.mTitle = title;
            notifyChanged();
        }
    }

    public void setTitle(int titleResId) {
        setTitle(this.mContext.getString(titleResId));
        this.mTitleRes = titleResId;
    }

    public int getTitleRes() {
        return this.mTitleRes;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setIcon(Drawable icon) {
        if ((icon == null && this.mIcon != null) || (icon != null && this.mIcon != icon)) {
            this.mIcon = icon;
            notifyChanged();
        }
    }

    public void setIcon(int iconResId) {
        this.mIconResId = iconResId;
        setIcon(this.mContext.getResources().getDrawable(iconResId));
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public void setSummary(CharSequence summary) {
        if ((summary == null && this.mSummary != null) || (summary != null && !summary.equals(this.mSummary))) {
            this.mSummary = summary;
            notifyChanged();
        }
    }

    public void setSummary(int summaryResId) {
        setSummary(this.mContext.getString(summaryResId));
    }

    public void setEnabled(boolean enabled) {
        if (this.mEnabled != enabled) {
            this.mEnabled = enabled;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean isEnabled() {
        return this.mEnabled && this.mDependencyMet && this.mParentDependencyMet;
    }

    public void setSelectable(boolean selectable) {
        if (this.mSelectable != selectable) {
            this.mSelectable = selectable;
            notifyChanged();
        }
    }

    public boolean isSelectable() {
        return this.mSelectable;
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        this.mShouldDisableView = shouldDisableView;
        notifyChanged();
    }

    public boolean getShouldDisableView() {
        return this.mShouldDisableView;
    }

    long getId() {
        return this.mId;
    }

    protected void onClick() {
    }

    public void setKey(String key) {
        this.mKey = key;
        if (this.mRequiresKey && !hasKey()) {
            requireKey();
        }
    }

    public String getKey() {
        return this.mKey;
    }

    void requireKey() {
        if (this.mKey == null) {
            throw new IllegalStateException("Preference does not have a key assigned.");
        }
        this.mRequiresKey = true;
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(this.mKey);
    }

    public boolean isPersistent() {
        return this.mPersistent;
    }

    protected boolean shouldPersist() {
        return this.mPreferenceManager != null && isPersistent() && hasKey();
    }

    public void setPersistent(boolean persistent) {
        this.mPersistent = persistent;
    }

    protected boolean callChangeListener(Object newValue) {
        return this.mOnChangeListener == null ? true : this.mOnChangeListener.onPreferenceChange(this, newValue);
    }

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.mOnChangeListener = onPreferenceChangeListener;
    }

    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return this.mOnChangeListener;
    }

    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        this.mOnClickListener = onPreferenceClickListener;
    }

    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return this.mOnClickListener;
    }

    public void performClick(AmigoPreferenceScreen preferenceScreen) {
        if (isEnabled()) {
            onClick();
            if (this.mOnClickListener == null || !this.mOnClickListener.onPreferenceClick(this)) {
                AmigoPreferenceManager preferenceManager = getPreferenceManager();
                if (preferenceManager != null) {
                    OnPreferenceTreeClickListener listener = preferenceManager.getOnPreferenceTreeClickListener();
                    if (!(preferenceScreen == null || listener == null || !listener.onPreferenceTreeClick(preferenceScreen, this))) {
                        return;
                    }
                }
                if (this.mIntent != null) {
                    getContext().startActivity(this.mIntent);
                }
            }
        }
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public Context getContext() {
        return this.mContext;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getSharedPreferences();
    }

    public Editor getEditor() {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.getEditor();
    }

    public boolean shouldCommit() {
        if (this.mPreferenceManager == null) {
            return false;
        }
        return this.mPreferenceManager.shouldCommit();
    }

    public int compareTo(AmigoPreference another) {
        if (this.mOrder != DEFAULT_ORDER || (this.mOrder == DEFAULT_ORDER && another.mOrder != DEFAULT_ORDER)) {
            return this.mOrder - another.mOrder;
        }
        if (this.mTitle == null) {
            return 1;
        }
        if (another.mTitle == null) {
            return -1;
        }
        return AmigoCharSequences.compareToIgnoreCase(this.mTitle, another.mTitle);
    }

    final void setOnPreferenceChangeInternalListener(OnPreferenceChangeInternalListener listener) {
        this.mListener = listener;
    }

    protected void notifyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceChange(this);
        }
    }

    protected void notifyHierarchyChanged() {
        if (this.mListener != null) {
            this.mListener.onPreferenceHierarchyChange(this);
        }
    }

    public AmigoPreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    protected void onAttachedToHierarchy(AmigoPreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
        this.mId = preferenceManager.getNextId();
        dispatchSetInitialValue();
    }

    protected void onAttachedToActivity() {
        registerDependency();
    }

    private void registerDependency() {
        if (!TextUtils.isEmpty(this.mDependencyKey)) {
            AmigoPreference preference = findPreferenceInHierarchy(this.mDependencyKey);
            if (preference != null) {
                preference.registerDependent(this);
                return;
            }
            throw new IllegalStateException("Dependency \"" + this.mDependencyKey + "\" not found for preference \"" + this.mKey + "\" (title: \"" + this.mTitle + "\"");
        }
    }

    private void unregisterDependency() {
        if (this.mDependencyKey != null) {
            AmigoPreference oldDependency = findPreferenceInHierarchy(this.mDependencyKey);
            if (oldDependency != null) {
                oldDependency.unregisterDependent(this);
            }
        }
    }

    protected AmigoPreference findPreferenceInHierarchy(String key) {
        if (TextUtils.isEmpty(key) || this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    private void registerDependent(AmigoPreference dependent) {
        if (this.mDependents == null) {
            this.mDependents = new ArrayList();
        }
        this.mDependents.add(dependent);
        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(AmigoPreference dependent) {
        if (this.mDependents != null) {
            this.mDependents.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        List<AmigoPreference> dependents = this.mDependents;
        if (dependents != null) {
            int dependentsCount = dependents.size();
            for (int i = 0; i < dependentsCount; i++) {
                ((AmigoPreference) dependents.get(i)).onDependencyChanged(this, disableDependents);
            }
        }
    }

    public void onDependencyChanged(AmigoPreference dependency, boolean disableDependent) {
        if (this.mDependencyMet == disableDependent) {
            this.mDependencyMet = !disableDependent;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public void onParentChanged(AmigoPreference parent, boolean disableChild) {
        if (this.mParentDependencyMet == disableChild) {
            this.mParentDependencyMet = !disableChild;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    public void setDependency(String dependencyKey) {
        unregisterDependency();
        this.mDependencyKey = dependencyKey;
        registerDependency();
    }

    public String getDependency() {
        return this.mDependencyKey;
    }

    protected void onPrepareForRemoval() {
        unregisterDependency();
    }

    public void setDefaultValue(Object defaultValue) {
        this.mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        if (shouldPersist() && getSharedPreferences().contains(this.mKey)) {
            onSetInitialValue(true, null);
        } else if (this.mDefaultValue != null) {
            onSetInitialValue(false, this.mDefaultValue);
        }
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }

    private void tryCommit(Editor editor) {
        if (this.mPreferenceManager.shouldCommit()) {
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    protected boolean persistString(String value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedString(null)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putString(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected String getPersistedString(String defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getString(this.mKey, defaultReturnValue);
    }

    protected boolean persistStringSet(Set<String> values) {
        if (!shouldPersist()) {
            return false;
        }
        if (values.equals(getPersistedStringSet(null))) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putStringSet(this.mKey, values);
        tryCommit(editor);
        return true;
    }

    protected Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getStringSet(this.mKey, defaultReturnValue);
    }

    protected boolean persistInt(int value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedInt(value ^ -1)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putInt(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected int getPersistedInt(int defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getInt(this.mKey, defaultReturnValue);
    }

    protected boolean persistFloat(float value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedFloat(Float.NaN)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putFloat(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected float getPersistedFloat(float defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getFloat(this.mKey, defaultReturnValue);
    }

    protected boolean persistLong(long value) {
        if (!shouldPersist()) {
            return false;
        }
        if (value == getPersistedLong(-1 ^ value)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putLong(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected long getPersistedLong(long defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getLong(this.mKey, defaultReturnValue);
    }

    protected boolean persistBoolean(boolean value) {
        boolean z = false;
        if (!shouldPersist()) {
            return false;
        }
        if (!value) {
            z = true;
        }
        if (value == getPersistedBoolean(z)) {
            return true;
        }
        Editor editor = this.mPreferenceManager.getEditor();
        editor.putBoolean(this.mKey, value);
        tryCommit(editor);
        return true;
    }

    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        return !shouldPersist() ? defaultReturnValue : this.mPreferenceManager.getSharedPreferences().getBoolean(this.mKey, defaultReturnValue);
    }

    boolean hasSpecifiedLayout() {
        return this.mHasSpecifiedLayout;
    }

    public String toString() {
        return getFilterableStringBuilder().toString();
    }

    StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title).append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary).append(' ');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }

    void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            this.mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!this.mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            } else if (state != null) {
                container.putParcelable(this.mKey, state);
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        this.mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    void dispatchRestoreInstanceState(Bundle container) {
        if (hasKey()) {
            Parcelable state = container.getParcelable(this.mKey);
            if (state != null) {
                this.mBaseMethodCalled = false;
                onRestoreInstanceState(state);
                if (!this.mBaseMethodCalled) {
                    throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        this.mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }

    private void AnalyzeNativePreferenceAttributeSet(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16842754:
                    this.mIconResId = attrs.getAttributeIntValue(i, 0);
                    break;
                case 16842765:
                    this.mPersistent = attrs.getAttributeBooleanValue(i, this.mPersistent);
                    break;
                case 16842766:
                    this.mEnabled = attrs.getAttributeBooleanValue(i, true);
                    break;
                case 16842994:
                    this.mLayoutResId = attrs.getAttributeIntValue(i, this.mLayoutResId);
                    break;
                case 16843233:
                    this.mTitle = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    break;
                case 16843238:
                    this.mSelectable = attrs.getAttributeBooleanValue(i, true);
                    break;
                case 16843240:
                    this.mKey = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    break;
                case 16843241:
                    this.mSummary = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    break;
                case 16843242:
                    this.mOrder = attrs.getAttributeIntValue(i, this.mOrder);
                    break;
                case 16843243:
                    this.mWidgetLayoutResId = attrs.getAttributeIntValue(i, this.mWidgetLayoutResId);
                    break;
                case 16843244:
                    this.mDependencyKey = NativePreferenceManager.getAttributeStringValue(context, attrs, i);
                    break;
                case 16843246:
                    this.mShouldDisableView = attrs.getAttributeBooleanValue(i, this.mShouldDisableView);
                    break;
                case 16843491:
                    this.mFragment = attrs.getAttributeValue(i);
                    break;
                default:
                    break;
            }
        }
    }

    private void AnalyzeAmigoPreferenceAttributeSet(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoPreference, defStyle, defStyleRes);
        for (int i = a.getIndexCount(); i >= 0; i--) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.AmigoPreference_amigoicon) {
                this.mIconResId = a.getResourceId(attr, 0);
            } else if (attr == R.styleable.AmigoPreference_amigokey) {
                this.mKey = a.getString(attr);
            } else if (attr == R.styleable.AmigoPreference_amigotitle) {
                this.mTitleRes = a.getResourceId(attr, 0);
                this.mTitle = a.getString(attr);
            } else if (attr == R.styleable.AmigoPreference_amigosummary) {
                this.mSummary = a.getString(attr);
            } else if (attr == R.styleable.AmigoPreference_amigoorder) {
                this.mOrder = a.getInt(attr, this.mOrder);
            } else if (attr == R.styleable.AmigoPreference_amigofragment) {
                this.mFragment = a.getString(attr);
            } else if (attr == R.styleable.AmigoPreference_amigolayout) {
                this.mLayoutResId = a.getResourceId(attr, this.mLayoutResId);
            } else if (attr == R.styleable.AmigoPreference_amigowidgetLayout) {
                this.mWidgetLayoutResId = a.getResourceId(attr, this.mWidgetLayoutResId);
            } else if (attr == R.styleable.AmigoPreference_amigoenabled) {
                this.mEnabled = a.getBoolean(attr, true);
            } else if (attr == R.styleable.AmigoPreference_amigoselectable) {
                this.mSelectable = a.getBoolean(attr, true);
            } else if (attr == R.styleable.AmigoPreference_amigopersistent) {
                this.mPersistent = a.getBoolean(attr, this.mPersistent);
            } else if (attr == R.styleable.AmigoPreference_amigodependency) {
                this.mDependencyKey = a.getString(attr);
            } else if (attr == R.styleable.AmigoPreference_amigodefaultValue) {
                this.mDefaultValue = onGetDefaultValue(a, attr);
            } else if (attr == R.styleable.AmigoPreference_amigoshouldDisableView) {
                this.mShouldDisableView = a.getBoolean(attr, this.mShouldDisableView);
            }
        }
        a.recycle();
    }
}
