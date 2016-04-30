package com.gionee.feedback.logic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import com.gionee.feedback.db.DBManager;
import com.gionee.feedback.db.DataChangeObserver;
import com.gionee.feedback.db.IDraftProvider;
import com.gionee.feedback.db.ProviderFactory;
import com.gionee.feedback.exception.FeedBackException;
import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.Message;
import com.gionee.feedback.logic.vo.ReplyInfo;
import com.gionee.feedback.logic.vo.ResultCode;
import com.gionee.feedback.net.AsyncWorkService;
import com.gionee.feedback.net.HttpUtils;
import com.gionee.feedback.net.IAppData;
import com.gionee.feedback.net.Job.Callback;
import com.gionee.feedback.net.RecordJob;
import com.gionee.feedback.net.SendJob;
import com.gionee.feedback.ui.Notifier;
import com.gionee.feedback.utils.Log;
import com.gionee.feedback.utils.Utils;
import com.gionee.note.app.dataupgrade.DataUpgrade;
import java.util.List;
import java.util.UUID;

public final class DataManager implements IDataManager, IAppData {
    private static final String TAG = "DataManager";
    private static Context sAPPContext = null;
    private volatile boolean isRegister;
    private IAppData mAppData;
    private AsyncWorkService mAsyncWorkService;
    private SendState mCurSendState;
    private DBManager mDBManager;
    private RecordJob mLoopRecordJob;
    private NetworkReceiver mReceiver;

    private static class DataManagerHolder {
        public static final DataManager INSTANCE = new DataManager();

        private DataManagerHolder() {
        }
    }

    class NetworkReceiver extends BroadcastReceiver {
        NetworkReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(DataManager.TAG, "onReceive==>" + action);
            if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                Log.d(DataManager.TAG, "network connect change!");
                if (HttpUtils.isNetworkAvailable(context)) {
                    DataManager.this.networkChangeLoop();
                }
            }
        }
    }

    private final class RecordCallback implements Callback<List<FeedbackInfo>> {
        private boolean isNotify;

        protected RecordCallback(boolean isNotify) {
            this.isNotify = isNotify;
        }

        public void onResult(List<FeedbackInfo> result) {
            if (result != null && !result.isEmpty()) {
                DataManager.this.mDBManager.update((FeedbackInfo[]) result.toArray(new FeedbackInfo[result.size()]));
                if (this.isNotify) {
                    Notifier.notify(DataManager.sAPPContext);
                }
            }
        }

        public void onError(int errorCode) {
            Log.d(DataManager.TAG, "get records error == " + errorCode);
        }
    }

    private final class SendCallback implements Callback<Long> {
        private Message.Callback mCallback;
        private FeedbackInfo mSendFeedbackInfo;

        SendCallback(Message message, FeedbackInfo feedbackInfo) {
            this.mSendFeedbackInfo = feedbackInfo;
            this.mCallback = message.getCallback();
        }

        public void onError(int errorCode) {
            DataManager.this.setCurSendState(SendState.SEND_FAILED);
            if (errorCode == ResultCode.CODE_PARSE_ERROR.value()) {
                this.mCallback.onResult(ResultCode.CODE_PARSE_ERROR);
            } else if (errorCode == ResultCode.CODE_NETWORK_DISCONNECTED.value()) {
                this.mCallback.onResult(ResultCode.CODE_NETWORK_DISCONNECTED);
            } else if (errorCode == ResultCode.CODE_NETWORK_UNAVAILABLE.value()) {
                this.mCallback.onResult(ResultCode.CODE_NETWORK_UNAVAILABLE);
            } else if (this.mCallback != null) {
                this.mCallback.onResult(ResultCode.CODE_SEND_FAILED);
            }
        }

        public void onResult(Long result) {
            long contentID = result.longValue();
            Log.d(DataManager.TAG, "send onResult :contentID = " + contentID + DataUpgrade.SPLIT + this.mSendFeedbackInfo);
            if (this.mSendFeedbackInfo != null && contentID > 0) {
                DataManager.this.setCurSendState(SendState.SEND_SUCCESS);
                IDraftProvider iProvider = ProviderFactory.draftProvider(DataManager.sAPPContext);
                iProvider.delete(iProvider.queryHead());
                this.mSendFeedbackInfo.setContentID(contentID);
                DataManager.this.mDBManager.insert(this.mSendFeedbackInfo);
                if (this.mCallback != null) {
                    this.mCallback.onResult(ResultCode.CODE_SEND_SUCESSFUL);
                }
            } else if (contentID == -3) {
                DataManager.this.setCurSendState(SendState.SEND_FAILED);
            }
        }
    }

    public static synchronized DataManager getInstance(Context context) {
        DataManager dataManager;
        synchronized (DataManager.class) {
            sAPPContext = context.getApplicationContext();
            dataManager = DataManagerHolder.INSTANCE;
        }
        return dataManager;
    }

    private DataManager() {
        this.mDBManager = null;
        this.isRegister = false;
        Log.d(TAG, "DataManager()");
        this.mDBManager = DBManager.getInstance(sAPPContext);
        this.mReceiver = new NetworkReceiver();
        setCurSendState(SendState.INITIAL);
    }

    public void registerDataObserver(DataChangeObserver observer) {
        if (!this.isRegister) {
            this.isRegister = true;
            this.mDBManager.registerDataObserver(observer);
            registerNetReceiver();
        }
    }

    public void unregisteredDataObserver(DataChangeObserver observer) {
        if (this.isRegister) {
            this.mDBManager.unregisteredDataObserver(observer);
            unregisterNetReceiver();
            this.isRegister = false;
        }
    }

    private void registerNetReceiver() {
        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        sAPPContext.registerReceiver(this.mReceiver, netIntentFilter);
    }

    private void unregisterNetReceiver() {
        sAPPContext.unregisterReceiver(this.mReceiver);
    }

    public void deleteFeedbackInfos(FeedbackInfo... infos) {
        this.mDBManager.delete(infos);
    }

    public void updateFeedbackInfos(FeedbackInfo... infos) {
        this.mDBManager.update(infos);
    }

    public void readAllReplies(List<FeedbackInfo> infos) {
        for (FeedbackInfo feedbackInfo : infos) {
            List<ReplyInfo> replyInfos = feedbackInfo.getReplyInfos();
            if (!(replyInfos == null || replyInfos.isEmpty())) {
                boolean isChange = false;
                for (ReplyInfo replyInfo : replyInfos) {
                    if (!replyInfo.isReaded()) {
                        replyInfo.setReaded(true);
                        isChange = true;
                    }
                }
                if (isChange) {
                    updateFeedbackInfos(feedbackInfo);
                }
            }
        }
    }

    public boolean hasUnreadReplies() {
        return this.mDBManager.hasNewReplies();
    }

    public void sendMessage(Message... messages) {
        if (messages == null || getCurSendState() == SendState.SENDING) {
            String str;
            String str2 = TAG;
            if (("no operation currentState:" + getCurSendState() + "--message is" + messages) == null) {
                str = "null";
            } else {
                str = messages.toString();
            }
            Log.d(str2, str);
            return;
        }
        setCurSendState(SendState.SENDING);
        for (Message message : messages) {
            FeedbackInfo sendFeedbackInfo = buildMessageFeedbackInfo(message);
            Log.d(TAG, "sendMessage :" + sendFeedbackInfo.toString());
            try {
                getWorkService().submit(new SendJob(sAPPContext, new SendCallback(message, sendFeedbackInfo), message, this));
            } catch (FeedBackException e) {
                e.printStackTrace();
            }
        }
    }

    public void recycle() {
        if (this.mAsyncWorkService != null && !this.mAsyncWorkService.isShutDown()) {
            this.mAsyncWorkService.shutdown();
        }
    }

    private AsyncWorkService getWorkService() {
        if (this.mAsyncWorkService == null || this.mAsyncWorkService.isShutDown()) {
            this.mAsyncWorkService = new AsyncWorkService();
        }
        return this.mAsyncWorkService;
    }

    private FeedbackInfo buildMessageFeedbackInfo(Message message) {
        FeedbackInfo feedbackInfo = new FeedbackInfo();
        feedbackInfo.setID(message.getID());
        feedbackInfo.setContentID(-1);
        feedbackInfo.setContent(message.getMessage());
        feedbackInfo.setUserContact(message.getContact());
        feedbackInfo.setAttachTextArray(message.getAttachs());
        feedbackInfo.setSendTime(Utils.getSystemTime(sAPPContext));
        return feedbackInfo;
    }

    public void loopGetRecord() {
        Log.d(TAG, "loopGetRecord");
        try {
            this.mLoopRecordJob = new RecordJob(sAPPContext, new RecordCallback(false), true, this);
            getWorkService().submit(this.mLoopRecordJob);
        } catch (FeedBackException e) {
            e.printStackTrace();
        }
    }

    public void stopLoopRecord() {
        Log.d(TAG, "stopLoopRecord");
        if (this.mLoopRecordJob != null) {
            this.mLoopRecordJob.stopLoopRecord();
        }
        this.mLoopRecordJob = null;
    }

    private synchronized void setCurSendState(SendState sendState) {
        if (this.mCurSendState != sendState) {
            this.mCurSendState = sendState;
        }
    }

    public synchronized SendState getCurSendState() {
        return this.mCurSendState;
    }

    public void resetSendState() {
        Log.d(TAG, "resetSendState()");
        setCurSendState(SendState.INITIAL);
    }

    private void networkChangeLoop() {
        if (this.mLoopRecordJob == null || !this.mLoopRecordJob.isLoop()) {
            loopGetRecord();
        }
    }

    public void getAllRecords() {
        getAllRecordsNotify(false);
    }

    public void getAllRecordsNotify(boolean isNotify) {
        try {
            getWorkService().submit(new RecordJob(sAPPContext, new RecordCallback(isNotify), false, this));
        } catch (FeedBackException e) {
            e.printStackTrace();
        }
    }

    public void storageAppKey(String appKey) {
        if (this.mDBManager.isStoragedAppData()) {
            Log.d(TAG, "isStoragedAppData--->true");
            return;
        }
        this.mDBManager.storageAppData(appKey, getImei(sAPPContext));
    }

    private String getImei(Context context) {
        String imei = ((TelephonyManager) context.getApplicationContext().getSystemService("phone")).getDeviceId();
        if (imei == null) {
            return UUID.randomUUID().toString();
        }
        return imei;
    }

    public synchronized String getAppKey() {
        return getAppData().getAppKey();
    }

    public synchronized String getImei() {
        return getAppData().getImei();
    }

    private IAppData getAppData() {
        if (this.mAppData == null) {
            this.mAppData = this.mDBManager.getAppData();
            Log.d(TAG, " getAppData imei = " + this.mAppData.getImei() + "  appkey = " + this.mAppData.getAppKey());
        }
        return this.mAppData;
    }
}
