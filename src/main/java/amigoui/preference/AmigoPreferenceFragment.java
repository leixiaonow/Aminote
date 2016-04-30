package amigoui.preference;

import amigoui.preference.AmigoPreferenceManager.OnPreferenceTreeClickListener;
import amigoui.widget.AmigoListView;
import amigoui.widget.AmigoWidgetResource;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;

public abstract class AmigoPreferenceFragment extends Fragment implements OnPreferenceTreeClickListener {
    private static final int FIRST_REQUEST_CODE = 100;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final String PREFERENCES_TAG = "android:preferences";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AmigoPreferenceFragment.this.bindPreferences();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHavePrefs;
    private boolean mInitDone;
    private AmigoListView mList;
    private OnKeyListener mListOnKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            Object selectedItem = AmigoPreferenceFragment.this.mList.getSelectedItem();
            if (!(selectedItem instanceof AmigoPreference)) {
                return false;
            }
            return ((AmigoPreference) selectedItem).onKey(AmigoPreferenceFragment.this.mList.getSelectedView(), keyCode, event);
        }
    };
    private AmigoPreferenceManager mPreferenceManager;
    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            AmigoPreferenceFragment.this.mList.focusableViewAvailable(AmigoPreferenceFragment.this.mList);
        }
    };

    public interface OnPreferenceStartFragmentCallback {
        boolean onPreferenceStartFragment(AmigoPreferenceFragment amigoPreferenceFragment, AmigoPreference amigoPreference);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPreferenceManager = new AmigoPreferenceManager(getActivity(), 100);
        this.mPreferenceManager.setFragment(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(AmigoWidgetResource.getIdentifierByLayout(getActivity(), "amigo_preference_list_fragment"), container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mHavePrefs) {
            bindPreferences();
        }
        this.mInitDone = true;
        if (savedInstanceState != null) {
            Bundle container = savedInstanceState.getBundle(PREFERENCES_TAG);
            if (container != null) {
                AmigoPreferenceScreen preferenceScreen = getPreferenceScreen();
                if (preferenceScreen != null) {
                    preferenceScreen.restoreHierarchyState(container);
                }
            }
        }
    }

    public void onStart() {
        super.onStart();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(this);
    }

    public void onStop() {
        super.onStop();
        this.mPreferenceManager.dispatchActivityStop();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(null);
    }

    public void onDestroyView() {
        this.mList = null;
        this.mHandler.removeCallbacks(this.mRequestFocus);
        this.mHandler.removeMessages(1);
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mPreferenceManager.dispatchActivityDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AmigoPreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle(PREFERENCES_TAG, container);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mPreferenceManager.dispatchActivityResult(requestCode, resultCode, data);
    }

    public AmigoPreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    public void setPreferenceScreen(AmigoPreferenceScreen preferenceScreen) {
        if (this.mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
            this.mHavePrefs = true;
            if (this.mInitDone) {
                postBindPreferences();
            }
        }
    }

    public AmigoPreferenceScreen getPreferenceScreen() {
        return this.mPreferenceManager.getPreferenceScreen();
    }

    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromIntent(intent, getPreferenceScreen()));
    }

    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromResource(getActivity(), preferencesResId, getPreferenceScreen()));
    }

    public boolean onPreferenceTreeClick(AmigoPreferenceScreen preferenceScreen, AmigoPreference preference) {
        if (preference.getFragment() == null || !(getActivity() instanceof OnPreferenceStartFragmentCallback)) {
            return false;
        }
        return ((OnPreferenceStartFragmentCallback) getActivity()).onPreferenceStartFragment(this, preference);
    }

    public AmigoPreference findPreference(CharSequence key) {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    private void requirePreferenceManager() {
        if (this.mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void postBindPreferences() {
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.obtainMessage(1).sendToTarget();
        }
    }

    private void bindPreferences() {
        AmigoPreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.bind(getListView());
        }
    }

    protected void onBindPreferences() {
    }

    protected void onUnbindPreferences() {
    }

    public AmigoListView getListView() {
        ensureList();
        return this.mList;
    }

    public boolean hasListView() {
        if (this.mList != null) {
            return true;
        }
        View root = getView();
        if (root == null) {
            return false;
        }
        View rawListView = root.findViewById(16908298);
        if (!(rawListView instanceof AmigoListView)) {
            return false;
        }
        this.mList = (AmigoListView) rawListView;
        if (this.mList == null) {
            return false;
        }
        return true;
    }

    private void ensureList() {
        if (this.mList == null) {
            View root = getView();
            if (root == null) {
                throw new IllegalStateException("Content view not yet created");
            }
            View rawListView = root.findViewById(16908298);
            if (rawListView instanceof AmigoListView) {
                this.mList = (AmigoListView) rawListView;
                if (this.mList == null) {
                    throw new RuntimeException("Your content must have a AmigoListView whose id attribute is 'android.R.id.list'");
                }
                this.mList.setOnKeyListener(this.mListOnKeyListener);
                this.mHandler.post(this.mRequestFocus);
                return;
            }
            throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a AmigoListView class");
        }
    }
}
