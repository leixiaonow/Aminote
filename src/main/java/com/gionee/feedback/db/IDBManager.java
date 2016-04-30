package com.gionee.feedback.db;

import com.gionee.feedback.net.IAppData;

public interface IDBManager<T> {
    void delete(T... tArr);

    IAppData getAppData();

    boolean hasNewReplies();

    long insert(T t);

    void update(T... tArr);
}
