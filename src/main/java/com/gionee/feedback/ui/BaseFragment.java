package com.gionee.feedback.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.utils.Log;
import java.util.List;

@TargetApi(11)
public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    protected boolean isAttach = false;
    protected FeedBackActivity mActivity;
    protected View mContentView;
    protected DataManager mDataManager;
    protected State mState;

    protected abstract View creatView(LayoutInflater layoutInflater, ViewGroup viewGroup);

    protected abstract void initView();

    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContentView = creatView(inflater, container);
        return this.mContentView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.isAttach = true;
        Log.d(TAG, "onAttach -> " + getSimpleName());
        if (activity == null || !(activity instanceof FeedBackActivity)) {
            Log.d(TAG, "activity cast Execption");
            return;
        }
        this.mActivity = (FeedBackActivity) activity;
        this.mDataManager = DataManager.getInstance(this.mActivity);
    }

    public void onDetach() {
        this.isAttach = false;
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated -> " + getSimpleName());
        initData();
        initView();
    }

    protected <T extends View> T getView(int id) {
        T result = this.mContentView.findViewById(id);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("view 0x" + Integer.toHexString(id) + " doesn't exist");
    }

    protected <T extends View> T getView(View rootView, int id) {
        T result = rootView.findViewById(id);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("view 0x" + Integer.toHexString(id) + " doesn't exist");
    }

    protected void showToast(int toast) {
        Toast.makeText(getActivity(), getString(toast), 0).show();
    }

    private String getSimpleName() {
        return getClass().getSimpleName();
    }

    protected void initData() {
    }

    protected void reset() {
    }

    protected void updateDatas(List<FeedbackInfo> feedbackInfos) {
        Log.d(TAG, "updateDatas -> " + getSimpleName() + "  size = " + (feedbackInfos != null ? feedbackInfos.size() : 0));
    }

    protected void updateView() {
        Log.d(TAG, "updateView -> " + getSimpleName());
    }

    protected void onBackPressed() {
    }
}
