package com.gionee.note.common;

public interface FutureListener<T> {
    void onFutureDone(Future<T> future);
}
