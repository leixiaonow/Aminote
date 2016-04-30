package amigoui.preference;

import amigoui.widget.AmigoWidgetResource;
import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AmigoPreferenceGroupAdapter extends BaseAdapter implements OnPreferenceChangeInternalListener {
    private static final String TAG = "PreferenceGroupAdapter";
    private final int FRAME_LIST_BACKGROUND_BOTTOM = 4;
    private final int FRAME_LIST_BACKGROUND_FULL = 1;
    private final int FRAME_LIST_BACKGROUND_MIDDLE = 3;
    private final int FRAME_LIST_BACKGROUND_NULL = 0;
    private final int FRAME_LIST_BACKGROUND_TOP = 2;
    private final int FRAME_LIST_BACKGROUND_TOTAL = 5;
    private Context mContext;
    private boolean[] mDisplayDivider;
    private Handler mHandler = new Handler();
    private boolean mHasReturnedViewTypeCount = false;
    private boolean mIsGioneeStyle = false;
    private volatile boolean mIsSyncing = false;
    private int[] mPreferenceBackgroundIndexs;
    private int[] mPreferenceBackgroundRes;
    private AmigoPreferenceGroup mPreferenceGroup;
    private ArrayList<PreferenceLayout> mPreferenceLayouts;
    private List<AmigoPreference> mPreferenceList;
    private Runnable mSyncRunnable = new Runnable() {
        public void run() {
            AmigoPreferenceGroupAdapter.this.syncMyPreferences();
        }
    };
    private PreferenceLayout mTempPreferenceLayout = new PreferenceLayout();

    private static class PreferenceLayout implements Comparable<PreferenceLayout> {
        private String name;
        private int resId;
        private int widgetResId;

        private PreferenceLayout() {
        }

        public int compareTo(PreferenceLayout other) {
            int compareNames = this.name.compareTo(other.name);
            if (compareNames != 0) {
                return compareNames;
            }
            if (this.resId != other.resId) {
                return this.resId - other.resId;
            }
            if (this.widgetResId == other.widgetResId) {
                return 0;
            }
            return this.widgetResId - other.widgetResId;
        }
    }

    public AmigoPreferenceGroupAdapter(AmigoPreferenceGroup preferenceGroup) {
        this.mPreferenceGroup = preferenceGroup;
        this.mPreferenceGroup.setOnPreferenceChangeInternalListener(this);
        this.mPreferenceList = new ArrayList();
        this.mPreferenceLayouts = new ArrayList();
        syncMyPreferences();
    }

    public AmigoPreferenceGroupAdapter(AmigoPreferenceGroup preferenceGroup, Context context, boolean isGioneeStyle) {
        this.mPreferenceGroup = preferenceGroup;
        this.mPreferenceGroup.setOnPreferenceChangeInternalListener(this);
        this.mPreferenceList = new ArrayList();
        this.mPreferenceLayouts = new ArrayList();
        this.mContext = context;
        this.mIsGioneeStyle = isGioneeStyle;
        if (this.mIsGioneeStyle) {
            getFrameListBackground(this.mContext);
        }
        syncMyPreferences();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void syncMyPreferences() {
        /*
        r2 = this;
        monitor-enter(r2);
        r1 = r2.mIsSyncing;	 Catch:{ all -> 0x0038 }
        if (r1 == 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r2);	 Catch:{ all -> 0x0038 }
    L_0x0006:
        return;
    L_0x0007:
        r1 = 1;
        r2.mIsSyncing = r1;	 Catch:{ all -> 0x0038 }
        monitor-exit(r2);	 Catch:{ all -> 0x0038 }
        r0 = new java.util.ArrayList;
        r1 = r2.mPreferenceList;
        r1 = r1.size();
        r0.<init>(r1);
        r1 = r2.mPreferenceGroup;
        r2.flattenPreferenceGroup(r0, r1);
        r2.mPreferenceList = r0;
        r1 = r2.mIsGioneeStyle;
        if (r1 == 0) goto L_0x0029;
    L_0x0021:
        r1 = r2.mPreferenceList;
        r1 = r2.shouldDisplayDivider(r1);
        r2.mDisplayDivider = r1;
    L_0x0029:
        r2.notifyDataSetChanged();
        monitor-enter(r2);
        r1 = 0;
        r2.mIsSyncing = r1;	 Catch:{ all -> 0x0035 }
        r2.notifyAll();	 Catch:{ all -> 0x0035 }
        monitor-exit(r2);	 Catch:{ all -> 0x0035 }
        goto L_0x0006;
    L_0x0035:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0035 }
        throw r1;
    L_0x0038:
        r1 = move-exception;
        monitor-exit(r2);	 Catch:{ all -> 0x0038 }
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: amigoui.preference.AmigoPreferenceGroupAdapter.syncMyPreferences():void");
    }

    private boolean[] shouldDisplayDivider(List<AmigoPreference> preferences) {
        int size = preferences.size();
        if (preferences == null || size <= 0) {
            return null;
        }
        boolean[] shouldDisplayDivider = new boolean[size];
        int i = 0;
        while (i < size) {
            shouldDisplayDivider[i] = false;
            if ((preferences.get(i) instanceof AmigoPreferenceCategory) && i > 0) {
                shouldDisplayDivider[i - 1] = true;
            }
            i++;
        }
        return shouldDisplayDivider;
    }

    private void getFrameListBackground(Context context) {
        this.mPreferenceBackgroundRes = new int[5];
        this.mPreferenceBackgroundRes[0] = 0;
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(this.mContext, "amigoframeListBackground"), outValue, true);
        this.mPreferenceBackgroundRes[1] = outValue.resourceId;
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(this.mContext, "amigoframeListTopBackground"), outValue, true);
        this.mPreferenceBackgroundRes[2] = outValue.resourceId;
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(this.mContext, "amigoframeListMiddleBackground"), outValue, true);
        this.mPreferenceBackgroundRes[3] = outValue.resourceId;
        context.getTheme().resolveAttribute(AmigoWidgetResource.getIdentifierByAttr(this.mContext, "amigoframeListBottomBackground"), outValue, true);
        this.mPreferenceBackgroundRes[4] = outValue.resourceId;
    }

    private void flattenPreferenceGroup(List<AmigoPreference> preferences, AmigoPreferenceGroup group) {
        group.sortPreferences();
        int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            AmigoPreference preference = group.getPreference(i);
            preferences.add(preference);
            if (!(this.mHasReturnedViewTypeCount || preference.hasSpecifiedLayout())) {
                addPreferenceClassName(preference);
            }
            if (preference instanceof AmigoPreferenceGroup) {
                AmigoPreferenceGroup preferenceAsGroup = (AmigoPreferenceGroup) preference;
                if (preferenceAsGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, preferenceAsGroup);
                }
            }
            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    private PreferenceLayout createPreferenceLayout(AmigoPreference preference, PreferenceLayout in) {
        PreferenceLayout pl = in != null ? in : new PreferenceLayout();
        pl.name = preference.getClass().getName();
        pl.resId = preference.getLayoutResource();
        pl.widgetResId = preference.getWidgetLayoutResource();
        return pl;
    }

    private void addPreferenceClassName(AmigoPreference preference) {
        PreferenceLayout pl = createPreferenceLayout(preference, null);
        int insertPos = Collections.binarySearch(this.mPreferenceLayouts, pl);
        if (insertPos < 0) {
            this.mPreferenceLayouts.add((insertPos * -1) - 1, pl);
        }
    }

    public int getCount() {
        return this.mPreferenceList.size();
    }

    public AmigoPreference getItem(int position) {
        if (position < 0 || position >= getCount()) {
            return null;
        }
        return (AmigoPreference) this.mPreferenceList.get(position);
    }

    public long getItemId(int position) {
        if (position < 0 || position >= getCount()) {
            return Long.MIN_VALUE;
        }
        return getItem(position).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        AmigoPreference preference = getItem(position);
        this.mTempPreferenceLayout = createPreferenceLayout(preference, this.mTempPreferenceLayout);
        if (Collections.binarySearch(this.mPreferenceLayouts, this.mTempPreferenceLayout) < 0) {
            convertView = null;
        }
        if (!this.mIsGioneeStyle) {
            return preference.getView(convertView, parent);
        }
        preference.setShowDivider(this.mDisplayDivider[position]);
        return preference.getView(convertView, parent);
    }

    public boolean isEnabled(int position) {
        if (position < 0 || position >= getCount()) {
            return true;
        }
        return getItem(position).isSelectable();
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public void onPreferenceChange(AmigoPreference preference) {
        notifyDataSetChanged();
    }

    public void onPreferenceHierarchyChange(AmigoPreference preference) {
        this.mHandler.removeCallbacks(this.mSyncRunnable);
        this.mHandler.post(this.mSyncRunnable);
    }

    public boolean hasStableIds() {
        return true;
    }

    public int getItemViewType(int position) {
        if (!this.mHasReturnedViewTypeCount) {
            this.mHasReturnedViewTypeCount = true;
        }
        AmigoPreference preference = getItem(position);
        if (preference.hasSpecifiedLayout()) {
            return -1;
        }
        this.mTempPreferenceLayout = createPreferenceLayout(preference, this.mTempPreferenceLayout);
        int viewType = Collections.binarySearch(this.mPreferenceLayouts, this.mTempPreferenceLayout);
        if (viewType < 0) {
            return -1;
        }
        return viewType;
    }

    public int getViewTypeCount() {
        if (!this.mHasReturnedViewTypeCount) {
            this.mHasReturnedViewTypeCount = true;
        }
        return Math.max(1, this.mPreferenceLayouts.size());
    }
}
