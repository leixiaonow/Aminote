package com.gionee.feedback.db;

public interface ISubject {
    void registerDataObserver(DataChangeObserver dataChangeObserver);

    void unregisteredDataObserver(DataChangeObserver dataChangeObserver);
}
