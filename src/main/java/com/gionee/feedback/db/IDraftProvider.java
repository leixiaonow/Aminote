package com.gionee.feedback.db;

public interface IDraftProvider<T> extends IUpdateProvider<T>, IInsertProvider<T>, IDeleteProvider<T> {
    T queryHead();
}
