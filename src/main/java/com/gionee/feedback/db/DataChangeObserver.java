package com.gionee.feedback.db;

import com.gionee.feedback.logic.vo.FeedbackInfo;
import java.util.List;

public interface DataChangeObserver {
    void onDataChange(List<FeedbackInfo> list);
}
