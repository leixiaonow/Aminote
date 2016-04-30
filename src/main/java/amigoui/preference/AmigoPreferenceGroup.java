package amigoui.preference;

import com.gionee.aminote.R;
import amigoui.preference.AmigoGenericInflater.Parent;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AmigoPreferenceGroup extends AmigoPreference implements Parent<AmigoPreference> {
    private boolean mAttachedToActivity;
    private int mCurrentPreferenceOrder;
    private boolean mOrderingAsAdded;
    private List<AmigoPreference> mPreferenceList;

    public AmigoPreferenceGroup(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs, defStyle, defStyleRes);
        this.mOrderingAsAdded = true;
        this.mCurrentPreferenceOrder = 0;
        this.mAttachedToActivity = false;
        this.mPreferenceList = new ArrayList();
        if (!NativePreferenceManager.getAnalyzeNativePreferenceXml() || attrs == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AmigoPreferenceGroup, defStyle, 0);
            this.mOrderingAsAdded = a.getBoolean(R.styleable.AmigoPreferenceGroup_amigoorderingFromXml, this.mOrderingAsAdded);
            a.recycle();
            return;
        }
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            switch (attrs.getAttributeNameResource(i)) {
                case 16843239:
                    this.mOrderingAsAdded = attrs.getAttributeBooleanValue(i, this.mOrderingAsAdded);
                    break;
                default:
                    break;
            }
        }
    }

    public AmigoPreferenceGroup(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public AmigoPreferenceGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setOrderingAsAdded(boolean orderingAsAdded) {
        this.mOrderingAsAdded = orderingAsAdded;
    }

    public boolean isOrderingAsAdded() {
        return this.mOrderingAsAdded;
    }

    public void addItemFromInflater(AmigoPreference preference) {
        addPreference(preference);
    }

    public int getPreferenceCount() {
        return this.mPreferenceList.size();
    }

    public AmigoPreference getPreference(int index) {
        return (AmigoPreference) this.mPreferenceList.get(index);
    }

    public boolean addPreference(AmigoPreference preference) {
        if (this.mPreferenceList.contains(preference)) {
            return true;
        }
        if (preference.getOrder() == AmigoPreference.DEFAULT_ORDER) {
            if (this.mOrderingAsAdded) {
                int i = this.mCurrentPreferenceOrder;
                this.mCurrentPreferenceOrder = i + 1;
                preference.setOrder(i);
            }
            if (preference instanceof AmigoPreferenceGroup) {
                ((AmigoPreferenceGroup) preference).setOrderingAsAdded(this.mOrderingAsAdded);
            }
        }
        int insertionIndex = Collections.binarySearch(this.mPreferenceList, preference);
        if (insertionIndex < 0) {
            insertionIndex = (insertionIndex * -1) - 1;
        }
        if (!onPrepareAddPreference(preference)) {
            return false;
        }
        synchronized (this) {
            this.mPreferenceList.add(insertionIndex, preference);
        }
        preference.onAttachedToHierarchy(getPreferenceManager());
        if (this.mAttachedToActivity) {
            preference.onAttachedToActivity();
        }
        notifyHierarchyChanged();
        return true;
    }

    public boolean removePreference(AmigoPreference preference) {
        boolean returnValue = removePreferenceInt(preference);
        notifyHierarchyChanged();
        return returnValue;
    }

    private boolean removePreferenceInt(AmigoPreference preference) {
        boolean remove;
        synchronized (this) {
            preference.onPrepareForRemoval();
            remove = this.mPreferenceList.remove(preference);
        }
        return remove;
    }

    public void removeAll() {
        synchronized (this) {
            List<AmigoPreference> preferenceList = this.mPreferenceList;
            for (int i = preferenceList.size() - 1; i >= 0; i--) {
                removePreferenceInt((AmigoPreference) preferenceList.get(0));
            }
        }
        notifyHierarchyChanged();
    }

    protected boolean onPrepareAddPreference(AmigoPreference preference) {
        if (!super.isEnabled()) {
            preference.setEnabled(false);
        }
        return true;
    }

    public AmigoPreference findPreference(CharSequence key) {
        if (TextUtils.equals(getKey(), key)) {
            return this;
        }
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            AmigoPreference preference = getPreference(i);
            String curKey = preference.getKey();
            if (curKey != null && curKey.equals(key)) {
                return preference;
            }
            if (preference instanceof AmigoPreferenceGroup) {
                AmigoPreference returnedPreference = ((AmigoPreferenceGroup) preference).findPreference(key);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            }
        }
        return null;
    }

    protected boolean isOnSameScreenAsChildren() {
        return true;
    }

    protected void onAttachedToActivity() {
        super.onAttachedToActivity();
        this.mAttachedToActivity = true;
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onAttachedToActivity();
        }
    }

    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        this.mAttachedToActivity = false;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).setEnabled(enabled);
        }
    }

    void sortPreferences() {
        synchronized (this) {
            Collections.sort(this.mPreferenceList);
        }
    }

    protected void dispatchSaveInstanceState(Bundle container) {
        super.dispatchSaveInstanceState(container);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchSaveInstanceState(container);
        }
    }

    protected void dispatchRestoreInstanceState(Bundle container) {
        super.dispatchRestoreInstanceState(container);
        int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchRestoreInstanceState(container);
        }
    }
}
