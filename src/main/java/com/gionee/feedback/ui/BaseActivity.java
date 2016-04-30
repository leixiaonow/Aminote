package com.gionee.feedback.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.gionee.feedback.IAlarmGetRecord;
import com.gionee.feedback.logic.DataManager;
import com.gionee.feedback.logic.IDataManager;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    private IAlarmGetRecord mAlarmGetRecord;
    protected Context mContext;
    protected IDataManager mDataManager;
    protected List<FeedbackInfo> mFeedbackInfos = new ArrayList();

    protected abstract void init();

    protected abstract void initActionBar();

    protected abstract void initView();

    protected abstract void setContentView();

    protected abstract void showState(State state);

    protected abstract void updateDatas(List<FeedbackInfo> list);

    protected abstract void updateView();

    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        this.mAlarmGetRecord = new AlarmGetRecordImpl();
        this.mAlarmGetRecord.cancelAlarmGetRecord(this.mContext);
        this.mDataManager = DataManager.getInstance(this.mContext);
        setContentView();
        init();
        initActionBar();
        initView();
    }

    protected final void onDestroy() {
        this.mDataManager.recycle();
        this.mAlarmGetRecord.setAlarmGetRecord(this.mContext);
        super.onDestroy();
    }
}
