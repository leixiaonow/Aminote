package com.gionee.feedback.logic;

import com.gionee.feedback.logic.vo.FeedbackInfo;
import com.gionee.feedback.logic.vo.Message;

public interface IDataManager {
    void deleteFeedbackInfos(FeedbackInfo... feedbackInfoArr);

    void getAllRecords();

    SendState getCurSendState();

    boolean hasUnreadReplies();

    void loopGetRecord();

    void recycle();

    void sendMessage(Message... messageArr);

    void stopLoopRecord();

    void updateFeedbackInfos(FeedbackInfo... feedbackInfoArr);
}
