package com.gionee.note.common;

public class NoteRuntimeException extends RuntimeException {
    public NoteRuntimeException(String name) {
        super(name);
    }

    public NoteRuntimeException(String name, Throwable cause) {
        super(name, cause);
    }

    public NoteRuntimeException(Exception cause) {
        super(cause);
    }
}
