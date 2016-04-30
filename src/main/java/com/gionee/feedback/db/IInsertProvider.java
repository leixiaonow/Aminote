package com.gionee.feedback.db;

public interface IInsertProvider<T> {
    long insert(T t);
}
