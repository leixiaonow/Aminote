package com.gionee.note.common;

public class BadJsonableException extends NoteRuntimeException {
    public BadJsonableException(String msg) {
        super(msg);
    }

    public BadJsonableException(Exception cause) {
        super(cause);
    }
}
