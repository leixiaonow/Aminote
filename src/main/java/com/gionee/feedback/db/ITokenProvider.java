package com.gionee.feedback.db;

public interface ITokenProvider<T> extends IUpdateProvider<T> {
    T getToken();
}
