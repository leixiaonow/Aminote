package com.gionee.feedback.ui;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.gionee.feedback.db.DataChangeObserver;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.logic.SendState;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.utils.Log;
import com.gionee.res.Layout;
import com.gionee.res.Text;
import com.gionee.res.Widget;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FeedBackActivity extends BaseActivity {
    private static final HashMap<State, BaseFragment> STATE_FRAGMENT = new HashMap();
    private static final String TAG = "FeedBackActivity";
    private ActionBar mActionBar;
    private BadgeView mBadgeView;
    private View mCustomView;
    private DataChangeObserver mDataChangeObserver = new DataChangeObserver() {
        public void onDataChange(List<FeedbackInfo> infos) {
            FeedBackActivity.this.mFeedbackInfos = infos;
            Log.d(FeedBackActivity.TAG, "onDataChange infos = " + infos);
            FeedBackActivity.this.updateDatas(infos);
            FeedBackActivity.this.updateView();
        }
    };
    private DataManager mDataManager;
    private State mShowState = State.SEND;

    static {
        STATE_FRAGMENT.put(State.SEND, SendFragment.getInstance(State.SEND));
        STATE_FRAGMENT.put(State.RECORD, RecordFragment.getInstance(State.RECORD));
    }

    protected void setContentView() {
        FrameLayout frameLayout = new FrameLayout(this.mContext);
        frameLayout.setId(Widget.gn_fb_id_main_layout.getIdentifier(this.mContext));
        setContentView(frameLayout);
    }

    protected void init() {
        setRequestedOrientation(1);
        this.mDataManager = DataManager.getInstance(this.mContext);
    }

    protected void onResume() {
        super.onResume();
        this.mDataManager.registerDataObserver(this.mDataChangeObserver);
        this.mDataManager.loopGetRecord();
    }

    protected void onPause() {
        this.mDataManager.stopLoopRecord();
        this.mDataManager.unregisteredDataObserver(this.mDataChangeObserver);
        super.onPause();
    }

    protected void initActionBar() {
        this.mActionBar = getActionBar();
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
        this.mActionBar.setDisplayShowCustomEnabled(true);
        this.mActionBar.setDisplayShowHomeEnabled(false);
        this.mActionBar.setTitle(Text.gn_fb_string_title_feedback.getIdentifier(this.mContext));
        this.mCustomView = LayoutInflater.from(this).inflate(Layout.gn_fb_layout_customview.getIdentifier(this.mContext), null);
        ImageView iv = (ImageView) this.mCustomView.findViewById(Widget.gn_fb_id_historymenu.getIdentifier(this.mContext));
        iv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FeedBackActivity.this.showState(State.RECORD);
            }
        });
        this.mBadgeView = new BadgeView(this.mContext, iv);
        showBadgeView();
        this.mActionBar.setCustomView(this.mCustomView, new LayoutParams(-2, -2, 21));
    }

    private void onBack() {
        BaseFragment fragment = getCurrentFragment();
        fragment.onBackPressed();
        if (fragment.mState == State.SEND) {
            super.onBackPressed();
        } else {
            showState(State.SEND);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            onBack();
        }
        return true;
    }

    public void onBackPressed() {
        onBack();
    }

    protected void initView() {
        String extras = getIntent().getStringExtra(Notifier.GN_FB_NOTIFICATION_EXTRAS);
        Log.d(TAG, "extras:" + extras);
        SendState sendState = this.mDataManager.getCurSendState();
        BaseFragment sendFragment = (BaseFragment) STATE_FRAGMENT.get(State.SEND);
        BaseFragment recordFragment = (BaseFragment) STATE_FRAGMENT.get(State.RECORD);
        boolean isNotify = Notifier.GN_FB_INTENT_FROM_NOTIFICATION.equals(extras);
        if (sendState == SendState.SEND_SUCCESS || isNotify) {
            this.mShowState = State.RECORD;
            if (!isNotify) {
                this.mDataManager.resetSendState();
            }
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(Widget.gn_fb_id_main_layout.getIdentifier(this.mContext), sendFragment);
        ft.add(Widget.gn_fb_id_main_layout.getIdentifier(this.mContext), recordFragment);
        ft.commitAllowingStateLoss();
        showState(this.mShowState);
        updateActionBar(this.mShowState);
    }

    protected void updateView() {
        showBadgeView();
        for (BaseFragment fragment : STATE_FRAGMENT.values()) {
            fragment.updateView();
        }
    }

    protected void updateDatas(List<FeedbackInfo> infos) {
        for (BaseFragment fragment : STATE_FRAGMENT.values()) {
            fragment.updateDatas(infos);
        }
    }

    public void showState(State state) {
        hideSoftInput();
        BaseFragment currentFragment = getCurrentFragment();
        currentFragment.reset();
        currentFragment.onBackPressed();
        Collection<State> states = STATE_FRAGMENT.keySet();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        for (State s : states) {
            if (s == state) {
                this.mShowState = state;
                ft.show((Fragment) STATE_FRAGMENT.get(s));
            } else {
                ft.hide((Fragment) STATE_FRAGMENT.get(s));
            }
        }
        ft.commitAllowingStateLoss();
        updateActionBar(this.mShowState);
    }

    private BaseFragment getCurrentFragment() {
        return (BaseFragment) STATE_FRAGMENT.get(this.mShowState);
    }

    private void updateActionBar(State state) {
        if (state.equals(State.RECORD)) {
            this.mCustomView.setVisibility(8);
            this.mActionBar.setTitle(Text.gn_fb_string_title_record.getIdentifier(this.mContext));
            hideSoftInput();
            return;
        }
        this.mCustomView.setVisibility(0);
        this.mActionBar.setTitle(Text.gn_fb_string_title_feedback.getIdentifier(this.mContext));
    }

    private void showBadgeView() {
        if (this.mBadgeView != null) {
            if (this.mDataManager.hasUnreadReplies()) {
                this.mBadgeView.show();
            } else {
                this.mBadgeView.gone();
            }
        }
    }

    private void hideSoftInput() {
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }
}
